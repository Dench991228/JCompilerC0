package jcompiler.analyzer;

import jcompiler.action.Function;
import jcompiler.action.GlobalVariable;
import jcompiler.action.Instruction;
import jcompiler.action.ObjectFile;
import jcompiler.analyzer.exceptions.*;
import jcompiler.tokenizer.Token;
import jcompiler.tokenizer.TokenType;
import jcompiler.util.Pos;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class Analyzer {

    private StmtAnalyzer StmtAnalyzer;

    private AnalyzerUtil Util;

    /*用来记录各个分支有没有return*/
    public static LinkedList<Boolean> ReturnState = new LinkedList<>();

    /*用来记录各层嵌套是不是在循环里面*/
    public static LinkedList<Boolean> LoopState = new LinkedList<>();

    /*这个语法分析器，当前作用域的符号表*/
    public static SymbolTable AnalyzerTable;

    /*这个语法分析器当前分析函数的期望返回*/
    public static Token ExpectedReturnType = null;
    /*语法分析器对应的输出文件*/
    public static ObjectFile ObjFile = null;
    /*语法分析器当前正在解析的函数*/
    public static Function CurrentFunction = null;
    /*_start函数*/
    public static Function StartFunction = new Function();
    /*main函数对应的编号是多少*/
    private int number_main=0;
    /*标准库：函数名称-指令*/
    public static final HashMap<String, Instruction> StandardLibrary = new HashMap<>();
    /*函数名称集合*/
    public static final HashSet<String> FunctionNames = new HashSet<>();
    /*记录正在嵌套的while语句前面的nop*/
    public static LinkedList<Instruction> StartOfWhile = new LinkedList<>();
    /*记录所有的break语句对应的br，每一个list代表一层循环嵌套里面的全部break*/
    public static LinkedList<LinkedList<Instruction>> BreakStatement = new LinkedList<>();

    /*初始化标准库*/
    static{
        AnalyzerTable = new SymbolTable();
        SymbolEntry getInt = SymbolEntry.getFunctionEntry(Token.INTEGER, new LinkedList<Token>(), new Pos(-1,-1));
        StandardLibrary.put("getint", Instruction.getInstruction("scan.i"));
        SymbolEntry getDouble = SymbolEntry.getFunctionEntry(Token.DOUBLE, new LinkedList<Token>(), new Pos(-1,-1));
        StandardLibrary.put("getdouble", Instruction.getInstruction("scan.f"));
        SymbolEntry getChar = SymbolEntry.getFunctionEntry(Token.INTEGER, new LinkedList<Token>(), new Pos(-1,-1));
        StandardLibrary.put("getchar", Instruction.getInstruction("scan.c"));
        LinkedList<Token> param_putInt = new LinkedList<>();
        param_putInt.addLast(Token.INTEGER);
        SymbolEntry putInt = SymbolEntry.getFunctionEntry(Token.VOID, param_putInt, new Pos(-1,-1));
        StandardLibrary.put("putint", Instruction.getInstruction("print.i"));
        SymbolEntry putChar = SymbolEntry.getFunctionEntry(Token.VOID, param_putInt, new Pos(-1,-1));
        StandardLibrary.put("putchar", Instruction.getInstruction("print.c"));
        SymbolEntry putStr = SymbolEntry.getFunctionEntry(Token.VOID, param_putInt, new Pos(-1,-1));
        StandardLibrary.put("putstr", Instruction.getInstruction("print.s"));
        SymbolEntry putLn = SymbolEntry.getFunctionEntry(Token.VOID, new LinkedList<>(), new Pos(-1,-1));
        StandardLibrary.put("putln", Instruction.getInstruction("println"));
        LinkedList<Token> param_single_double = new LinkedList<>();
        param_single_double.addLast(Token.DOUBLE);
        SymbolEntry putDouble = SymbolEntry.getFunctionEntry(Token.DOUBLE, param_single_double, new Pos(-1,-1));
        StandardLibrary.put("putdouble", Instruction.getInstruction("print.f"));

        AnalyzerTable.putIdent(new Token(TokenType.IDENT, "getint", new Pos(-1,-1)), getInt);
        AnalyzerTable.putIdent(new Token(TokenType.IDENT, "getdouble", new Pos(-1,-1)), getDouble);
        AnalyzerTable.putIdent(new Token(TokenType.IDENT, "getchar", new Pos(-1,-1)), getChar);
        AnalyzerTable.putIdent(new Token(TokenType.IDENT, "putint", new Pos(-1,-1)), putInt);
        AnalyzerTable.putIdent(new Token(TokenType.IDENT, "putchar", new Pos(-1,-1)), putChar);
        AnalyzerTable.putIdent(new Token(TokenType.IDENT, "putstr", new Pos(-1,-1)), putStr);
        AnalyzerTable.putIdent(new Token(TokenType.IDENT, "putln", new Pos(-1,-1)), putLn);
        AnalyzerTable.putIdent(new Token(TokenType.IDENT, "putdouble", new Pos(-1,-1)), putDouble);
        CurrentFunction = StartFunction;
    }

    public Analyzer(AnalyzerUtil util, ObjectFile obj) throws FileNotFoundException {
        this.StmtAnalyzer = new StmtAnalyzer(util);
        ObjFile = obj;
        ObjFile.addFunction(StartFunction);
        this.Util = util;
        GlobalVariable gbv = GlobalVariable.StringGlobal("_start");
        ObjFile.addGlobalVariable(gbv);
    }

    /*解析一个函数的参数*/
    private void analyseParamDecl(LinkedList<Token> params) {
        /*看一眼有没有const*/
        this.Util.nextIf(TokenType.CONST_KW);
        Token param_ident = this.Util.next(TokenType.IDENT);
        this.Util.expect(TokenType.COLON);
        Token param_type = this.Util.next(TokenType.TY);
        if(param_type.getValue().toString().compareTo("int")==0)param_type = Token.INTEGER;
        else if(param_type.getValue().toString().compareTo("double")==0)param_type = Token.DOUBLE;
        else throw new IdentifierTypeException();
        params.addLast(param_type);
        SymbolEntry param_entry = SymbolEntry.getParameterEntry(param_type, false, param_ident.getStartPos());
        /*在当前的函数上登记这个参数*/
        param_entry.setVariableCategory(1);
        param_entry.setPosition(Analyzer.CurrentFunction.registerParam(param_type));
        /*把这个参数加入到符号表中，并且设置为已经初始化*/
        AnalyzerTable.putIdent(param_ident, param_entry);
        AnalyzerTable.setInitialized(param_ident);
    }

    /*解析一个函数*/
    private void analyseFunction(){
        Analyzer.resetStacks();
        /*fn*/
        this.Util.expect(TokenType.FN_KW);
        /*函数的标识符*/
        Token function_ident = this.Util.next(TokenType.IDENT);
        GlobalVariable gbv = GlobalVariable.StringGlobal(function_ident.getValue().toString());
        ObjFile.addGlobalVariable(gbv);
        FunctionNames.add(function_ident.getValue().toString());
        LinkedList<Token> params = new LinkedList<>();//函数的参数列表，里面全部是type
        this.Util.expect(TokenType.L_PAREN);
        Analyzer.addSymbolTable();//用来记录参数的符号表
        Function func = new Function();//当前正在解析的函数
        Analyzer.CurrentFunction = func;
        /*解析参数列表，如果有参数的话*/
        /*参数列表里面的形参和函数内部的东西在一个作用域里面*/
        while(this.Util.peek().getType()!=TokenType.R_PAREN){
            this.analyseParamDecl(params);
            if(this.Util.peek().getType()!=TokenType.R_PAREN){
                this.Util.expect(TokenType.COMMA);
            }
        }
        this.Util.next();
        this.Util.expect(TokenType.ARROW);
        Token function_type = this.Util.next(TokenType.TY);
        if(function_type.getValue().toString().compareTo("double")==0)function_type=Token.DOUBLE;
        else if(function_type.getValue().toString().compareTo("int")==0) function_type=Token.INTEGER;
        else function_type=Token.VOID;
        Analyzer.ExpectedReturnType = function_type;
        /*创建函数的表项*/
        SymbolEntry function_entry = SymbolEntry.getFunctionEntry(function_type, params, function_ident.getStartPos());
        /*把它放到当前符号表的上级符号表中*/
        Analyzer.AnalyzerTable.getFatherTable().putIdent(function_ident, function_entry);
        int number_function = Analyzer.ObjFile.addFunction(func);
        func.registerReturn(function_type);
        function_entry.setPosition(number_function);
        if(function_ident.getValue().toString().compareTo("main")==0){
            this.number_main = number_function;
        }
        this.StmtAnalyzer.analyseBlockStmt();
        /*如果函数内部没有返回，看一眼是不是void*/
        if(!Analyzer.ReturnState.peekLast()&&((String)function_type.getValue()).compareTo("void")!=0){
            throw new BranchNoReturnException();
        }
        else if(!Analyzer.ReturnState.peekLast()){//没有完全返回，但是因为是void，所以在最后加一个就行
            Analyzer.CurrentFunction.addInstruction(Instruction.getInstruction("ret"));
        }
        Analyzer.withdraw();
        Analyzer.CurrentFunction = StartFunction;
    }

    /*顶层的分析*/
    public void analyse(){
        while(this.Util.peek().getType()!=TokenType.EOF){
            Token t = this.Util.peek();
            switch(t.getType()){
                case LET_KW:
                case CONST_KW:
                    this.StmtAnalyzer.analyseStatement();
                    break;
                case FN_KW:
                    this.analyseFunction();
                    break;
                default:
                    throw new StmtSyntaxException();
            }
        }
        /*给_start函数加上调用main的过程*/
        Instruction ins;
        if(number_main==0)throw new NoMainException();
        ins = Instruction.getInstruction("stackalloc", 1);
        Analyzer.CurrentFunction.addInstruction(ins);
        ins = Instruction.getInstruction("call", number_main);
        Analyzer.CurrentFunction.addInstruction(ins);
    }

    /*分析进入一层嵌套的时候调用这个函数，如果是while，就入栈一个true，否则根栈顶元素一样，break/continue的时候，看栈顶元素是不是true*/
    public static void putLoopState(boolean isLoop){
        if(isLoop){
            Analyzer.LoopState.addLast(true);
        }
        else{
            boolean flag = LoopState.peekLast();
            LoopState.addLast(flag);
        }
    }

    /*分析进入一个分支的时候调用这个函数，如果是if或者elif那就入栈一个状态，否则不管*/
    /*只管分支，不管别的*/
    /*遇到return的时候把栈顶的状态改为true*/
    /*看最后到栈底的元素是不是true*/
    public static void putReturnState(){
        Analyzer.ReturnState.addLast(false);
    }

    /*进入新函数的时候，重新设置这些栈*/
    public static void resetStacks(){
        ReturnState.clear();
        LoopState.clear();
        ReturnState.addLast(false);
        LoopState.addLast(false);
    }

    /*进入新的作用域的时候，要重新创建一张符号表*/
    public static void addSymbolTable(){
        AnalyzerTable = new SymbolTable(Analyzer.AnalyzerTable);
    }

    /*退出一个作用域时，需要回退符号表*/
    public static void withdraw(){
        Analyzer.AnalyzerTable = Analyzer.AnalyzerTable.getFatherTable();
    }
}

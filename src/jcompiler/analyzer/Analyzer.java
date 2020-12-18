package jcompiler.analyzer;

import jcompiler.analyzer.exceptions.BranchNoReturnException;
import jcompiler.analyzer.exceptions.ErrorTokenTypeException;
import jcompiler.analyzer.exceptions.StmtSyntaxException;
import jcompiler.tokenizer.Token;
import jcompiler.tokenizer.TokenType;
import jcompiler.util.Pos;

import java.io.FileNotFoundException;
import java.util.LinkedList;

public class Analyzer {

    private StmtAnalyzer StmtAnalyzer;

    private AnalyzerUtil Util;

    /*用来记录各个分支有没有return*/
    public static LinkedList<Boolean> ReturnState = new LinkedList<>();

    /*用来记录各层嵌套是不是在循环里面*/
    public static LinkedList<Boolean> LoopState = new LinkedList<>();

    public static SymbolTable AnalyzerTable;
    /*初始化标准库*/
    static{
        AnalyzerTable = new SymbolTable();
        SymbolEntry getInt = SymbolEntry.getFunctionEntry(Token.INTEGER, new LinkedList<Token>(), new Pos(-1,-1));
        SymbolEntry getDouble = SymbolEntry.getFunctionEntry(Token.DOUBLE, new LinkedList<Token>(), new Pos(-1,-1));
        SymbolEntry getChar = SymbolEntry.getFunctionEntry(Token.INTEGER, new LinkedList<Token>(), new Pos(-1,-1));
        LinkedList<Token> param_putInt = new LinkedList<>();
        param_putInt.addLast(Token.INTEGER);
        SymbolEntry putInt = SymbolEntry.getFunctionEntry(Token.VOID, param_putInt, new Pos(-1,-1));
        SymbolEntry putChar = SymbolEntry.getFunctionEntry(Token.VOID, param_putInt, new Pos(-1,-1));
        SymbolEntry putStr = SymbolEntry.getFunctionEntry(Token.VOID, param_putInt, new Pos(-1,-1));
        SymbolEntry putLn = SymbolEntry.getFunctionEntry(Token.VOID, param_putInt, new Pos(-1,-1));

        AnalyzerTable.putIdent(new Token(TokenType.IDENT, "getint", new Pos(-1,-1)), getInt);
        AnalyzerTable.putIdent(new Token(TokenType.IDENT, "getdouble", new Pos(-1,-1)), getDouble);
        AnalyzerTable.putIdent(new Token(TokenType.IDENT, "getchar", new Pos(-1,-1)), getChar);
        AnalyzerTable.putIdent(new Token(TokenType.IDENT, "putint", new Pos(-1,-1)), putInt);
        AnalyzerTable.putIdent(new Token(TokenType.IDENT, "putchar", new Pos(-1,-1)), putChar);
        AnalyzerTable.putIdent(new Token(TokenType.IDENT, "putstr", new Pos(-1,-1)), putStr);
        AnalyzerTable.putIdent(new Token(TokenType.IDENT, "putln", new Pos(-1,-1)), putLn);
    }
    public Analyzer(AnalyzerUtil util) throws FileNotFoundException {
        this.StmtAnalyzer = new StmtAnalyzer(util);
        this.Util = util;
    }

    /*解析一个函数的参数*/
    private void analyseParamDecl() {
        /*看一眼有没有const*/
        this.Util.nextIf(TokenType.CONST_KW);
        this.Util.expect(TokenType.IDENT);
        this.Util.expect(TokenType.COLON);
        this.Util.expect(TokenType.TY);
    }

    /*解析一个函数*/
    private void analyseFunction(){
        Analyzer.resetStacks();
        this.Util.expect(TokenType.FN_KW);
        this.Util.expect(TokenType.IDENT);
        this.Util.expect(TokenType.L_PAREN);
        while(this.Util.peek().getType()!=TokenType.R_PAREN){
            this.analyseParamDecl();
            if(this.Util.peek().getType()!=TokenType.R_PAREN){
                this.Util.expect(TokenType.COMMA);
            }
        }
        this.Util.next();
        this.Util.expect(TokenType.ARROW);
        Token t = this.Util.next();
        if(t.getType()!=TokenType.TY)throw new ErrorTokenTypeException();
        this.StmtAnalyzer.analyseStatement();
        if(!Analyzer.ReturnState.peekLast()&&((String)t.getValue()).compareTo("void")!=0)throw new BranchNoReturnException();
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

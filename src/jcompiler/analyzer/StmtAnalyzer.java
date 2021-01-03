package jcompiler.analyzer;

import jcompiler.action.Instruction;
import jcompiler.analyzer.exceptions.ErrorTokenTypeException;
import jcompiler.analyzer.exceptions.LoopControlException;
import jcompiler.analyzer.exceptions.ReturnTypeError;
import jcompiler.analyzer.exceptions.StmtSyntaxException;
import jcompiler.tokenizer.Token;
import jcompiler.tokenizer.TokenType;
import jcompiler.util.BinaryHelper;

import java.io.FileNotFoundException;

/*
* 语法分析器，使用OPG完成expr的分析，先完成expr部分
* */
public class StmtAnalyzer {
    /*使用的基础设施*/
    private AnalyzerUtil Util;

    /*使用的opg analyzer*/
    private ExprAnalyzer ExprAnalyzer;

    /*是不是在第一个if语句中*/
    private boolean FirstIf;

    /*无参数创建analyzer*/
    public StmtAnalyzer(){
        super();
    }

    /*根据输入文件创建新的tokenizer*/
    public StmtAnalyzer(AnalyzerUtil util) throws FileNotFoundException {
        this.Util = util;
        this.ExprAnalyzer = new ExprAnalyzer(util);
    }

    /*解析运算式语句*/
    private void analyseExpression(){
        this.ExprAnalyzer.analyseExpr(null);
        this.Util.expect(TokenType.SEMICOLON);
    }

    /*解析声明语句*/
    private void analyseDeclStmt(){
        Token first = this.Util.peek();
        Token variable_ident;
        Token variable_type;
        switch (first.getType()){
            case LET_KW:
                this.Util.next();
                variable_ident = this.Util.next(TokenType.IDENT);
                this.Util.expect(TokenType.COLON);
                variable_type = this.Util.next(TokenType.TY);
                if(variable_type.getValue().toString().compareTo("double")==0)variable_type=Token.DOUBLE;
                else if(variable_type.getValue().toString().compareTo("int")==0)variable_type=Token.INTEGER;
                if(variable_type.getValue().toString().compareTo("void")==0)throw new ErrorTokenTypeException();
                boolean isInitialized = false;
                SymbolEntry entry = SymbolEntry.getVariableEntry(variable_type, false, variable_ident.getStartPos());
                Analyzer.AnalyzerTable.putIdent(variable_ident, entry);
                if(this.Util.nextIf(TokenType.ASSIGN)!=null){//需要考虑赋值
                    Analyzer.AnalyzerTable.moveAddressStackTop(variable_ident);//把目标全局变量挪到顶上
                    isInitialized = true;
                    this.ExprAnalyzer.setExpectedType(variable_type);
                    this.ExprAnalyzer.analyseExpr(variable_type);
                    Instruction ins = Instruction.getInstruction("store.64");
                    Analyzer.CurrentFunction.addInstruction(ins);
                }
                this.Util.expect(TokenType.SEMICOLON);
                if(isInitialized)Analyzer.AnalyzerTable.setInitialized(variable_ident);
                break;
            case CONST_KW:
                this.Util.next();
                variable_ident = this.Util.next(TokenType.IDENT);
                this.Util.expect(TokenType.COLON);
                variable_type = this.Util.next(TokenType.TY);
                if(variable_type.getValue().toString().compareTo("void")==0)throw new ErrorTokenTypeException();
                if(variable_type.getValue().toString().compareTo("double")==0)variable_type=Token.DOUBLE;
                else if(variable_type.getValue().toString().compareTo("int")==0)variable_type=Token.INTEGER;
                this.Util.expect(TokenType.ASSIGN);
                this.ExprAnalyzer.setExpectedType(variable_type);
                SymbolEntry const_entry = SymbolEntry.getVariableEntry(variable_type, true, variable_ident.getStartPos());
                Analyzer.AnalyzerTable.putIdent(variable_ident, const_entry);
                Analyzer.AnalyzerTable.moveAddressStackTop(variable_ident);
                this.ExprAnalyzer.analyseExpr(variable_type);
                this.Util.expect(TokenType.SEMICOLON);
                Instruction ins = Instruction.getInstruction("store.64");
                Analyzer.CurrentFunction.addInstruction(ins);
                Analyzer.AnalyzerTable.setInitialized(variable_ident);
                break;
            default:
                throw new StmtSyntaxException();
        }
    }

    /*解析if语句*/
    /*进入if语句的时候，在返回状态栈里面放入一个新状态，表明这一系列if-else语句的返回状态*/

    private void analyseIfStmt(){
        boolean has_branch_no_return = false;//有没有分支没返回
        boolean has_else_clause = false;//有没有else语句
        Analyzer.ReturnState.addLast(false);
        Analyzer.putLoopState(false);
        this.Util.expect(TokenType.IF_KW);
        this.ExprAnalyzer.analyseExpr(Token.INTEGER);
        this.FirstIf=true;
        this.analyseBlockStmt();
        this.FirstIf=false;
        boolean last_state = Analyzer.ReturnState.pollLast();
        if(!last_state)has_branch_no_return = true;
        while(this.Util.peek().getType()==TokenType.ELSE_KW){
            this.Util.next();
            if(this.Util.peek().getType()==TokenType.L_BRACE){//左大括号，不会有elif了
                Analyzer.ReturnState.addLast(false);
                this.analyseBlockStmt();
                last_state = Analyzer.ReturnState.pollLast();
                if(!last_state)has_branch_no_return = true;
                has_else_clause = true;
                break;
            }
            else{
                this.Util.expect(TokenType.IF_KW);
                this.ExprAnalyzer.analyseExpr(Token.INTEGER);
                Analyzer.ReturnState.addLast(false);
                this.analyseBlockStmt();
                last_state = Analyzer.ReturnState.pollLast();
                if(!last_state)has_branch_no_return = true;
            }
        }
        if(!has_branch_no_return&&has_else_clause){
            Analyzer.ReturnState.pollLast();
            Analyzer.ReturnState.addLast(true);
        }
        Analyzer.LoopState.pollLast();
    }

    /*解析while语句*/
    private void analyseWhileStmt(){
        this.Util.expect(TokenType.WHILE_KW);
        /*这里添加一条nop语句，while块结束后，就跳转到这里*/
        Instruction nop_fore = Instruction.getInstruction("nop");
        Analyzer.CurrentFunction.addInstruction(nop_fore);
        /*while的条件解析*/
        this.ExprAnalyzer.analyseExpr(Token.INTEGER);
        //之前的算式解析完之后，末尾是一个条件跳转，用来跳到nop_back的，但是没有操作数，先保存一下，后面设置好
        Instruction conditional_jump = Analyzer.CurrentFunction.getLastInstruction();

        Analyzer.putLoopState(true);
        Analyzer.putReturnState();
        Analyzer.addSymbolTable();//加上一层符号表
        this.analyseBlockStmt();//解析语句块
        /*语句块编译完了，这里我得加一个无条件跳转到前面的nop_fore，进行下一轮循环*/
        Instruction unconditional_jump = Instruction.getInstruction("br", -Analyzer.CurrentFunction.getOffset(nop_fore));
        Analyzer.CurrentFunction.addInstruction(unconditional_jump);

        //TODO 把前面的conditional_jump的操作数补齐，跳转到下面的语句
        int offset = Analyzer.CurrentFunction.getOffset(conditional_jump);
        conditional_jump.setOperand(BinaryHelper.BinaryInteger(offset-1));//正着跳，需要减一
        //无条件跳转完了，接下来是跳过while的语句，如果前面的条件跳转GG了，就跳转到这里，这里也有一个nop
        Instruction nop_back = Instruction.getInstruction("nop");
        Analyzer.CurrentFunction.addInstruction(nop_back);

        //之后的东西
        Analyzer.withdraw();
        Analyzer.ReturnState.pollLast();
        Analyzer.LoopState.pollLast();
    }

    /*解析返回语句*/
    private void analyseReturnStmt(){
        this.Util.expect(TokenType.RETURN_KW);
        Analyzer.ReturnState.addLast(true);
        if(Analyzer.ExpectedReturnType.getValue().toString().compareTo("void")!=0){//不是void，那就必须有东西
            Instruction ins = Instruction.getInstruction("arga",0);
            Analyzer.CurrentFunction.addInstruction(ins);
            if(this.Util.peek().getType()==TokenType.SEMICOLON)throw new ReturnTypeError();
            this.ExprAnalyzer.setExpectedType(Analyzer.ExpectedReturnType);
            this.ExprAnalyzer.analyseExpr(Analyzer.ExpectedReturnType);
            ins = Instruction.getInstruction("store.64");
            Analyzer.CurrentFunction.addInstruction(ins);
        }
        this.Util.expect(TokenType.SEMICOLON);
    }

    /*解析break语句*/
    private void analyseBreakStmt(){
        this.Util.expect(TokenType.BREAK_KW);
        this.Util.expect(TokenType.SEMICOLON);
    }

    /*解析continue语句*/
    private void analyseContinueStmt(){
        this.Util.expect(TokenType.CONTINUE_KW);
        this.Util.expect(TokenType.SEMICOLON);
    }

    /*解析空语句*/
    private void analyseEmptyStmt() {
        this.Util.expect(TokenType.SEMICOLON);
    }

    /*解析语句块*/
    public void analyseBlockStmt(){
        this.Util.expect(TokenType.L_BRACE);
        while(this.Util.peek().getType()!=TokenType.R_BRACE){
            this.analyseStatement();
        }
        this.Util.expect(TokenType.R_BRACE);
    }

    /*对外服务，语句分析*/
    public void analyseStatement(){
        Token t = this.Util.peek();
        switch(t.getType()){
            case SEMICOLON://空语句
                this.analyseEmptyStmt();
                break;
            case L_BRACE://语句块
                Analyzer.putLoopState(false);
                Analyzer.addSymbolTable();
                this.analyseBlockStmt();
                Analyzer.withdraw();
                Analyzer.LoopState.pollLast();
                break;
            case WHILE_KW://while语句
                this.analyseWhileStmt();
                break;
            case IF_KW://if语句
                this.analyseIfStmt();
                break;
            case LET_KW://这两个都是声明
            case CONST_KW:
                this.analyseDeclStmt();
                break;
            case RETURN_KW://返回语句
                this.analyseReturnStmt();
                break;
            case BREAK_KW:
                if(Analyzer.LoopState.peekLast())this.analyseBreakStmt();
                else throw new LoopControlException();
                break;
            case CONTINUE_KW:
                if(Analyzer.LoopState.peekLast())this.analyseContinueStmt();
                else throw new LoopControlException();
                break;
            default://运算式
                this.analyseExpression();
                break;
        }
    }
}

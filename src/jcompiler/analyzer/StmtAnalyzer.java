package jcompiler.analyzer;

import jcompiler.action.Instruction;
import jcompiler.analyzer.exceptions.ErrorTokenTypeException;
import jcompiler.analyzer.exceptions.LoopControlException;
import jcompiler.analyzer.exceptions.ReturnTypeError;
import jcompiler.analyzer.exceptions.StmtSyntaxException;
import jcompiler.tokenizer.Token;
import jcompiler.tokenizer.TokenType;

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
        this.ExprAnalyzer.analyseExpr(Token.INTEGER);
        Analyzer.putLoopState(true);
        Analyzer.putReturnState();
        Analyzer.addSymbolTable();
        this.analyseBlockStmt();
        Analyzer.withdraw();
        Analyzer.ReturnState.pollLast();
        Analyzer.LoopState.pollLast();
    }

    /*解析返回语句*/
    private void analyseReturnStmt(){
        this.Util.expect(TokenType.RETURN_KW);
        Analyzer.ReturnState.addLast(true);
        if(Analyzer.ExpectedReturnType.getValue().toString().compareTo("void")!=0){//不是void，那就必须有东西
            if(this.Util.peek().getType()==TokenType.SEMICOLON)throw new ReturnTypeError();
            this.ExprAnalyzer.setExpectedType(Analyzer.ExpectedReturnType);
            this.ExprAnalyzer.analyseExpr(Analyzer.ExpectedReturnType);
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

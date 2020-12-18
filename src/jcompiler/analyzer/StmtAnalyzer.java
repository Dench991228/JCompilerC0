package jcompiler.analyzer;

import jcompiler.analyzer.exceptions.ErrorTokenTypeException;
import jcompiler.analyzer.exceptions.LoopControlException;
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
        this.ExprAnalyzer.analyseExpr();
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
                if(variable_type.getValue().toString().compareTo("void")==0)throw new ErrorTokenTypeException();
                boolean isInitialized = false;
                if(this.Util.nextIf(TokenType.ASSIGN)!=null){
                    isInitialized = true;
                    this.ExprAnalyzer.analyseExpr();
                }
                this.Util.expect(TokenType.SEMICOLON);
                SymbolEntry entry = SymbolEntry.getVariableEntry(variable_type, false, variable_ident.getStartPos());
                Analyzer.AnalyzerTable.putIdent(variable_ident, entry);
                if(isInitialized)Analyzer.AnalyzerTable.setInitialized(variable_ident);
                break;
            case CONST_KW:
                this.Util.next();
                variable_ident = this.Util.next(TokenType.IDENT);
                this.Util.expect(TokenType.COLON);
                variable_type = this.Util.next(TokenType.TY);
                if(variable_type.getValue().toString().compareTo("void")==0)throw new ErrorTokenTypeException();
                this.Util.expect(TokenType.ASSIGN);
                this.ExprAnalyzer.analyseExpr();
                this.Util.expect(TokenType.SEMICOLON);
                SymbolEntry const_entry = SymbolEntry.getVariableEntry(variable_type, true, variable_ident.getStartPos());
                Analyzer.AnalyzerTable.putIdent(variable_ident, const_entry);
                Analyzer.AnalyzerTable.setInitialized(variable_ident);
                break;
            default:
                throw new StmtSyntaxException();
        }
    }

    /*解析if语句*/
    private void analyseIfStmt(){
        Analyzer.putReturnState();
        Analyzer.putLoopState(false);
        this.Util.expect(TokenType.IF_KW);
        this.ExprAnalyzer.analyseExpr();
        this.analyseBlockStmt();
        Analyzer.ReturnState.pollLast();
        while(this.Util.peek().getType()==TokenType.ELSE_KW){
            this.Util.next();
            if(this.Util.peek().getType()==TokenType.L_BRACE){//左大括号，不会有elif了
                this.analyseBlockStmt();
                break;
            }
            else{
                this.Util.expect(TokenType.IF_KW);
                this.ExprAnalyzer.analyseExpr();
                Analyzer.putReturnState();
                this.analyseBlockStmt();
                Analyzer.ReturnState.pollLast();
            }
        }
        Analyzer.LoopState.pollLast();
    }

    /*解析while语句*/
    private void analyseWhileStmt(){
        this.Util.expect(TokenType.WHILE_KW);
        this.ExprAnalyzer.analyseExpr();
        Analyzer.putLoopState(true);
        Analyzer.putReturnState();
        this.analyseBlockStmt();
        Analyzer.ReturnState.pollLast();
        Analyzer.LoopState.pollLast();
    }

    /*解析返回语句*/
    private void analyseReturnStmt(){
        this.Util.expect(TokenType.RETURN_KW);
        Analyzer.ReturnState.pollLast();
        Analyzer.ReturnState.addLast(true);
        if(this.Util.nextIf(TokenType.SEMICOLON)==null){
            this.ExprAnalyzer.analyseExpr();
            this.Util.expect(TokenType.SEMICOLON);
        }
    }

    /*解析语句块*/
    private void analyseBlockStmt(){
        Analyzer.addSymbolTable();
        this.Util.expect(TokenType.L_BRACE);
        while(this.Util.peek().getType()!=TokenType.R_BRACE){
            this.analyseStatement();
        }
        this.Util.expect(TokenType.R_BRACE);
        Analyzer.withdraw();
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

    /*对外服务，语句分析*/
    public void analyseStatement(){
        Token t = this.Util.peek();
        switch(t.getType()){
            case SEMICOLON://空语句
                this.analyseEmptyStmt();
                break;
            case L_BRACE://语句块
                Analyzer.putLoopState(false);
                this.analyseBlockStmt();
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

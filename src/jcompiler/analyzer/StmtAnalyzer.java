package jcompiler.analyzer;

import jcompiler.analyzer.exceptions.ErrorTokenTypeException;
import jcompiler.analyzer.exceptions.StmtSyntaxError;
import jcompiler.tokenizer.Token;
import jcompiler.tokenizer.TokenType;
import jcompiler.tokenizer.Tokenizer;
import jcompiler.tokenizer.exceptions.UnknownTokenException;

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
        switch (first.getType()){
            case LET_KW:
                this.Util.next();
                this.Util.expect(TokenType.IDENT);
                this.Util.expect(TokenType.COLON);
                this.Util.expect(TokenType.TY);
                if(this.Util.nextIf(TokenType.ASSIGN)!=null){
                    this.ExprAnalyzer.analyseExpr();
                }
                this.Util.expect(TokenType.SEMICOLON);
                break;
            case CONST_KW:
                this.Util.next();
                this.Util.expect(TokenType.IDENT);
                this.Util.expect(TokenType.COLON);
                this.Util.expect(TokenType.TY);
                this.Util.expect(TokenType.ASSIGN);
                this.ExprAnalyzer.analyseExpr();
                this.Util.expect(TokenType.SEMICOLON);
                break;
            default:
                throw new StmtSyntaxError();
        }
    }

    /*解析if语句*/
    private void analyseIfStmt(){
        this.Util.expect(TokenType.IF_KW);
        this.ExprAnalyzer.analyseExpr();
        this.analyseBlockStmt();
        while(this.Util.peek().getType()==TokenType.ELSE_KW){
            this.Util.next();
            if(this.Util.peek().getType()==TokenType.L_BRACE){//左大括号，不会有elif了
                this.analyseBlockStmt();
                break;
            }
            else{
                this.Util.expect(TokenType.IF_KW);
                this.ExprAnalyzer.analyseExpr();
                this.analyseBlockStmt();
            }
        }
    }

    /*解析while语句*/
    private void analyseWhileStmt(){
        this.Util.expect(TokenType.WHILE_KW);
        this.ExprAnalyzer.analyseExpr();
        this.analyseBlockStmt();
    }

    /*解析返回语句*/
    private void analyseReturnStmt(){
        this.Util.expect(TokenType.RETURN_KW);
        if(this.Util.nextIf(TokenType.SEMICOLON)==null){
            this.ExprAnalyzer.analyseExpr();
            this.Util.expect(TokenType.SEMICOLON);
        }
    }

    /*解析语句块*/
    private void analyseBlockStmt(){
        this.Util.expect(TokenType.L_BRACE);
        while(this.Util.peek().getType()!=TokenType.R_BRACE){
            this.analyseStatement();
        }
        this.Util.expect(TokenType.R_BRACE);
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
                this.analyseBlockStmt();
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
                this.analyseBreakStmt();
                break;
            case CONTINUE_KW:
                this.analyseContinueStmt();
                break;
            default://运算式
                this.analyseExpression();
                break;
        }
    }
}

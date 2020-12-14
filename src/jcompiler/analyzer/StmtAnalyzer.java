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
    private void analyseExpression() throws Exception {
        this.ExprAnalyzer.analyseExpr();
        this.Util.expect(TokenType.SEMICOLON);
    }

    /*解析声明语句*/
    private void analyseDeclStmt() throws Exception {
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
    private void analyseIfStmt() throws Exception {
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
                this.analyseIfStmt();
            }
        }
    }

    /*解析while语句*/
    private void analyseWhileStmt() throws Exception {
        this.Util.expect(TokenType.WHILE_KW);
        this.ExprAnalyzer.analyseExpr();
        this.analyseBlockStmt();
    }

    /*解析返回语句*/
    private void analyseReturnStmt() throws Exception {
        this.Util.expect(TokenType.RETURN_KW);
        if(this.Util.nextIf(TokenType.SEMICOLON)==null){
            this.ExprAnalyzer.analyseExpr();
            this.Util.expect(TokenType.SEMICOLON);
        }
    }

    /*解析语句块*/
    private void analyseBlockStmt()throws StmtSyntaxError{

    }

    /*解析空语句*/
    private void analyseEmptyStmt() throws Exception {
        this.Util.expect(TokenType.SEMICOLON);
    }
}

package jcompiler.analyzer;

import jcompiler.analyzer.exceptions.StmtSyntaxError;
import jcompiler.tokenizer.Token;
import jcompiler.tokenizer.TokenType;

import java.io.FileNotFoundException;

public class Analyzer {

    private StmtAnalyzer StmtAnalyzer;

    private AnalyzerUtil Util;

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
        this.Util.expect(TokenType.TY);
        this.StmtAnalyzer.analyseStatement();
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
                    throw new StmtSyntaxError();
            }
        }
    }
}

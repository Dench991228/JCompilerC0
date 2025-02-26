package jcompiler.analyzer;

import jcompiler.analyzer.exceptions.ErrorTokenTypeException;
import jcompiler.tokenizer.Token;
import jcompiler.tokenizer.TokenType;
import jcompiler.tokenizer.Tokenizer;
import jcompiler.tokenizer.exceptions.UnknownTokenException;

/*语法分析基础设施，务必保证全部的语法分析类都适用同一个*/
public class AnalyzerUtil {
    /*使用的词法分析器*/
    private Tokenizer Tokenizer;
    /*偷偷往前看了一个的token*/
    private Token PeekedToken;
    public AnalyzerUtil(Tokenizer tokenizer){
        this.Tokenizer = tokenizer;
    }
    /*若干基础设施*/
    // 看一眼下一个token是啥
    public Token peek() throws UnknownTokenException {
        if(this.PeekedToken==null){
            this.PeekedToken = this.Tokenizer.getToken();
        }
        return this.PeekedToken;
    }
    // 前进到下一个token
    public Token next() throws UnknownTokenException{
        Token t;
        if(this.PeekedToken!=null){
            t = this.PeekedToken;
            this.PeekedToken = null;
            //System.out.println(t.toString());
        }
        else{
            t = this.Tokenizer.getToken();
            //System.out.println(t.toString());
        }
        return t;
    }
    //前进道下一个token，如果不是特定类型就抛出异常
    public Token next(TokenType tt)throws ErrorTokenTypeException{
        Token t = this.next();
        if(t.getType()!=tt)throw new ErrorTokenTypeException();
        else return t;
    }
    // 检查一下下一个token是不是这个类型
    public boolean check(TokenType tt)throws UnknownTokenException{
        return this.peek().getType()==tt;
    }
    // 如果下一个token是这个类型，那就前进，返回这个token，否则是null
    public Token nextIf(TokenType tt) throws UnknownTokenException{
        if(this.peek().getType()==tt)return this.next();
        else{
            return null;
        }
    }
    // 期望下一个token的类别，如果不是，就抛出异常，否则就往下
    public void expect(TokenType tt){
        if(this.peek().getType()==tt)this.next();
        else{
            throw new ErrorTokenTypeException();
        }
    }
}

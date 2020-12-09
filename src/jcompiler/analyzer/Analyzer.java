package jcompiler.analyzer;

import jcompiler.analyzer.exceptions.ErrorTokenTypeException;
import jcompiler.tokenizer.Token;
import jcompiler.tokenizer.TokenType;
import jcompiler.tokenizer.Tokenizer;
import jcompiler.tokenizer.exceptions.UnknownTokenException;

/*
* 语法分析器，使用OPG完成expr的分析，先完成expr部分
* */
public class Analyzer {

    /*词法分析器*/
    private Tokenizer Tokenizer;//用来进行词法分析的词法分析器

    /*偷偷往前看了一个的token*/
    private Token PeekedToken;

    /*无参数创建analyzer*/
    public Analyzer(){
        super();
    }

    /*根据输入文件创建新的tokenizer*/
    public Analyzer(String input_file){
        this.Tokenizer = new Tokenizer(input_file);
    }

    /*若干基础设施*/
    // 看一眼下一个token是啥
    private Token peek() throws UnknownTokenException {
        if(this.PeekedToken==null){
            this.PeekedToken = this.Tokenizer.getToken();
        }
        return this.PeekedToken;
    }
    // 前进到下一个token
    private Token next() throws UnknownTokenException{
        if(this.PeekedToken!=null){
            Token t = this.PeekedToken;
            this.PeekedToken = null;
            return t;
        }
        else{
            return this.Tokenizer.getToken();
        }
    }
    // 检查一下下一个token是不是这个类型
    private boolean check(TokenType tt)throws UnknownTokenException{
        return this.peek().getType()==tt;
    }
    // 如果下一个token是这个类型，那就前进，返回这个token，否则是null
    private Token nextIf(TokenType tt) throws UnknownTokenException{
        if(this.peek().getType()==tt)return this.next();
        else{
            return null;
        }
    }
    // 期望下一个token的类别，如果不是，就抛出异常，否则就往下
    private void expect(TokenType tt) throws Exception {
        if(this.peek().getType()==tt)this.next();
        else{
            throw new ErrorTokenTypeException();
        }
    }
}

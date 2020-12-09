package jcompiler.analyzer;

import jcompiler.tokenizer.Tokenizer;

/*
* 使用OPG解决expression的分析问题
* */
public class ExprAnalyzer {

    private Tokenizer Tokenizer;//用来进行词法分析的词法分析器

    /*无参数创建analyzer*/
    public ExprAnalyzer(){
        super();
    }

    /*根据输入文件创建新的tokenizer*/
    public ExprAnalyzer(String input_file){
        this.Tokenizer = new Tokenizer(input_file);
    }

    /*若干基础设施*/
}

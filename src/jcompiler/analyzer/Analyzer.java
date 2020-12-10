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
    /*使用的基础设施*/
    private AnalyzerUtil Util;

    /*无参数创建analyzer*/
    public Analyzer(){
        super();
    }

    /*根据输入文件创建新的tokenizer*/
    public Analyzer(AnalyzerUtil util){
        this.Util = util;
    }

}

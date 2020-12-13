package jcompiler.analyzer;

import jcompiler.analyzer.exceptions.ErrorTokenTypeException;
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
    public void analyseExpression(){

    }

    /*解析声明语句*/
    public void analyseDeclStmt(){

    }

    /*解析if语句*/
    public void analyseIfStmt(){

    }

    /*解析while语句*/
    public void analyseWhileStmt(){

    }

    /*解析返回语句*/
    public void analyseReturnStmt(){

    }

    /*解析语句块*/
    public void analyseBlockStmt(){

    }

    /*解析空语句*/
    public void analyseEmptyStmt(){

    }
}

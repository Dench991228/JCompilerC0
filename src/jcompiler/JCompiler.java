package jcompiler;

import jcompiler.analyzer.AnalyzerUtil;
import jcompiler.analyzer.ExprAnalyzer;
import jcompiler.tokenizer.Token;
import jcompiler.tokenizer.TokenType;
import jcompiler.tokenizer.Tokenizer;
import jcompiler.tokenizer.exceptions.UnknownTokenException;

import java.io.*;
import java.util.Scanner;

public class JCompiler {
    public static void main(String[] args){
        Tokenizer t = new Tokenizer("test.txt");
        AnalyzerUtil util = new AnalyzerUtil(t);
        try{
            ExprAnalyzer expr_analyzer = new ExprAnalyzer(util);
            expr_analyzer.analyseExpr();
            System.out.println();
        }
        catch(FileNotFoundException e){
            System.out.println("file not found");
        }
    }
}

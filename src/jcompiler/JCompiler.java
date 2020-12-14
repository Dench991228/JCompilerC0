package jcompiler;

import jcompiler.analyzer.Analyzer;
import jcompiler.analyzer.AnalyzerUtil;
import jcompiler.analyzer.ExprAnalyzer;
import jcompiler.analyzer.StmtAnalyzer;
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
            Analyzer analyser = new Analyzer(util);
            analyser.analyse();
            System.out.println();
        }
        catch(FileNotFoundException e){
            System.out.println("file not found");
        }
    }
}

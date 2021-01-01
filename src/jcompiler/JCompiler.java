package jcompiler;

import jcompiler.action.GlobalVariable;
import jcompiler.analyzer.Analyzer;
import jcompiler.analyzer.AnalyzerUtil;
import jcompiler.analyzer.ExprAnalyzer;
import jcompiler.analyzer.StmtAnalyzer;
import jcompiler.tokenizer.Token;
import jcompiler.tokenizer.TokenType;
import jcompiler.tokenizer.Tokenizer;
import jcompiler.tokenizer.exceptions.UnknownTokenException;
import jcompiler.util.BinaryHelper;

import java.io.*;
import java.util.Scanner;

public class JCompiler {
    public static void main(String[] args){
        /*
        Tokenizer t = new Tokenizer(args[0]);
        AnalyzerUtil util = new AnalyzerUtil(t);
        try{
            Analyzer analyser = new Analyzer(util);
            analyser.analyse();
            System.out.println();
        }
        catch(FileNotFoundException e){
            System.out.println("file not found");
        }*/
        GlobalVariable gb = GlobalVariable.StringGlobal("this is a test sentence");
        System.out.println(gb);
        System.out.println(gb.toByte());
        System.out.println(BinaryHelper.BinaryLong(5000L));
        System.out.println(BinaryHelper.BinaryLong(128L));
        System.out.println(BinaryHelper.BinaryDouble(128D));
    }
}

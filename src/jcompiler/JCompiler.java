package jcompiler;

import jcompiler.action.GlobalVariable;
import jcompiler.action.Instruction;
import jcompiler.action.ObjectFile;
import jcompiler.analyzer.Analyzer;
import jcompiler.analyzer.AnalyzerUtil;
import jcompiler.analyzer.ExprAnalyzer;
import jcompiler.analyzer.StmtAnalyzer;
import jcompiler.tokenizer.Tokenizer;
import jcompiler.tokenizer.exceptions.UnknownTokenException;
import jcompiler.util.BinaryHelper;

import java.io.*;
import java.util.Scanner;

public class JCompiler {
    public static void main(String[] args){
        Tokenizer t = new Tokenizer(args[0]);
        AnalyzerUtil util = new AnalyzerUtil(t);
        ObjectFile obj = new ObjectFile();
        try{
            Analyzer analyser = new Analyzer(util, obj);
            analyser.analyse();
            System.out.println();
            System.out.println(obj);
            obj.writeToFile(args[1]);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

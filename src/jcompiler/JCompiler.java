package jcompiler;

import jcompiler.tokenizer.StringUtil;

import java.io.*;

public class JCompiler {
    public static void main(String[] args){
        StringUtil su = new StringUtil("test.txt");
        while(!su.isCurEOF()){
            su.skipBlank();
            if(!su.isCurEOF())System.out.print(su.getChar());
        }
    }
}

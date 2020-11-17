package jcompiler;

import jcompiler.tokenizer.Token;
import jcompiler.tokenizer.TokenType;
import jcompiler.tokenizer.Tokenizer;
import jcompiler.tokenizer.exceptions.UnknownTokenException;

import java.io.*;

public class JCompiler {
    public static void main(String[] args){
        Tokenizer t = new Tokenizer("test.txt");
        try{
            Token token = t.getToken();
            while(token.getType()!= TokenType.EOF){
                System.out.println(token.toString());
                token = t.getToken();
            }
        }
        catch(UnknownTokenException e){
            System.out.println("Stumbled Upon Unknown Token!");
        }
    }
}

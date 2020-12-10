package jcompiler.analyzer;

import jcompiler.analyzer.exceptions.ErrorTokenTypeException;
import jcompiler.tokenizer.TokenType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class PriorityMatrix {
    private Map<TokenType, Map<TokenType, Character>> matrix;
    private List<TokenType> OperandTypes;
    public PriorityMatrix() throws FileNotFoundException {
        this.matrix = new HashMap<>();
        this.OperandTypes = List.of(TokenType.SHARP, TokenType.PLUS, TokenType.MINUS, TokenType.MUL, TokenType.DIV, TokenType.EQ, TokenType.NEQ, TokenType.GT, TokenType.LT, TokenType.GE, TokenType.LE, TokenType.ASSIGN, TokenType.AS_KW, TokenType.L_PAREN, TokenType.R_PAREN, TokenType.COMMA, TokenType.NEG);
        Scanner sc = new Scanner(new File("operand_priority.md"));
        String new_line = sc.nextLine();
        new_line = sc.nextLine();
        for(TokenType tt:this.OperandTypes){
            new_line = sc.nextLine();
            new_line = new_line.split("\\|",1)[1];
            this.putLine(tt, new_line);
        }
    }
    /*输入一个操作符，判断优先级*/
    private void putLine(TokenType tt, String value){
        String[] values = value.split("\\|");
        int i = 0;
        if(!this.matrix.containsKey(tt)){
            this.matrix.put(tt, new HashMap<>());
        }
        for(TokenType t:this.OperandTypes){
            matrix.get(tt).put(t, values[i].charAt(0));
            i++;
        }
    }
    /*比较两个算符的优先级*/
    public char compare(TokenType tt1, TokenType tt2) throws ErrorTokenTypeException {
        if(!this.matrix.containsKey(tt1)){
            throw new ErrorTokenTypeException();
        }
        if(!this.matrix.get(tt1).containsKey(tt2)){
            throw new ErrorTokenTypeException();
        }
        return this.matrix.get(tt1).get(tt2);
    }
}

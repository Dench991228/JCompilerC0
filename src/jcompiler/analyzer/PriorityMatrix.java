package jcompiler.analyzer;

import jcompiler.tokenizer.TokenType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PriorityMatrix {
    private Map<TokenType, Map<TokenType, Character>> matrix;
    private List<TokenType> OperandTypes;
    public PriorityMatrix(){
        this.matrix = new HashMap<>();
        this.OperandTypes = List.of(TokenType.SHARP, TokenType.PLUS, TokenType.MINUS, TokenType.MUL, TokenType.DIV, TokenType.EQ, TokenType.NEQ, TokenType.GT, TokenType.LT, TokenType.GE, TokenType.LE, TokenType.ASSIGN, TokenType.AS_KW, TokenType.L_PAREN, TokenType.R_PAREN);
        this.putLine(TokenType.SHARP, "r|s|s|s|s|s|s|s|s|s|s|s|s|s|e");
        this.putLine(TokenType.PLUS, "r|r|r|s|s|r|r|r|r|r|r|r|s|s|r");
        this.putLine(TokenType.MINUS, "r|r|r|s|s|r|r|r|r|r|r|r|s|s|r");
        this.putLine(TokenType.MUL, "r|r|r|r|r|r|r|r|r|r|r|r|s|s|r");
        this.putLine(TokenType.DIV, "r|r|r|r|r|r|r|r|r|r|r|r|s|s|r");
        this.putLine(TokenType.EQ, "r|s|s|s|s|r|r|r|r|r|r|r|s|s|r");
        this.putLine(TokenType.NEQ, "r|s|s|s|s|r|r|r|r|r|r|r|s|s|r");
        this.putLine(TokenType.GT, "r|s|s|s|s|r|r|r|r|r|r|r|s|s|r");
        this.putLine(TokenType.LT, "r|s|s|s|s|r|r|r|r|r|r|r|s|s|r");
        this.putLine(TokenType.GE, "r|s|s|s|s|r|r|r|r|r|r|r|s|s|r");
        this.putLine(TokenType.LE, "r|s|s|s|s|r|r|r|r|r|r|r|s|s|r");
        this.putLine(TokenType.ASSIGN, "r|s|s|s|s|s|s|s|s|s|s|s|s|s|s");
        this.putLine(TokenType.AS_KW, "r|r|r|r|r|r|r|r|r|r|r|r|r|e|r");
        this.putLine(TokenType.L_PAREN, "e|s|s|s|s|s|s|s|s|s|s|s|s|s|s");
        this.putLine(TokenType.R_PAREN, "r|r|r|r|r|r|r|r|r|r|r|r|r|e|r");
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
}

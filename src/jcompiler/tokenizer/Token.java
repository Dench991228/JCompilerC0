package jcompiler.tokenizer;

import jcompiler.util.Pos;

public class Token {
    private TokenType Type;
    private Object Value;
    private Pos StartPos;
    public Token(TokenType t, Object v,Pos s){
        this.Type = t;
        this.Value = v;
        this.StartPos = s;
    }
    @Override
    public String toString(){
        StringBuilder result = new StringBuilder();
        result.append("Token(").append(Type);
        if(this.Type==TokenType.EOF){
            result.append(')');
            return result.toString();
        }
        return result.append(",Value:").append(Value).append("),at:").append(StartPos).toString();
    }

    public TokenType getType() {
        return Type;
    }

    public Object getValue() {
        return Value;
    }

    public Pos getStartPos() {
        return StartPos;
    }
}

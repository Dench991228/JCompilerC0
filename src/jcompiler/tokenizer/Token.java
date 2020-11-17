package jcompiler.tokenizer;

import jcompiler.util.Pos;

public class Token {
    private TokenType Type;
    private Object Value;
    private Pos StartPos;
    private Pos EndPos;
    public Token(TokenType t, Object v,Pos s, Pos e){
        this.Type = t;
        this.Value = v;
        this.StartPos = s;
        this.EndPos = e;
    }
    @Override
    public String toString(){
        return new StringBuilder().append("Token(").append(Type).append(",Value:").append(Value).append("),at:").append(StartPos).toString();
    }
}

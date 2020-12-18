package jcompiler.analyzer;

import jcompiler.tokenizer.Token;
import jcompiler.tokenizer.TokenType;
import jcompiler.util.Pos;

import java.util.LinkedList;

/*符号表表项*/
public class SymbolEntry {
    /*符号是不是变量，true就是变量，false就是函数*/
    private boolean IsVar;
    /*类型，如果是变量就是变量类型，如果是函数，就是函数返回值*/
    private Token Type;

    /*变量相关*/
    /*如果是变量，这表示它是不是常量*/
    private boolean IsConst;
    /*如果是变量，表明其是否被初始化过*/
    private boolean IsInitialized;

    /*函数相关*/
    /*参数列表*/
    private LinkedList<Token> ParameterTypes;

    /*定义的位置*/
    private Pos DeclPos;

    public SymbolEntry(){

    }

    /*获得一个变量的表项*/
    public SymbolEntry getVariableEntry(Token variable_type,boolean is_constant, Pos decl_pos){
        SymbolEntry entry = new SymbolEntry();
        entry.IsConst = is_constant;
        entry.Type = variable_type;
        entry.IsVar = true;
        entry.IsInitialized = false;
        entry.DeclPos = decl_pos;
        return entry;
    }

    /*获取一个函数的表项*/
    public SymbolEntry getFunctionEntry(Token return_type, LinkedList<Token> param_types, Pos decl_pos){
        SymbolEntry entry = new SymbolEntry();
        entry.Type = return_type;
        entry.IsVar = false;
        entry.ParameterTypes = param_types;
        entry.DeclPos = decl_pos;
        return entry;
    }

    public void setInitialized(boolean initialized){
        this.IsInitialized = initialized;
    }

    public boolean isVar() {
        return IsVar;
    }

    public Token getType() {
        return Type;
    }

    public boolean isConst() {
        return IsConst;
    }

    public boolean isInitialized() {
        return IsInitialized;
    }

    public LinkedList<Token> getParameterTypes() {
        return ParameterTypes;
    }

    public boolean compareParams(LinkedList<Token> params){
        if(this.ParameterTypes.size()!=params.size())return false;
        else{
            int i, len = params.size();
            for(i=0;i<len;i++){
                if(params.get(i).getValue().toString().compareTo(this.ParameterTypes.get(i).getValue().toString())!=0){
                    return false;
                }
            }
        }
        return true;
    }
}

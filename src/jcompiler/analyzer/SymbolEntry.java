package jcompiler.analyzer;

import jcompiler.action.GlobalVariable;
import jcompiler.tokenizer.Token;
import jcompiler.tokenizer.TokenType;
import jcompiler.util.Pos;

import java.util.LinkedList;

/*符号表表项*/
public class SymbolEntry {
    /*符号是不是变量，true就是变量，false就是函数*/
    private boolean IsVar;
    /*0是全局变量，1是函数参数，2是局部变量*/
    private int VariableCategory;
    /*如果是全局变量，就是编号；如果是函数参数，就是偏移量（0是返回值，如果有的话）；如果是局部变量，也是偏移量，如果是函数，就是函数的id*/
    private int Position;
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

    /*获得一个变量的表项，并且想办法把它登记到相应的函数/全局变量中*/
    public static SymbolEntry getVariableEntry(Token variable_type,boolean is_constant, Pos decl_pos){
        SymbolEntry entry = new SymbolEntry();
        entry.IsConst = is_constant;
        entry.Type = variable_type;
        entry.IsVar = true;
        entry.IsInitialized = false;
        entry.DeclPos = decl_pos;
        if(Analyzer.AnalyzerTable.getFatherTable()==null){//全局变量
            entry.VariableCategory = 0;
            GlobalVariable gbv;
            if(variable_type==Token.INTEGER){
                gbv = GlobalVariable.IntegerGlobal(0L, is_constant);
            }
            else{
                gbv = GlobalVariable.DoubleGlobal(0.0D, is_constant);
            }
            int id = Analyzer.ObjFile.addGlobalVariable(gbv);//全局变量第几个
            entry.Position = id;
        }
        else{//局部变量
            entry.VariableCategory = 2;
            int id = Analyzer.CurrentFunction.registerLocal(variable_type);
            entry.Position = id;
        }
        return entry;
    }

    /*获得一个变量的表项，并且想办法把它登记到相应的函数/全局变量中*/
    public static SymbolEntry getParameterEntry(Token variable_type,boolean is_constant, Pos decl_pos){
        SymbolEntry entry = new SymbolEntry();
        entry.IsConst = is_constant;
        entry.Type = variable_type;
        entry.IsVar = true;
        entry.IsInitialized = false;
        entry.DeclPos = decl_pos;
        entry.VariableCategory = 1;
        return entry;
    }

    /*获取一个函数的表项*/
    public static SymbolEntry getFunctionEntry(Token return_type, LinkedList<Token> param_types, Pos decl_pos){
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

    public int getVariableCategory() {
        return VariableCategory;
    }

    public void setVariableCategory(int variableCategory) {
        VariableCategory = variableCategory;
    }

    public int getPosition() {
        return Position;
    }

    public void setPosition(int position) {
        Position = position;
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

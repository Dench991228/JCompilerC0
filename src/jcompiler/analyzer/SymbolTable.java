package jcompiler.analyzer;

import jcompiler.action.Instruction;
import jcompiler.analyzer.exceptions.IdentifierTypeException;
import jcompiler.analyzer.exceptions.NotDeclaredException;
import jcompiler.analyzer.exceptions.RepeatDeclareException;
import jcompiler.tokenizer.Token;

import java.util.HashMap;

/*符号表，每张表都是hash表，主键是identifier的名字，值是表项，还要有一个指向上级符号表的指针*/
public class SymbolTable {
    /*上级符号表*/
    private final SymbolTable FatherTable;
    /*当前的表项*/
    private final HashMap<String, SymbolEntry> Table = new HashMap<>();

    /*创建一个符号表*/
    public SymbolTable(){
        this.FatherTable = null;
    }

    /*在上级符号表的基础上，创建一个符号表*/
    public SymbolTable(SymbolTable father){
        this.FatherTable = father;
    }

    /*根据identifier的名字，获取变量，并且把这个变量放到顶上*/
    public SymbolEntry findVariable(Token token) throws NotDeclaredException, IdentifierTypeException {
        SymbolTable cur = this;
        String token_name = (String)token.getValue();
        while(cur!=null){
            SymbolEntry entry = cur.Table.getOrDefault(token_name, null);
            if(entry==null){//当前作用域找不到，那就去上级找
                cur = cur.FatherTable;
            }
            else{//找到了，判断一下是不是
                if(!entry.isVar())throw new IdentifierTypeException();
                else{
                    return entry;
                }
            }
        }
        throw new NotDeclaredException();
    }

    /*根据identifier的名字，获取函数*/
    public SymbolEntry findFunction(Token token) throws NotDeclaredException, IdentifierTypeException {
        SymbolTable cur = this;
        String token_name = (String)token.getValue();
        while(cur!=null){
            SymbolEntry entry = cur.Table.getOrDefault(token_name, null);
            if(entry==null){//当前作用域找不到，那就去上级找
                cur = cur.FatherTable;
            }
            else{//找到了，判断一下是不是
                if(entry.isVar())throw new IdentifierTypeException();
                else{
                    return entry;
                }
            }
        }
        throw new NotDeclaredException();
    }

    /*放一个identifier进来，可能抛出重复异常，仅考虑相同作用域*/
    public void putIdent(Token token, SymbolEntry entry) throws RepeatDeclareException {
        String token_name = (String)token.getValue();
        if(this.Table.containsKey(token_name))throw new RepeatDeclareException();
        else{
            this.Table.put(token_name, entry);
        }
    }

    public SymbolTable getFatherTable() {
        return FatherTable;
    }

    public void setInitialized(Token token){
        SymbolEntry entry = this.findVariable(token);
        entry.setInitialized(true);
    }

    /*把一个标识符对应的值放到顶上*/
    public void moveValueStackTop(Token ident){
        SymbolEntry entry = this.findVariable(ident);
        Instruction ins;
        switch (entry.getVariableCategory()){
            case 0://全局变量
                ins = Instruction.getInstruction("globa", entry.getPosition());
                Analyzer.CurrentFunction.addInstruction(ins);
                ins = Instruction.getInstruction("load.64");
                Analyzer.CurrentFunction.addInstruction(ins);
                break;
            case 1://参数
            case 2://局部变量
                ins = Instruction.getInstruction("loca", entry.getPosition());
                Analyzer.CurrentFunction.addInstruction(ins);
                ins = Instruction.getInstruction("load64");
                Analyzer.CurrentFunction.addInstruction(ins);
                break;
        }
    }
    /*把一个标识符对应的地址放到顶上*/
    public void moveAddressStackTop(Token ident){
        SymbolEntry entry = this.findVariable(ident);
        Instruction ins;
        switch (entry.getVariableCategory()){
            case 0://全局变量
                ins = Instruction.getInstruction("globa", entry.getPosition());
                Analyzer.CurrentFunction.addInstruction(ins);
                break;
            case 1://参数
            case 2://局部变量
                ins = Instruction.getInstruction("loca", entry.getPosition());
                Analyzer.CurrentFunction.addInstruction(ins);
                break;
        }
    }
}

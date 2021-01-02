package jcompiler.action;

import java.util.LinkedList;
import java.util.List;

public class ObjectFile {
    /*魔数*/
    private final int magic = 0x72303b3e;
    /*版本号*/
    private final int version = 1;
    /*输出文件的名字*/
    private String name;
    /*全部的函数*/
    private final List<Function> Functions = new LinkedList<>();
    /*全部的全局变量*/
    private final List<GlobalVariable> GlobalVariables = new LinkedList<>();

    public ObjectFile(){

    }

    public ObjectFile(String s){
        this.name = s;
    }

    /*增加一个全局变量，并且返回其是第几个*/
    public int addGlobalVariable(GlobalVariable gbv){
        this.GlobalVariables.add(gbv);
        return GlobalVariables.size()-1;
    }

    /*增加一个函数，并且返回其id*/
    public int addFunction(Function func){
        this.Functions.add(func);
        return this.Functions.size();
    }
}

package jcompiler.action;

import jcompiler.util.BinaryHelper;

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
        func.setID(this.Functions.size()-1);
        return this.Functions.size()-1;
    }
    @Override
    public String toString(){
        StringBuilder s= new StringBuilder();
        s.append("magic number:").append(BinaryHelper.BinaryInteger(this.magic)).append('\n');//魔数
        s.append("version:").append(BinaryHelper.BinaryInteger(this.magic)).append('\n');//版本号
        s.append("number of global variables:").append(BinaryHelper.BinaryInteger(this.GlobalVariables.size())).append('\n');//多少全局变量
        for(GlobalVariable gbv:this.GlobalVariables){
            s.append(gbv.toString()).append('\n');
        }
        s.append("number of functions:").append(BinaryHelper.BinaryInteger(this.Functions.size())).append('\n');//多少个函数
        for(Function func:this.Functions){
            s.append(func.toString()).append('\n');
        }
        return s.toString();
    }
}

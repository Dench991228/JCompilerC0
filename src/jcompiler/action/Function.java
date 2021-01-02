package jcompiler.action;

import jcompiler.util.BinaryHelper;

import java.util.LinkedList;
import java.util.List;

public class Function {
    /*id*/
    private int ID;
    /*返回值占据几个slots，浮点数和长整型是1*/
    private int ReturnSlot;
    /*参数一共占据多少个槽*/
    private int ParamSlot;
    /*局部变量一共占据多少个槽*/
    private int LocSlot;
    /*函数的指令*/
    List<Instruction> Instructions;

    int getID() {
        return ID;
    }

    public int getReturnSlot() {
        return ReturnSlot;
    }

    public int getParamSlot() {
        return ParamSlot;
    }

    public int getLocSlot() {
        return LocSlot;
    }

    void setID(int ID) {
        this.ID = ID;
    }

    public void setReturnSlot(int returnSlot) {
        ReturnSlot = returnSlot;
    }

    public void setParamSlot(int paramSlot) {
        ParamSlot = paramSlot;
    }

    public void setLocSlot(int locSlot) {
        LocSlot = locSlot;
    }

    public void addInstruction(Instruction ins){
        this.Instructions.add(ins);
    }

    /*把函数变成二进制*/
    public List<Byte> toByte(){
        LinkedList<Byte> result = new LinkedList<>();
        /*把编号加进来*/
        result.addAll(BinaryHelper.BinaryInteger(this.ID));
        /*返回值槽数*/
        result.addAll(BinaryHelper.BinaryInteger(this.ReturnSlot));
        /*参数值槽数*/
        result.addAll(BinaryHelper.BinaryInteger(this.ParamSlot));
        /*局部变量槽数*/
        result.addAll(BinaryHelper.BinaryInteger(this.LocSlot));
        /*一共有多少指令*/
        result.addAll(BinaryHelper.BinaryInteger(this.Instructions.size()));
        /*指令的二进制*/
        for(Instruction ins:this.Instructions){
            result.addAll(ins.toByte());
        }
        return result;
    }
}

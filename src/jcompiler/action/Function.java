package jcompiler.action;

import jcompiler.action.exceptions.NoSuchInstructionException;
import jcompiler.tokenizer.Token;
import jcompiler.util.BinaryHelper;

import java.util.LinkedList;
import java.util.List;

public class Function {
    /*id*/
    private int ID;
    /*返回值占据几个slots，浮点数和长整型是1*/
    private int ReturnSlot = 0;
    /*参数一共占据多少个槽*/
    private int ParamSlot = 0;
    /*局部变量一共占据多少个槽*/
    private int LocSlot = 0;
    /*函数的指令*/
    List<Instruction> Instructions = new LinkedList<>();

    int getID() {
        return ID;
    }

    void setID(int ID) {
        this.ID = ID;
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
    /*给这个函数登记一个参数，不管是啥，都是1个槽
    * type是参数的类型
    * @return 返回这个参数的偏移量
    * */
    public int registerParam(Token type){
        int offset = this.ParamSlot+1;
        this.ParamSlot++;
        return offset;
    }

    /*给这个函数登记一个返回值，只要不是void，就有一个槽*/
    public void registerReturn(Token type){
        if(type==Token.DOUBLE||type==Token.INTEGER){
            this.ReturnSlot+=1;
        }
    }

    /*给这个函数登记一个临时变量，必定是1个槽*/
    public int registerLocal(Token type){
        int offset = this.LocSlot;
        this.LocSlot++;
        return offset;
    }
    @Override
    public String toString(){
        StringBuilder s = new StringBuilder();
        s.append("Function Name:").append(BinaryHelper.BinaryInteger(this.ID)).append('\n');
        s.append("Return Slot:").append(BinaryHelper.BinaryInteger(this.ReturnSlot)).append("\n");
        s.append("Parameter Slot:").append(BinaryHelper.BinaryInteger(this.ParamSlot)).append("\n");
        s.append("Local Slot:").append(BinaryHelper.BinaryInteger(this.LocSlot)).append("\n");
        s.append("Instructions:\n");
        for(Instruction ins:Instructions){
            s.append(ins.toString()).append("\n");
        }
        return s.toString();
    }

    public int getParamSlot() {
        return ParamSlot;
    }

    /*计算这条指令跳转到即将加入进来的指令要跳几步：新指令下标-（当前指令下标+1）*/
    public int getOffset(Instruction ins){
        int index = this.Instructions.indexOf(ins);//瞅一眼这个在不在里面
        if(index==-1)throw new NoSuchInstructionException();//如果不包括这条指令，那就GG
        int cur_position = this.Instructions.size();
        return cur_position - index;
    }
    /*返回一个函数的最后一条指令*/
    public Instruction getLastInstruction(){
        return this.Instructions.get(this.Instructions.size()-1);
    }

    public int getReturnSlot(){
        return this.ReturnSlot;
    }
}

package jcompiler.action;

import jcompiler.util.BinaryHelper;

import java.util.*;

public class GlobalVariable {
    /*是不是常数*/
    private int IsConst;
    /*具体的值*/
    private List<Byte> Value;

    private GlobalVariable(){
        this.IsConst = 0;
    }
    /*创建一个字符串全局变量*/
    public static GlobalVariable StringGlobal(String s){
        GlobalVariable gbv = new GlobalVariable();
        gbv.IsConst = 1;
        char[] characters = s.toCharArray();
        gbv.Value = new LinkedList<>();
        for(char c:characters){
            byte b = (byte)(c%256);
            gbv.Value.add(b);
        }
        return gbv;
    }

    /*创建一个整数全局变量*/
    public static GlobalVariable IntegerGlobal(Long i, boolean is_const){
        List<Byte> long_bytes = BinaryHelper.BinaryLong(i);
        GlobalVariable gbv = new GlobalVariable();
        gbv.IsConst = is_const?1:0;
        gbv.Value = long_bytes;
        return gbv;
    }

    /*创建一个浮点数全局变量*/
    public static GlobalVariable DoubleGlobal(Double d, boolean is_const){
        List<Byte> long_bytes = BinaryHelper.BinaryDouble(d);
        GlobalVariable gbv = new GlobalVariable();
        gbv.IsConst = is_const?1:0;
        gbv.Value = long_bytes;
        return gbv;
    }

    public List<Byte> toByte(){
        int length_array = this.Value.size();
        LinkedList<Byte> bytes = new LinkedList<>();
        bytes.add((byte)this.IsConst);
        bytes.addAll(BinaryHelper.BinaryInteger(length_array));
        bytes.addAll(this.Value);
        return bytes;
    }

    @Override
    public String toString(){
        String s = "Is constant value:"+((byte)(this.IsConst));
        s+="value:"+this.Value.toString();
        return s;
    }
}

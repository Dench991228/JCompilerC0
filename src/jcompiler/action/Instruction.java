package jcompiler.action;

import jcompiler.analyzer.Analyzer;
import jcompiler.analyzer.SymbolEntry;
import jcompiler.tokenizer.Token;
import jcompiler.tokenizer.TokenType;
import jcompiler.util.BinaryHelper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Instruction {
    /*指令码*/
    private int OpCode;
    /*指令名称，没什么卵用，就是用来debug*/
    private String InsName;
    /*操作数，以字节的形式储存*/
    private List<Byte> Operand;
    /*指令名称到指令号的映射关系*/
    private static HashMap<String, Integer> Instructions = new HashMap<>();
    /*初始化，读取相关指令*/
    static{
        try{
            Scanner sc = new Scanner(new File("instruction.txt"));
            while(sc.hasNextLine()){
                String input_line = sc.nextLine();
                String[] name_content = input_line.split("\\t");
                Instructions.put(name_content[1], Integer.parseInt(name_content[0]));
            }
        }
        catch(IOException e){
            System.out.println("Instructions: Instructions load failed!");
        }
    }

    private Instruction(){

    }
    /*生成一个无参数的指令，不对参数与指令的关系做检查*/
    public static Instruction getInstruction(String s){
        Instruction ins = new Instruction();
        ins.InsName = s;
        ins.OpCode = Instructions.get(s);//获得指令码
        ins.Operand = new LinkedList<>();
        return ins;
    }

    /*生成一个四字节参数的指令，不对参数与指令的关系做检查*/
    public static Instruction getInstruction(String s, int param){
        Instruction ins = new Instruction();
        ins.OpCode = Instructions.get(s);//获得指令码
        ins.Operand = BinaryHelper.BinaryInteger(param);
        ins.InsName = s;
        return ins;
    }

    /*生成一个八字节参数的指令，不对参数与指令的关系做检查*/
    public static Instruction getInstruction(String s, long param){
        Instruction ins = new Instruction();
        ins.OpCode = Instructions.get(s);//获得指令码
        ins.Operand = BinaryHelper.BinaryLong(param);
        ins.InsName = s;
        return ins;
    }

    /*生成一个八字节参数的指令，不对参数与指令的关系做检查*/
    public static Instruction getInstruction(String s, double param){
        long l = Double.doubleToRawLongBits(param);
        Instruction ins = new Instruction();
        ins.OpCode = Instructions.get(s);//获得指令码
        ins.Operand=new LinkedList<>();
        int i=7;
        while(i>=0){
            ins.Operand.add((byte)((l>>(i*8))&0xff));
            i--;
        }
        ins.InsName = s;
        return ins;
    }

    public List<Byte> toByte(){
        List<Byte> result = new LinkedList<>();
        result.add((byte)(this.OpCode));
        if(this.Operand!=null)result.addAll(this.Operand);
        return result;
    }
    @Override
    public String toString(){
        String s = (byte)(this.OpCode)+"("+this.InsName+"):";
        s+=this.Operand.toString();
        return s;
    }
    /*调用完一个函数之后，调用者需要弹出其参数*/
    public static void popAllParameter(Token function_ident){
        SymbolEntry function_entry = Analyzer.AnalyzerTable.findFunction(function_ident);
        int function_id = function_entry.getPosition();
        Function function = Analyzer.ObjFile.getFunctions().get(function_id);
        int num_pop = function.getParamSlot();
        Instruction ins = Instruction.getInstruction("popn", num_pop);
        Analyzer.CurrentFunction.addInstruction(ins);
    }
}

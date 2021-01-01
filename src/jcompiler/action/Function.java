package jcompiler.action;

import java.util.List;

public class Function {
    /*id*/
    int ID;
    /*返回值占据几个slots，浮点数和长整型是1*/
    int ReturnSlot;
    /*参数一共占据多少个槽*/
    int ParamSlot;
    /*局部变量一共占据多少个槽*/
    int LocSlot;
    /*函数的指令*/
    List<Instruction> Instructions;
}

package jcompiler.util;

import java.util.LinkedList;
import java.util.List;

/*辅助生成各种和二进制有关的*/
public class BinaryHelper {
    /*整数转换成字节序列*/
    public static List<Byte> BinaryInteger(int l){
        LinkedList<Byte> result = new LinkedList<>();
        int i = 0;
        for(i=0;i<4;i++){
            result.addFirst((byte)((l>>(i*8))&0xff));
        }
        return result;
    }
    /*长整数转换成字节序列*/
    public static List<Byte> BinaryLong(long l){
        LinkedList<Byte> result = new LinkedList<>();
        int i = 0;
        for(i=0;i<8;i++){
            result.addFirst((byte)((l>>(i*8))&0xff));
        }
        return result;
    }
    /*双精度浮点数转换成字节序列*/
    public static List<Byte> BinaryDouble(double d){
        long l = Double.doubleToRawLongBits(d);
        return BinaryLong(l);
    }
}

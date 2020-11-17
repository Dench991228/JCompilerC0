package jcompiler.tokenizer;

import jcompiler.util.Pos;

import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

public class StringUtil {

    private Scanner Reader;
    private LinkedList<String> Lines = new LinkedList<>();//全部程序的内容
    private Pos CurPos = new Pos(0,0);//当前的位置

    /*确定文件位置，以及初始化LinkedList*/
    public StringUtil(String file_name){
        try{
            this.Reader = new Scanner(new File(file_name));
        }
        catch(IOException e){
            System.out.println("Something is wrong!");
        }
        while(Reader.hasNextLine()){
            Lines.add(Reader.nextLine()+'\n');
        }
    }
    /*判断这个位置是不是EOF*/
    private boolean isEOF(Pos p){
        return p.Row>=Lines.size();
    }
    /* 获得下一个指针的位置 */
    private Pos nextPos(Pos p){
        /*不在最后一个字符处*/
        if(p.Col<Lines.get(p.Row).length()-1){
            return new Pos(p.Row,p.Col+1);
        }
        else{
            return new Pos(p.Row+1, 0);
        }
    }
    /* 获得上一个指针的位置 */
    private Pos formerPos(Pos p){
        /*如果是在一行开始，退回到上一行的末尾*/
        if(p.Col==0){
            return new Pos(p.Row-1, Lines.get(p.Row-1).length()-1);
        }
        else{
            return new Pos(p.Row,p.Col-1);
        }
    }
    /*获得这个位置的字符*/
    private char getCharAtPos(Pos p){
        return Lines.get(p.Row).charAt(p.Col);
    }
    /*判断这个编译器是不是到了EOF*/
    public boolean isCurEOF(){ return isEOF(CurPos);}
    /*获取当前的字符，并且指针往前，务必在调用前保证不是EOF*/
    public char getChar(){
        char result = this.getCharAtPos(this.CurPos);
        this.CurPos = this.nextPos(this.CurPos);
        return result;
    }
    /*瞅一眼下一个字符是啥，务必保证当前不是EOF*/
    public char peekChar(){
        Pos next = this.nextPos(this.CurPos);
        if(isEOF(next))return (char)(0);
        else return this.getCharAtPos(next);
    }
    /*返回一个字符，本质上是指针往回走，不能在文件开头这么做*/
    public void unread(){
        this.CurPos = this.formerPos(this.CurPos);
    }
    /*移动到一个不是空格的地方*/
    public void skipBlank(){
        char cur_char = this.getCharAtPos(this.CurPos);
        while(!this.isEOF(this.CurPos)&&(cur_char==' '||cur_char=='\n'||cur_char=='\t')){
            this.CurPos = this.nextPos(this.CurPos);
            if(!this.isEOF(CurPos))cur_char = this.getCharAtPos(this.CurPos);
        }
    }
}

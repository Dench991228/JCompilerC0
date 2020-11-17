package jcompiler.tokenizer;

import jcompiler.tokenizer.exceptions.UnknownTokenException;
import jcompiler.util.Pos;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.TreeMap;

/* 状态图中的一个点 */
class StateNode{
    /* 状态转移对应关系 */
    private TreeMap<Character, StateNode> Transfer = new TreeMap<>();

    /* 是不是合法的终结状态 */
    private boolean IsTerm;

    /*如果这个节点是终结节点，那么从这里退出的节点应该有如下的TokenType*/
    private TokenType Type;

    /*初始化一个节点，只需要考虑是否终结符即可*/
    public StateNode(boolean term, TokenType type){
        super();
        this.IsTerm = term;
        this.Type = type;
    }

    /*添加转移关系*/
    public void addTransfer(char c, StateNode target){
        this.Transfer.put(c, target);
    }

    /*获得转换过去的节点*/
    public StateNode transfer(char c){
        return this.Transfer.getOrDefault(c, null);
    }

    /*获取这个节点的是不是终结节点*/
    public boolean getIsTerm(){
        return this.IsTerm;
    }

    /*获取这个节点对应的TokenType，如果没有就是null*/
    public TokenType getTokenType(){
        return this.Type;
    }

    /*把一个不是终结节点的节点*/
    public void changeToTerminal(TokenType type){
        this.IsTerm = true;
        this.Type = type;
    }
}



/*词法分析器，本质状态图*/
public class Tokenizer {
    /*状态图的初始节点*/
    private static StateNode StartNode;

    /*保留字，通过读取文件的方法完成*/
    private static HashMap<String, TokenType> ReservedWords = new HashMap<>();

    /*已经读取的字符构成的字符串*/
    private StringBuilder SavedWord = new StringBuilder();

    /* 当前这个单词的开始位置 */
    private Pos StartPos;
    /*属于这个词法分析器的StringUtil*/
    private StringUtil Worker;

    /*添加一个运算符到状态图里面*/
    private static void addOperand(String word, TokenType type){
        int i, len = word.length();
        StateNode cur_node = StartNode;
        for(i=0;i<len;i++){
            /*如果没有后继节点，那就新建*/
            if(cur_node.transfer(word.charAt(i))==null){
                StateNode new_found;
                if(i==len-1){
                    new_found = new StateNode(true, type);
                }
                else{
                    new_found = new StateNode(false, null);
                }
                cur_node.addTransfer(word.charAt(i), new_found);
                cur_node = new_found;
            }
            /*如果有后继节点，那就转移到那，如果在此结束，而这个节点不是结束节点，那就设置其TokenType*/
            else{
                cur_node = cur_node.transfer(word.charAt(i));
                if(i==len-1){
                    cur_node.changeToTerminal(type);
                }
            }
        }
    }
    /*初始化保留字集合，保留字必须是一个ident（如果它不是保留字），从文件读取*/
    static{
        try{
            Scanner sc = new Scanner(new File("reserved.txt"));
            while(sc.hasNextLine()){
                String input_line = sc.nextLine();
                String[] name_content = input_line.split(":");
                //System.out.println(name_content[1]+":"+TokenType.valueOf(name_content[0]));
                ReservedWords.put(name_content[1], TokenType.valueOf(name_content[0]));
            }
        }
        catch(IOException e){
            System.out.println("Tokenizer: Reserved word file not found!");
        }
    }
    /*初始化状态图，硬编码部分，主要包括整数和标识符*/
    static{
        /*新建各种状态*/
        StartNode = new StateNode(false, null);
        /* 标识符的终结状态，注意要排除保留字的可能性 */
        StateNode ident_node = new StateNode(true, TokenType.IDENT);
        /* 整数的终结状态 */
        StateNode integer_node = new StateNode(true, TokenType.UINT_LITERAL);

        /* 添加起始节点的各种转移关系 */
        char cur_lower = 'a';
        char cur_upper = 'A';
        for(int i=0;i<26;i++){
            StartNode.addTransfer(cur_lower, ident_node);
            StartNode.addTransfer(cur_upper, ident_node);
            cur_lower++;
            cur_upper++;
        }
        StartNode.addTransfer('_', ident_node);

        char cur_digit = '0';
        for(int i=0;i<10;i++){
            StartNode.addTransfer(cur_digit, integer_node);
            cur_digit++;
        }

        /*添加整数节点的各种转移关系*/
        cur_digit = '0';
        for(int i=0;i<10;i++){
            integer_node.addTransfer(cur_digit, integer_node);
            cur_digit++;
        }

        /*添加标识符的各种转移关系*/
        cur_lower = 'a';
        cur_upper = 'A';
        for(int i=0;i<26;i++){
            ident_node.addTransfer(cur_lower, ident_node);
            ident_node.addTransfer(cur_upper, ident_node);
            cur_lower++;
            cur_upper++;
        }
        ident_node.addTransfer('_', ident_node);
        cur_digit = '0';
        for(int i=0;i<10;i++){
            ident_node.addTransfer(cur_digit, integer_node);
            cur_digit++;
        }
    }
    /*初始化状态图，运算符方面*/
    static{
        try{
            Scanner sc = new Scanner(new File("operands.txt"));
            while(sc.hasNextLine()){
                String input_line = sc.nextLine();
                String[] name_content = input_line.split("@");
                //System.out.println(name_content[1]+":"+TokenType.valueOf(name_content[0]));
                addOperand(name_content[1],TokenType.valueOf(name_content[0]));
            }
        }
        catch(IOException e){
            System.out.println("Tokenizer: Reserved word file not found!");
        }
    }
    public Tokenizer(String input_file){
        this.Worker = new StringUtil(input_file);
    }
    public Token getToken()throws UnknownTokenException {
        if(!this.Worker.isCurEOF())this.Worker.skipBlank();
        if(this.Worker.isCurEOF())return new Token(TokenType.EOF, null,null);

        StateNode cur_node = StartNode;
        this.StartPos = this.Worker.getCurPos();
        while(true){
            if(!this.Worker.isCurEOF()){
                char next_char = this.Worker.peekChar();
                if(cur_node.transfer(next_char)!=null){
                    this.SavedWord.append(next_char);
                    cur_node = cur_node.transfer(this.Worker.getChar());
                }
                else{
                    break;
                }
            }
            else{
                break;
            }
        }
        /*到了一个终结状态*/
        if(cur_node.getIsTerm()){
            TokenType type = cur_node.getTokenType();
            switch(type){
                case UINT_LITERAL://数字字面量
                    int value = Integer.parseInt(this.SavedWord.toString());
                    this.SavedWord = new StringBuilder();
                    return new Token(TokenType.UINT_LITERAL, value, this.StartPos);
                case IDENT://广义标识符
                    String name = this.SavedWord.toString();
                    this.SavedWord = new StringBuilder();
                    return new Token(ReservedWords.getOrDefault(this.SavedWord.toString(), type), name, this.StartPos);
                default://其他类型，基本上意味着是操作符
                    name = this.SavedWord.toString();
                    this.SavedWord = new StringBuilder();
                    return new Token(type, name, this.StartPos);
            }
        }
        /*有问题*/
        else{
            throw new UnknownTokenException();
        }
    }
}

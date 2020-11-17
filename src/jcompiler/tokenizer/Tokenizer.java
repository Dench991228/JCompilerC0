package jcompiler.tokenizer;

import java.util.HashMap;
import java.util.HashSet;
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
}
/*词法分析器，本质状态图*/
public class Tokenizer {
    /*状态图的初始节点*/
    private static StateNode StartNode;
    /*保留字，通过读取文件的方法完成*/
    private static HashMap<String, TokenType> ReservedWords = new HashMap<>();
    /*已经读取的字符构成的字符串*/
    private static StringBuilder SavedWord = new StringBuilder();
    /*属于这个词法分析器的StringUtil*/
    private StringUtil Worker;
    /*添加一个运算符到状态图里面*/
    private static void addOperand(String word, TokenType type){
        
    }
    /*初始化保留字集合，保留字必须是一个ident（如果它不是保留字），从文件读取*/
    static{

    }
    /*初始化状态图，小部分节点硬编码，大部分自动编码*/
    static{

    }
}

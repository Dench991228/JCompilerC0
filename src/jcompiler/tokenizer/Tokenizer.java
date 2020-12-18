package jcompiler.tokenizer;

import jcompiler.tokenizer.exceptions.UnknownTokenException;
import jcompiler.util.Pos;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
                ReservedWords.put(name_content[1], TokenType.valueOf(name_content[0]));
            }
        }
        catch(IOException e){
            System.out.println("Tokenizer: Reserved word file not found!");
        }
    }
    /*初始化状态图，硬编码部分，主要包括整数和标识符，以及字符串字面量*/
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

        /*初始节点到整数节点*/
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

        /*浮点数字面量识别*/
        /*整数遇到点，进入整数加点状态*/
        StateNode integer_with_dot = new StateNode(false, null);
        integer_node.addTransfer('.', integer_with_dot);
        /*整数加点遇到整数，进入合法非科学计数法浮点数*/
        StateNode double_no_sci = new StateNode(true, TokenType.DOUBLE_LITERAL);
        cur_digit = '0';
        for(int i=0;i<10;i++){
            integer_with_dot.addTransfer(cur_digit, double_no_sci);
            double_no_sci.addTransfer(cur_digit, double_no_sci);
            cur_digit++;
        }
        /*上述节点遇到e进入e结尾浮点数*/
        StateNode double_with_e = new StateNode(false, null);
        double_no_sci.addTransfer('e', double_with_e);
        double_no_sci.addTransfer('E', double_with_e);
        /*TODO 可能遇到加号或者减号*/
        /*从e出来之后可能遇到一个加号或者减号*/
        StateNode double_with_e_operand = new StateNode(false, null);
        double_with_e.addTransfer('+', double_with_e_operand);
        double_with_e.addTransfer('-', double_with_e_operand);
        /*上述节点遇到整数进入合法的科学计数法浮点数*/
        /*double_sci遇到整数还在自己这里*/
        StateNode double_sci = new StateNode(true,TokenType.DOUBLE_LITERAL);
        cur_digit = '0';
        for(int i=0;i<10;i++){
            double_with_e_operand.addTransfer(cur_digit, double_sci);
            double_with_e.addTransfer(cur_digit, double_sci);
            double_sci.addTransfer(cur_digit, double_sci);
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
            ident_node.addTransfer(cur_digit, ident_node);
            cur_digit++;
        }

        /*字符串字面量相关节点*/
        StateNode string_left_quote = new StateNode(false,null);//字符串的左侧引号
        StateNode string_regular_char = new StateNode(false, null);//字符串的常规字符
        StateNode string_slash = new StateNode(false,null);//遇到转义字符的斜杠
        StateNode string_escaped_char = new StateNode(false,null);//转义字符结束
        StateNode string_right_quote = new StateNode(true, TokenType.STRING_LITERAL);//字符串字面量结束
        StartNode.addTransfer('\"', string_left_quote);//初始状态，遇到左引号，进入字符串匹配过程
        /*左引号状态相关*/
        /*遇到常规字符，进入常规字符匹配状态*/
        /*遇到另一个引号，进入右引号状态*/
        /*遇到反斜杠进入转义字符匹配*/
        for(char c=0;c<=(char)(65534);c++){
            if(c!='\"'&&c!='\\'){
                string_left_quote.addTransfer(c, string_regular_char);//左引号之后遇到各种正常的字符，都应该去往字符串正常字符
            }
        }
        string_left_quote.addTransfer('\"', string_right_quote);//遇到另一个引号，直接结束字符串
        string_left_quote.addTransfer('\\', string_slash);//遇到反斜杠，进入转义字符匹配状态
        /*常规字符相关*/
        /*遇到常规字符，到自己*/
        /*遇到反斜杠，去反斜杠状态*/
        /*遇到右引号，去右引号状态*/
        for(char c=0;c<=(char)(65534);c++){
            if(c!='\"'&&c!='\\'){
                string_regular_char.addTransfer(c, string_regular_char);//左引号之后遇到各种正常的字符，都应该去往字符串正常字符
            }
        }
        string_regular_char.addTransfer('\"', string_right_quote);//遇到另一个引号，直接结束字符串
        string_regular_char.addTransfer('\\', string_slash);//遇到反斜杠，进入转义字符匹配状态
        /*斜杠状态相关*/
        /*只考虑'"\rnt转移到转义状态，其他一律不行*/
        HashSet<Character> escaped_char = new HashSet<>(Arrays.asList('t', 'n', 'r', '\\', '\'', '\"'));
        for(char c:escaped_char){
            string_slash.addTransfer(c, string_escaped_char);
        }
        /*转义符状态*/
        /*和常规字符一样的*/
        for(char c=0;c<=(char)(65534);c++){
            if(c!='\"'&&c!='\\'){
                string_escaped_char.addTransfer(c, string_regular_char);//左引号之后遇到各种正常的字符，都应该去往字符串正常字符
            }
        }
        string_escaped_char.addTransfer('\"', string_right_quote);//遇到另一个引号，直接结束字符串
        string_escaped_char.addTransfer('\\', string_slash);//遇到反斜杠，进入转义字符匹配状态

        /*字符字面量*/
        /*左边的单引号*/
        StateNode node_single_quote = new StateNode(false, null);
        StartNode.addTransfer('\'', node_single_quote);
        /*遇到斜线*/
        StateNode char_slash = new StateNode(false, null);
        node_single_quote.addTransfer('\\', char_slash);
        /*从斜线出来到转义字符*/
        StateNode char_escaped = new StateNode(false, null);
        for(char c:escaped_char){
            char_slash.addTransfer(c, char_escaped);
        }
        /*常规的字符*/
        StateNode char_normal = new StateNode(false, null);
        for(char c=0; c<=(char)(65534);c++){
            if(c!='\\'&&c!='\"'){
                node_single_quote.addTransfer(c, char_normal);
            }
        }
        /*最终的状态*/
        StateNode char_end = new StateNode(true, TokenType.CHAR_LITERAL);
        char_normal.addTransfer('\'', char_end);
        char_escaped.addTransfer('\'', char_end);

        /*尝试分析注释，注意，除号也是在这里分析的*/
        /*遇到一个斜线*/
        StateNode div_node = new StateNode(true, TokenType.DIV);
        StartNode.addTransfer('/', div_node);
        /*遇到连续两个斜线*/
        StateNode double_div = new StateNode(false, null);
        div_node.addTransfer('/', double_div);
        /*只要没遇到换行就一直在double_div这里循环*/
        for(char c=0; c<=(char)(65534);c++){
            if(c!='\n'){
                double_div.addTransfer(c, double_div);
            }
        }
        /*遇到换行，就结束注释*/
        StateNode comment_finish = new StateNode(true, TokenType.CMT);
        double_div.addTransfer('\n', comment_finish);
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
            String name = this.SavedWord.toString();
            Token result;
            switch(type){
                case CHAR_LITERAL:
                    this.SavedWord = new StringBuilder();
                    name = name.substring(1, name.length()-1);
                    name = name.replaceAll("\\\\\"", "\"").replaceAll("\\\\'", "\'").replaceAll("\\\\\\\\","\\\\");
                    name = name.replaceAll("\\\\n","\n").replaceAll("\\\\r","\r").replaceAll("\\\\t","\t");
                    char c = name.charAt(0);
                    result = new Token(TokenType.CHAR_LITERAL, (int)(c), this.StartPos);
                    break;
                case DOUBLE_LITERAL:
                    double double_value = Double.parseDouble(this.SavedWord.toString());
                    this.SavedWord = new StringBuilder();
                    result = new Token(TokenType.DOUBLE_LITERAL, double_value, this.StartPos);
                    break;
                case UINT_LITERAL://数字字面量
                    long value = Long.parseLong(this.SavedWord.toString());
                    this.SavedWord = new StringBuilder();
                    result = new Token(TokenType.UINT_LITERAL, value, this.StartPos);
                    break;
                case STRING_LITERAL:
                    //掐头去尾
                    //转移
                    this.SavedWord = new StringBuilder();
                    name = name.substring(1, name.length()-1);
                    name = name.replaceAll("\\\\\"", "\"").replaceAll("\\\\\'", "\'").replaceAll("\\\\\\\\","\\\\");
                    name = name.replaceAll("\\\\n","\n").replaceAll("\\\\r","\r").replaceAll("\\\\t","\t");
                    result = new Token(TokenType.STRING_LITERAL, name, this.StartPos);
                    break;
                case IDENT://广义标识符
                    this.SavedWord = new StringBuilder();
                    //System.out.println("Ident checking:"+this.SavedWord.toString());
                    result =  new Token(ReservedWords.getOrDefault(name, type), name, this.StartPos);
                    break;
                default://其他类型，基本上意味着是操作符
                    this.SavedWord = new StringBuilder();
                    result = new Token(type, name, this.StartPos);
                    break;
            }
            if(result.getType()==TokenType.CMT){
                //System.out.println("comment was found!");
                return getToken();
            }
            else{
                //System.out.println(result.getType());
                return result;
            }
        }
        /*有问题*/
        else{
            throw new UnknownTokenException();
        }
    }
}

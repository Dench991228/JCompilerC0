package jcompiler.analyzer;

import jcompiler.analyzer.exceptions.ReductionErrorException;
import jcompiler.tokenizer.Token;
import jcompiler.tokenizer.TokenType;
import jcompiler.util.Pos;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Set;

enum NonTerminalType{
    L_EXPR,
    EXPR,
    PARAM_LIST;
    @Override
    public String toString(){
        return this.name();
    }
}
class NonTerminal{
    /*非终结符的种类*/
    private NonTerminalType Type;
    /*句柄列表*/
    private LinkedList<Token> Tokens;

    public NonTerminal(){

    }

    public NonTerminal(NonTerminalType nt){
        this.Type = nt;
        this.Tokens = new LinkedList<>();
    }

    public NonTerminalType getType(){
        return this.Type;
    }

    /*添加一个终结符，在最后面*/
    public void addTerminal(Token token){
        this.Tokens.addFirst(token);
    }

    /*添加一个非终结符，在最前面*/
    public void addNonTerminal(NonTerminal nt){
        nt.Tokens.addAll(this.Tokens);
        this.Tokens = nt.Tokens;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.Type.toString()).append(":");
        for(Token t:Tokens){
            sb.append(t.getValue().toString());
        }
        return sb.toString();
    }
}
/*专门用来进行expr的分析，使用opg完成*/
public class ExprAnalyzer {
    /*基础设施*/
    private AnalyzerUtil Util;
    /*在栈中的终结符和非终结符，新放进来的在前面*/
    private LinkedList<Object> Stack;
    /*优先级矩阵*/
    private PriorityMatrix Matrix;

    /*二元运算符的集合*/
    private static Set<TokenType> BinaryOperands = new HashSet<>();
    static{//初始化过程：获得二元运算符
        Scanner sc = null;
        try {
            sc = new Scanner(new File("operands.txt"));
            while(sc.hasNextLine()){
                String input_line = sc.nextLine();
                String[] name_content = input_line.split("@");
                //System.out.println(name_content[1]+":"+TokenType.valueOf(name_content[0]));
                BinaryOperands.add(TokenType.valueOf(name_content[0]));
                //System.out.println(TokenType.valueOf(name_content[0]));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public ExprAnalyzer(AnalyzerUtil util) throws FileNotFoundException {
        this.Util = util;
        this.Stack = new LinkedList<>();
        this.Matrix =  new PriorityMatrix();
    }
    /*获得栈顶的终结符*/
    private Token getTopTerminal(){
        for(Object o:this.Stack){
            if(o instanceof Token)return (Token)o;
        }
        return null;
    }
    /*判断栈顶是不是token*/
    private boolean isTopToken(){
        return this.Stack.peekFirst() instanceof Token;
    }
    /*判断栈顶是不是特定的Token类型*/
    private boolean isTopTokenType(TokenType tt){
        if(!isTopToken())return false;
        else{
            Token t = (Token)this.Stack.peekFirst();
            if(t==null)return false;
            return t.getType() == tt;
        }
    }
    /*判断栈顶是不是非终结符*/
    private boolean isTopNonTerm(){
        return this.Stack.peekFirst() instanceof NonTerminal;
    }
    /*判断栈顶是不是特定的非终结符*/
    private boolean isTopNonTermType(NonTerminalType ntt){
        if(!isTopNonTerm())return false;
        else{
            NonTerminal t = (NonTerminal) this.Stack.peekFirst();
            if(t==null)return false;
            return t.getType() == ntt;
        }
    }

    /*尝试在栈顶规约出expr*/
    private void reduceExpr()throws ReductionErrorException {
        NonTerminal nt = new NonTerminal(NonTerminalType.EXPR);
        /*栈顶是终结符*/
        if(this.isTopToken()){
            Token t = (Token)this.Stack.pollFirst();
            switch (t.getType()){
                case IDENT://标识符
                case UINT_LITERAL://整数字面量
                case STRING_LITERAL://字面量
                    //System.out.println("A literal expression or a identifier expression was reduced!");
                    nt.addTerminal(t);
                    /*两个非终结符挨在一起，那就有问题*/
                    if(this.isTopNonTerm()){
                        //System.out.println(this.Stack.peekFirst());
                        throw new ReductionErrorException();
                    }
                    this.Stack.addFirst(nt);
                    System.out.println(nt.toString()+" was reduced");
                    return;
                case R_PAREN://函数调用或者括号表达式
                    /*把右括号加进去*/
                    nt.addTerminal(t);
                    //右括号出去之后，前面应该要么param_list要么expr，因为不是等号，所以肯定没有左值表达式
                    if(!this.isTopNonTerm())throw new ReductionErrorException();
                    nt.addNonTerminal((NonTerminal) this.Stack.pollFirst());
                    if(!this.isTopToken()||!this.isTopTokenType(TokenType.L_PAREN)){
                        //System.out.println(nt.toString());
                        throw new ReductionErrorException();
                    }
                    nt.addTerminal((Token) this.Stack.pollFirst());
                    /*瞅一眼前面有没有ident，如果有，就放进去*/
                    if(this.isTopToken()&&this.isTopTokenType(TokenType.IDENT)){
                        //System.out.println("A procedure call expression was reduced!");
                        nt.addTerminal((Token)this.Stack.pollFirst());
                    }
                    else{
                       // System.out.println("A group expression was reduced!");
                    }
                    if(this.isTopNonTerm())throw new ReductionErrorException();
                    System.out.println(nt.toString()+" was reduced");
                    this.Stack.addFirst(nt);
                    return;
                case TY://类型转换
                    //System.out.println("A type change expression was reduced!");
                    nt.addTerminal(t);
                    /*判断接下来是不是as关键字*/
                    if(!(this.Stack.peekFirst() instanceof Token)||(((Token) this.Stack.peekFirst()).getType())!=TokenType.AS_KW){
                        throw new ReductionErrorException();
                    }
                    /*把as关键字加进去*/
                    t = (Token)this.Stack.pollFirst();
                    nt.addTerminal(t);
                    /*判断接下来是不是expr*/
                    if(!this.isTopNonTerm()||this.isTopNonTermType(NonTerminalType.EXPR)||this.Stack.isEmpty())throw new ReductionErrorException();
                    nt.addNonTerminal((NonTerminal) this.Stack.pollFirst());
                    if(this.isTopNonTerm())throw new ReductionErrorException();
                    System.out.println(nt.toString()+" was reduced");
                    this.Stack.addFirst(nt);
                    return;

                default:
                    throw new ReductionErrorException();
            }
        }
        else{//栈顶是非终结符，需要考虑赋值表达式、运算符表达式、取反表达式
            LinkedList<Object> read = new LinkedList<>();
            /*顶上不是expr，就有问题*/
            if(!this.isTopNonTermType(NonTerminalType.EXPR))throw new ReductionErrorException();
            //System.out.println("reduceExpr: whether the terminal on the top is binary_operand:"+ BinaryOperands.contains(this.getTopTerminal().getType()));
            if(!this.isTopToken()&&this.getTopTerminal().getType()==TokenType.NEG){//取反表达式
                read.addLast(this.Stack.pollFirst());
                //System.out.println("A negative expression was reduced!");
                read.addLast(this.Stack.pollFirst());
            }
            else if(!this.isTopToken()&&this.getTopTerminal().getType()==TokenType.ASSIGN){//赋值表达式
                read.addLast(this.Stack.pollFirst());
                //System.out.println("An assignment expression was reduced!");
                read.addLast(this.Stack.pollFirst());
                if(!this.isTopNonTerm()||!this.isTopNonTermType(NonTerminalType.L_EXPR))throw new ReductionErrorException();
                read.addLast(this.Stack.pollFirst());
            }
            else if(!this.isTopToken()&&BinaryOperands.contains(this.getTopTerminal().getType())){//运算符表达式
                read.addLast(this.Stack.pollFirst());
                //System.out.println("A binary operand expression was reduced!");
                read.addLast(this.Stack.pollFirst());
                if(!this.isTopNonTerm()||!this.isTopNonTermType(NonTerminalType.EXPR))throw new ReductionErrorException();
                read.addLast(this.Stack.pollFirst());
            }
            else{//有问题
                throw new ReductionErrorException();
            }
            for(Object o:read){
                if(o instanceof Token)nt.addTerminal((Token)o);
                else nt.addNonTerminal((NonTerminal)o);
            }
            System.out.println(nt.toString()+" was reduced");
            this.Stack.addFirst(nt);
        }
    }
    /*尝试在栈顶规约出param_list，当且仅当顶端终结符是','，时进行这个规约*/
    /*参数列表形如param_list,expr，或者expr,expr*/
    private void reduceParamList(){
        NonTerminal nt = new NonTerminal(NonTerminalType.PARAM_LIST);
        /*把顶端的expr放进来*/
        if(!this.isTopNonTerm()||!this.isTopNonTermType(NonTerminalType.EXPR))throw new ReductionErrorException();
        nt.addNonTerminal((NonTerminal) this.Stack.pollFirst());
        /*放一个逗号进来*/
        if(!this.isTopToken()||!this.isTopTokenType(TokenType.COMMA))throw new ReductionErrorException();
        nt.addTerminal((Token) this.Stack.pollFirst());
        /*把最后的expr或者param_list放进来*/
        if(!this.isTopNonTerm()||!(this.getTopTerminal().getType()==TokenType.L_PAREN)){
            throw new ReductionErrorException();
        }
        nt.addNonTerminal((NonTerminal) this.Stack.pollFirst());
        this.Stack.addFirst(nt);
        System.out.println(nt.toString()+" was reduced");
    }
    /*尝试在栈顶规约出L_EXPR*/
    /*形如ident，ident一定是变量*/
    private void reduceLeftExpr(){
        NonTerminal nt = new NonTerminal(NonTerminalType.L_EXPR);
        if(!this.isTopToken()||!this.isTopTokenType(TokenType.IDENT))throw new ReductionErrorException();
        nt.addTerminal((Token)this.Stack.pollFirst());
        this.Stack.addFirst(nt);
        System.out.println(nt.toString()+" was reduced");
    }

    private void tryReduce(Token token) throws ReductionErrorException{
        if(token.getType()==TokenType.ASSIGN){//如果是等号，就尝试规约左值表达式
            System.out.println("a left value expression was reduced!");
            reduceLeftExpr();
        }
        else if(this.getTopTerminal().getType()==TokenType.COMMA){//如果顶端是逗号，那就尝试规约参数列表
            System.out.println("a parameter list was reduced!");
            reduceParamList();
        }
        else{
            reduceExpr();
        }
    }

    /*尝试把一个token放到栈中，判断优先级，看看是移进，还是规约*/
    private void putToken(Token token) throws ReductionErrorException{
        System.out.println("trying to shift in:"+token);
        char c = this.Matrix.compare(this.getTopTerminal().getType(), token.getType());
        if(c=='e')throw new ReductionErrorException();
        if(token.getType()==TokenType.SHARP&&this.getTopTerminal().getType()==TokenType.SHARP&&this.isTopNonTermType(NonTerminalType.EXPR)){
            return ;
        }
        while(c=='r'){
            tryReduce(token);
            c = this.Matrix.compare(this.getTopTerminal().getType(), token.getType());
            if(c=='e')throw new ReductionErrorException();
            if(token.getType()==TokenType.SHARP&&this.getTopTerminal().getType()==TokenType.SHARP&&this.isTopNonTermType(NonTerminalType.EXPR)){
                break ;
            }
        }
        System.out.println(token+" was shifted in");
        this.Stack.addFirst(token);
    }
    /*解析一个expr*/
    public void analyseExpr(){
        /*添加sharp符号在开始时*/
        this.Stack.addFirst(new Token(TokenType.SHARP, 0, new Pos(-1,-1)));
        while(this.Matrix.OperandTypes.contains(this.Util.peek().getType())){
            Token next = this.Util.next();
            /*什么情况下需要把减号变成负号：不是右括号,ident,不是LITERAL,不是type*/
            if(next.getType()==TokenType.MINUS&&this.isTopToken()&&!this.isTopTokenType(TokenType.UINT_LITERAL)&&!this.isTopTokenType(TokenType.IDENT)&&!this.isTopTokenType(TokenType.TY)&&!this.isTopTokenType(TokenType.STRING_LITERAL)&&!this.isTopTokenType(TokenType.R_PAREN)){
                next.setType(TokenType.NEG);
            }
            this.putToken(next);
        }
        this.putToken(new Token(TokenType.SHARP, 0, new Pos(-1,-1)));
    }
}

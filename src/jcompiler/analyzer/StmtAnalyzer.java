package jcompiler.analyzer;

import jcompiler.action.Instruction;
import jcompiler.analyzer.exceptions.ErrorTokenTypeException;
import jcompiler.analyzer.exceptions.LoopControlException;
import jcompiler.analyzer.exceptions.ReturnTypeError;
import jcompiler.analyzer.exceptions.StmtSyntaxException;
import jcompiler.tokenizer.Token;
import jcompiler.tokenizer.TokenType;
import jcompiler.util.BinaryHelper;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

/*
* 语法分析器，使用OPG完成expr的分析，先完成expr部分
* */
public class StmtAnalyzer {
    /*使用的基础设施*/
    private AnalyzerUtil Util;

    /*使用的opg analyzer*/
    private ExprAnalyzer ExprAnalyzer;

    /*是不是在第一个if语句中*/
    private boolean FirstIf;

    /*无参数创建analyzer*/
    public StmtAnalyzer(){
        super();
    }

    /*根据输入文件创建新的tokenizer*/
    public StmtAnalyzer(AnalyzerUtil util) throws FileNotFoundException {
        this.Util = util;
        this.ExprAnalyzer = new ExprAnalyzer(util);
    }

    /*解析运算式语句*/
    private void analyseExpression(){
        this.ExprAnalyzer.analyseExpr(null);
        this.Util.expect(TokenType.SEMICOLON);
    }

    /*解析声明语句*/
    private void analyseDeclStmt(){
        Token first = this.Util.peek();
        Token variable_ident;
        Token variable_type;
        switch (first.getType()){
            case LET_KW:
                this.Util.next();
                variable_ident = this.Util.next(TokenType.IDENT);
                this.Util.expect(TokenType.COLON);
                variable_type = this.Util.next(TokenType.TY);
                if(variable_type.getValue().toString().compareTo("double")==0)variable_type=Token.DOUBLE;
                else if(variable_type.getValue().toString().compareTo("int")==0)variable_type=Token.INTEGER;
                if(variable_type.getValue().toString().compareTo("void")==0)throw new ErrorTokenTypeException();
                boolean isInitialized = false;
                SymbolEntry entry = SymbolEntry.getVariableEntry(variable_type, false, variable_ident.getStartPos());
                Analyzer.AnalyzerTable.putIdent(variable_ident, entry);
                if(this.Util.nextIf(TokenType.ASSIGN)!=null){//需要考虑赋值
                    Analyzer.AnalyzerTable.moveAddressStackTop(variable_ident);//把目标全局变量挪到顶上
                    isInitialized = true;
                    this.ExprAnalyzer.setExpectedType(variable_type);
                    this.ExprAnalyzer.analyseExpr(variable_type);
                    Instruction ins = Instruction.getInstruction("store.64");
                    Analyzer.CurrentFunction.addInstruction(ins);
                }
                this.Util.expect(TokenType.SEMICOLON);
                if(isInitialized)Analyzer.AnalyzerTable.setInitialized(variable_ident);
                break;
            case CONST_KW:
                this.Util.next();
                variable_ident = this.Util.next(TokenType.IDENT);
                this.Util.expect(TokenType.COLON);
                variable_type = this.Util.next(TokenType.TY);
                if(variable_type.getValue().toString().compareTo("void")==0)throw new ErrorTokenTypeException();
                if(variable_type.getValue().toString().compareTo("double")==0)variable_type=Token.DOUBLE;
                else if(variable_type.getValue().toString().compareTo("int")==0)variable_type=Token.INTEGER;
                this.Util.expect(TokenType.ASSIGN);
                this.ExprAnalyzer.setExpectedType(variable_type);
                SymbolEntry const_entry = SymbolEntry.getVariableEntry(variable_type, true, variable_ident.getStartPos());
                Analyzer.AnalyzerTable.putIdent(variable_ident, const_entry);
                Analyzer.AnalyzerTable.moveAddressStackTop(variable_ident);
                this.ExprAnalyzer.analyseExpr(variable_type);
                this.Util.expect(TokenType.SEMICOLON);
                Instruction ins = Instruction.getInstruction("store.64");
                Analyzer.CurrentFunction.addInstruction(ins);
                Analyzer.AnalyzerTable.setInitialized(variable_ident);
                break;
            default:
                throw new StmtSyntaxException();
        }
    }

    /*解析if语句*/
    /*进入if语句的时候，在返回状态栈里面放入一个新状态，表明这一系列if-else语句的返回状态*/

    private void analyseIfStmt(){
        boolean has_branch_no_return = false;//有没有分支没返回
        boolean has_else_clause = false;//有没有else语句
        Analyzer.ReturnState.addLast(false);
        Analyzer.putLoopState(false);
        LinkedList<Instruction> block_end_instruction = new LinkedList<>();//用来保存块最后的跳转
        Instruction former_judge;//用来保存上一个语句的条件跳转
        this.Util.expect(TokenType.IF_KW);//第一个if语句
        this.ExprAnalyzer.analyseExpr(Token.INTEGER);
        //获取这里的语句，保存下来，如果有问题就到下一个子句
        former_judge = Instruction.getInstruction("br.false");
        Analyzer.CurrentFunction.addInstruction(former_judge);

        this.FirstIf=true;
        this.analyseBlockStmt();
        //if块里面的东西没了，记得下来，最后统一跳转到最后的话
        Instruction end_block = Instruction.getInstruction("br");
        Analyzer.CurrentFunction.addInstruction(end_block);
        block_end_instruction.add(end_block);
        this.FirstIf=false;
        boolean last_state = Analyzer.ReturnState.pollLast();
        if(!last_state)has_branch_no_return = true;
        while(this.Util.peek().getType()==TokenType.ELSE_KW){//各种各样的else-if和else
            this.Util.next();
            if(this.Util.peek().getType()==TokenType.L_BRACE){//左大括号else 字句，不会有elif了
                former_judge.setOperand(BinaryHelper.BinaryInteger(Analyzer.CurrentFunction.getOffset(former_judge)-1));//上一句不对，就到这
                Analyzer.ReturnState.addLast(false);
                this.analyseBlockStmt();
                last_state = Analyzer.ReturnState.pollLast();
                if(!last_state)has_branch_no_return = true;
                has_else_clause = true;
                break;
            }
            else{
                this.Util.expect(TokenType.IF_KW);
                former_judge.setOperand(BinaryHelper.BinaryInteger(Analyzer.CurrentFunction.getOffset(former_judge)-1));
                this.ExprAnalyzer.analyseExpr(Token.INTEGER);
                former_judge = Instruction.getInstruction("br.false");//更新条件跳转语句
                Analyzer.CurrentFunction.addInstruction(former_judge);
                Analyzer.ReturnState.addLast(false);
                this.analyseBlockStmt();
                end_block = Instruction.getInstruction("br");
                block_end_instruction.add(end_block);
                Analyzer.CurrentFunction.addInstruction(end_block);
                last_state = Analyzer.ReturnState.pollLast();
                if(!last_state)has_branch_no_return = true;
            }
        }
        //最后的语句，在加入到函数之前，先设置好前面的块结束，无条件跳转
        for(Instruction ins:block_end_instruction){
            ins.setOperand(BinaryHelper.BinaryInteger(Analyzer.CurrentFunction.getOffset(ins)));
        }
        if(!has_else_clause){//没有else语句，把最后一个子句的跳转拿过来设置一下
            System.out.println("\n\nhahaha\n");
            former_judge.setOperand(BinaryHelper.BinaryInteger(Analyzer.CurrentFunction.getOffset(former_judge)));
        }
        //用来被人跳转
        Instruction end_if = Instruction.getInstruction("nop");
        Analyzer.CurrentFunction.addInstruction(end_if);
        if(!has_branch_no_return&&has_else_clause){
            Analyzer.ReturnState.pollLast();
            Analyzer.ReturnState.addLast(true);
        }
        Analyzer.LoopState.pollLast();
    }

    /*解析while语句*/
    private void analyseWhileStmt(){
        this.Util.expect(TokenType.WHILE_KW);
        /*这里添加一条nop语句，while块结束后，就跳转到这里*/
        Instruction nop_fore = Instruction.getInstruction("nop");
        Analyzer.CurrentFunction.addInstruction(nop_fore);

        /*更新Analyzer中相应的while语句开始记录*/
        Analyzer.StartOfWhile.add(nop_fore);
        /*更新Analyzer中保存break语句的地方*/
        Analyzer.BreakStatement.add(new LinkedList<Instruction>());

        /*while的条件解析*/
        this.ExprAnalyzer.analyseExpr(Token.INTEGER);
        //之前的算式解析完之后，末尾是一个条件跳转，用来跳到nop_back的，但是没有操作数，先保存一下，后面设置好
        Instruction conditional_jump = Instruction.getInstruction("br.false");
        Analyzer.CurrentFunction.addInstruction(conditional_jump);

        Analyzer.putLoopState(true);
        Analyzer.putReturnState();
        Analyzer.addSymbolTable();//加上一层符号表
        this.analyseBlockStmt();//解析语句块
        /*语句块编译完了，这里我得加一个无条件跳转到前面的nop_fore，进行下一轮循环*/
        Instruction unconditional_jump = Instruction.getInstruction("br", -Analyzer.CurrentFunction.getOffset(nop_fore));
        Analyzer.CurrentFunction.addInstruction(unconditional_jump);

        int offset = Analyzer.CurrentFunction.getOffset(conditional_jump);
        conditional_jump.setOperand(BinaryHelper.BinaryInteger(offset-1));//正着跳，需要减一
        //无条件跳转完了，接下来是跳过while的语句，如果前面的条件跳转GG了，就跳转到这里，这里也有一个nop
        //更新break本层循环的break语句
        for(Instruction break_statement:Analyzer.BreakStatement.peekLast()){
            break_statement.setOperand(BinaryHelper.BinaryInteger(Analyzer.CurrentFunction.getOffset(break_statement)));
        }
        Instruction nop_back = Instruction.getInstruction("nop");
        Analyzer.CurrentFunction.addInstruction(nop_back);
        //之后的东西
        Analyzer.withdraw();
        Analyzer.ReturnState.pollLast();
        Analyzer.LoopState.pollLast();
        /*更新Analyzer中保存的东西*/
        Analyzer.BreakStatement.pollLast();
        Analyzer.StartOfWhile.pollLast();
    }

    /*解析返回语句*/
    private void analyseReturnStmt(){
        this.Util.expect(TokenType.RETURN_KW);
        Analyzer.ReturnState.addLast(true);
        if(Analyzer.ExpectedReturnType.getValue().toString().compareTo("void")!=0){//不是void，那就必须有东西
            Instruction ins = Instruction.getInstruction("arga",0);
            Analyzer.CurrentFunction.addInstruction(ins);
            if(this.Util.peek().getType()==TokenType.SEMICOLON)throw new ReturnTypeError();
            this.ExprAnalyzer.setExpectedType(Analyzer.ExpectedReturnType);
            this.ExprAnalyzer.analyseExpr(Analyzer.ExpectedReturnType);
            ins = Instruction.getInstruction("store.64");
            Analyzer.CurrentFunction.addInstruction(ins);
        }
        this.Util.expect(TokenType.SEMICOLON);
        Analyzer.CurrentFunction.addInstruction(Instruction.getInstruction("ret"));
    }

    /*解析break语句*/
    private void analyseBreakStmt(){
        this.Util.expect(TokenType.BREAK_KW);
        this.Util.expect(TokenType.SEMICOLON);
        Instruction ins = Instruction.getInstruction("br");
        Analyzer.CurrentFunction.addInstruction(ins);
        Analyzer.BreakStatement.peekLast().add(ins);
    }

    /*解析continue语句*/
    private void analyseContinueStmt(){
        this.Util.expect(TokenType.CONTINUE_KW);
        this.Util.expect(TokenType.SEMICOLON);
        Instruction ins = Instruction.getInstruction("br", -Analyzer.CurrentFunction.getOffset(Analyzer.StartOfWhile.peekLast()));
        Analyzer.CurrentFunction.addInstruction(ins);
    }

    /*解析空语句*/
    private void analyseEmptyStmt() {
        this.Util.expect(TokenType.SEMICOLON);
    }

    /*解析语句块*/
    public void analyseBlockStmt(){
        this.Util.expect(TokenType.L_BRACE);
        while(this.Util.peek().getType()!=TokenType.R_BRACE){
            this.analyseStatement();
        }
        this.Util.expect(TokenType.R_BRACE);
    }

    /*对外服务，语句分析*/
    public void analyseStatement(){
        Token t = this.Util.peek();
        switch(t.getType()){
            case SEMICOLON://空语句
                this.analyseEmptyStmt();
                break;
            case L_BRACE://语句块
                Analyzer.putLoopState(false);
                Analyzer.addSymbolTable();
                this.analyseBlockStmt();
                Analyzer.withdraw();
                Analyzer.LoopState.pollLast();
                break;
            case WHILE_KW://while语句
                this.analyseWhileStmt();
                break;
            case IF_KW://if语句
                this.analyseIfStmt();
                break;
            case LET_KW://这两个都是声明
            case CONST_KW:
                this.analyseDeclStmt();
                break;
            case RETURN_KW://返回语句
                this.analyseReturnStmt();
                break;
            case BREAK_KW:
                if(Analyzer.LoopState.peekLast())this.analyseBreakStmt();
                else throw new LoopControlException();
                break;
            case CONTINUE_KW:
                if(Analyzer.LoopState.peekLast())this.analyseContinueStmt();
                else throw new LoopControlException();
                break;
            default://运算式
                this.analyseExpression();
                break;
        }
    }
}

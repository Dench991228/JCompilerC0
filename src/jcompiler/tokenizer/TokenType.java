package jcompiler.tokenizer;

public enum TokenType {

    /* 关键字部分 */
    /** fn */
    FN_KW,//fn
    LET_KW,//let
    CONST_KW,//const
    AS_KW,//as
    WHILE_KW,//while
    IF_KW,//if
    ELSE_KW,//else
    RETURN_KW,//return
    CONTINUE_KW,
    BREAK_KW,

    /*评论：默认是一个单词*/
    CMT,

    /* 字面量部分 */
    UINT_LITERAL,//无符号整数
    STRING_LITERAL,//字符串
    CHAR_LITERAL,
    DOUBLE_LITERAL,

    /* 标识符 */
    IDENT,

    /* 运算符 */
    NEG,
    PLUS,
    MINUS,
    MUL,
    DIV,
    ASSIGN,
    EQ,
    NEQ,
    LT,
    GT,
    LE,
    GE,
    L_PAREN,
    R_PAREN,
    L_BRACE,
    R_BRACE,
    ARROW,
    COMMA,
    COLON,
    SEMICOLON,
    SHARP,
    /* 文件结尾 */
    EOF,
    /*类型系统*/
    TY;
    @Override
    public String toString(){
        return this.name();
    }
}

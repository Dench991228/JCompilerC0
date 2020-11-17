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

    /* 字面量部分 */
    UINT_LITERAL,//无符号整数
    STRING_LITERAL,//字符串

    /* 标识符 */
    IDENT,

    /* 运算符 */
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
    /* 文件结尾 */
    EOF;
    @Override
    public String toString(){
        return this.name();
    }
}

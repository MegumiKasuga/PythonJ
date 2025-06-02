package edu.carole.lexer;

/**
 * 表示Python源代码中的一个token
 */
public class Token {    public enum Type {
        // 字面量
        NUMBER, STRING, RAW_STRING, F_STRING, TRIPLE_STRING, TRIPLE_RAW_STRING, TRIPLE_F_STRING, BOOLEAN, NONE,// 标识符和关键字
        IDENTIFIER, 
        IF, ELIF, ELSE, WHILE, FOR, IN, DEF, CLASS, RETURN, BREAK, CONTINUE,
        AND, OR, NOT, IS, IMPORT, FROM, AS, PASS, TRY, EXCEPT, FINALLY,
        LAMBDA, YIELD, WITH, SUPER,
        TRUE, FALSE,
          // 操作符
        PLUS, MINUS, MULTIPLY, DIVIDE, MODULO, POWER, FLOOR_DIVIDE,
        EQUAL, NOT_EQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL,
        ASSIGN, PLUS_ASSIGN, MINUS_ASSIGN, MULTIPLY_ASSIGN, DIVIDE_ASSIGN, 
        MODULO_ASSIGN, POWER_ASSIGN, FLOOR_DIVIDE_ASSIGN,
        BITWISE_AND, BITWISE_OR, BITWISE_XOR, LEFT_SHIFT, RIGHT_SHIFT,
        AND_ASSIGN, OR_ASSIGN, XOR_ASSIGN, LEFT_SHIFT_ASSIGN, RIGHT_SHIFT_ASSIGN,// 分隔符
        LEFT_PAREN, RIGHT_PAREN, LEFT_BRACKET, RIGHT_BRACKET, LEFT_BRACE, RIGHT_BRACE,
        COMMA, DOT, COLON, SEMICOLON, AT,
        
        // 特殊
        NEWLINE, INDENT, DEDENT, EOF
    }
    
    private final Type type;
    private final String value;
    private final int line;
    private final int column;
    
    public Token(Type type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }
    
    public Type getType() { return type; }
    public String getValue() { return value; }
    public int getLine() { return line; }
    public int getColumn() { return column; }
    
    @Override
    public String toString() {
        return String.format("Token{type=%s, value='%s', line=%d, col=%d}", 
                           type, value, line, column);
    }
}

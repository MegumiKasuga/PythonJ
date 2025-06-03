package edu.carole.lexer;

import java.util.*;

/**
 * Python词法分析器
 * 将Python源代码转换为token流
 */
public class Lexer {
    private final String source;
    private int current = 0;
    private int line = 1;
    private int column = 1;
    private final List<Token> tokens = new ArrayList<>();
    private final Stack<Integer> indentStack = new Stack<>();
      private static final Map<String, Token.Type> KEYWORDS;
    static {
        Map<String, Token.Type> map = new HashMap<>();
        map.put("if", Token.Type.IF);
        map.put("elif", Token.Type.ELIF);
        map.put("else", Token.Type.ELSE);
        map.put("while", Token.Type.WHILE);
        map.put("for", Token.Type.FOR);
        map.put("in", Token.Type.IN);
        map.put("def", Token.Type.DEF);
        map.put("class", Token.Type.CLASS);
        map.put("return", Token.Type.RETURN);
        map.put("break", Token.Type.BREAK);
        map.put("continue", Token.Type.CONTINUE);
        map.put("and", Token.Type.AND);
        map.put("or", Token.Type.OR);
        map.put("not", Token.Type.NOT);
        map.put("is", Token.Type.IS);
        map.put("as", Token.Type.AS);
        map.put("True", Token.Type.TRUE);
        map.put("False", Token.Type.FALSE);
        map.put("None", Token.Type.NONE);
        map.put("pass", Token.Type.PASS);
        map.put("try", Token.Type.TRY);
        map.put("except", Token.Type.EXCEPT);
        map.put("finally", Token.Type.FINALLY);
        map.put("lambda", Token.Type.LAMBDA);
        map.put("yield", Token.Type.YIELD);
        map.put("with", Token.Type.WITH);
        map.put("super", Token.Type.SUPER);
        map.put("global", Token.Type.GLOBAL);
        map.put("nonlocal", Token.Type.NONLOCAL);
        KEYWORDS = Collections.unmodifiableMap(map);
    }
    
    public Lexer(String source) {
        this.source = source;
        indentStack.push(0); // 初始缩进级别
    }
    
    public List<Token> tokenize() {
        while (!isAtEnd()) {
            tokenizeNext();
        }
        
        // 处理文件结尾的dedent
        while (indentStack.size() > 1) {
            indentStack.pop();
            addToken(Token.Type.DEDENT, "");
        }
        
        addToken(Token.Type.EOF, "");
        return tokens;
    }
    
    private void tokenizeNext() {
        char c = advance();
        
        switch (c) {
            case ' ': case '\r': case '\t':
                // 忽略空白字符（但不是换行符）
                break;
            case '\n':
                addToken(Token.Type.NEWLINE, "\n");
                line++;
                column = 1;
                handleIndentation();
                break;
            case '#':
                // 注释，跳过到行末
                while (peek() != '\n' && !isAtEnd()) advance();
                break;
            case '(':
                addToken(Token.Type.LEFT_PAREN, "(");
                break;
            case ')':
                addToken(Token.Type.RIGHT_PAREN, ")");
                break;
            case '[':
                addToken(Token.Type.LEFT_BRACKET, "[");
                break;
            case ']':
                addToken(Token.Type.RIGHT_BRACKET, "]");
                break;
            case '{':
                addToken(Token.Type.LEFT_BRACE, "{");
                break;            case '}':
                addToken(Token.Type.RIGHT_BRACE, "}");
                break;
            case ',':
                addToken(Token.Type.COMMA, ",");
                break;
            case '@':
                addToken(Token.Type.AT, "@");
                break;
            case '.':
                addToken(Token.Type.DOT, ".");
                break;
            case ':':
                addToken(Token.Type.COLON, ":");
                break;
            case ';':
                addToken(Token.Type.SEMICOLON, ";");
                break;
            case '+':
                if (match('=')) {
                    addToken(Token.Type.PLUS_ASSIGN, "+=");
                } else {
                    addToken(Token.Type.PLUS, "+");
                }
                break;            case '-':
                if (match('=')) {
                    addToken(Token.Type.MINUS_ASSIGN, "-=");
                } else if (match('>')) {
                    addToken(Token.Type.ARROW, "->");
                } else {
                    addToken(Token.Type.MINUS, "-");
                }
                break;case '*':
                if (match('*')) {
                    if (match('=')) {
                        addToken(Token.Type.POWER_ASSIGN, "**=");
                    } else {
                        addToken(Token.Type.POWER, "**");
                    }
                } else if (match('=')) {
                    addToken(Token.Type.MULTIPLY_ASSIGN, "*=");
                } else {
                    addToken(Token.Type.MULTIPLY, "*");
                }
                break;
            case '/':
                if (match('/')) {
                    if (match('=')) {
                        addToken(Token.Type.FLOOR_DIVIDE_ASSIGN, "//=");
                    } else {
                        addToken(Token.Type.FLOOR_DIVIDE, "//");
                    }
                } else if (match('=')) {
                    addToken(Token.Type.DIVIDE_ASSIGN, "/=");
                } else {
                    addToken(Token.Type.DIVIDE, "/");
                }
                break;
            case '%':
                if (match('=')) {
                    addToken(Token.Type.MODULO_ASSIGN, "%=");
                } else {
                    addToken(Token.Type.MODULO, "%");
                }
                break;
            case '&':
                if (match('=')) {
                    addToken(Token.Type.AND_ASSIGN, "&=");
                } else {
                    addToken(Token.Type.BITWISE_AND, "&");
                }
                break;
            case '|':
                if (match('=')) {
                    addToken(Token.Type.OR_ASSIGN, "|=");
                } else {
                    addToken(Token.Type.BITWISE_OR, "|");
                }
                break;
            case '^':
                if (match('=')) {
                    addToken(Token.Type.XOR_ASSIGN, "^=");
                } else {
                    addToken(Token.Type.BITWISE_XOR, "^");
                }
                break;
            case '<':
                if (match('<')) {
                    if (match('=')) {
                        addToken(Token.Type.LEFT_SHIFT_ASSIGN, "<<=");
                    } else {
                        addToken(Token.Type.LEFT_SHIFT, "<<");
                    }
                } else if (match('=')) {
                    addToken(Token.Type.LESS_EQUAL, "<=");
                } else {
                    addToken(Token.Type.LESS, "<");
                }
                break;
            case '>':
                if (match('>')) {
                    if (match('=')) {
                        addToken(Token.Type.RIGHT_SHIFT_ASSIGN, ">>=");
                    } else {
                        addToken(Token.Type.RIGHT_SHIFT, ">>");
                    }
                } else if (match('=')) {
                    addToken(Token.Type.GREATER_EQUAL, ">=");
                } else {
                    addToken(Token.Type.GREATER, ">");
                }
                break;
            case '=':
                if (match('=')) {
                    addToken(Token.Type.EQUAL, "==");
                } else {
                    addToken(Token.Type.ASSIGN, "=");
                }
                break;
            case '!':
                if (match('=')) {
                    addToken(Token.Type.NOT_EQUAL, "!=");                }
                break;case '"':
            case '\'':
                // Check for triple quotes
                if (peek() == c && peekNext() == c) {
                    string(c, false, false, true);
                } else {
                    string(c, false, false, false);
                }
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    throw new RuntimeException("Unexpected character: " + c + " at line " + line);
                }
                break;
        }
    }
    
    private void handleIndentation() {
        if (isAtEnd()) return;
        
        int spaces = 0;
        while (peek() == ' ') {
            spaces++;
            advance();
        }
        
        // 如果这行是空行或注释，忽略缩进
        if (peek() == '\n' || peek() == '#') return;
        
        int currentIndent = indentStack.peek();
        
        if (spaces > currentIndent) {
            indentStack.push(spaces);
            addToken(Token.Type.INDENT, "");
        } else if (spaces < currentIndent) {
            while (!indentStack.isEmpty() && indentStack.peek() > spaces) {
                indentStack.pop();
                addToken(Token.Type.DEDENT, "");
            }
            if (indentStack.isEmpty() || indentStack.peek() != spaces) {
                throw new RuntimeException("Indentation error at line " + line);
            }
        }
    }
      private void string(char quote, boolean isRaw, boolean isFormatted, boolean isTriple) {
        StringBuilder value = new StringBuilder();
        
        if (isTriple) {
            // Handle triple quotes
            if (!match(quote) || !match(quote)) {
                throw new RuntimeException("Invalid triple quote at line " + line);
            }
            
            // For triple quotes, we need to read until we find the closing triple quote
            while (!isAtEnd()) {
                if (peek() == quote && peekNext() == quote && current + 2 < source.length() && source.charAt(current + 2) == quote) {
                    // Found closing triple quote
                    advance(); // first quote
                    advance(); // second quote  
                    advance(); // third quote
                    break;
                }
                
                if (peek() == '\n') {
                    line++;
                    column = 1;
                }
                
                if (!isRaw && peek() == '\\') {
                    advance(); // Skip backslash
                    char escaped = advance();
                    switch (escaped) {
                        case 'n': value.append('\n'); break;
                        case 't': value.append('\t'); break;
                        case 'r': value.append('\r'); break;
                        case '\\': value.append('\\'); break;
                        case '\'': value.append('\''); break;
                        case '"': value.append('"'); break;
                        default: value.append(escaped); break;
                    }
                } else {
                    value.append(advance());
                }
            }
        } else {
            // Handle single/double quotes
            while (peek() != quote && !isAtEnd()) {
                if (peek() == '\n') {
                    throw new RuntimeException("Unterminated string at line " + line);
                }
                
                if (!isRaw && peek() == '\\') {
                    advance(); // Skip backslash
                    char escaped = advance();
                    switch (escaped) {
                        case 'n': value.append('\n'); break;
                        case 't': value.append('\t'); break;
                        case 'r': value.append('\r'); break;
                        case '\\': value.append('\\'); break;
                        case '\'': value.append('\''); break;
                        case '"': value.append('"'); break;
                        default: value.append(escaped); break;
                    }
                } else {
                    value.append(advance());
                }
            }
            
            if (isAtEnd()) {
                throw new RuntimeException("Unterminated string at line " + line);
            }
            
            advance(); // Skip closing quote
        }
        
        // Determine token type based on string properties
        Token.Type tokenType;
        if (isTriple) {
            if (isRaw && isFormatted) {
                tokenType = Token.Type.TRIPLE_F_STRING; // rf/fr triple
            } else if (isRaw) {
                tokenType = Token.Type.TRIPLE_RAW_STRING;
            } else if (isFormatted) {
                tokenType = Token.Type.TRIPLE_F_STRING;
            } else {
                tokenType = Token.Type.TRIPLE_STRING;
            }
        } else {
            if (isRaw && isFormatted) {
                tokenType = Token.Type.F_STRING; // rf/fr single
            } else if (isRaw) {
                tokenType = Token.Type.RAW_STRING;
            } else if (isFormatted) {
                tokenType = Token.Type.F_STRING;
            } else {
                tokenType = Token.Type.STRING;
            }
        }
        
        addToken(tokenType, value.toString());
    }
    
    private void number() {
        StringBuilder value = new StringBuilder();
        value.append(source.charAt(current - 1)); // 添加第一个数字
        
        while (isDigit(peek())) {
            value.append(advance());
        }
        
        // 处理小数点
        if (peek() == '.' && isDigit(peekNext())) {
            value.append(advance()); // 添加小数点
            while (isDigit(peek())) {
                value.append(advance());
            }
        }
        
        addToken(Token.Type.NUMBER, value.toString());
    }
      private void identifier() {
        StringBuilder value = new StringBuilder();
        value.append(source.charAt(current - 1)); // 添加第一个字符
        
        while (isAlphaNumeric(peek())) {
            value.append(advance());
        }
        
        String text = value.toString();
        
        // Check for string prefixes
        if (checkStringPrefix(text)) {
            return; // String prefix was handled
        }
        
        Token.Type type = KEYWORDS.getOrDefault(text, Token.Type.IDENTIFIER);
        addToken(type, text);
    }
    
    private boolean checkStringPrefix(String prefix) {
        // Check if this identifier is followed by a quote (indicating a string literal)
        if (peek() != '"' && peek() != '\'') {
            return false;
        }
        
        char quote = peek();
        boolean isRaw = false;
        boolean isFormatted = false;
        boolean isTriple = false;
        
        // Parse prefix flags
        String lowerPrefix = prefix.toLowerCase();
        if (lowerPrefix.equals("r")) {
            isRaw = true;
        } else if (lowerPrefix.equals("f")) {
            isFormatted = true;
        } else if (lowerPrefix.equals("rf") || lowerPrefix.equals("fr")) {
            isRaw = true;
            isFormatted = true;
        } else {
            return false; // Not a string prefix
        }
        
        advance(); // consume the quote
        
        // Check for triple quotes
        if (peek() == quote && peekNext() == quote) {
            isTriple = true;
        }
        
        string(quote, isRaw, isFormatted, isTriple);
        return true;
    }
    
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        
        current++;
        column++;
        return true;
    }
    
    private char advance() {
        column++;
        return source.charAt(current++);
    }
    
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }
    
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }
    
    private boolean isAtEnd() {
        return current >= source.length();
    }
    
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || 
               (c >= 'A' && c <= 'Z') || 
               c == '_';
    }
    
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
    
    private void addToken(Token.Type type, String value) {
        tokens.add(new Token(type, value, line, column - value.length()));
    }
}

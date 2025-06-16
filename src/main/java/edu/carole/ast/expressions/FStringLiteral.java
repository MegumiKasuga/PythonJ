package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

/**
 * F-String literal for formatted string expressions
 */
public class FStringLiteral extends ASTNode {
    private final String value;
    private final boolean isRaw;
    private final boolean isTriple;
    private final int line, column;

    @Getter
    private final String file;
    
    public FStringLiteral(String file, String value, int line, int column) {
        this(file, value, false, false, line, column);
    }
    
    public FStringLiteral(String file, String value, boolean isRaw, boolean isTriple,
                          int line, int column) {
        this.value = value;
        this.isRaw = isRaw;
        this.isTriple = isTriple;
        this.line = line;
        this.column = column;
        this.file = file;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getColumn() {
        return column;
    }

    public String getValue() {
        return value;
    }
    
    public boolean isRaw() {
        return isRaw;
    }
    
    public boolean isTriple() {
        return isTriple;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitFStringLiteral(this);
    }
}

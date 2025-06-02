package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * F-String literal for formatted string expressions
 */
public class FStringLiteral extends ASTNode {
    private final String value;
    private final boolean isRaw;
    private final boolean isTriple;
    
    public FStringLiteral(String value) {
        this(value, false, false);
    }
    
    public FStringLiteral(String value, boolean isRaw, boolean isTriple) {
        this.value = value;
        this.isRaw = isRaw;
        this.isTriple = isTriple;
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

package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * 字面量模式 - 匹配特定的字面量值
 */
public class LiteralPattern extends ASTNode {
    private final ASTNode value;
    
    public LiteralPattern(ASTNode value) {
        this.value = value;
    }
    
    public ASTNode getValue() { return value; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitLiteralPattern(this);
    }
}

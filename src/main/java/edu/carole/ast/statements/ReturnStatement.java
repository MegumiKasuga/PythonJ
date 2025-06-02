package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * return语句
 */
public class ReturnStatement extends ASTNode {
    private final ASTNode value;
    
    public ReturnStatement(ASTNode value) {
        this.value = value;
    }
    
    public ASTNode getValue() { return value; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitReturnStatement(this);
    }
}

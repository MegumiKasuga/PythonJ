package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * Represents a starred expression (*args) in function calls
 */
public class StarredExpression extends ASTNode {
    private final ASTNode expression;
    
    public StarredExpression(ASTNode expression) {
        this.expression = expression;
    }
    
    public ASTNode getExpression() {
        return expression;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitStarredExpression(this);
    }
    
    @Override
    public String toString() {
        return "*" + expression.toString();
    }
}

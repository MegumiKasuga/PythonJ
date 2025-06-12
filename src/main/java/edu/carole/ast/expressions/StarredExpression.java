package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * Represents a starred expression (*args) in function calls
 */
public class StarredExpression extends ASTNode {
    private final ASTNode expression;
    private final int line, column;
    
    public StarredExpression(ASTNode expression, int line, int column) {
        this.expression = expression;
        this.line = line;
        this.column = column;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getColumn() {
        return column;
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

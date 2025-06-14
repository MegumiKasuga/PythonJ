package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * 表达式语句
 */
public class ExpressionStatement extends ASTNode {
    private final ASTNode expression;
    private final int line, column;
    
    public ExpressionStatement(ASTNode expression, int line, int column) {
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
        return visitor.visitExpressionStatement(this);
    }
}

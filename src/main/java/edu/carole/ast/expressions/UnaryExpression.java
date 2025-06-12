package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * 一元表达式
 */
public class UnaryExpression extends ASTNode {
    public enum Operator {
        MINUS, NOT
    }
    
    private final Operator operator;
    private final ASTNode operand;
    private final int line, column;
    
    public UnaryExpression(Operator operator, ASTNode operand, int line, int column) {
        this.operator = operator;
        this.operand = operand;
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

    public Operator getOperator() { return operator; }
    public ASTNode getOperand() { return operand; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitUnaryExpression(this);
    }
}

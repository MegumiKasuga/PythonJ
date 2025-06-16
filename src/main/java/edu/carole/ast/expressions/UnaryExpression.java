package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

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

    @Getter
    private final String file;
    
    public UnaryExpression(String file, Operator operator, ASTNode operand, int line, int column) {
        this.operator = operator;
        this.operand = operand;
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

    public Operator getOperator() { return operator; }
    public ASTNode getOperand() { return operand; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitUnaryExpression(this);
    }
}

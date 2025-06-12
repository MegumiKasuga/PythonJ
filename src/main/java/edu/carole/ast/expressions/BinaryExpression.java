package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * 二元表达式
 */
public class BinaryExpression extends ASTNode {    public enum Operator {
        PLUS, MINUS, MULTIPLY, DIVIDE, MODULO, POWER, FLOOR_DIVIDE,
        EQUAL, NOT_EQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL,
        AND, OR, IS, IN,
        BITWISE_AND, BITWISE_OR, BITWISE_XOR, LEFT_SHIFT, RIGHT_SHIFT
    }
    
    private final ASTNode left;
    private final Operator operator;
    private final ASTNode right;
    private final int line, column;
    
    public BinaryExpression(ASTNode left, Operator operator, ASTNode right, int line, int column) {
        this.left = left;
        this.operator = operator;
        this.right = right;
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

    public ASTNode getLeft() { return left; }
    public Operator getOperator() { return operator; }
    public ASTNode getRight() { return right; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitBinaryExpression(this);
    }
}

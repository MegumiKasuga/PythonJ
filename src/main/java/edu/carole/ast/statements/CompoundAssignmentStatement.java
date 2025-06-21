package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import edu.carole.ast.expressions.BinaryExpression;
import lombok.Getter;

/**
 * 复合赋值语句 (e.g., i += 1, j -= 2)
 */
public class CompoundAssignmentStatement extends ASTNode {
    public enum Operator {
        PLUS_ASSIGN,        // +=
        MINUS_ASSIGN,       // -=
        MULTIPLY_ASSIGN,    // *=
        DIVIDE_ASSIGN,      // /=
        MODULO_ASSIGN,      // %=
        POWER_ASSIGN,       // **=
        FLOOR_DIVIDE_ASSIGN,// //=
        AND_ASSIGN,         // &=
        OR_ASSIGN,          // |=
        XOR_ASSIGN,         // ^=
        LEFT_SHIFT_ASSIGN,  // <<=
        RIGHT_SHIFT_ASSIGN  // >>=
    }
    
    private final String target;
    private final Operator operator;
    private final ASTNode value;

    private final int line, column;

    @Getter
    private final String file;
    
    public CompoundAssignmentStatement(String file, String target, Operator operator, ASTNode value, int line, int column) {
        this.target = target;
        this.operator = operator;
        this.value = value;
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

    public String getTarget() { return target; }
    public Operator getOperator() { return operator; }

    public BinaryExpression.Operator getBinaryOperator() {
        return switch (operator) {
            case OR_ASSIGN -> BinaryExpression.Operator.OR;
            case PLUS_ASSIGN -> BinaryExpression.Operator.PLUS;
            case AND_ASSIGN -> BinaryExpression.Operator.AND;
            case MINUS_ASSIGN -> BinaryExpression.Operator.MINUS;
            case MULTIPLY_ASSIGN -> BinaryExpression.Operator.MULTIPLY;
            case DIVIDE_ASSIGN -> BinaryExpression.Operator.DIVIDE;
            case MODULO_ASSIGN -> BinaryExpression.Operator.MODULO;
            case POWER_ASSIGN -> BinaryExpression.Operator.POWER;
            case FLOOR_DIVIDE_ASSIGN -> BinaryExpression.Operator.FLOOR_DIVIDE;
            case XOR_ASSIGN -> BinaryExpression.Operator.BITWISE_XOR;
            case LEFT_SHIFT_ASSIGN -> BinaryExpression.Operator.LEFT_SHIFT;
            case RIGHT_SHIFT_ASSIGN -> BinaryExpression.Operator.RIGHT_SHIFT;
        };
    }
    public ASTNode getValue() { return value; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitCompoundAssignmentStatement(this);
    }
}

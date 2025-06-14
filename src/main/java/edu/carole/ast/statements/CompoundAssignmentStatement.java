package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

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
    
    public CompoundAssignmentStatement(String target, Operator operator, ASTNode value, int line, int column) {
        this.target = target;
        this.operator = operator;
        this.value = value;
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

    public String getTarget() { return target; }
    public Operator getOperator() { return operator; }
    public ASTNode getValue() { return value; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitCompoundAssignmentStatement(this);
    }
}

package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * 字面量
 */
public class Literal extends ASTNode {
    private final Object value;
    private final int line, column;
    
    public Literal(Object value, int line, int column) {
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

    public Object getValue() {
        return value;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitLiteral(this);
    }
}

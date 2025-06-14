package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * return语句
 */
public class ReturnStatement extends ASTNode {
    private final ASTNode value;
    private final int line, column;
    
    public ReturnStatement(ASTNode value, int line, int column) {
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

    public ASTNode getValue() { return value; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitReturnStatement(this);
    }
}

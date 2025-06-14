package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * break语句
 */
public class BreakStatement extends ASTNode {

    private final int line, column;

    public BreakStatement(int line, int column) {
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

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitBreakStatement(this);
    }
}

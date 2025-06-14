package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * continue语句
 */
public class ContinueStatement extends ASTNode {

    private final int line, column;
    public ContinueStatement(int line, int column) {
        this.column = column;
        this.line = line;
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
        return visitor.visitContinueStatement(this);
    }
}

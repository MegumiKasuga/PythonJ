package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

/**
 * continue语句
 */
public class ContinueStatement extends ASTNode {

    private final int line, column;

    @Getter
    private final String file;

    public ContinueStatement(String file, int line, int column) {
        this.column = column;
        this.line = line;
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

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitContinueStatement(this);
    }
}

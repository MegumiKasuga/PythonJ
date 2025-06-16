package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

/**
 * pass语句
 */
public class PassStatement extends ASTNode {

    private final int line, column;

    @Getter
    private final String file;

    public PassStatement(String file, int line, int column) {
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

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitPassStatement(this);
    }
}

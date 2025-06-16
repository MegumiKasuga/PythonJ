package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

public class YieldStatement extends ASTNode {

    private final ASTNode value;
    private final int line, column;

    @Getter
    private final String file;

    public YieldStatement(String file, ASTNode value, int line, int column) {
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

    public ASTNode getValue() {
        return value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitYieldStatement(this);
    }
}

package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

/**
 * 标识符
 */
public class Identifier extends ASTNode {
    private final String name;
    private final int line, column;

    @Getter
    private final String file;

    public Identifier(String file, String name, int line, int column) {
        this.name = name;
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

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitIdentifier(this);
    }
}

package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

/**
 * 通配符模式 (_) - 匹配所有内容
 */
public class WildcardPattern extends ASTNode {

    private final int line, column;

    @Getter
    private final String file;

    public WildcardPattern(String file, int line, int column) {
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
        return visitor.visitWildcardPattern(this);
    }
}

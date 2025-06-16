package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

/**
 * OR模式 - 匹配多个模式中的任意一个
 */
public class OrPattern extends ASTNode {
    private final ASTNode left;
    private final ASTNode right;
    private final int line, column;

    @Getter
    private final String file;
    
    public OrPattern(String file, ASTNode left, ASTNode right, int line, int column) {
        this.left = left;
        this.right = right;
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

    public ASTNode getLeft() { return left; }
    public ASTNode getRight() { return right; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitOrPattern(this);
    }
}

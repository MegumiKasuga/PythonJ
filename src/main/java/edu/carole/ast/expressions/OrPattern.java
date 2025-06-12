package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * OR模式 - 匹配多个模式中的任意一个
 */
public class OrPattern extends ASTNode {
    private final ASTNode left;
    private final ASTNode right;
    private final int line, column;
    
    public OrPattern(ASTNode left, ASTNode right, int line, int column) {
        this.left = left;
        this.right = right;
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

    public ASTNode getLeft() { return left; }
    public ASTNode getRight() { return right; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitOrPattern(this);
    }
}

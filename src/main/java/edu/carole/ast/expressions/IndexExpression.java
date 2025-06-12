package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * 索引访问表达式 (supports both simple indexing and slicing)
 */
public class IndexExpression extends ASTNode {
    private final ASTNode object;
    private final ASTNode index;  // 可以是简单表达式或 SliceExpression
    private final int line, column;
    
    public IndexExpression(ASTNode object, ASTNode index, int line, int column) {
        this.object = object;
        this.index = index;
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

    public ASTNode getObject() { return object; }
    public ASTNode getIndex() { return index; }
    
    /**
     * 检查是否为切片操作
     */
    public boolean isSlice() {
        return index instanceof SliceExpression;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitIndexExpression(this);
    }
}

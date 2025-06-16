package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

import java.util.List;

/**
 * 序列模式 - 匹配列表、元组等序列结构
 */
public class SequencePattern extends ASTNode {
    private final List<ASTNode> patterns;
    private final boolean isTuple; // true for tuple patterns, false for list patterns
    private final int line, column;

    @Getter
    private final String file;
    
    public SequencePattern(String file, List<ASTNode> patterns, boolean isTuple,
                           int line, int column) {
        this.patterns = patterns;
        this.isTuple = isTuple;
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

    public List<ASTNode> getPatterns() { return patterns; }
    public boolean isTuple() { return isTuple; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitSequencePattern(this);
    }
}

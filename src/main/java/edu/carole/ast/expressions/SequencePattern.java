package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.List;

/**
 * 序列模式 - 匹配列表、元组等序列结构
 */
public class SequencePattern extends ASTNode {
    private final List<ASTNode> patterns;
    private final boolean isTuple; // true for tuple patterns, false for list patterns
    
    public SequencePattern(List<ASTNode> patterns, boolean isTuple) {
        this.patterns = patterns;
        this.isTuple = isTuple;
    }
    
    public List<ASTNode> getPatterns() { return patterns; }
    public boolean isTuple() { return isTuple; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitSequencePattern(this);
    }
}

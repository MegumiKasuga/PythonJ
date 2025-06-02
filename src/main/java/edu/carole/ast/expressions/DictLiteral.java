package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.Map;

/**
 * 字典字面量
 */
public class DictLiteral extends ASTNode {
    private final Map<ASTNode, ASTNode> entries;
    
    public DictLiteral(Map<ASTNode, ASTNode> entries) {
        this.entries = entries;
    }
    
    public Map<ASTNode, ASTNode> getEntries() {
        return entries;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitDictLiteral(this);
    }
}

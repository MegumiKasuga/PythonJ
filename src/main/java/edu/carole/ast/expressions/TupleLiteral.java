package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.List;

/**
 * 元组字面量
 */
public class TupleLiteral extends ASTNode {
    private final List<ASTNode> elements;
    
    public TupleLiteral(List<ASTNode> elements) {
        this.elements = elements;
    }
    
    public List<ASTNode> getElements() {
        return elements;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitTupleLiteral(this);
    }
}

package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.List;

/**
 * 列表字面量
 */
public class ListLiteral extends ASTNode {
    private final List<ASTNode> elements;
    
    public ListLiteral(List<ASTNode> elements) {
        this.elements = elements;
    }
    
    public List<ASTNode> getElements() {
        return elements;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitListLiteral(this);
    }
}

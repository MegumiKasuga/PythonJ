package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.List;

public class SetLiteral extends ASTNode {
    private final List<ASTNode> elements;
    
    public SetLiteral(List<ASTNode> elements) {
        this.elements = elements;
    }
    
    public List<ASTNode> getElements() {
        return elements;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitSetLiteral(this);
    }
    
    @Override
    public String toString() {
        return "SetLiteral{" + elements + "}";
    }
}

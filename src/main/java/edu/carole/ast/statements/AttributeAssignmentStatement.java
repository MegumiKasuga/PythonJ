package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * 属性赋值语句 (e.g., self.name = value)
 */
public class AttributeAssignmentStatement extends ASTNode {
    private final ASTNode object;
    private final String attribute;
    private final ASTNode value;
    
    public AttributeAssignmentStatement(ASTNode object, String attribute, ASTNode value) {
        this.object = object;
        this.attribute = attribute;
        this.value = value;
    }
    
    public ASTNode getObject() { return object; }
    public String getAttribute() { return attribute; }
    public ASTNode getValue() { return value; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitAttributeAssignmentStatement(this);
    }
}

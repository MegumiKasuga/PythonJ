package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * 索引赋值语句 (e.g., dict["key"] = value)
 */
public class IndexAssignmentStatement extends ASTNode {
    private final ASTNode object;
    private final ASTNode index;
    private final ASTNode value;
    
    public IndexAssignmentStatement(ASTNode object, ASTNode index, ASTNode value) {
        this.object = object;
        this.index = index;
        this.value = value;
    }
    
    public ASTNode getObject() { return object; }
    public ASTNode getIndex() { return index; }
    public ASTNode getValue() { return value; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitIndexAssignmentStatement(this);
    }
}

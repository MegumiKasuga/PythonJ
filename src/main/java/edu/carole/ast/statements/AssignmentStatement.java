package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * 赋值语句
 */
public class AssignmentStatement extends ASTNode {
    private final String target;
    private final ASTNode value;
    
    public AssignmentStatement(String target, ASTNode value) {
        this.target = target;
        this.value = value;
    }
    
    public String getTarget() { return target; }
    public ASTNode getValue() { return value; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitAssignmentStatement(this);
    }
}

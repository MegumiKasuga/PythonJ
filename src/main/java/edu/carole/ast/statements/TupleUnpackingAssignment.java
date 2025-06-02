package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.List;

/**
 * 元组解包赋值语句 (e.g., a, b = func())
 */
public class TupleUnpackingAssignment extends ASTNode {
    private final List<String> targets;
    private final ASTNode value;
    
    public TupleUnpackingAssignment(List<String> targets, ASTNode value) {
        this.targets = targets;
        this.value = value;
    }
    
    public List<String> getTargets() { return targets; }
    public ASTNode getValue() { return value; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitTupleUnpackingAssignment(this);
    }
}

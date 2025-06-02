package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.List;

/**
 * if语句
 */
public class IfStatement extends ASTNode {
    private final ASTNode condition;
    private final List<ASTNode> thenBranch;
    private final List<ASTNode> elseBranch;
    
    public IfStatement(ASTNode condition, List<ASTNode> thenBranch, List<ASTNode> elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }
    
    public ASTNode getCondition() { return condition; }
    public List<ASTNode> getThenBranch() { return thenBranch; }
    public List<ASTNode> getElseBranch() { return elseBranch; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitIfStatement(this);
    }
}

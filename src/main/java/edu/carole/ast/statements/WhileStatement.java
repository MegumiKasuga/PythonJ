package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.List;

/**
 * while语句
 */
public class WhileStatement extends ASTNode {
    private final ASTNode condition;
    private final List<ASTNode> body;
    
    public WhileStatement(ASTNode condition, List<ASTNode> body) {
        this.condition = condition;
        this.body = body;
    }
    
    public ASTNode getCondition() { return condition; }
    public List<ASTNode> getBody() { return body; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitWhileStatement(this);
    }
}

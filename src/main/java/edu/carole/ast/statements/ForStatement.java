package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.List;

/**
 * for语句
 */
public class ForStatement extends ASTNode {
    private final String variable;
    private final ASTNode iterable;
    private final List<ASTNode> body;
    
    public ForStatement(String variable, ASTNode iterable, List<ASTNode> body) {
        this.variable = variable;
        this.iterable = iterable;
        this.body = body;
    }
    
    public String getVariable() { return variable; }
    public ASTNode getIterable() { return iterable; }
    public List<ASTNode> getBody() { return body; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitForStatement(this);
    }
}

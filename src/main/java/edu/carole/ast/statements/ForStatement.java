package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.List;
import java.util.ArrayList;

/**
 * for语句
 */
public class ForStatement extends ASTNode {
    private final String variable;
    private final ASTNode iterable;
    private final List<ASTNode> body;
    private final List<ASTNode> elseBody;
    
    public ForStatement(String variable, ASTNode iterable, List<ASTNode> body) {
        this(variable, iterable, body, new ArrayList<>());
    }
    
    public ForStatement(String variable, ASTNode iterable, List<ASTNode> body, List<ASTNode> elseBody) {
        this.variable = variable;
        this.iterable = iterable;
        this.body = body;
        this.elseBody = elseBody;
    }
    
    public String getVariable() { return variable; }
    public ASTNode getIterable() { return iterable; }
    public List<ASTNode> getBody() { return body; }
    public List<ASTNode> getElseBody() { return elseBody; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitForStatement(this);
    }
}

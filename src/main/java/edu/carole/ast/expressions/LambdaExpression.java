package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.List;

/**
 * Lambda表达式
 */
public class LambdaExpression extends ASTNode {
    private final List<String> parameters;
    private final ASTNode body;
    
    public LambdaExpression(List<String> parameters, ASTNode body) {
        this.parameters = parameters;
        this.body = body;
    }
    
    public List<String> getParameters() {
        return parameters;
    }
    
    public ASTNode getBody() {
        return body;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitLambdaExpression(this);
    }
}

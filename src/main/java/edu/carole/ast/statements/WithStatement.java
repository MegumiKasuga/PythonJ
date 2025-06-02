package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.List;

/**
 * with语句，用于上下文管理
 */
public class WithStatement extends ASTNode {
    private final ASTNode contextExpression;
    private final String targetVariable; // 可选的 as 变量
    private final List<ASTNode> body;
    
    public WithStatement(ASTNode contextExpression, String targetVariable, List<ASTNode> body) {
        this.contextExpression = contextExpression;
        this.targetVariable = targetVariable;
        this.body = body;
    }
    
    public ASTNode getContextExpression() { 
        return contextExpression; 
    }
    
    public String getTargetVariable() { 
        return targetVariable; 
    }
    
    public List<ASTNode> getBody() { 
        return body; 
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitWithStatement(this);
    }
}

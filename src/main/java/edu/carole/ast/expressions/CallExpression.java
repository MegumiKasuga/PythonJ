package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.List;

/**
 * 函数调用表达式
 */
public class CallExpression extends ASTNode {
    private final ASTNode function;
    private final List<ASTNode> arguments;
    
    public CallExpression(ASTNode function, List<ASTNode> arguments) {
        this.function = function;
        this.arguments = arguments;
    }
    
    public ASTNode getFunction() { return function; }
    public List<ASTNode> getArguments() { return arguments; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitCallExpression(this);
    }
}

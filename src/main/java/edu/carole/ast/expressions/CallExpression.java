package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 函数调用表达式
 * 支持位置参数和关键字参数
 */
public class CallExpression extends ASTNode {
    private final ASTNode function;
    private final List<ASTNode> positionalArguments; // 位置参数
    private final Map<String, ASTNode> keywordArguments; // 关键字参数 name -> value expression
    
    // 原始构造器，保持向后兼容
    public CallExpression(ASTNode function, List<ASTNode> arguments) {
        this.function = function;
        this.positionalArguments = arguments;
        this.keywordArguments = new HashMap<>();
    }
    
    // 新构造器，支持关键字参数
    public CallExpression(ASTNode function, List<ASTNode> positionalArguments, Map<String, ASTNode> keywordArguments) {
        this.function = function;
        this.positionalArguments = positionalArguments;
        this.keywordArguments = keywordArguments;
    }
    
    public ASTNode getFunction() { return function; }
    
    public List<ASTNode> getArguments() { 
        // 保留向后兼容，返回位置参数
        return positionalArguments; 
    }
    
    public List<ASTNode> getPositionalArguments() { return positionalArguments; }
    
    public Map<String, ASTNode> getKeywordArguments() { return keywordArguments; }
    
    // 检查是否存在关键字参数
    public boolean hasKeywordArguments() {
        return keywordArguments != null && !keywordArguments.isEmpty();
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitCallExpression(this);
    }
}

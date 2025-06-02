package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * 装饰器 - 表示一个函数或类的装饰器
 */
public class Decorator extends ASTNode {
    private final ASTNode expression;  // 装饰器表达式
    private final ASTNode target;      // 被装饰的函数或类
    
    public Decorator(ASTNode expression, ASTNode target) {
        this.expression = expression;
        this.target = target;
    }
    
    public ASTNode getExpression() {
        return expression;
    }
    
    public ASTNode getTarget() {
        return target;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitDecorator(this);
    }
}

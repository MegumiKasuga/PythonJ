package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * 装饰器 - 表示一个函数或类的装饰器
 */
public class Decorator extends ASTNode {
    private final ASTNode expression;  // 装饰器表达式
    private final ASTNode target;      // 被装饰的函数或类
    private Decorator parent = null;
    private ASTNode root = null;
    private final int line, column;
    
    public Decorator(ASTNode expression, ASTNode target, int line, int column) {
        this.expression = expression;
        this.target = target;
        if (target instanceof ClassDef || target instanceof FunctionDef) {
            root = target;  // 设置父节点为装饰器
        }
        if (target instanceof Decorator decorator) {
            decorator.parent = this;
            root = decorator.root;
        }
        this.line = line;
        this.column = column;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getColumn() {
        return column;
    }

    public ASTNode getExpression() {
        return expression;
    }
    
    public ASTNode getTarget() {
        return target;
    }

    public ASTNode getRoot() {
        return root;
    }

    public Decorator getDecoratorEntry() {
        if (parent != null) {
            return parent.getDecoratorEntry();
        } else {
            return this;
        }
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitDecorator(this);
    }
}

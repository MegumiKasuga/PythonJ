package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

/**
 * Super表达式: super() 或 super(ClassName)
 * 用于访问父类的方法和属性
 */
public class SuperExpression extends ASTNode {
    private final String className; // 可选，用于指定特定的父类
    private final int line, column;

    @Getter
    private final String file;

    public SuperExpression(String file, int line, int column) {
        this.className = null; // super() - 自动使用最近的父类
        this.line = line;
        this.column = column;
        this.file = file;
    }
    
    public SuperExpression(String file, String className, int line, int column) {
        this.className = className; // super(ClassName) - 指定特定父类
        this.line = line;
        this.column = column;
        this.file = file;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getColumn() {
        return column;
    }

    public String getClassName() {
        return className;
    }
    
    public boolean hasSpecificClass() {
        return className != null;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitSuperExpression(this);
    }
}

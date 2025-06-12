package edu.carole.ast;

import edu.carole.ast.ast.ASTVisitor;

/**
 * 抽象语法树节点的基类
 */
public abstract class ASTNode {
    public abstract <T> T accept(ASTVisitor<T> visitor);

    public int getLine() {
        return -1; // 默认返回-1，子类可以覆盖此方法提供具体行号
    }

    public int getColumn() {
        return -1; // 默认返回-1，子类可以覆盖此方法提供具体列号
    }
}


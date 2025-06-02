package edu.carole.ast;

import edu.carole.ast.ast.ASTVisitor;

/**
 * 抽象语法树节点的基类
 */
public abstract class ASTNode {
    public abstract <T> T accept(ASTVisitor<T> visitor);
}


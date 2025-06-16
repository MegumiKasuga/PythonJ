package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

/**
 * 条件表达式 (三元运算符)
 * 表示 Python 的 expr1 if condition else expr2 语法
 */
public class ConditionalExpression extends ASTNode {
    private final ASTNode condition;
    private final ASTNode trueExpression;
    private final ASTNode falseExpression;
    private final int line, column;

    @Getter
    private final String file;
    
    public ConditionalExpression(String file, ASTNode trueExpression, ASTNode condition, ASTNode falseExpression,
                                 int line, int column) {
        this.trueExpression = trueExpression;
        this.condition = condition;
        this.falseExpression = falseExpression;
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

    public ASTNode getCondition() {
        return condition;
    }
    
    public ASTNode getTrueExpression() {
        return trueExpression;
    }
    
    public ASTNode getFalseExpression() {
        return falseExpression;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitConditionalExpression(this);
    }
}

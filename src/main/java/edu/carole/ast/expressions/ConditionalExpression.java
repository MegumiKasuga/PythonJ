package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * 条件表达式 (三元运算符)
 * 表示 Python 的 expr1 if condition else expr2 语法
 */
public class ConditionalExpression extends ASTNode {
    private final ASTNode condition;
    private final ASTNode trueExpression;
    private final ASTNode falseExpression;
    
    public ConditionalExpression(ASTNode trueExpression, ASTNode condition, ASTNode falseExpression) {
        this.trueExpression = trueExpression;
        this.condition = condition;
        this.falseExpression = falseExpression;
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

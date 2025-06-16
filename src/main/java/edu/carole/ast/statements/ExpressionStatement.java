package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

/**
 * 表达式语句
 */
public class ExpressionStatement extends ASTNode {
    private final ASTNode expression;
    private final int line, column;

    @Getter
    private final String file;
    
    public ExpressionStatement(String file, ASTNode expression, int line, int column) {
        this.expression = expression;
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

    public ASTNode getExpression() {
        return expression;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitExpressionStatement(this);
    }
}

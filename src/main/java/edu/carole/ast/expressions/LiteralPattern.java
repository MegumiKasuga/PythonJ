package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

/**
 * 字面量模式 - 匹配特定的字面量值
 */
public class LiteralPattern extends ASTNode {
    private final ASTNode value;
    private final int line, column;

    @Getter
    private final String file;
    
    public LiteralPattern(String file, ASTNode value, int line, int column) {
        this.value = value;
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

    public ASTNode getValue() { return value; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitLiteralPattern(this);
    }
}

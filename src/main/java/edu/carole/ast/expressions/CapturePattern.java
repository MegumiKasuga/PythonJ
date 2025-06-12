package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * 捕获模式 - 将匹配的值绑定到变量
 */
public class CapturePattern extends ASTNode {
    private final String name;
    private final int line, column;
    
    public CapturePattern(String name, int line, int column) {
        this.name = name;
        this.line = line;
        this.column = column;
    }
    
    public String getName() { return name; }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitCapturePattern(this);
    }
}

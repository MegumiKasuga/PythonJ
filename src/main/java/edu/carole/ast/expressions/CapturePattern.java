package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * 捕获模式 - 将匹配的值绑定到变量
 */
public class CapturePattern extends ASTNode {
    private final String name;
    
    public CapturePattern(String name) {
        this.name = name;
    }
    
    public String getName() { return name; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitCapturePattern(this);
    }
}

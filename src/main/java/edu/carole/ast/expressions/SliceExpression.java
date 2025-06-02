package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * 切片表达式 (e.g., list[start:stop:step])
 */
public class SliceExpression extends ASTNode {
    private final ASTNode start;   // 可以为 null
    private final ASTNode stop;    // 可以为 null  
    private final ASTNode step;    // 可以为 null
    
    public SliceExpression(ASTNode start, ASTNode stop, ASTNode step) {
        this.start = start;
        this.stop = stop;
        this.step = step;
    }
    
    public ASTNode getStart() { return start; }
    public ASTNode getStop() { return stop; }
    public ASTNode getStep() { return step; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitSliceExpression(this);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SliceExpression(");
        sb.append("start=").append(start);
        sb.append(", stop=").append(stop);
        sb.append(", step=").append(step);
        sb.append(")");
        return sb.toString();
    }
}

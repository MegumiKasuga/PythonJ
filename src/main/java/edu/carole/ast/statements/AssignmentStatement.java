package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

/**
 * 赋值语句
 */
public class AssignmentStatement extends ASTNode {
    private final String target;
    private final ASTNode value;
    private final int line, column;

    @Getter
    private final String file;
    
    public AssignmentStatement(String file, String target, ASTNode value, int line, int column) {
        this.target = target;
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

    public String getTarget() { return target; }
    public ASTNode getValue() { return value; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitAssignmentStatement(this);
    }
}

package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

/**
 * 索引赋值语句 (e.g., dict["key"] = value)
 */
public class IndexAssignmentStatement extends ASTNode {
    private final ASTNode object;
    private final ASTNode index;
    private final ASTNode value;
    private final int line, column;

    @Getter
    private final String file;
    
    public IndexAssignmentStatement(String file, ASTNode object, ASTNode index, ASTNode value, int line, int column) {
        this.object = object;
        this.index = index;
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

    public ASTNode getObject() { return object; }
    public ASTNode getIndex() { return index; }
    public ASTNode getValue() { return value; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitIndexAssignmentStatement(this);
    }
}

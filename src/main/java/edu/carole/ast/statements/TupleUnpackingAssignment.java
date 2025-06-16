package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

import java.util.List;

/**
 * 元组解包赋值语句 (e.g., a, b = func())
 */
public class TupleUnpackingAssignment extends ASTNode {
    private final List<String> targets;
    private final ASTNode value;
    private final int line, column;

    @Getter
    private final String file;
    
    public TupleUnpackingAssignment(String file, List<String> targets, ASTNode value, int line, int column) {
        this.targets = targets;
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

    public List<String> getTargets() { return targets; }
    public ASTNode getValue() { return value; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitTupleUnpackingAssignment(this);
    }
}

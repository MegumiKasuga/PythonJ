package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

import java.util.List;

public class SetLiteral extends ASTNode {
    private final List<ASTNode> elements;
    private final int line, column;

    @Getter
    private final String file;
    
    public SetLiteral(String file, List<ASTNode> elements, int line, int column) {
        this.elements = elements;
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

    public List<ASTNode> getElements() {
        return elements;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitSetLiteral(this);
    }
    
    @Override
    public String toString() {
        return "SetLiteral{" + elements + "}";
    }
}

package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

import java.util.List;

/**
 * 元组字面量
 */
public class TupleLiteral extends ASTNode {
    private final List<ASTNode> elements;
    private final int line, column;

    @Getter
    private final String file;
    
    public TupleLiteral(String file, List<ASTNode> elements, int line, int column) {
        this.elements = elements;
        this.line = line;
        this.column = column;
        this.file = file;
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public int getLine() {
        return line;
    }

    public List<ASTNode> getElements() {
        return elements;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitTupleLiteral(this);
    }
}

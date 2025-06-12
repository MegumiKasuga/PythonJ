package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.List;

/**
 * 列表字面量
 */
public class ListLiteral extends ASTNode {
    private final List<ASTNode> elements;
    private final int line, column;
    
    public ListLiteral(List<ASTNode> elements, int line, int column) {
        this.elements = elements;
        this.line = line;
        this.column = column;
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
        return visitor.visitListLiteral(this);
    }
}

package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.Map;

/**
 * 字典字面量
 */
public class DictLiteral extends ASTNode {
    private final Map<ASTNode, ASTNode> entries;
    private final int line, column;
    
    public DictLiteral(Map<ASTNode, ASTNode> entries, int line, int column) {
        this.entries = entries;
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

    public Map<ASTNode, ASTNode> getEntries() {
        return entries;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitDictLiteral(this);
    }
}

package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * 标识符
 */
public class Identifier extends ASTNode {
    private final String name;
    
    public Identifier(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitIdentifier(this);
    }
}

package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

/**
 * 属性访问表达式
 */
public class AttributeExpression extends ASTNode {
    private final ASTNode object;
    private final String attribute;
    private final int line, column;
    
    public AttributeExpression(ASTNode object, String attribute, int line, int column) {
        this.object = object;
        this.attribute = attribute;
        this.line = line;
        this.column = column;
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return object.toString() + "." + attribute;
    }

    public ASTNode getObject() { return object; }
    public String getAttribute() { return attribute; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitAttributeExpression(this);
    }
}

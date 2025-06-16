package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

/**
 * 属性赋值语句 (e.g., self.name = value)
 */
public class AttributeAssignmentStatement extends ASTNode {
    private final ASTNode object;
    private final String attribute;
    private final ASTNode value;
    private final int line, column;

    @Getter
    private final String file;
    
    public AttributeAssignmentStatement(String file, ASTNode object, String attribute,
                                        ASTNode value, int line, int column) {
        this.object = object;
        this.attribute = attribute;
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
    public String getAttribute() { return attribute; }
    public ASTNode getValue() { return value; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitAttributeAssignmentStatement(this);
    }
}

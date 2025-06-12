package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

public class YieldStatement extends ASTNode {

    private final ASTNode value;

    public YieldStatement(ASTNode value) {
        this.value = value;
    }

    public ASTNode getValue() {
        return value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitYieldStatement(this);
    }
}

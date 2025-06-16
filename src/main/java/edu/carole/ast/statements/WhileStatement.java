package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

import java.util.List;
import java.util.ArrayList;

/**
 * while语句
 */
public class WhileStatement extends ASTNode {
    private final ASTNode condition;
    private final List<ASTNode> body;
    private final List<ASTNode> elseBody;
    private final int line, column;

    @Getter
    private final String file;
    
    public WhileStatement(String file, ASTNode condition, List<ASTNode> body, int line, int column) {
        this(file, condition, body, new ArrayList<>(), line, column);
    }
    
    public WhileStatement(String file, ASTNode condition, List<ASTNode> body, List<ASTNode> elseBody, int line, int column) {
        this.condition = condition;
        this.body = body;
        this.elseBody = elseBody;
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

    public ASTNode getCondition() { return condition; }
    public List<ASTNode> getBody() { return body; }
    public List<ASTNode> getElseBody() { return elseBody; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitWhileStatement(this);
    }
}

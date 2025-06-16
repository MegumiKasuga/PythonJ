package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

import java.util.List;
import java.util.ArrayList;

/**
 * for语句
 */
public class ForStatement extends ASTNode {
    private final String variable;
    private final ASTNode iterable;
    private final List<ASTNode> body;
    private final List<ASTNode> elseBody;

    private final int line, column;

    @Getter
    private final String file;
    
    public ForStatement(String file, String variable, ASTNode iterable, List<ASTNode> body, int line, int column) {
        this(file, variable, iterable, body, new ArrayList<>(), line, column);
    }
    
    public ForStatement(String file, String variable, ASTNode iterable, List<ASTNode> body, List<ASTNode> elseBody, int line, int column) {
        this.variable = variable;
        this.iterable = iterable;
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

    public String getVariable() { return variable; }
    public ASTNode getIterable() { return iterable; }
    public List<ASTNode> getBody() { return body; }
    public List<ASTNode> getElseBody() { return elseBody; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitForStatement(this);
    }
}

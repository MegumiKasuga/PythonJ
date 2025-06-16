package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

import java.util.List;

/**
 * 程序根节点
 */
public class Program extends ASTNode {
    private final List<ASTNode> statements;

    @Getter
    private final String file;
    
    public Program(String file, List<ASTNode> statements) {
        this.file = file;
        this.statements = statements;
    }
    
    public List<ASTNode> getStatements() {
        return statements;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitProgram(this);
    }
}

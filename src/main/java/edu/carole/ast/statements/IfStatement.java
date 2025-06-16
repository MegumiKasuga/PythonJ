package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * if语句
 */
public class IfStatement extends ASTNode {
    private final List<Map.Entry<ASTNode, List<ASTNode>>> conditionBranches;
    private final List<ASTNode> elseBranch;
    private final int line, column;

    @Getter
    private final String file;
    
    public IfStatement(String file, List<Map.Entry<ASTNode, List<ASTNode>>> conditionBranches,
                       List<ASTNode> elseBranch, int line, int column) {
        this.elseBranch = elseBranch;
        this.conditionBranches = conditionBranches; // 如果有elif分支，可以在这里初始化
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

    public List<ASTNode> getElseBranch() { return elseBranch; }

    public List<Map.Entry<ASTNode, List<ASTNode>>> getConditionBranches() {
        return conditionBranches;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitIfStatement(this);
    }
}

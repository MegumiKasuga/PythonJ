package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.List;

/**
 * global语句
 * 用于声明变量为全局变量
 * 例如: global x, y, z
 */
public class GlobalStatement extends ASTNode {
    private final List<String> variables;
    
    public GlobalStatement(List<String> variables) {
        this.variables = variables;
    }
    
    public List<String> getVariables() {
        return variables;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitGlobalStatement(this);
    }
}

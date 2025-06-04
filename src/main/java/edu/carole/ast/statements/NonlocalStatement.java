package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.List;

/**
 * nonlocal语句
 * 用于声明变量为非局部变量（外层作用域变量）
 * 例如: nonlocal x, y, z
 */
public class NonlocalStatement extends ASTNode {
    private final List<String> variables;
    
    public NonlocalStatement(List<String> variables) {
        this.variables = variables;
    }
    
    public List<String> getVariables() {
        return variables;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitNonlocalStatement(this);
    }
}

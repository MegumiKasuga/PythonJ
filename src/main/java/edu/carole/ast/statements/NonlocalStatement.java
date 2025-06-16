package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

import java.util.List;

/**
 * nonlocal语句
 * 用于声明变量为非局部变量（外层作用域变量）
 * 例如: nonlocal x, y, z
 */
public class NonlocalStatement extends ASTNode {
    private final List<String> variables;
    private final int line, column;

    @Getter
    private final String file;
    
    public NonlocalStatement(String file, List<String> variables, int line, int column) {
        this.variables = variables;
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

    public List<String> getVariables() {
        return variables;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitNonlocalStatement(this);
    }
}

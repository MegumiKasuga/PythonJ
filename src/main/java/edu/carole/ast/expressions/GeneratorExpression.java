package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.List;

/**
 * 生成器表达式
 */
public class GeneratorExpression extends ASTNode {
    private final ASTNode element;
    private final List<ListComprehension.ComprehensionClause> clauses;
    private final int line, column;
    
    public GeneratorExpression(ASTNode element, List<ListComprehension.ComprehensionClause> clauses,
                               int line, int column) {
        this.element = element;
        this.clauses = clauses;
        this.line = line;
        this.column = column;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getColumn() {
        return column;
    }

    public ASTNode getElement() {
        return element;
    }
    
    public List<ListComprehension.ComprehensionClause> getClauses() {
        return clauses;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitGeneratorExpression(this);
    }
}

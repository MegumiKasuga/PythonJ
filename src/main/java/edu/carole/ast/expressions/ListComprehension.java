package edu.carole.ast.expressions;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.List;

/**
 * 列表推导式
 */
public class ListComprehension extends ASTNode {
    private final ASTNode element;
    private final List<ComprehensionClause> clauses;
    private final int line, column;
    
    public ListComprehension(ASTNode element, List<ComprehensionClause> clauses, int line, int column) {
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
    
    public List<ComprehensionClause> getClauses() {
        return clauses;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitListComprehension(this);
    }
    
    /**
     * 推导式子句 (for variable in iterable [if condition])
     */
    public static class ComprehensionClause {

        private final String variable;
        private final ASTNode iterable;
        private final ASTNode condition;  // 可为null
        
        public ComprehensionClause(String variable, ASTNode iterable, ASTNode condition) {
            this.variable = variable;
            this.iterable = iterable;
            this.condition = condition;
        }
        
        public String getVariable() { return variable; }

        public ASTNode getIterable() { return iterable; }

        public ASTNode getCondition() { return condition; }
    }
}

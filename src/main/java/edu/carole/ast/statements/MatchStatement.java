package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.List;

/**
 * match语句 (Python 3.10+ structural pattern matching)
 */
public class MatchStatement extends ASTNode {
    private final ASTNode subject;
    private final List<CaseClause> cases;
    private final List<ASTNode> defaultBody; // optional "default" case body, if any
    private final int line, column;
    
    public MatchStatement(ASTNode subject, List<CaseClause> cases, List<ASTNode> defaultBody,
                          int line, int column) {
        this.subject = subject;
        this.cases = cases;
        this.defaultBody = defaultBody; // 可以是null或空列表，表示没有default分支
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

    public ASTNode getSubject() { return subject; }
    public List<CaseClause> getCases() { return cases; }
    public List<ASTNode> getDefaultBody() { return defaultBody; }
    public boolean hasDefaultCase() {
        return defaultBody != null && !defaultBody.isEmpty();
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitMatchStatement(this);
    }
    
    /**
     * case语句子句
     */
    public static class CaseClause {
        private final ASTNode pattern;
        private final ASTNode guard;  // optional "if" condition
        private final List<ASTNode> body;
        
        public CaseClause(ASTNode pattern, ASTNode guard, List<ASTNode> body) {
            this.pattern = pattern;
            this.guard = guard;
            this.body = body;
        }
        
        public ASTNode getPattern() { return pattern; }
        public ASTNode getGuard() { return guard; }
        public List<ASTNode> getBody() { return body; }
    }
}

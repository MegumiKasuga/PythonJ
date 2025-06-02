package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.List;

/**
 * try-except语句
 */
public class TryExceptStatement extends ASTNode {
    public static class ExceptClause {
        private final String exceptionType; // 异常类型，可以为null（捕获所有异常）
        private final String variable;      // 异常变量名，可以为null
        private final List<ASTNode> body;   // except块的语句
        
        public ExceptClause(String exceptionType, String variable, List<ASTNode> body) {
            this.exceptionType = exceptionType;
            this.variable = variable;
            this.body = body;
        }
        
        public String getExceptionType() { return exceptionType; }
        public String getVariable() { return variable; }
        public List<ASTNode> getBody() { return body; }
    }
    
    private final List<ASTNode> tryBody;
    private final List<ExceptClause> exceptClauses;
    private final List<ASTNode> finallyBody; // 可选的finally块
    
    public TryExceptStatement(List<ASTNode> tryBody, List<ExceptClause> exceptClauses, List<ASTNode> finallyBody) {
        this.tryBody = tryBody;
        this.exceptClauses = exceptClauses;
        this.finallyBody = finallyBody;
    }
    
    public List<ASTNode> getTryBody() { return tryBody; }
    public List<ExceptClause> getExceptClauses() { return exceptClauses; }
    public List<ASTNode> getFinallyBody() { return finallyBody; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitTryExceptStatement(this);
    }
}

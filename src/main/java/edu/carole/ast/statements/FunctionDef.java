package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.List;

/**
 * 函数定义
 */
public class FunctionDef extends ASTNode {
    private final String name;
    private final List<String> parameters;
    private final List<ASTNode> body;
    private final String varargsParam; // *args parameter name
    
    public FunctionDef(String name, List<String> parameters, List<ASTNode> body) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
        this.varargsParam = null;
    }
    
    public FunctionDef(String name, List<String> parameters, List<ASTNode> body, String varargsParam) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
        this.varargsParam = varargsParam;
    }
    
    public String getName() { return name; }
    public List<String> getParameters() { return parameters; }
    public List<ASTNode> getBody() { return body; }
    public String getVarargsParam() { return varargsParam; }
    public boolean hasVarargs() { return varargsParam != null; }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitFunctionDef(this);
    }
}

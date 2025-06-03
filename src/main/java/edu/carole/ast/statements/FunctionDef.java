package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import java.util.List;
import java.util.Map;

/**
 * 函数定义
 */
public class FunctionDef extends ASTNode {
    private final String name;
    private final List<FunctionParameter> parameters;  // 使用新的参数结构
    private final List<ASTNode> body;
    private final ASTNode returnTypeHint; // 返回值类型提示
      // 主要构造器
    public FunctionDef(String name, List<FunctionParameter> parameters, List<ASTNode> body) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
        this.returnTypeHint = null;
    }
    
    // 静态工厂方法用于向后兼容
    public static FunctionDef fromParameterNames(String name, List<String> parameterNames, List<ASTNode> body) {
        List<FunctionParameter> parameters = parameterNames.stream()
                .map(FunctionParameter::new)
                .collect(java.util.stream.Collectors.toList());
        return new FunctionDef(name, parameters, body);
    }
    
    // 带返回值类型提示的构造器
    public FunctionDef(String name, List<FunctionParameter> parameters, List<ASTNode> body, ASTNode returnTypeHint) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
        this.returnTypeHint = returnTypeHint;
    }
    
    // 向后兼容的构造器
    public FunctionDef(String name, List<String> parameterNames, List<ASTNode> body, 
                       String varargsParam, String kwargsParam, Map<String, ASTNode> defaultValues) {
        this.name = name;
        this.parameters = convertLegacyParameters(parameterNames, varargsParam, kwargsParam, defaultValues);
        this.body = body;
        this.returnTypeHint = null;
    }
      // 转换工具方法  
    private List<FunctionParameter> convertLegacyParameters(List<String> parameterNames, 
                                                           String varargsParam, String kwargsParam, 
                                                           Map<String, ASTNode> defaultValues) {
        List<FunctionParameter> result = new java.util.ArrayList<>();
        
        // 添加普通参数
        for (String paramName : parameterNames) {
            ASTNode defaultValue = (defaultValues != null) ? defaultValues.get(paramName) : null;
            result.add(new FunctionParameter(paramName, defaultValue));
        }
        
        // 添加 *args 参数
        if (varargsParam != null) {
            result.add(new FunctionParameter(varargsParam, FunctionParameter.ParameterType.VARARGS));
        }
        
        // 添加 **kwargs 参数
        if (kwargsParam != null) {
            result.add(new FunctionParameter(kwargsParam, FunctionParameter.ParameterType.KWARGS));
        }
        
        return result;
    }
    // Getters
    public String getName() { return name; }
    public List<FunctionParameter> getParameters() { return parameters; }
    public List<ASTNode> getBody() { return body; }
    public ASTNode getReturnTypeHint() { return returnTypeHint; }
    
    // 便利方法
    public boolean hasReturnTypeHint() { return returnTypeHint != null; }
    
    // 向后兼容的方法
    public List<String> getParameterNames() {
        return parameters.stream()
                .filter(FunctionParameter::isNormal)
                .map(FunctionParameter::getIdentifier)
                .collect(java.util.stream.Collectors.toList());
    }
    
    public String getVarargsParam() {
        return parameters.stream()
                .filter(FunctionParameter::isVarargs)
                .map(FunctionParameter::getIdentifier)
                .findFirst()
                .orElse(null);
    }
    
    public String getKwargsParam() {
        return parameters.stream()
                .filter(FunctionParameter::isKwargs)
                .map(FunctionParameter::getIdentifier)
                .findFirst()
                .orElse(null);
    }
    
    public Map<String, ASTNode> getDefaultValues() {
        Map<String, ASTNode> result = new java.util.HashMap<>();
        for (FunctionParameter param : parameters) {
            if (param.hasDefaultValue()) {
                result.put(param.getIdentifier(), param.getDefaultValue());
            }
        }
        return result;
    }
    
    public boolean hasVarargs() { return getVarargsParam() != null; }
    public boolean hasKwargs() { return getKwargsParam() != null; }
    public boolean hasDefault(String param) { 
        return parameters.stream()
                .anyMatch(p -> p.getIdentifier().equals(param) && p.hasDefaultValue());
    }
    public ASTNode getDefaultValue(String param) {
        return parameters.stream()
                .filter(p -> p.getIdentifier().equals(param))
                .map(FunctionParameter::getDefaultValue)
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitFunctionDef(this);
    }
}

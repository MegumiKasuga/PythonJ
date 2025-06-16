package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import lombok.Getter;

/**
 * 函数参数数据结构
 * 记录参数的标识符、默认值、类型(*args, **kwargs)以及类型提示
 */
public class FunctionParameter {
    public enum ParameterType {
        NORMAL,     // 普通参数: a
        VARARGS,    // 可变位置参数: *args
        KWARGS      // 关键字参数: **kwargs
    }
    
    private final String identifier;        // 参数名
    private final ParameterType type;       // 参数类型
    private final ASTNode defaultValue;     // 默认值表达式 (可为null)
    private final ASTNode typeHint;         // 类型提示表达式 (可为null)
    private final int line, column;

    @Getter
    private final String file;
    
    // 普通参数构造器
    public FunctionParameter(String file, String identifier, int line, int column) {
        this.identifier = identifier;
        this.type = ParameterType.NORMAL;
        this.defaultValue = null;
        this.typeHint = null;
        this.line = line;
        this.column = column;
        this.file = file;
    }
    
    // 带默认值的普通参数构造器
    public FunctionParameter(String file, String identifier, ASTNode defaultValue, int line, int column) {
        this.identifier = identifier;
        this.type = ParameterType.NORMAL;
        this.defaultValue = defaultValue;
        this.typeHint = null;
        this.line = line;
        this.column = column;
        this.file = file;
    }
    
    // 带类型提示的参数构造器
    public FunctionParameter(String file, String identifier, ASTNode defaultValue, ASTNode typeHint, int line, int column) {
        this.identifier = identifier;
        this.type = ParameterType.NORMAL;
        this.defaultValue = defaultValue;
        this.typeHint = typeHint;
        this.line = line;
        this.column = column;
        this.file = file;
    }
    
    // 特殊参数类型构造器 (*args, **kwargs)
    public FunctionParameter(String file, String identifier, ParameterType type, int line, int column) {
        this.identifier = identifier;
        this.type = type;
        this.defaultValue = null;
        this.typeHint = null;
        this.line = line;
        this.column = column;
        this.file = file;
    }
    
    // 完整构造器
    public FunctionParameter(String file, String identifier, ParameterType type, ASTNode defaultValue, ASTNode typeHint, int line, int column) {
        this.identifier = identifier;
        this.type = type;
        this.defaultValue = defaultValue;
        this.typeHint = typeHint;
        this.line = line;
        this.column = column;
        this.file = file;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    // Getters
    public String getIdentifier() { return identifier; }
    public ParameterType getType() { return type; }
    public ASTNode getDefaultValue() { return defaultValue; }
    public ASTNode getTypeHint() { return typeHint; }
    
    // 便利方法
    public boolean hasDefaultValue() { return defaultValue != null; }
    public boolean hasTypeHint() { return typeHint != null; }
    public boolean isNormal() { return type == ParameterType.NORMAL; }
    public boolean isVarargs() { return type == ParameterType.VARARGS; }
    public boolean isKwargs() { return type == ParameterType.KWARGS; }
    public boolean isRequired() { return isNormal() && !hasDefaultValue(); }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        if (isVarargs()) {
            sb.append("*");
        } else if (isKwargs()) {
            sb.append("**");
        }
        
        sb.append(identifier);
        
        if (hasTypeHint()) {
            sb.append(": ").append(typeHint.toString());
        }
        
        if (hasDefaultValue()) {
            sb.append(" = ").append(defaultValue.toString());
        }
        
        return sb.toString();
    }
}

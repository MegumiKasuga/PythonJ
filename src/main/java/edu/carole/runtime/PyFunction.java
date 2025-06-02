package edu.carole.runtime;

import edu.carole.ast.ASTNode;
import edu.carole.interpreter.Environment;
import edu.carole.interpreter.Interpreter;

import java.util.*;

/**
 * Python函数对象
 */
public class PyFunction extends PyObject {
    private final String name;
    private final List<String> parameters;
    private final List<ASTNode> body;
    private final Environment closure;
    private final Map<String, PyObject> attributes = new HashMap<>();
    private final String varargsParam; // *args parameter name
    
    public PyFunction(String name, List<String> parameters, List<ASTNode> body, Environment closure) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
        this.closure = closure;
        this.varargsParam = null;
    }
    
    public PyFunction(String name, List<String> parameters, List<ASTNode> body, Environment closure, String varargsParam) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
        this.closure = closure;
        this.varargsParam = varargsParam;
    }
      @Override
    public String getTypeName() { return "function"; }
    
    @Override
    public String toString() { return "<function " + name + ">"; }
    
    @Override
    public boolean isTruthy() { return true; }
    
    @Override
    public PyObject getAttribute(String attributeName) {
        if (attributes.containsKey(attributeName)) {
            return attributes.get(attributeName);
        } else if ("__name__".equals(attributeName)) {
            return new PyString(name);
        }
        return super.getAttribute(attributeName);
    }
    
    @Override
    public void setAttribute(String attributeName, PyObject value) {
        attributes.put(attributeName, value);
    }    @Override
    public PyObject call(List<PyObject> arguments) {
        return call(arguments, null);
    }    @Override
    public PyObject call(List<PyObject> arguments, Interpreter interpreter) {
        // Create new function scope
        Environment environment = new Environment(closure);
        
        // Check parameter count for regular parameters
        int regularParamCount = parameters.size();
        int argCount = arguments.size();
        
        if (varargsParam == null) {
            // No varargs, must match exactly
            if (argCount != regularParamCount) {
                throw new RuntimeException(name + "() takes " + regularParamCount + 
                    " positional arguments but " + argCount + " were given");
            }
        } else {
            // Has varargs, must have at least the regular parameters
            if (argCount < regularParamCount) {
                throw new RuntimeException(name + "() takes at least " + regularParamCount + 
                    " positional arguments but " + argCount + " were given");
            }
        }
        
        // Bind regular parameters
        for (int i = 0; i < regularParamCount; i++) {
            environment.define(parameters.get(i), arguments.get(i));
        }
        
        // Bind varargs parameter if present
        if (varargsParam != null) {
            List<PyObject> varargsValues = new ArrayList<>();
            for (int i = regularParamCount; i < argCount; i++) {
                varargsValues.add(arguments.get(i));
            }
            environment.define(varargsParam, new PyTuple(varargsValues));
        }
        
        // Execute function body with interpreter context
        return callWithInterpreter(environment, interpreter);
    }    /**
     * 使用指定的解释器调用函数，如果没有提供解释器则创建新的
     */
    public PyObject callWithInterpreter(Environment functionEnvironment, Interpreter interpreter) {
        if (interpreter == null) {
            interpreter = new Interpreter();
        }
        
        // 保存原来的环境并设置为函数环境
        Environment previousEnv = interpreter.getEnvironment();
        interpreter.setEnvironment(functionEnvironment);
        
        try {
            for (ASTNode statement : body) {
                statement.accept(interpreter);
            }
        } catch (ReturnException returnException) {
            return returnException.getValue();
        } finally {
            // 恢复原来的环境
            interpreter.setEnvironment(previousEnv);
        }
        
        return PyNone.INSTANCE;
    }
      /**
     * 返回异常，用于控制流
     */
    public static class ReturnException extends RuntimeException {
        private final PyObject value;
        
        public ReturnException(PyObject value) {
            this.value = value;
        }
        
        public PyObject getValue() {
            return value;
        }
    }
}

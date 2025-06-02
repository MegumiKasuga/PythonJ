package edu.carole.runtime;

import java.util.*;

/**
 * Python绑定方法
 */
public class PyBoundMethod extends PyObject {
    private final PyInstance instance;
    private final PyObject method;
    
    public PyBoundMethod(PyInstance instance, PyObject method) {
        this.instance = instance;
        this.method = method;
    }
    
    @Override
    public String getTypeName() { return "method"; }
    
    @Override
    public String toString() { 
        return "<bound method of " + instance.toString() + ">"; 
    }
    
    @Override
    public boolean isTruthy() { return true; }
      @Override
    public PyObject call(List<PyObject> arguments) {
        List<PyObject> boundArgs = new ArrayList<>();
        boundArgs.add(instance);
        boundArgs.addAll(arguments);
        return method.call(boundArgs);
    }
    
    @Override
    public PyObject call(List<PyObject> arguments, edu.carole.interpreter.Interpreter interpreter) {
        List<PyObject> boundArgs = new ArrayList<>();
        boundArgs.add(instance);
        boundArgs.addAll(arguments);
        
        // 设置方法调用上下文（用于super()）
        if (interpreter != null) {
            edu.carole.interpreter.Environment env = interpreter.getEnvironment();
            env.setCurrentClass(instance.getPyClass());
            env.setCurrentInstance(instance);
        }
        
        return method.call(boundArgs, interpreter);
    }
}

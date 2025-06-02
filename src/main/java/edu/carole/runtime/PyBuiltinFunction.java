package edu.carole.runtime;

import java.util.List;

/**
 * Python内置函数
 */
public class PyBuiltinFunction extends PyObject {
    private final String name;
    private final BuiltinFunction function;
    
    @FunctionalInterface
    public interface BuiltinFunction {
        PyObject call(List<PyObject> arguments);
    }
    
    public PyBuiltinFunction(String name, BuiltinFunction function) {
        this.name = name;
        this.function = function;
    }
    
    @Override
    public String getTypeName() { return "builtin_function_or_method"; }
    
    @Override
    public String toString() { return "<built-in function " + name + ">"; }
    
    @Override
    public boolean isTruthy() { return true; }
    
    @Override
    public PyObject call(List<PyObject> arguments) {
        return function.call(arguments);
    }
}

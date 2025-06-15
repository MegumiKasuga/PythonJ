package edu.carole.runtime;

import edu.carole.interpreter.Interpreter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Python内置函数
 */
public class PyBuiltinFunction extends PyObject {
    private final String name;
    private final BuiltinFunction function;
    
    @FunctionalInterface
    public interface BuiltinFunction {
        PyObject call(List<PyObject> arguments, Map<String, PyObject> keywordArguments, Interpreter interpreter);
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
    public PyObject call(List<PyObject> arguments, Interpreter interpreter) {
        return call(arguments, null, interpreter);
    }

    public PyObject call(List<PyObject> posArgs, Map<String, PyObject> kwargs, Interpreter interpreter) {
        return function.call(posArgs, kwargs, interpreter);
    }
}

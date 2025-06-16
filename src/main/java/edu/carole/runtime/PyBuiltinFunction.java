package edu.carole.runtime;

import edu.carole.interpreter.Interpreter;

import java.util.*;

/**
 * Python内置函数
 */
public class PyBuiltinFunction extends PyObject implements InstanceBindable {
    private final String name;
    private final BuiltinFunction function;
    private PyObject boundInstance = null;
    
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
        ArrayList<PyObject> args = new ArrayList<>();
        // add 'self' to this func
        if (boundInstance != null) {
            args.add(0, boundInstance);
        }
        args.addAll(posArgs);
        return function.call(args, kwargs, interpreter);
    }

    @Override
    public PyBuiltinFunction bindToInstance(PyObject instance) {
        PyBuiltinFunction func = new PyBuiltinFunction(name, function);
        func.boundInstance = instance;
        return func;
    }
}

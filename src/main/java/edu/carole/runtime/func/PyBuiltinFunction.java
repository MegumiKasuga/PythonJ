package edu.carole.runtime.func;

import edu.carole.interpreter.Environment;
import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.InstanceBindable;
import edu.carole.runtime.PyBool;
import edu.carole.runtime.PyObject;
import edu.carole.runtime.instance.PyInstance;

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


    public boolean isStaticMethod() {
        return attributes.containsKey("__isstaticmethod__") &&
                attributes.get("__isstaticmethod__").isTruthy();
    }

    public void setStaticMethod(boolean isStaticMethod) {
        attributes.put("__isstaticmethod__", PyBool.fromValue(isStaticMethod));
    }

    public boolean isAbstractMethod() {
        return attributes.containsKey("__isabstractmethod__") &&
                attributes.get("__isabstractmethod__").isTruthy();
    }

    public void setAbstractMethod(boolean isAbstract) {
        attributes.put("__isabstractmethod__", PyBool.fromValue(isAbstract));
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
        Environment previous = interpreter.getEnvironment();
        try {
            ArrayList<PyObject> args = new ArrayList<>();
            Environment environment = new Environment(interpreter);
            interpreter.setEnvironment(environment);
            // add 'self' to this func
            if (boundInstance != null) {
                args.add(0, boundInstance);
                if (boundInstance instanceof PyInstance ins) {
                    environment.setCurrentClass(ins.getPyClass());
                    environment.setCurrentInstance(ins);
                }
            }
            args.addAll(posArgs);
            return function.call(args, kwargs, interpreter);
        } finally {
            interpreter.setEnvironment(previous);
        }
    }

    @Override
    public PyBuiltinFunction bindToInstance(Interpreter interpreter, PyObject instance) {
        PyBuiltinFunction func = new PyBuiltinFunction(name, function);
        func.boundInstance = instance;
        return func;
    }
}

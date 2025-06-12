package edu.carole.runtime;

import edu.carole.interpreter.Interpreter;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class PyProperty extends PyObject {

    private PyObject cache = PyNone.INSTANCE;
    private final PyFunction getter;
    private PyFunction setter = null;
    private PyObject boundInstance = null;

    public PyProperty(PyFunction getter, PyFunction setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public PyProperty(PyFunction getter) {
        this.getter = getter;
    }

    public PyProperty boundToInstance(PyObject instance) {
        PyProperty prop = new PyProperty(
                getter.bindToInstance(instance),
                setter.bindToInstance(instance)
        );
        prop.boundInstance = instance;
        return prop;
    }

    public PyObject getBoundInstance() {
        return boundInstance;
    }

    public PyFunction getGetter() {
        return getter;
    }

    public String getName() {
        return getter.getName();
    }

    public void setSetter(PyFunction setter) {
        this.setter = setter;
    }

    public boolean isReadonly() {
        return setter == null;
    }

    public PyObject getCache() {
        return cache;
    }

    @Override
    public String getTypeName() {
        return cache.getTypeName();
    }

    @Override
    public String toString() {
        return cache.toString();
    }

    @Override
    public boolean isTruthy() {
        return cache.isTruthy();
    }

    @Override
    public PyObject call(List<PyObject> arguments) {
        return call(arguments, null);
    }

    @Override
    public PyObject call(List<PyObject> arguments, Interpreter interpreter) {
        if (boundInstance == null) {
            return this;
        }
        if (arguments.isEmpty()) {
            PyObject result = getter.call(arguments, interpreter);
            cache = result;
            return result;
        } else if (arguments.size() == 1) {
            if (isReadonly()) {
                throw new RuntimeException("Cannot set value on a read-only property");
            }
            return setter.call(arguments, interpreter);
        } else {
            throw new RuntimeException("Property requires 1 or 2 arguments, got " + arguments.size());
        }
    }

    @Override
    public PyObject call(List<PyObject> arguments, Map<String, PyObject> keywordArguments, Interpreter interpreter) {
        if (!keywordArguments.isEmpty()) {
            throw new RuntimeException("Property does not support keyword arguments");
        } else {
            return call(arguments, interpreter);
        }
    }
}

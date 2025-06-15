package edu.carole.runtime.property;

import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.PyBuiltinFunction;
import edu.carole.runtime.PyObject;

import java.util.ArrayList;
import java.util.List;

public class BuiltinProperty extends AbstractProperty {

    public BuiltinProperty(String name, PyBuiltinFunction.BuiltinFunction getter, PyBuiltinFunction.BuiltinFunction setter) {
        super(name, new PyBuiltinFunction(name, getter),
                setter == null ? null :
                        new PyBuiltinFunction(name, setter));
    }

    public BuiltinProperty(String name, PyBuiltinFunction.BuiltinFunction getter) {
        super(name, new PyBuiltinFunction(name, getter));
    }

    @Override
    public AbstractProperty boundToInstance(PyObject instance) {
        return this;
    }

    @Override
    public AbstractProperty getBoundProperty(PyObject getter, PyObject setter) {
        return this;
    }

    @Override
    public PyBuiltinFunction getGetter() {
        return (PyBuiltinFunction) super.getGetter();
    }

    @Override
    public PyBuiltinFunction getSetter() {
        return (PyBuiltinFunction) super.getSetter();
    }

    @Override
    public void setSetter(PyObject setter) {
        if (!(setter instanceof PyBuiltinFunction bf)) {
            throw new RuntimeException("Setter Must be an PyBuiltinFunction");
        }
        setSetter(bf);
    }

    public void setSetter(PyBuiltinFunction setter) {
        super.setSetter(setter);
    }

    public void setSetter(PyBuiltinFunction.BuiltinFunction bf) {
        setSetter(new PyBuiltinFunction(getName(), bf));
    }

    @Override
    public PyObject call(List<PyObject> arguments, Interpreter interpreter) {
        ArrayList<PyObject> args = new ArrayList<>();
        args.add(0, this);
        args.addAll(arguments);
        if (args.size() == 1) {
            PyObject oldValue = getCache();
            runListener(this, true, true, oldValue, oldValue);
            PyObject neoValue = getGetter().call(args, interpreter);
            setCache(neoValue);
            runListener(this, true, false, oldValue, neoValue);
            return neoValue;
        } else if (args.size() == 2) {
            if (isReadonly()) {
                throw new RuntimeException("Cannot set value on a read-only property");
            }
            PyObject oldValue = getCache();
            runListener(this, false, true, oldValue, oldValue);
            PyObject result = getSetter().call(args, interpreter);
            runListener(this, false, false, oldValue, getCache());
            return result;
        } else {
            throw new RuntimeException("Property requires 0 or 1 arguments, got " + arguments.size());
        }
    }
}

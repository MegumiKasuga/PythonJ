package edu.carole.runtime.property;

import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.PyFunction;
import edu.carole.runtime.PyNone;
import edu.carole.runtime.PyObject;

import java.util.List;
import java.util.Map;

public class PyProperty extends AbstractProperty {

    public PyProperty(PyFunction getter, PyFunction setter) {
        super(getter.getName(), getter, setter);
    }

    public PyProperty(PyFunction getter) {
        this(getter, null);
    }

    public PyFunction getGetter() {
        return (PyFunction) super.getGetter();
    }

    @Override
    public PyFunction getSetter() {
        return (PyFunction) super.getSetter();
    }

    public void setSetter(PyFunction setter) {
        super.setSetter(setter);
    }

    @Override
    public AbstractProperty getBoundProperty(PyObject getter, PyObject setter) {
        return new PyProperty((PyFunction) getter, (PyFunction) setter);
    }

    @Override
    public PyObject call(List<PyObject> arguments, Interpreter interpreter) {
        if (getBoundInstance() == null) {
            return this;
        }
        if (arguments.isEmpty()) {
            PyObject oldValue = getCache();
            runListener(this, true, true, oldValue, oldValue);
            PyObject result = getGetter().call(arguments, interpreter);
            runListener(this, true, false, oldValue, result);
            setCache(result);
            return result;
        } else if (arguments.size() == 1) {
            if (isReadonly()) {
                throw new RuntimeException("Cannot set value on a read-only property");
            }
            PyObject oldValue = getCache();
            runListener(this, false, true, oldValue, oldValue);
            PyObject result = getSetter().call(arguments, interpreter);
            runListener(this, false, false, oldValue, getCache());
            return result;
        } else {
            throw new RuntimeException("Property requires 0 or 1 arguments, got " + arguments.size());
        }
    }

}

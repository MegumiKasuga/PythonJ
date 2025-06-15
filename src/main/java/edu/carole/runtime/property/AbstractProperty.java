package edu.carole.runtime.property;

import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.PyFunction;
import edu.carole.runtime.PyNone;
import edu.carole.runtime.PyObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractProperty extends PyObject {

    private CachedValueUpdateListener listener = null;
    private final PyObject getter;

    private final String name;
    private PyObject setter;
    private PyObject boundInstance = null;
    private PyObject cache = PyNone.INSTANCE;

    public AbstractProperty(String name, PyObject getter, PyObject setter) {
        this.name = name;
        this.getter = getter;
        this.setter = setter;
    }

    public AbstractProperty(String name, PyObject getter) {
        this(name, getter, null);
    }

    public void setListener(CachedValueUpdateListener listener) {
        this.listener = listener;
    }

    public AbstractProperty boundToInstance(PyObject instance) {
        PyObject neoGetter, neoSetter;
        if (getter instanceof PyFunction pyFunc) {
            neoGetter = pyFunc.bindToInstance(instance);
        } else {
            neoGetter = getter;
        }
        if (setter instanceof PyFunction pyFunc) {
            neoSetter = pyFunc.bindToInstance(instance);
        } else {
            neoSetter = setter;
        }
        AbstractProperty prop = getBoundProperty(neoGetter, neoSetter);
        prop.boundInstance = instance;
        return prop;
    }

    public String getName() {
        return name;
    }

    public PyObject getBoundInstance() {
        return boundInstance;
    }

    public boolean isReadonly() {
        return setter == null;
    }

    public abstract AbstractProperty getBoundProperty(PyObject getter, PyObject setter);

    public PyObject getGetter() {
        return getter;
    }

    public PyObject getSetter() {
        return setter;
    }

    public void setSetter(PyObject setter) {
        this.setter = setter;
    }

    public PyObject getCache() {
        return cache;
    }

    public void setCache(PyObject cache) {
        this.cache = cache;
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

    public abstract PyObject call(List<PyObject> arguments, Interpreter interpreter);

    public PyObject call(List<PyObject> arguments, Map<String, PyObject> keywordArguments, Interpreter interpreter) {
        if (!keywordArguments.isEmpty()) {
            throw new RuntimeException("Property does not support keyword arguments");
        } else {
            return call(arguments, interpreter);
        }
    }

    protected void runListener(AbstractProperty self, boolean isGetterCalled, boolean isPre, PyObject oldValue, PyObject newValue) {
        if (listener == null) return;
        listener.onValueUpdated(self,
                isGetterCalled ? Method.GETTER : Method.SETTER,
                isPre ? Stage.PRE : Stage.POST,
                oldValue, newValue);
    }

    public interface CachedValueUpdateListener {

        void onValueUpdated(AbstractProperty self, Method method, Stage stage, PyObject oldValue, PyObject newValue);
    }

    public enum Stage {
        PRE, POST;

        @Override
        public String toString() {
            return switch (this) {
                case PRE -> "pre";
                case POST -> "post";
            };
        }
    }

    public enum Method {
        GETTER, SETTER;

        @Override
        public String toString() {
            return switch (this) {
                case GETTER -> "getter";
                case SETTER -> "setter";
            };
        }
    }
}

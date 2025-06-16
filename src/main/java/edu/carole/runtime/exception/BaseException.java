package edu.carole.runtime.exception;

import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.*;
import edu.carole.runtime.property.BuiltinProperty;
import edu.carole.runtime.property.PyProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class BaseException extends PyClass {

    private boolean suppressContext = false;
    private PyObject cause = PyNone.INSTANCE;
    private PyObject context = PyNone.INSTANCE;

    private final Consumer<PyInstance> instanceConsumer;
    private final Consumer<Map<String, PyObject>> methodCustomizer;

    public BaseException(String name) {
        super(name, new HashMap<>());
        initMethods(getMethods());
        initProperties(getClassAttributes());
        instanceConsumer = null;
        this.methodCustomizer = null;
    }

    public BaseException(String name, Consumer<Map<String, PyObject>> methodCustomizer,
                          Consumer<PyInstance> instanceCustomizer, PyClass... baseClasses) {
        super(name, new HashMap<>(), baseClasses == null ? new ArrayList<>() : new ArrayList<>(List.of(baseClasses)));
        this.instanceConsumer = instanceCustomizer;
        this.methodCustomizer = methodCustomizer;
    }

    public void initMethods(Map<String, PyObject> methods) {
        methods.put("add_note", new PyBuiltinFunction("add_note", (args, kwargs, inter) -> {
            if (args.size() != 2)
                throw new RuntimeException("must has one arg");
            PyObject instance = args.get(0);
            PyList list = (PyList) instance.getAttribute("__notes__");
            list.getElements().add(args.get(1));
            return PyNone.INSTANCE;
        }));
        methods.put("with_traceback", new PyBuiltinFunction("with_traceback", (args, kwargs, inter) -> {
            if (args.size() != 2)
                throw new RuntimeException("must has one arg");
            PyObject instance = args.get(0);
            PyInstance ins = (PyInstance) args.get(1);
            PyObject tb = ins.getAttribute("__traceback__");
            instance.setAttribute("__traceback__", tb);
            return instance;
        }));
        if (methodCustomizer != null) {
            methodCustomizer.accept(methods);
        }
    }

    public void initProperties(Map<String, PyObject> map) {
    }

    @Override
    public PyObject call(List<PyObject> arguments, Map<String, PyObject> keywordArguments, Interpreter interpreter) {
        final PyInstance instance = (PyInstance) super.call(arguments, keywordArguments, interpreter);
        instance.setAttribute("__cause__", PyNone.INSTANCE);
        instance.setAttribute("__context__", PyNone.INSTANCE);
        instance.setAttribute("__suppress_context__", PyBool.FALSE);
        instance.setAttribute("__traceback__", PyNone.INSTANCE);
        instance.setAttribute("__notes__", new PyList(new ArrayList<>()));
        if (instanceConsumer != null) {
            instanceConsumer.accept(instance);
        }
        return instance;
    }

    public void setCause(PyObject cause) {
        this.cause = cause;
    }

    public void setContext(PyObject context) {
        this.context = context;
    }

    public void setSuppressContext(boolean suppressContext) {
        this.suppressContext = suppressContext;
    }

    public PyObject getCause() {
        return cause;
    }

    public PyObject getContext() {
        return context;
    }

    public boolean isSuppressContext() {
        return suppressContext;
    }
}

package edu.carole.runtime.exception;

import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.*;
import edu.carole.runtime.property.BuiltinProperty;
import edu.carole.runtime.property.PyProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseException extends PyClass {

    private static final BaseException instance = new BaseException("BaseException");

    public static BaseException getInstance() {
        return instance;
    }

    private boolean suppressContext = false;
    private PyObject cause = PyNone.INSTANCE;
    private PyObject context = PyNone.INSTANCE;
    private BaseException(String name) {
        super(name, new HashMap<>());
        initProperties(getClassAttributes());
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
        instance.setAttribute("add_note", new PyBuiltinFunction("add_note", (args, kwargs, inter) -> {
            if (args.size() != 1)
                throw new RuntimeException("must has one arg");
            PyList list = (PyList) instance.getAttribute("__notes__");
            list.getElements().add(args.get(0));
            return PyNone.INSTANCE;
        }));
        instance.setAttribute("with_traceback", new PyBuiltinFunction("with_traceback", (args, kwargs, inter) -> {
            if (args.size() != 1)
                throw new RuntimeException("must has one arg");
            PyInstance ins = (PyInstance) args.get(0);
            PyObject tb = ins.getAttribute("__traceback__");
            instance.setAttribute("__traceback__", tb);
            return instance;
        }));
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

package edu.carole.runtime.exception;

import edu.carole.ast.ASTNode;
import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.*;
import edu.carole.runtime.instance.PyInstance;
import lombok.Getter;

import java.util.Map;
import java.util.function.Consumer;

public class ExceptionWrapper extends RuntimeException {

    @Getter
    private final PyInstance exception;
    public ExceptionWrapper(PyInstance exception) {
        this.exception = exception;
    }

    @Override
    public String getMessage() {
        Map<String, PyObject> attr = exception.getAttributes();
        if (!attr.containsKey("__notes__"))
            return super.getMessage();
        PyList notes = (PyList) attr.get("__notes__");
        if (notes.getElements().isEmpty()) return super.getMessage();
        return notes.getElements().get(notes.size() - 1).toString();
    }

    public void addTraceback(ASTNode node, PyObject... param) {
        Map<String, PyObject> attr = exception.getAttributes();
        if (!attr.containsKey("__traceback__")) {
            attr.put("__traceback__", new PyTraceback(node, param));
        } else if (!(attr.get("__traceback__") instanceof PyTraceback pyTb)) {
            attr.put("__traceback__", new PyTraceback(node, param));
        } else {
            PyTraceback neoPyTb = new PyTraceback(node, param);
            neoPyTb.setNext(pyTb);
            attr.put("__traceback__", neoPyTb);
        }
    }

    public void addNote(Interpreter interpreter, String note) {
        PyList notes = (PyList) exception.getAttribute(interpreter, "__notes__");
        notes.getElements().add(new PyString(note));
    }

    public void setCause(Interpreter interpreter, PyInstance cause) {
        if (!isException(interpreter, cause))
            throw new RuntimeException("cause must be an exception");
        exception.setAttribute(interpreter, "__cause__", cause);
        exception.setAttribute(interpreter, "__suppress_context__", PyBool.TRUE);
    }

    public static boolean isException(Interpreter interpreter, PyInstance instance) {
        return interpreter.getExceptions().isException(instance);
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return super.getStackTrace();
    }

    public static ExceptionWrapper getWrapper(RuntimeException e) {
        return (e instanceof ExceptionWrapper wrapper) ? wrapper : null;
    }

    public static RuntimeException consumeWrapper(RuntimeException e, Consumer<ExceptionWrapper> consumer) {
        ExceptionWrapper wrapper = getWrapper(e);
        if (wrapper == null) return e;
        consumer.accept(wrapper);
        return wrapper;
    }

    public static RuntimeException consumeWrapper(RuntimeException e, Consumer<ExceptionWrapper> onWrapper,
                                         Consumer<RuntimeException> onNotWrapper) {
        ExceptionWrapper wrapper = getWrapper(e);
        if (wrapper == null) {
            onNotWrapper.accept(e);
            return e;
        }
        onWrapper.accept(wrapper);
        return wrapper;
    }

}

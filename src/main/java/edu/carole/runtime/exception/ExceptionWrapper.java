package edu.carole.runtime.exception;

import edu.carole.ast.ASTNode;
import edu.carole.runtime.*;
import lombok.Getter;

import java.util.Map;

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

    public void setCause(PyInstance cause) {
        if (!isException(cause))
            throw new RuntimeException("cause must be an exception");
        exception.setAttribute("__cause__", cause);
        exception.setAttribute("__suppress_context__", PyBool.TRUE);
    }

    public static boolean isException(PyInstance instance) {
        return instance.getPyClass().getMRO().contains(BaseException.getInstance());
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return super.getStackTrace();
    }


}

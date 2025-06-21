package edu.carole.runtime.exception;

import edu.carole.ast.ASTNode;
import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.PyInt;
import edu.carole.runtime.PyNone;
import edu.carole.runtime.PyObject;
import edu.carole.runtime.property.BuiltinProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class PyTraceback extends PyObject {

    private final int line, column;
    private final String fileName;

    @Setter
    private PyTraceback next;

    private final PyFrame frame;

    private final HashMap<String, PyObject> attributes;

    public PyTraceback(ASTNode frame, PyObject... params) {
        this.line = frame.getLine();
        this.column = frame.getColumn();
        this.frame = new PyFrame(frame, params);
        next = null;
        this.attributes = new HashMap<>();
        addAttributes(attributes);
        this.fileName = frame.getFile();
    }

    private void addAttributes(Map<String, PyObject> attribute) {
        attribute.put("tb_frame", frame);
        attribute.put("tb_lineno", new PyInt(line));
        attribute.put("tb_lasti", PyNone.INSTANCE);
        attribute.put("tb_next", new BuiltinProperty("tb_next",
        (args, kwargs, inter) -> {
            return next == null ? PyNone.INSTANCE : next;
        }, (args, kwargs, inter) -> {
            if (args.size() != 1)
                throw new RuntimeException("must has one arg");
            if (!(args.get(0) instanceof PyTraceback pyTb))
                throw new RuntimeException("must be instance of traceback");
            this.setNext(pyTb);
            return PyNone.INSTANCE;
        }));
    }

    @Override
    public PyObject getAttribute(Interpreter interpreter, String name) {
        if (attributes.containsKey(name))
            return attributes.get(name);
        return super.getAttribute(interpreter, name);
    }

    @Override
    public String getTypeName() {
        return "traceback";
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public boolean isTruthy() {
        return false;
    }
}

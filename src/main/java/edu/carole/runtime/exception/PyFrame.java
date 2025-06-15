package edu.carole.runtime.exception;

import edu.carole.ast.ASTNode;
import edu.carole.runtime.PyObject;
import lombok.Getter;

@Getter
public class PyFrame extends PyObject {

    private final ASTNode node;
    private final PyObject[] params;
    public PyFrame(ASTNode node, PyObject... params) {
        this.node = node;
        this.params = params;
    }

    @Override
    public String getTypeName() {
        return "tb_frame";
    }

    @Override
    public String toString() {
        return "";
    }

    @Override
    public boolean isTruthy() {
        return true;
    }
}

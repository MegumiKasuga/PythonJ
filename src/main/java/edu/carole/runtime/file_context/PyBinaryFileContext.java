package edu.carole.runtime.file_context;

import edu.carole.runtime.PyObject;
import edu.carole.runtime.io.IOManager;

public class PyBinaryFileContext extends PyFileContext {
    public PyBinaryFileContext(String path, String mode) {
        super(path, mode);
    }

    public PyBinaryFileContext(IOManager manager, String path, String mode) {
        super(manager, path, mode);
    }

    // TODO: finish this context
    @Override
    public PyObject contextEnter() {
        return null;
    }

    @Override
    public PyObject contextExit(PyObject exceptionType, PyObject exceptionValue, PyObject traceback) {
        return null;
    }
}

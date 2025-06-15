package edu.carole.runtime.file_context;

import edu.carole.runtime.PyBool;
import edu.carole.runtime.PyBuiltinFunction;
import edu.carole.runtime.PyObject;
import edu.carole.runtime.PyString;
import edu.carole.runtime.property.BuiltinProperty;
import edu.carole.runtime.property.PyProperty;
import edu.carole.runtime.io.IOManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public abstract class PyFileContext extends PyObject {

    private boolean isOpen;
    private final String path, mode;
    private final IOManager manager;
    private final HashMap<String, PyObject> attributes;
    private int bufferSize = 32;


    public PyFileContext(String path, String mode) {
        this(IOManager.getInstance(), path, mode);
    }

    public PyFileContext(IOManager manager, String path, String mode) {
        isOpen = false;
        this.manager = manager;
        this.path = path;
        this.mode = mode;
        attributes = new HashMap<>();
        initAttributes(attributes);
    }

    public void setBufferSize(int bufferSize) {
        if (bufferSize < 0) bufferSize = 32;
        this.bufferSize = bufferSize;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void initAttributes(Map<String, PyObject> attributes) {
        attributes.put("is_open", new BuiltinProperty("is_open",
                (args, kwargs, inter) -> PyBool.fromValue(isOpen)
        ));
        attributes.put("mode", new BuiltinProperty("mode",
                (args, kwargs, inter) -> new PyString(mode)));
        attributes.put("filename", new BuiltinProperty("filename",
                (args, kwargs, inter) -> new PyString(path)));
        attributes.put("close", new PyBuiltinFunction("close",
                (args, kwargs, inter) ->
                this.contextExit(null, null, null)));
    }

    public abstract PyObject contextEnter();

    public abstract PyObject contextExit(PyObject exceptionType, PyObject exceptionValue, PyObject traceback);

    public HashMap<String, PyObject> getAttributes() {
        return attributes;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        this.isOpen = open;
    }

    @Override
    public PyObject getAttribute(String name) {
        if (!attributes.containsKey(name)) {
            return super.getAttribute(name);
        }
        return attributes.get(name);
    }

    public boolean readingMode() {
        return mode.contains("r");
    }

    public boolean writingMode() {
        return mode.contains("w");
    }

    public boolean appendMode() {
        return mode.contains("+") || mode.contains("a");
    }

    public boolean binaryMode() {
        return mode.contains("b");
    }

    public boolean textMode() {
        return !binaryMode();
    }

    @Override
    public String getTypeName() {
        return "file";
    }

    public IOManager getManager() {
        return manager;
    }

    public String getPath() {
        return path;
    }

    public String getMode() {
        return mode;
    }

    @Override
    public String toString() {
        return "<file '" + path + "' mode='" + mode + "' " + (isOpen ? "open" : "closed") + ">";
    }

    @Override
    public boolean isTruthy() {
        return isOpen;
    }

    public InputStream createInputStream() throws IOException {
        return manager.createInputStream(path, mode);
    }

    public OutputStream createOutputStream() throws IOException {
        return manager.createOutputStream(path, mode);
    }


    public static boolean supportsMode(String mode) {
        if (mode == null) {
            return false;
        }

        // 支持的模式：r, w, a (及其变体)
        return mode.matches("^[rwa][bt]?\\+?$");
    }
}

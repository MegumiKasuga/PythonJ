package edu.carole.runtime.file_context;

import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.*;
import edu.carole.runtime.func.PyBuiltinFunction;
import edu.carole.runtime.io.IOManager;
import edu.carole.runtime.property.BuiltinProperty;

import java.io.*;
import java.util.Map;

public class PyBinaryFileContext extends PyFileContext {
    private OutputStream outStream = null;
    private ByteArrayInputStream inStream = null;

    public PyBinaryFileContext(IOManager manager, String path, String mode) {
        super(manager, path, mode);
    }

    @Override
    public void initAttributes(Map<String, PyObject> attributes) {
        super.initAttributes(attributes);
        attributes.put("read", new PyBuiltinFunction("read", (args, kwargs, inter) -> {
            if (args.size() > 1) {
                throw new RuntimeException("read() takes at most 1 argument (" + args.size() + " given)");
            }
            return read(args.isEmpty() ? null : args.get(0));
        }));
        attributes.put("write", new PyBuiltinFunction("write", (args, kwargs, inter) -> {
            if (args.size() != 1) {
                throw new RuntimeException("write() takes exactly one argument (" + args.size() + " given)");
            }
            return write(args.get(0));
        }));
        attributes.put("flush", new PyBuiltinFunction("flush", (args, kwargs, inter) -> {
            if (args.size() != 0) {
                throw new RuntimeException("flush() takes no arguments (" + args.size() + " given)");
            }
            return flush();
        }));
        attributes.put("skip", new PyBuiltinFunction("skip", (args, kwargs, inter) -> {
            if (args.size() != 1) {
                throw new RuntimeException("write() takes exactly one argument (" + args.size() + " given)");
            }
            return skip(args.get(0));
        }));
        attributes.put("available", new BuiltinProperty("available", (args, kwargs, inter) -> {
            if (!isOpen() || !readingMode()) return new PyInt(0);
            if (inStream == null) return new PyInt(0);
            return new PyInt(inStream.available());
        }));
        attributes.put("__iter__", new PyBuiltinFunction("__iter__", (args, kwargs, inter) -> {
            return PyNone.INSTANCE;
        }));
        attributes.put("__next__", new PyBuiltinFunction("__next__", (args, kwargs, inter) -> {
            try {
                return readOneByte();
            } catch (RuntimeException e) {
                if (e.getMessage().equals("EOF"))
                    throw new RuntimeException("StopIteration");
                throw e;
            }
        }));
    }

    // TODO: finish this context
    @Override
    public PyObject contextEnter(Interpreter interpreter) {
        try {
            setOpen(true);
            if (readingMode()) {
                InputStream stream = createInputStream();
                inStream = new ByteArrayInputStream(stream.readAllBytes());
                stream.close();
            } else if (writingMode() || appendMode()) {
                outStream = createOutputStream();
            } else {
                throw new RuntimeException("Unsupported file mode: " + getMode());
            }
        } catch (IOException e) {
            setOpen(false);
            throw new RuntimeException("Cannot open file '" + getPath() + "': " + e.getMessage());
        }
        return this;
    }

    @Override
    public PyObject contextExit(Interpreter inter) {
        try {
            if (isOpen()) {
                setOpen(false);
                if (inStream != null) {
                    inStream.close();
                }
                if (outStream != null) {
                    outStream.close();
                }
            }
        } catch (IOException ignored) {}
        return PyBool.FALSE;
    }

    public long getCount(PyObject size) {
        long count = 1;
        if (size instanceof PyInt || size instanceof PyFloat) {
            if (size instanceof PyInt integer) {
                count = Math.max(count, integer.getValue());
            } else {
                PyFloat f = (PyFloat) size;
                count = Math.max(count, Math.round(f.getValue()));
            }
        }
        return Math.min(count, inStream.available());
    }

    public PyObject read(PyObject size) {
        if (!isOpen()) {
            throw new RuntimeException("I/O operation on closed file.");
        }
        if (!readingMode()) {
            throw new RuntimeException("File not open for reading");
        }
        if (inStream.available() < 1) {
            throw new RuntimeException("EOF");
        }
        if (size == null) {
            byte[] data = inStream.readAllBytes();
            return new PyBytes(data);
        }
        long count = getCount(size);
        try {
            byte[] bytes = inStream.readNBytes((int) count);
            return new PyBytes(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Error reading file: " + e.getMessage());
        }
    }

    public PyObject read() {
        return read(null);
    }

    public PyObject readOneByte() {
        if (!isOpen()) {
            throw new RuntimeException("I/O operation on closed file.");
        }
        if (!readingMode()) {
            throw new RuntimeException("File not open for reading");
        }
        if (inStream.available() < 1) {
            throw new RuntimeException("EOF");
        }
        try {
            byte[] data = inStream.readNBytes(1);
            return new PyBytes(data);
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + e.getMessage());
        }
    }

    public PyObject skip(PyObject size) {
        if (!isOpen()) {
            throw new RuntimeException("I/O operation on closed file.");
        }
        if (!readingMode()) {
            throw new RuntimeException("File not open for reading");
        }
        if (inStream.available() < 1) {
            throw new RuntimeException("EOF");
        }
        if (size == null) {
            byte[] data = inStream.readAllBytes();
            return new PyBytes(data);
        }
        long count = getCount(size);
        return new PyInt(inStream.skip(count));
    }

    public PyObject write(PyObject bytes) {
        if (!isOpen()) {
            throw new RuntimeException("I/O operation on closed file");
        }
        if (!writingMode() && !appendMode()) {
            throw new RuntimeException("File not open for writing");
        }
        byte[] data;
        if (bytes instanceof PyBytes b) {
            data = b.getValue();
        } else if (bytes instanceof PyByteArray b) {
            data = b.getValue();
        } else if (bytes instanceof PyInt integer) {
            data = longToByteArray(integer.getValue());
        } else {
            throw new Interpreter.PyExceptionWrapper(
                    PyException.typeError("data must be an instance of bytes, bytearray, int")
            );
        }
        try {
            outStream.write(data);
            return new PyInt(data.length);
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file: " + e.getMessage());
        }
    }

    public PyObject flush() {
        if (!isOpen()) {
            throw new RuntimeException("I/O operation on closed file");
        }
        try {
            if (outStream != null) {
                outStream.flush();
            }
            return PyNone.INSTANCE;
        } catch (IOException e) {
            throw new RuntimeException("Error flushing file: " + e.getMessage());
        }
    }

    public static byte[] intToByteArray(int b) {
        byte[] result = new byte[4];
        result[0] = (byte) ((b >> 24) & 0xFF);
        result[1] = (byte) ((b >> 16) & 0xFF);
        result[2] = (byte) ((b >> 8) & 0xFF);
        result[3] = (byte) (b & 0xFF);
        return result;
    }

    public static byte[] longToByteArray(long b) {
        byte[] result = new byte[8];
        result[0] = (byte) ((b >> 56) & 0xFF);
        result[1] = (byte) ((b >> 48) & 0xFF);
        result[2] = (byte) ((b >> 40) & 0xFF);
        result[3] = (byte) ((b >> 32) & 0xFF);
        result[4] = (byte) ((b >> 24) & 0xFF);
        result[5] = (byte) ((b >> 16) & 0xFF);
        result[6] = (byte) ((b >> 8) & 0xFF);
        result[7] = (byte) (b & 0xFF);
        return result;
    }
}

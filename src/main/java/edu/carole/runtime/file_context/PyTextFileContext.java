package edu.carole.runtime.file_context;

import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.*;
import edu.carole.runtime.io.IOManager;
import edu.carole.runtime.property.BuiltinProperty;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * File context manager that supports real file I/O operations
 * Implements the context manager protocol for Python 'with' statements
 * Now uses the pluggable IOManager for flexible I/O handling
 */
public class PyTextFileContext extends PyFileContext {
    private final String charSet;
    private BufferedReader reader;
    private BufferedWriter writer;
    private final List<String> content; // For storing file content when reading
    private final IOManager ioManager; // Use IOManager for flexible I/O
    private Charset charSetCache = null;
    private static final PyInt ONE = new PyInt(1);
    
    public PyTextFileContext(String filename) {
        this(filename, "r", "utf-8"); // Default to read mode
    }

    public PyTextFileContext(String filename, String mode) {
        this(filename, mode, "utf-8");
    }
    
    public PyTextFileContext(String filename, String mode, String charSet) {
        this(filename, mode, IOManager.getInstance(), charSet);
    }
    
    public PyTextFileContext(String filename, String mode, IOManager ioManager, String charSet) {
        super(filename, mode);
        this.charSet = charSet;
        this.content = new ArrayList<>();
        this.ioManager = ioManager != null ? ioManager : IOManager.getInstance();
    }

    @Override
    public void setBufferSize(int bufferSize) {
        if (bufferSize == 0) bufferSize = 1;
        super.setBufferSize(bufferSize);
    }

    public Charset getCharSet() {
        if (charSetCache != null) {
            return charSetCache;
        } else {
            try {
                charSetCache = Charset.forName(charSet);
            } catch (UnsupportedCharsetException unsupported) {
                PyException pyException = PyException.typeError("Unsupported charset '" + charSet + "'");
                contextExit(pyException, new PyString(charSet), null);
                throw new Interpreter.PyExceptionWrapper(
                    pyException
                );
            }
            return charSetCache;
        }
    }

    @Override
    public void initAttributes(Map<String, PyObject> attributes) {
        super.initAttributes(attributes);
        attributes.put("charset", new BuiltinProperty("charset", (args, kwargs) -> new PyString(charSet)));
        attributes.put("read", new PyBuiltinFunction("read", (args, kwargs) -> {
            if (args.size() == 0) {
                return read();
            } else if (args.size() == 1) {
                return read(args.get(0));
            } else {
                throw new RuntimeException("read() takes at most 1 argument (" + args.size() + " given)");
            }
        }));
        attributes.put("readline", new PyBuiltinFunction("readline", (args, kwargs) -> {
            if (args.size() != 0) {
                throw new RuntimeException("readline() takes no arguments (" + args.size() + " given)");
            }
            return readline();
        }));
        attributes.put("readlines", new PyBuiltinFunction("readlines", (args, kwargs) -> {
            if (args.size() != 0) {
                throw new RuntimeException("readlines() takes no arguments (" + args.size() + " given)");
            }
            return readlines();
        }));
        attributes.put("write", new PyBuiltinFunction("write", (args, kwargs) -> {
            if (args.size() != 1) {
                throw new RuntimeException("write() takes exactly one argument (" + args.size() + " given)");
            }
            return write(args.get(0));
        }));
        attributes.put("writelines", new PyBuiltinFunction("writelines", (args, kwargs) -> {
            if (args.size() != 1) {
                throw new RuntimeException("writelines() takes exactly one argument (" + args.size() + " given)");
            }
            return writelines(args.get(0));
        }));
        attributes.put("flush", new PyBuiltinFunction("flush", (args, kwargs) -> {
            if (args.size() != 0) {
                throw new RuntimeException("flush() takes no arguments (" + args.size() + " given)");
            }
            return flush();
        }));
    }



    @Override
    public PyObject contextEnter() {
        try {
            setOpen(true);
            
            if (readingMode()) {
                // Read mode - open file for reading using IOManager
                InputStream inputStream = createInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream, getCharSet()), getBufferSize());
//                System.out.println("Opening file for reading: " + filename);
                
                // Pre-read content for Python-like file operations
                String line;
                while ((line = reader.readLine()) != null) {
                    content.add(line);
                }
                reader.close();
                
                // Reopen for reading operations
                inputStream = createInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream, getCharSet()), getBufferSize());
                
            } else if (writingMode() || appendMode()) {
                // Write mode - open file for writing (truncate) using IOManager
                OutputStream outputStream = createOutputStream();
                writer = new BufferedWriter(new OutputStreamWriter(outputStream, getCharSet()), getBufferSize());
//                System.out.println("Opening file for writing: " + filename);
            } else {
                throw new RuntimeException("Unsupported file mode: " + getMode());
            }
            
            return this; // Return self as the context value
            
        } catch (IOException e) {
            setOpen(false);
            throw new RuntimeException("Cannot open file '" + getPath() + "': " + e.getMessage());
        }
    }

    @Override
    public PyObject contextExit(PyObject exceptionType, PyObject exceptionValue, PyObject traceback) {
        try {
            if (isOpen()) {
                setOpen(false);
                
                // Close any open streams
                if (reader != null) {
                    reader.close();
                    reader = null;
//                    System.out.println("Closing file (read mode): " + filename);
                }
                
                if (writer != null) {
                    writer.flush(); // Ensure all data is written
                    writer.close();
                    writer = null;
//                    System.out.println("Closing file (write mode): " + filename);
                }
            }
        } catch (IOException e) {
//            System.err.println("Error closing file '" + filename + "': " + e.getMessage());
        }
        
        // Don't suppress any exceptions - let them propagate
        return PyBool.FALSE;
    }
    
    // File reading methods
    public PyObject read() {
        return read(null);
    }
    
    public PyObject read(PyObject size) {
        if (!isOpen()) {
            throw new RuntimeException("I/O operation on closed file");
        }
        
        if (!readingMode()) {
            throw new RuntimeException("File not open for reading");
        }
        
        try {
            if (size == null) {
                // Read entire file
                StringBuilder sb = new StringBuilder();
                for (String line : content) {
                    sb.append(line).append("\n");
                }
                return new PyString(sb.toString());
            } else {
                // Read specified number of characters
                if (!(size instanceof PyInt)) {
                    throw new RuntimeException("read() argument must be an integer");
                }
                
                int numChars = (int) ((PyInt) size).getValue();
                StringBuilder sb = new StringBuilder();
                int totalChars = 0;
                
                for (String line : content) {
                    if (totalChars + line.length() + 1 <= numChars) {
                        sb.append(line).append("\n");
                        totalChars += line.length() + 1;
                    } else {
                        sb.append(line, 0, numChars - totalChars);
                        break;
                    }
                }
                
                return new PyString(sb.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading file: " + e.getMessage());
        }
    }
    
    public PyObject readline() {
        if (!isOpen()) {
            throw new RuntimeException("I/O operation on closed file");
        }
        
        if (!readingMode()) {
            throw new RuntimeException("File not open for reading");
        }
        
        try {
            if (reader != null) {
                String line = reader.readLine();
                return new PyString(line != null ? line + "\n" : "");
            } else {
                return new PyString("");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading line: " + e.getMessage());
        }
    }
    
    public PyObject readlines() {
        if (!isOpen()) {
            throw new RuntimeException("I/O operation on closed file");
        }
        
        if (!readingMode()) {
            throw new RuntimeException("File not open for reading");
        }
        
        List<PyObject> lines = new ArrayList<>();
        for (String line : content) {
            lines.add(new PyString(line + "\n"));
        }
        
        return new PyList(lines);
    }
    
    // File writing methods
    public PyObject write(PyObject data) {
        if (!isOpen()) {
            throw new RuntimeException("I/O operation on closed file");
        }
        
        if (!writingMode() && !appendMode()) {
            throw new RuntimeException("File not open for writing");
        }

        if (!(data instanceof PyString)) {
            throw new RuntimeException("write() argument must be a string");
        }
        try {
            String text = ((PyString) data).getValue();
            writer.write(text);
            return new PyInt(text.length());
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file: " + e.getMessage());
        }
    }
    
    public PyObject writelines(PyObject lines) {
        if (!isOpen()) {
            throw new RuntimeException("I/O operation on closed file");
        }
        
        if (!writingMode() && !appendMode()) {
            throw new RuntimeException("File not open for writing");
        }
        
        if (!(lines instanceof PyList)) {
            throw new RuntimeException("writelines() argument must be a list");
        }
        
        try {
            List<PyObject> lineList = ((PyList) lines).getElements();
            for (PyObject line : lineList) {
                if (!(line instanceof PyString)) {
                    throw new RuntimeException("writelines() list items must be strings");
                }
                writer.write(((PyString) line).getValue());
            }
            return PyNone.INSTANCE;
        } catch (IOException e) {
            throw new RuntimeException("Error writing lines to file: " + e.getMessage());
        }
    }
    
    public PyObject flush() {
        if (!isOpen()) {
            throw new RuntimeException("I/O operation on closed file");
        }
        
        try {
            if (writer != null) {
                writer.flush();
            }
            return PyNone.INSTANCE;
        } catch (IOException e) {
            throw new RuntimeException("Error flushing file: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "<file '" + getPath() + "' mode='" + getMode() + "' charset='" + charSet + "' " + (isOpen() ? "open" : "closed") + ">";
    }
}

package edu.carole.runtime;

import edu.carole.runtime.io.IOManager;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

/**
 * File context manager that supports real file I/O operations
 * Implements the context manager protocol for Python 'with' statements
 * Now uses the pluggable IOManager for flexible I/O handling
 */
public class PyFileContext extends PyObject {
    private final String filename;
    private final String mode;
    private boolean isOpen;
    private BufferedReader reader;
    private BufferedWriter writer;
    private final List<String> content; // For storing file content when reading
    private final IOManager ioManager; // Use IOManager for flexible I/O
    
    public PyFileContext(String filename) {
        this(filename, "r"); // Default to read mode
    }
    
    public PyFileContext(String filename, String mode) {
        this(filename, mode, IOManager.getInstance());
    }
    
    public PyFileContext(String filename, String mode, IOManager ioManager) {
        this.filename = filename;
        this.mode = mode != null ? mode : "r";
        this.isOpen = false;
        this.content = new ArrayList<>();
        this.ioManager = ioManager != null ? ioManager : IOManager.getInstance();
    }
      @Override
    public String getTypeName() {
        return "file";
    }
      @Override
    public PyObject contextEnter() {
        try {
            this.isOpen = true;
            
            if (mode.startsWith("r")) {
                // Read mode - open file for reading using IOManager
                InputStream inputStream = ioManager.createInputStream(filename, mode);
                reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                System.out.println("Opening file for reading: " + filename);
                
                // Pre-read content for Python-like file operations
                String line;
                while ((line = reader.readLine()) != null) {
                    content.add(line);
                }
                reader.close();
                
                // Reopen for reading operations
                inputStream = ioManager.createInputStream(filename, mode);
                reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                
            } else if (mode.startsWith("w")) {
                // Write mode - open file for writing (truncate) using IOManager
                OutputStream outputStream = ioManager.createOutputStream(filename, mode);
                writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                System.out.println("Opening file for writing: " + filename);
                
            } else if (mode.startsWith("a")) {
                // Append mode - open file for appending using IOManager
                OutputStream outputStream = ioManager.createOutputStream(filename, mode);
                writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                System.out.println("Opening file for appending: " + filename);
                
            } else {
                throw new RuntimeException("Unsupported file mode: " + mode);
            }
            
            return this; // Return self as the context value
            
        } catch (IOException e) {
            this.isOpen = false;
            throw new RuntimeException("Cannot open file '" + filename + "': " + e.getMessage());
        }
    }
      @Override
    public PyObject contextExit(PyObject exceptionType, PyObject exceptionValue, PyObject traceback) {
        try {
            if (this.isOpen) {
                this.isOpen = false;
                
                // Close any open streams
                if (reader != null) {
                    reader.close();
                    reader = null;
                    System.out.println("Closing file (read mode): " + filename);
                }
                
                if (writer != null) {
                    writer.flush(); // Ensure all data is written
                    writer.close();
                    writer = null;
                    System.out.println("Closing file (write mode): " + filename);
                }
            }
        } catch (IOException e) {
            System.err.println("Error closing file '" + filename + "': " + e.getMessage());
        }
        
        // Don't suppress any exceptions - let them propagate
        return PyBool.FALSE;
    }
      @Override
    public String toString() {
        return "<file '" + filename + "' mode='" + mode + "' " + (isOpen ? "open" : "closed") + ">";
    }
    
    // File reading methods
    public PyObject read() {
        return read(null);
    }
    
    public PyObject read(PyObject size) {
        if (!isOpen) {
            throw new RuntimeException("I/O operation on closed file");
        }
        
        if (!mode.startsWith("r")) {
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
        if (!isOpen) {
            throw new RuntimeException("I/O operation on closed file");
        }
        
        if (!mode.startsWith("r")) {
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
        if (!isOpen) {
            throw new RuntimeException("I/O operation on closed file");
        }
        
        if (!mode.startsWith("r")) {
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
        if (!isOpen) {
            throw new RuntimeException("I/O operation on closed file");
        }
        
        if (!mode.startsWith("w") && !mode.startsWith("a")) {
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
        if (!isOpen) {
            throw new RuntimeException("I/O operation on closed file");
        }
        
        if (!mode.startsWith("w") && !mode.startsWith("a")) {
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
        if (!isOpen) {
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
      // Add a method to check if file is open
    @Override
    public PyObject getAttribute(String name) {
        switch (name) {
            case "is_open":
                return PyBool.valueOf(isOpen);
            case "filename":
                return new PyString(filename);
            case "mode":
                return new PyString(mode);
            case "read":
                return new PyBuiltinFunction("read", args -> {
                    if (args.size() == 0) {
                        return read();
                    } else if (args.size() == 1) {
                        return read(args.get(0));
                    } else {
                        throw new RuntimeException("read() takes at most 1 argument (" + args.size() + " given)");
                    }
                });
            case "readline":
                return new PyBuiltinFunction("readline", args -> {
                    if (args.size() != 0) {
                        throw new RuntimeException("readline() takes no arguments (" + args.size() + " given)");
                    }
                    return readline();
                });
            case "readlines":
                return new PyBuiltinFunction("readlines", args -> {
                    if (args.size() != 0) {
                        throw new RuntimeException("readlines() takes no arguments (" + args.size() + " given)");
                    }
                    return readlines();
                });
            case "write":
                return new PyBuiltinFunction("write", args -> {
                    if (args.size() != 1) {
                        throw new RuntimeException("write() takes exactly one argument (" + args.size() + " given)");
                    }
                    return write(args.get(0));
                });
            case "writelines":
                return new PyBuiltinFunction("writelines", args -> {
                    if (args.size() != 1) {
                        throw new RuntimeException("writelines() takes exactly one argument (" + args.size() + " given)");
                    }
                    return writelines(args.get(0));
                });
            case "flush":
                return new PyBuiltinFunction("flush", args -> {
                    if (args.size() != 0) {
                        throw new RuntimeException("flush() takes no arguments (" + args.size() + " given)");
                    }
                    return flush();
                });
            default:
                return super.getAttribute(name);
        }    }
    
    @Override
    public boolean isTruthy() {
        return isOpen; // File context is truthy when the file is open
    }
}

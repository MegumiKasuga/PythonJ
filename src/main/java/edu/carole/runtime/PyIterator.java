package edu.carole.runtime;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Python迭代器对象，包装Java Iterator
 */
public class PyIterator extends PyObject implements Iterator<PyObject> {
    private final Iterator<PyObject> iterator;
    private PyObject pyObject;
    private final String sourceTypeName;
    private boolean finished = false;
    
    public PyIterator(Iterator<PyObject> iterator, String sourceTypeName) {
        this.iterator = iterator;
        this.pyObject = null; // PyObject is not used in this context
        this.sourceTypeName = sourceTypeName;
    }

    public PyIterator(PyObject pyObject, String sourceTypeName) {
        this.iterator = null;
        runIterMethod(pyObject);
        this.pyObject = pyObject;
        this.sourceTypeName = sourceTypeName;
    }

    private PyObject runIterMethod(PyObject obj) {
        PyObject iterMethod;
        try {
            iterMethod = obj.getAttribute("__iter__");
        } catch (RuntimeException e) {
            throw new RuntimeException("Object does not have an __iter__ method");
        }
        return iterMethod.call(List.of()); // Call the __iter__ method to initialize the iterator
    }
    
    @Override
    public String getTypeName() { 
        return sourceTypeName + "_iterator"; 
    }
    
    @Override
    public String toString() { 
        return "<" + sourceTypeName + "_iterator object>"; 
    }
    
    @Override
    public boolean isTruthy() {
        return true; 
    }
    
    @Override
    public PyObject getAttribute(String name) {
        return switch (name) {
            case "__iter__" -> new PyBuiltinFunction("__iter__", args -> {
                if (!args.isEmpty()) {
                    throw new RuntimeException("__iter__() takes no arguments (" + args.size() + " given)");
                }
                return this; // Iterators return themselves
            });
            case "__next__" -> new PyBuiltinFunction("__next__", args -> {
                if (!args.isEmpty()) {
                    throw new RuntimeException("__next__() takes no arguments (" + args.size() + " given)");
                }
                if (iterator == null) {
                    PyObject nextMethod;
                    try {
                        nextMethod = pyObject.getAttribute("__next__");
                    } catch (RuntimeException e) {
                        throw new RuntimeException("Iterator object does not have a __next__ method");
                    }
                    try {
                        return nextMethod.call(List.of()); // Call the __next__ method on the PyObject
                    } catch (RuntimeException e) {
                        throw new RuntimeException("StopIteration");
                    }
                } else {
                    try {
                        if (iterator.hasNext()) {
                            return iterator.next();
                        } else {
                            throw new RuntimeException("StopIteration");
                        }
                    } catch (NoSuchElementException e) {
                        throw new RuntimeException("StopIteration");
                    }
                }
            });
            default -> super.getAttribute(name);
        };
    }
    
    /**
     * Java iterator interface for compatibility
     */
    @Override
    public Iterator<PyObject> iterator() {
        if (iterator == null) {
            return this;
        }
        return iterator;
    }
    
    /**
     * Check if iterator has more elements
     */
    public boolean hasNext() {
        if (iterator != null) {
            return iterator.hasNext();
        }
        return !finished;
    }
    
    /**
     * Get next element from iterator
     */
    public PyObject next() {
        try {
            if (iterator != null) {
                if (hasNext()) {
                    return iterator.next();
                } else {
                    throw new RuntimeException("StopIteration");
                }
            } else {
                PyObject nextMethod = pyObject.getAttribute("__next__");
                try {
                    return nextMethod.call(List.of()); // Call the __next__ method on the PyObject
                } catch (RuntimeException e) {
                    if (e.getMessage().equals("StopIteration")) {
                        finished = true; // Mark as finished if StopIteration is raised
                        throw e;
                    } else {
                        throw e; // Rethrow other exceptions
                    }
                }
            }
        } catch(NoSuchElementException e){
                throw new RuntimeException("StopIteration");
        }
    }
}

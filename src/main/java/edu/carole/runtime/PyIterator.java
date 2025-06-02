package edu.carole.runtime;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Python迭代器对象，包装Java Iterator
 */
public class PyIterator extends PyObject {
    private final Iterator<PyObject> iterator;
    private final String sourceTypeName;
    
    public PyIterator(Iterator<PyObject> iterator, String sourceTypeName) {
        this.iterator = iterator;
        this.sourceTypeName = sourceTypeName;
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
        switch (name) {
            case "__iter__":
                return new PyBuiltinFunction("__iter__", args -> {
                    if (args.size() != 0) {
                        throw new RuntimeException("__iter__() takes no arguments (" + args.size() + " given)");
                    }
                    return this; // Iterators return themselves
                });
                
            case "__next__":
                return new PyBuiltinFunction("__next__", args -> {
                    if (args.size() != 0) {
                        throw new RuntimeException("__next__() takes no arguments (" + args.size() + " given)");
                    }
                    try {
                        if (iterator.hasNext()) {
                            return iterator.next();
                        } else {
                            throw new RuntimeException("StopIteration");
                        }
                    } catch (NoSuchElementException e) {
                        throw new RuntimeException("StopIteration");
                    }
                });
                
            default:
                return super.getAttribute(name);
        }
    }
    
    /**
     * Java iterator interface for compatibility
     */
    @Override
    public Iterator<PyObject> iterator() {
        return iterator;
    }
    
    /**
     * Check if iterator has more elements
     */
    public boolean hasNext() {
        return iterator.hasNext();
    }
    
    /**
     * Get next element from iterator
     */
    public PyObject next() {
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
}

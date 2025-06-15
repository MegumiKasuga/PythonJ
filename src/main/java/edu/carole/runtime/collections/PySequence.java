package edu.carole.runtime.collections;

import edu.carole.runtime.PyObject;
import edu.carole.runtime.PyBuiltinFunction;

/**
 * Python Sequence abstract base class
 * Corresponds to collections.abc.Sequence in Python
 */
public abstract class PySequence extends PyCollection {
    
    /**
     * Abstract method to get an item by index
     * @param key The index
     * @return The item at the given index
     */
    public abstract PyObject getItem(PyObject key);
    
    /**
     * Abstract method to get the count of occurrences of an item
     * @param item The item to count
     * @return Number of occurrences
     */
    public abstract PyObject count(PyObject item);
    
    /**
     * Abstract method to get the index of an item
     * @param item The item to find
     * @return Index of the item
     */
    public abstract PyObject index(PyObject item);
    
    /**
     * Abstract method to get the index of an item with start and end bounds
     * @param item The item to find
     * @param start Start index
     * @param end End index
     * @return Index of the item
     */
    public abstract PyObject index(PyObject item, PyObject start, PyObject end);
    
    @Override
    public PyObject getAttribute(String name) {
        switch (name) {
            case "__getitem__":
                return new PyBuiltinFunction("__getitem__", (args, kwargs, inter) -> {
                    if (args.size() != 1) {
                        throw new RuntimeException("__getitem__() takes exactly 1 argument (" + args.size() + " given)");
                    }
                    return this.getItem(args.get(0));
                });
                
            case "count":
                return new PyBuiltinFunction("count", (args, kwargs, inter) -> {
                    if (args.size() != 1) {
                        throw new RuntimeException("count() takes exactly one argument (" + args.size() + " given)");
                    }
                    return this.count(args.get(0));
                });
                
            case "index":
                return new PyBuiltinFunction("index", (args, kwargs, inter) -> {
                    if (args.size() < 1 || args.size() > 3) {
                        throw new RuntimeException("index() takes from 1 to 3 positional arguments but " + args.size() + " were given");
                    }
                    if (args.size() == 1) {
                        return this.index(args.get(0));
                    } else if (args.size() == 2) {
                        return this.index(args.get(0), args.get(1), null);
                    } else {
                        return this.index(args.get(0), args.get(1), args.get(2));
                    }
                });
                
            default:
                return super.getAttribute(name);
        }
    }
    
    @Override
    public boolean isInstanceOf(String typeName) {
        switch (typeName) {
            case "Sequence":
            case "collections.abc.Sequence":
                return true;
            default:
                return super.isInstanceOf(typeName);
        }
    }
}

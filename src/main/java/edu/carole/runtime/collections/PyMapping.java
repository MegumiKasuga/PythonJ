package edu.carole.runtime.collections;

import edu.carole.runtime.PyObject;
import edu.carole.runtime.PyBuiltinFunction;
import edu.carole.runtime.PyIterator;
import java.util.Iterator;
import java.util.Set;

/**
 * Python Mapping abstract base class
 * Corresponds to collections.abc.Mapping in Python
 */
public abstract class PyMapping extends PyCollection {
    
    /**
     * Abstract method to get an item by key
     * @param key The key
     * @return The value associated with the key
     */
    public abstract PyObject getItem(PyObject key);
    
    /**
     * Abstract method to get all keys
     * @return An iterable of keys
     */
    public abstract PyObject keys();
    
    /**
     * Abstract method to get all values
     * @return An iterable of values
     */
    public abstract PyObject values();
    
    /**
     * Abstract method to get all items as key-value pairs
     * @return An iterable of (key, value) tuples
     */
    public abstract PyObject items();
    
    /**
     * Abstract method to get a value with a default
     * @param key The key
     * @param defaultValue The default value if key not found
     * @return The value or default
     */
    public abstract PyObject get(PyObject key, PyObject defaultValue);
    
    @Override
    public PyObject getAttribute(String name) {
        switch (name) {
            case "__getitem__":
                return new PyBuiltinFunction("__getitem__", (args, kwargs) -> {
                    if (args.size() != 1) {
                        throw new RuntimeException("__getitem__() takes exactly 1 argument (" + args.size() + " given)");
                    }
                    return this.getItem(args.get(0));
                });
                
            case "keys":
                return new PyBuiltinFunction("keys", (args, kwargs) -> {
                    if (args.size() != 0) {
                        throw new RuntimeException("keys() takes no arguments (" + args.size() + " given)");
                    }
                    return this.keys();
                });
                
            case "values":
                return new PyBuiltinFunction("values", (args, kwargs) -> {
                    if (args.size() != 0) {
                        throw new RuntimeException("values() takes no arguments (" + args.size() + " given)");
                    }
                    return this.values();
                });
                
            case "items":
                return new PyBuiltinFunction("items", (args, kwargs) -> {
                    if (args.size() != 0) {
                        throw new RuntimeException("items() takes no arguments (" + args.size() + " given)");
                    }
                    return this.items();
                });
                
            case "get":
                return new PyBuiltinFunction("get", (args, kwargs) -> {
                    if (args.size() < 1 || args.size() > 2) {
                        throw new RuntimeException("get() takes from 1 to 2 arguments but " + args.size() + " were given");
                    }
                    PyObject defaultValue = args.size() == 2 ? args.get(1) : null;
                    return this.get(args.get(0), defaultValue);
                });
                
            default:
                return super.getAttribute(name);
        }
    }
    
    @Override
    public boolean isInstanceOf(String typeName) {
        switch (typeName) {
            case "Mapping":
            case "collections.abc.Mapping":
                return true;
            default:
                return super.isInstanceOf(typeName);
        }
    }
}

package edu.carole.runtime.collections;

import edu.carole.runtime.PyObject;
import edu.carole.runtime.PyBuiltinFunction;
import edu.carole.runtime.PyNone;

/**
 * Python MutableMapping abstract base class
 * Corresponds to collections.abc.MutableMapping in Python
 */
public abstract class PyMutableMapping extends PyMapping {
    
    /**
     * Abstract method to set an item by key
     * @param key The key
     * @param value The value to set
     */
    public abstract void setItem(PyObject key, PyObject value);
    
    /**
     * Abstract method to delete an item by key
     * @param key The key
     */
    public abstract void delItem(PyObject key);
    
    /**
     * Abstract method to clear all items
     */
    public abstract void clear();
    
    /**
     * Abstract method to pop an item by key
     * @param key The key
     * @param defaultValue Default value if key not found
     * @return The popped value
     */
    public abstract PyObject pop(PyObject key, PyObject defaultValue);
    
    /**
     * Abstract method to pop an arbitrary item
     * @return A (key, value) tuple
     */
    public abstract PyObject popitem();
    
    /**
     * Abstract method to update with items from another mapping or iterable
     * @param other The other mapping or iterable
     */
    public abstract void update(PyObject other);
    
    /**
     * Abstract method to set a default value for a key
     * @param key The key
     * @param defaultValue The default value
     * @return The current value for the key
     */
    public abstract PyObject setdefault(PyObject key, PyObject defaultValue);
    
    @Override
    public PyObject getAttribute(String name) {
        switch (name) {
            case "__setitem__":
                return new PyBuiltinFunction("__setitem__", (args, kwargs, inter) -> {
                    if (args.size() != 2) {
                        throw new RuntimeException("__setitem__() takes exactly 2 arguments (" + args.size() + " given)");
                    }
                    this.setItem(args.get(0), args.get(1));
                    return PyNone.INSTANCE;
                });
                
            case "__delitem__":
                return new PyBuiltinFunction("__delitem__", (args, kwargs, inter) -> {
                    if (args.size() != 1) {
                        throw new RuntimeException("__delitem__() takes exactly 1 argument (" + args.size() + " given)");
                    }
                    this.delItem(args.get(0));
                    return PyNone.INSTANCE;
                });
                
            case "clear":
                return new PyBuiltinFunction("clear", (args, kwargs, inter) -> {
                    if (args.size() != 0) {
                        throw new RuntimeException("clear() takes no arguments (" + args.size() + " given)");
                    }
                    this.clear();
                    return PyNone.INSTANCE;
                });
                
            case "pop":
                return new PyBuiltinFunction("pop", (args, kwargs, inter) -> {
                    if (args.size() < 1 || args.size() > 2) {
                        throw new RuntimeException("pop() takes from 1 to 2 arguments but " + args.size() + " were given");
                    }
                    PyObject defaultValue = args.size() == 2 ? args.get(1) : null;
                    return this.pop(args.get(0), defaultValue);
                });
                
            case "popitem":
                return new PyBuiltinFunction("popitem", (args, kwargs, inter) -> {
                    if (args.size() != 0) {
                        throw new RuntimeException("popitem() takes no arguments (" + args.size() + " given)");
                    }
                    return this.popitem();
                });
                
            case "update":
                return new PyBuiltinFunction("update", (args, kwargs, inter) -> {
                    if (args.size() != 1) {
                        throw new RuntimeException("update() takes exactly one argument (" + args.size() + " given)");
                    }
                    this.update(args.get(0));
                    return PyNone.INSTANCE;
                });
                
            case "setdefault":
                return new PyBuiltinFunction("setdefault", (args, kwargs, inter) -> {
                    if (args.size() < 1 || args.size() > 2) {
                        throw new RuntimeException("setdefault() takes from 1 to 2 arguments but " + args.size() + " were given");
                    }
                    PyObject defaultValue = args.size() == 2 ? args.get(1) : null;
                    return this.setdefault(args.get(0), defaultValue);
                });
                
            default:
                return super.getAttribute(name);
        }
    }
    
    @Override
    public boolean isInstanceOf(String typeName) {
        switch (typeName) {
            case "MutableMapping":
            case "collections.abc.MutableMapping":
                return true;
            default:
                return super.isInstanceOf(typeName);
        }
    }
}

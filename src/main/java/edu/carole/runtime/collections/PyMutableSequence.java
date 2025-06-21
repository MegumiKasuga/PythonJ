package edu.carole.runtime.collections;

import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.PyObject;
import edu.carole.runtime.func.PyBuiltinFunction;
import edu.carole.runtime.PyNone;

/**
 * Python MutableSequence abstract base class
 * Corresponds to collections.abc.MutableSequence in Python
 */
public abstract class PyMutableSequence extends PySequence {
    
    /**
     * Abstract method to set an item by index
     * @param key The index
     * @param value The value to set
     */
    public abstract void setItem(PyObject key, PyObject value);
    
    /**
     * Abstract method to delete an item by index
     * @param key The index
     */
    public abstract void delItem(PyObject key);
    
    /**
     * Abstract method to insert an item at the given index
     * @param index The index
     * @param item The item to insert
     */
    public abstract void insert(PyObject index, PyObject item);
    
    /**
     * Abstract method to append an item
     * @param item The item to append
     */
    public abstract void append(PyObject item);
    
    /**
     * Abstract method to remove an item
     * @param item The item to remove
     */
    public abstract void remove(PyObject item);
    
    /**
     * Abstract method to pop an item at index
     * @param index The index (optional)
     * @return The popped item
     */
    public abstract PyObject pop(PyObject index);
    
    /**
     * Abstract method to clear all items
     */
    public abstract void clear();
    
    /**
     * Abstract method to extend with items from an iterable
     * @param iterable The iterable to extend with
     */
    public abstract void extend(PyObject iterable);
    
    /**
     * Abstract method to reverse the sequence
     */
    public abstract void reverse();
    
    @Override
    public PyObject getAttribute(Interpreter interpreter, String name) {
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
                
            case "insert":
                return new PyBuiltinFunction("insert", (args, kwargs, inter) -> {
                    if (args.size() != 2) {
                        throw new RuntimeException("insert() takes exactly two arguments (" + args.size() + " given)");
                    }
                    this.insert(args.get(0), args.get(1));
                    return PyNone.INSTANCE;
                });
                
            case "append":
                return new PyBuiltinFunction("append", (args, kwargs, inter) -> {
                    if (args.size() != 1) {
                        throw new RuntimeException("append() takes exactly one argument (" + args.size() + " given)");
                    }
                    this.append(args.get(0));
                    return PyNone.INSTANCE;
                });
                
            case "remove":
                return new PyBuiltinFunction("remove", (args, kwargs, inter) -> {
                    if (args.size() != 1) {
                        throw new RuntimeException("remove() takes exactly one argument (" + args.size() + " given)");
                    }
                    this.remove(args.get(0));
                    return PyNone.INSTANCE;
                });
                
            case "pop":
                return new PyBuiltinFunction("pop", (args, kwargs, inter) -> {
                    if (args.size() > 1) {
                        throw new RuntimeException("pop() takes at most 1 argument (" + args.size() + " given)");
                    }
                    PyObject index = args.size() == 1 ? args.get(0) : null;
                    return this.pop(index);
                });
                
            case "clear":
                return new PyBuiltinFunction("clear", (args, kwargs, inter) -> {
                    if (args.size() != 0) {
                        throw new RuntimeException("clear() takes no arguments (" + args.size() + " given)");
                    }
                    this.clear();
                    return PyNone.INSTANCE;
                });
                
            case "extend":
                return new PyBuiltinFunction("extend", (args, kwargs, inter) -> {
                    if (args.size() != 1) {
                        throw new RuntimeException("extend() takes exactly one argument (" + args.size() + " given)");
                    }
                    this.extend(args.get(0));
                    return PyNone.INSTANCE;
                });
                
            case "reverse":
                return new PyBuiltinFunction("reverse", (args, kwargs, inter) -> {
                    if (args.size() != 0) {
                        throw new RuntimeException("reverse() takes no arguments (" + args.size() + " given)");
                    }
                    this.reverse();
                    return PyNone.INSTANCE;
                });
                
            default:
                return super.getAttribute(interpreter, name);
        }
    }
    
    @Override
    public boolean isInstanceOf(String typeName) {
        switch (typeName) {
            case "MutableSequence":
            case "collections.abc.MutableSequence":
                return true;
            default:
                return super.isInstanceOf(typeName);
        }
    }
}

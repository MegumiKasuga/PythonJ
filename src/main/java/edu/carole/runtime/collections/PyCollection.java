package edu.carole.runtime.collections;

import edu.carole.runtime.PyObject;
import edu.carole.runtime.PyBuiltinFunction;
import edu.carole.runtime.PyBool;

/**
 * Python Collection abstract base class
 * Corresponds to collections.abc.Collection in Python
 */
public abstract class PyCollection extends PyIterable {
    
    /**
     * Abstract method that must be implemented by all collections
     * @return Number of elements in the collection
     */
    public abstract PyObject len();
    
    /**
     * Abstract method to check if an element is in the collection
     * @param item The item to check for
     * @return True if the item is in the collection, False otherwise
     */
    public abstract boolean contains(PyObject item);
    
    @Override
    public PyObject getAttribute(String name) {
        switch (name) {
            case "__len__":
                return new PyBuiltinFunction("__len__", args -> {
                    if (args.size() != 0) {
                        throw new RuntimeException("__len__() takes no arguments (" + args.size() + " given)");
                    }
                    return this.len();
                });
                
            case "__contains__":
                return new PyBuiltinFunction("__contains__", args -> {
                    if (args.size() != 1) {
                        throw new RuntimeException("__contains__() takes exactly 1 argument (" + args.size() + " given)");
                    }
                    return PyBool.valueOf(this.contains(args.get(0)));
                });
                
            default:
                return super.getAttribute(name);
        }
    }
    
    @Override
    public boolean isInstanceOf(String typeName) {
        switch (typeName) {
            case "Collection":
            case "collections.abc.Collection":
                return true;
            default:
                return super.isInstanceOf(typeName);
        }
    }
}

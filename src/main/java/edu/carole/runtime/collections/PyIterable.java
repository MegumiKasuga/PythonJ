package edu.carole.runtime.collections;

import edu.carole.runtime.PyObject;
import edu.carole.runtime.PyBuiltinFunction;
import edu.carole.runtime.PyIterator;
import java.util.Iterator;

/**
 * Python Iterable abstract base class
 * Corresponds to collections.abc.Iterable in Python
 */
public abstract class PyIterable extends PyObject {
    
    /**
     * Abstract method that must be implemented by all iterables
     * @return Iterator over the elements
     */
    public abstract Iterator<PyObject> iterator();
    
    @Override
    public PyObject getAttribute(String name) {
        switch (name) {
            case "__iter__":
                return new PyBuiltinFunction("__iter__", args -> {
                    if (args.size() != 0) {
                        throw new RuntimeException("__iter__() takes no arguments (" + args.size() + " given)");
                    }
                    return new PyIterator(this.iterator(), this.getTypeName());
                });
                
            default:
                return super.getAttribute(name);
        }
    }
    
    /**
     * Check if this object is an instance of the given type
     */
    public boolean isInstanceOf(String typeName) {
        switch (typeName) {
            case "Iterable":
            case "collections.abc.Iterable":
                return true;
            default:
                return false;
        }
    }
}

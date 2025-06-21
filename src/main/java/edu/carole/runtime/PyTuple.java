package edu.carole.runtime;

import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.base.PyObjectWithMethods;
import edu.carole.runtime.registry.MethodBuilder;
import java.util.*;

/**
 * Python tuple type using method registration system
 */
public class PyTuple extends PyObjectWithMethods {
    private final List<PyObject> elements;

    public PyTuple(List<PyObject> elements) {
        this.elements = new ArrayList<>(elements);
        registerMethods();
    }

    public List<PyObject> getElements() { 
        return elements; 
    }

    @Override
    public String getTypeName() { 
        return "tuple"; 
    }

    @Override
    public String toString() {
        if (elements.size() == 1) {
            // Single element tuple displays with trailing comma
            PyObject element = elements.get(0);
            if (element instanceof PyString) {
                return "('" + element.toString() + "',)";
            } else {
                return "(" + element.toString() + ",)";
            }
        }
        
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) sb.append(", ");
            PyObject element = elements.get(i);
            if (element instanceof PyString) {
                sb.append("'").append(element.toString()).append("'");
            } else {
                sb.append(element.toString());
            }
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean isTruthy() { 
        return !elements.isEmpty(); 
    }

    @Override
    public PyObject len() {
        return new PyInt(elements.size());
    }

    @Override
    public PyObject getItem(PyObject key) {
        if (!(key instanceof PyInt)) {
            throw new RuntimeException("tuple indices must be integers");
        }
        int index = (int) ((PyInt) key).getValue();
        if (index < 0) index += elements.size();
        if (index < 0 || index >= elements.size()) {
            throw new RuntimeException("tuple index out of range");
        }
        return elements.get(index);
    }

    @Override
    public Iterator<PyObject> iterator(Interpreter interpreter) {
        return elements.iterator();
    }

    @Override
    protected void registerMethods() {
        // Magic methods        methodRegistry.registerMethod("__add__", MethodBuilder.oneArg(this::__add__));
        methodRegistry.registerMethod("__mul__", MethodBuilder.oneArg(this::__mul__));
        methodRegistry.registerMethod("__eq__", MethodBuilder.oneArg(this::__eq__));
        methodRegistry.registerMethod("__ne__", MethodBuilder.oneArg(this::__ne__));
        methodRegistry.registerMethod("__lt__", MethodBuilder.oneArg(this::__lt__));
        methodRegistry.registerMethod("__le__", MethodBuilder.oneArg(this::__le__));
        methodRegistry.registerMethod("__gt__", MethodBuilder.oneArg(this::__gt__));
        methodRegistry.registerMethod("__ge__", MethodBuilder.oneArg(this::__ge__));
        methodRegistry.registerMethod("__hash__", MethodBuilder.noArgs(this::__hash__));
        methodRegistry.registerMethod("__len__", MethodBuilder.noArgs(this::__len__));
        methodRegistry.registerMethod("__getitem__", MethodBuilder.oneArg(this::__getitem__));
        methodRegistry.registerMethod("__getslice__", MethodBuilder.varArgs(args ->
                this.__getslice__(args.get(0), args.size() > 1 ? args.get(1) : PyNone.INSTANCE, args.size() > 2 ? args.get(2) : PyNone.INSTANCE)));
        methodRegistry.registerMethod("__contains__", MethodBuilder.oneArg(this::__contains__));
        methodRegistry.registerMethod("__iter__", MethodBuilder.noArgs(this::__iter__));
        methodRegistry.registerMethod("__repr__", MethodBuilder.noArgs(this::__repr__));
        methodRegistry.registerMethod("__str__", MethodBuilder.noArgs(this::__str__));

        // Tuple methods
        methodRegistry.registerMethod("count", MethodBuilder.oneArg(this::count));
        methodRegistry.registerMethod("index", MethodBuilder.varArgs(this::index));
    }

    // Magic method implementations
    private PyObject __add__(PyObject other) {
        if (other instanceof PyTuple) {
            List<PyObject> newElements = new ArrayList<>(this.elements);
            newElements.addAll(((PyTuple) other).getElements());
            return new PyTuple(newElements);
        } else {
            throw new RuntimeException("can only concatenate tuple (not \"" + other.getTypeName() + "\") to tuple");
        }
    }

    private PyObject __mul__(PyObject other) {
        if (other instanceof PyInt) {
            long times = ((PyInt) other).getValue();
            if (times <= 0) {
                return new PyTuple(new ArrayList<>());
            }
            List<PyObject> newElements = new ArrayList<>();
            for (int i = 0; i < times; i++) {
                newElements.addAll(this.elements);
            }
            return new PyTuple(newElements);
        } else {
            throw new RuntimeException("can't multiply sequence by non-int of type '" + other.getTypeName() + "'");
        }
    }

    private PyObject __eq__(PyObject other) {
        return PyBool.valueOf(this.equals(other));
    }    private PyObject __ne__(PyObject other) {
        return PyBool.valueOf(!this.equals(other));
    }
    
    private PyObject __lt__(PyObject other, Interpreter interpreter) {
        if (other instanceof PyTuple) {
            return PyBool.valueOf(compareTo(other, interpreter) < 0);
        }
        throw new RuntimeException("'<' not supported between instances of 'tuple' and '" + other.getTypeName() + "'");
    }
    
    private PyObject __le__(PyObject other, Interpreter interpreter) {
        if (other instanceof PyTuple) {
            return PyBool.valueOf(compareTo(other, interpreter) <= 0);
        }
        throw new RuntimeException("'<=' not supported between instances of 'tuple' and '" + other.getTypeName() + "'");
    }
    
    private PyObject __gt__(PyObject other, Interpreter interpreter) {
        if (other instanceof PyTuple) {
            return PyBool.valueOf(compareTo(other, interpreter) > 0);
        }
        throw new RuntimeException("'>' not supported between instances of 'tuple' and '" + other.getTypeName() + "'");
    }
    
    private PyObject __ge__(PyObject other, Interpreter interpreter) {
        if (other instanceof PyTuple) {
            return PyBool.valueOf(compareTo(other, interpreter) >= 0);
        }
        throw new RuntimeException("'>=' not supported between instances of 'tuple' and '" + other.getTypeName() + "'");
    }
    
    private PyObject __hash__(Interpreter interpreter) {
        int hash = 1;
        for (PyObject element : elements) {
            // Call the __hash__ method on each element
            PyObject hashMethod = element.getAttribute(interpreter, "__hash__");
            PyObject elementHash = hashMethod.call(java.util.List.of(), interpreter);
            if (elementHash instanceof PyInt) {
                hash = 31 * hash + (int)((PyInt) elementHash).getValue();
            } else {
                throw new RuntimeException("unhashable type: '" + element.getTypeName() + "'");
            }
        }
        return new PyInt(hash);
    }

    private PyObject __len__() {
        return new PyInt(this.elements.size());
    }

    private PyObject __getitem__(PyObject key) {
        return this.getItem(key);
    }

    private PyObject __contains__(PyObject item) {
        return PyBool.valueOf(this.contains(item));
    }

    private PyObject __iter__() {
        return new PyIterator(elements.iterator(), "tuple");
    }

    private PyObject __repr__() {
        return new PyString(this.toString());
    }

    private PyObject __str__() {
        return new PyString(this.toString());
    }

    // Tuple method implementations
    private PyObject count(PyObject item) {
        int count = 0;
        for (PyObject element : elements) {
            if (element.equals(item)) {
                count++;
            }
        }
        return new PyInt(count);
    }

    private PyObject index(List<PyObject> args) {
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
    }

    private PyObject __getslice__(PyObject start, PyObject stop, PyObject step) {
        return this.getSlice(start, stop, step);
    }

    // Helper methods
    public boolean contains(PyObject item) {
        for (PyObject element : elements) {
            if (element.equals(item)) {
                return true;
            }
        }
        return false;
    }

    public PyObject index(PyObject item) {
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i).equals(item)) {
                return new PyInt(i);
            }
        }
        throw new RuntimeException("ValueError: tuple.index(x): x not in tuple");
    }

    public PyObject index(PyObject item, PyObject start, PyObject end) {
        int startIdx = 0;
        int endIdx = elements.size();
          if (start != null) {
            if (!(start instanceof PyInt)) {
                throw new RuntimeException("tuple indices must be integers");
            }
            startIdx = (int) ((PyInt) start).getValue();
            if (startIdx < 0) startIdx += elements.size();
            startIdx = Math.max(0, startIdx);
        }
        
        if (end != null) {
            if (!(end instanceof PyInt)) {
                throw new RuntimeException("tuple indices must be integers");
            }
            endIdx = (int) ((PyInt) end).getValue();
            if (endIdx < 0) endIdx += elements.size();
            endIdx = Math.min(elements.size(), endIdx);
        }
        
        for (int i = startIdx; i < endIdx; i++) {
            if (elements.get(i).equals(item)) {
                return new PyInt(i);
            }
        }
        throw new RuntimeException("ValueError: tuple.index(x): x not in tuple");
    }

    @Override
    public boolean equals(PyObject other) {
        if (other instanceof PyTuple) {
            PyTuple otherTuple = (PyTuple) other;
            if (elements.size() != otherTuple.elements.size()) {
                return false;
            }
            for (int i = 0; i < elements.size(); i++) {
                if (!elements.get(i).equals(otherTuple.elements.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    // Java Object.equals() and hashCode() for proper HashMap functionality
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PyTuple pyTuple = (PyTuple) obj;
        return elements.equals(pyTuple.elements);
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }
    
    /**
     * Helper method to compare tuples lexicographically (element by element)
     */
    private int compareTo(PyObject other, Interpreter interpreter) {
        List<PyObject> otherElements;
        if (other instanceof PyTuple) {
            otherElements = ((PyTuple) other).elements;
        } else {
            throw new RuntimeException("Cannot compare tuple with " + other.getTypeName());
        }
        
        int minSize = Math.min(elements.size(), otherElements.size());
        
        // Compare elements one by one
        for (int i = 0; i < minSize; i++) {
            PyObject elem1 = elements.get(i);
            PyObject elem2 = otherElements.get(i);
              // Try to use Python comparison methods if available
            try {
                // Check if elements are equal
                PyObject eqMethod = elem1.getAttribute(interpreter, "__eq__");
                PyObject eqResult = eqMethod.call(java.util.List.of(elem2), interpreter);
                if (eqResult instanceof PyBool && ((PyBool) eqResult).getValue()) {
                    continue; // Elements are equal, continue with next elements
                }
                
                // Try to use less than comparison
                try {
                    PyObject ltMethod = elem1.getAttribute(interpreter, "__lt__");
                    PyObject ltResult = ltMethod.call(java.util.List.of(elem2), interpreter);
                    if (ltResult instanceof PyBool) {
                        boolean isLess = ((PyBool) ltResult).getValue();
                        if (isLess) return -1; // elem1 < elem2
                        return 1; // elem1 > elem2 (since they're not equal)
                    }
                } catch (RuntimeException e) {
                    // __lt__ method not found, continue to fallback
                }
                
                // Fallback to string representation comparison if direct comparison fails
                return elem1.toString().compareTo(elem2.toString());
                
            } catch (Exception e) {
                // If comparison methods fail, fall back to string comparison
                return elem1.toString().compareTo(elem2.toString());
            }
        }
        
        // If all common elements are equal, the shorter tuple is "less than" the longer one
        return Integer.compare(elements.size(), otherElements.size());
    }

    @Override
    public PyObject getSlice(PyObject start, PyObject stop, PyObject step) {
        PySlice slice = new PySlice(start, stop, step);
        int[] indices = slice.indices(elements.size());
        int startIdx = indices[0];
        int stopIdx = indices[1];
        int stepVal = indices[2];
        
        List<PyObject> slicedElements = new ArrayList<>();
        
        if (stepVal > 0) {
            // Forward stepping
            for (int i = startIdx; i < stopIdx; i += stepVal) {
                slicedElements.add(elements.get(i));
            }
        } else {
            // Backward stepping
            for (int i = startIdx; i > stopIdx; i += stepVal) {
                slicedElements.add(elements.get(i));
            }
        }
        
        return new PyTuple(slicedElements);
    }
}

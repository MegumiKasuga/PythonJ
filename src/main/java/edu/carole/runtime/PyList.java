package edu.carole.runtime;

import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.base.PyObjectWithMethods;
import edu.carole.runtime.registry.MethodRegistry;
import edu.carole.runtime.registry.MethodBuilder;

import java.util.*;

/**
 * Python list type using method registration system
 */
public class PyList extends PyObjectWithMethods {
    private final List<PyObject> elements;
      public PyList(List<PyObject> elements) {
        this.elements = new ArrayList<>(elements);
    }
    
    @Override
    protected void registerMethods() {
        MethodRegistry methodRegistry = getMethodRegistry();
          // Magic methods
        methodRegistry.registerMethod("__add__", MethodBuilder.oneArg(this::__add__));
        methodRegistry.registerMethod("__mul__", MethodBuilder.oneArg(this::__mul__));
        methodRegistry.registerMethod("__eq__", MethodBuilder.oneArg(this::__eq__));
        methodRegistry.registerMethod("__ne__", MethodBuilder.oneArg(this::__ne__));
        methodRegistry.registerMethod("__lt__", MethodBuilder.oneArg(this::__lt__));
        methodRegistry.registerMethod("__le__", MethodBuilder.oneArg(this::__le__));
        methodRegistry.registerMethod("__gt__", MethodBuilder.oneArg(this::__gt__));
        methodRegistry.registerMethod("__ge__", MethodBuilder.oneArg(this::__ge__));
        methodRegistry.registerMethod("__str__", MethodBuilder.noArgs(this::__str__));
        methodRegistry.registerMethod("__repr__", MethodBuilder.noArgs(this::__repr__));
        methodRegistry.registerMethod("__len__", MethodBuilder.noArgs(this::__len__));        methodRegistry.registerMethod("__getitem__", MethodBuilder.oneArg(this::__getitem__));
        methodRegistry.registerMethod("__getslice__", MethodBuilder.varArgs(args ->
                this.__getslice__(args.get(0), args.size() > 1 ? args.get(1) : PyNone.INSTANCE, args.size() > 2 ? args.get(2) : new PyInt(1))));
        methodRegistry.registerMethod("__setitem__", MethodBuilder.twoArgs(args -> {
            this.setItem(args[0], args[1]);
            return PyNone.INSTANCE;
        }));
        methodRegistry.registerMethod("__setslice__", MethodBuilder.varArgs(args -> {
            this.__setslice__(args.get(0), args.size() > 1 ? args.get(1) : PyNone.INSTANCE, args.size() > 2 ? args.get(2) : new PyInt(1), args.get(args.size() - 1));
            return PyNone.INSTANCE;
        }));
        methodRegistry.registerMethod("__delitem__", MethodBuilder.oneArg(this::__delitem__));
        methodRegistry.registerMethod("__iter__", MethodBuilder.noArgs(this::__iter__));
        methodRegistry.registerMethod("__contains__", MethodBuilder.oneArg(this::__contains__));
        methodRegistry.registerMethod("__reversed__", MethodBuilder.noArgs(this::__reversed__));
        methodRegistry.registerMethod("__iadd__", MethodBuilder.oneArg(this::__iadd__));
        methodRegistry.registerMethod("__imul__", MethodBuilder.oneArg(this::__imul__));
        
        // List methods
        methodRegistry.registerMethod("append", MethodBuilder.oneArg(this::append));
        methodRegistry.registerMethod("extend", MethodBuilder.oneArg(this::extend));
        methodRegistry.registerMethod("insert", MethodBuilder.twoArgs(args -> this.insert(args[0], args[1])));
        methodRegistry.registerMethod("remove", MethodBuilder.oneArg(this::remove));
        methodRegistry.registerMethod("pop", MethodBuilder.varArgs(this::pop));
        methodRegistry.registerMethod("clear", MethodBuilder.noArgs(this::clear));
        methodRegistry.registerMethod("index", MethodBuilder.varArgs(this::index));
        methodRegistry.registerMethod("count", MethodBuilder.oneArg(this::count));
        methodRegistry.registerMethod("sort", MethodBuilder.varArgs(this::sort));
        methodRegistry.registerMethod("copy", MethodBuilder.noArgs(this::copy));
        methodRegistry.registerMethod("reverse", MethodBuilder.noArgs(this::reverse));
    }
    
    // Accessors
    public List<PyObject> getElements() { 
        return elements; 
    }
    
    public int size() { 
        return elements.size(); 
    }
    
    @Override
    public String getTypeName() { 
        return "list"; 
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) sb.append(", ");
            PyObject element = elements.get(i);
            if (element instanceof PyString) {
                sb.append("'").append(element.toString()).append("'");
            } else {
                sb.append(element.toString());
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    @Override
    public boolean isTruthy() { 
        return !elements.isEmpty(); 
    }
    
    public PyObject len() {
        return new PyInt(elements.size());
    }
    
    public PyObject getItem(PyObject key) {
        if (!(key instanceof PyInt)) {
            throw new RuntimeException("list indices must be integers");
        }
        int index = (int) ((PyInt) key).getValue();
        if (index < 0) index += elements.size();
        if (index < 0 || index >= elements.size()) {
            throw new RuntimeException("list index out of range");
        }
        return elements.get(index);
    }    
    
    public void setItem(PyObject key, PyObject value) {
        if (!(key instanceof PyInt)) {
            throw new RuntimeException("list indices must be integers");
        }
        int index = (int) ((PyInt) key).getValue();
        if (index < 0) index += elements.size();
        if (index < 0 || index >= elements.size()) {
            throw new RuntimeException("list assignment index out of range");
        }
        elements.set(index, value);
    }
    
    @Override
    public PyObject getSlice(PyObject start, PyObject stop, PyObject step) {
        PySlice slice = new PySlice(start, stop, step);
        int[] indices = slice.indices(elements.size());
        int startIdx = indices[0];
        int stopIdx = indices[1];
        int stepVal = indices[2];
        
        List<PyObject> result = new ArrayList<>();
        
        if (stepVal > 0) {
            for (int i = startIdx; i < stopIdx; i += stepVal) {
                result.add(elements.get(i));
            }
        } else {
            for (int i = startIdx; i > stopIdx; i += stepVal) {
                result.add(elements.get(i));
            }
        }
        
        return new PyList(result);
    }
    
    @Override
    public void setSlice(PyObject start, PyObject stop, PyObject step, PyObject value) {
        PySlice slice = new PySlice(start, stop, step);
        int[] indices = slice.indices(elements.size());
        int startIdx = indices[0];
        int stopIdx = indices[1];
        int stepVal = indices[2];
        
        // Extended slice assignment (step != 1) must be same length
        if (stepVal != 1) {
            if (!(value instanceof PyList)) {
                throw new RuntimeException("can only assign an iterable");
            }
            
            List<PyObject> newValues = ((PyList) value).getElements();
            List<Integer> sliceIndices = new ArrayList<>();
            
            if (stepVal > 0) {
                for (int i = startIdx; i < stopIdx; i += stepVal) {
                    sliceIndices.add(i);
                }
            } else {
                for (int i = startIdx; i > stopIdx; i += stepVal) {
                    sliceIndices.add(i);
                }
            }
            
            if (sliceIndices.size() != newValues.size()) {
                throw new RuntimeException("attempt to assign sequence of size " + newValues.size() + 
                                         " to extended slice of size " + sliceIndices.size());
            }
            
            for (int i = 0; i < sliceIndices.size(); i++) {
                elements.set(sliceIndices.get(i), newValues.get(i));
            }
        } else {
            // Simple slice assignment (step == 1) can change length
            if (!(value instanceof PyList)) {
                throw new RuntimeException("can only assign an iterable");
            }
            
            List<PyObject> newValues = ((PyList) value).getElements();
            
            // Remove the old slice elements
            for (int i = stopIdx - 1; i >= startIdx; i--) {
                if (i < elements.size()) {
                    elements.remove(i);
                }
            }
            
            // Insert the new elements
            for (int i = 0; i < newValues.size(); i++) {
                elements.add(startIdx + i, newValues.get(i));
            }
        }
    }
    
    public Iterator<PyObject> iterator() {
        return elements.iterator();
    }
    
    // Magic method implementations
    private PyObject __add__(PyObject other) {
        if (other instanceof PyList) {
            List<PyObject> newElements = new ArrayList<>(this.elements);
            newElements.addAll(((PyList) other).getElements());
            return new PyList(newElements);
        } else {
            throw new RuntimeException("can only concatenate list (not \"" + other.getTypeName() + "\") to list");
        }
    }
      private PyObject __mul__(PyObject other) {
        if (other instanceof PyInt) {
            long times = ((PyInt) other).getValue();
            if (times <= 0) {
                return new PyList(new ArrayList<>());
            }
            List<PyObject> newElements = new ArrayList<>();
            for (int i = 0; i < times; i++) {
                newElements.addAll(this.elements);
            }
            return new PyList(newElements);
        } else {
            throw new RuntimeException("can't multiply sequence by non-int of type '" + other.getTypeName() + "'");
        }
    }
    
    private PyObject __eq__(PyObject other) {
        if (other instanceof PyList) {
            List<PyObject> otherElements = ((PyList) other).getElements();
            if (this.elements.size() != otherElements.size()) {
                return PyBool.FALSE;
            }
            for (int i = 0; i < this.elements.size(); i++) {
                if (!this.elements.get(i).equals(otherElements.get(i))) {
                    return PyBool.FALSE;
                }
            }
            return PyBool.TRUE;
        }
        return PyBool.FALSE;
    }
      private PyObject __ne__(PyObject other) {
        PyObject eqResult = __eq__(other);
        return PyBool.valueOf(!((PyBool) eqResult).getValue());
    }
    
    private PyObject __lt__(PyObject other) {
        if (other instanceof PyList) {
            List<PyObject> otherElements = ((PyList) other).getElements();
            return PyBool.valueOf(compareElements(this.elements, otherElements) < 0);
        }
        throw new RuntimeException("'<' not supported between instances of 'list' and '" + other.getTypeName() + "'");
    }
    
    private PyObject __le__(PyObject other) {
        if (other instanceof PyList) {
            List<PyObject> otherElements = ((PyList) other).getElements();
            return PyBool.valueOf(compareElements(this.elements, otherElements) <= 0);
        }
        throw new RuntimeException("'<=' not supported between instances of 'list' and '" + other.getTypeName() + "'");
    }
    
    private PyObject __gt__(PyObject other) {
        if (other instanceof PyList) {
            List<PyObject> otherElements = ((PyList) other).getElements();
            return PyBool.valueOf(compareElements(this.elements, otherElements) > 0);
        }
        throw new RuntimeException("'>' not supported between instances of 'list' and '" + other.getTypeName() + "'");
    }
    
    private PyObject __ge__(PyObject other) {
        if (other instanceof PyList) {
            List<PyObject> otherElements = ((PyList) other).getElements();
            return PyBool.valueOf(compareElements(this.elements, otherElements) >= 0);
        }
        throw new RuntimeException("'>=' not supported between instances of 'list' and '" + other.getTypeName() + "'");
    }
    
    private PyObject __str__() {
        return new PyString(toString());
    }
    
    private PyObject __repr__() {
        return new PyString(toString());
    }
      private PyObject __len__() {
        return new PyInt(this.elements.size());
    }
    
    private PyObject __getitem__(PyObject key) {
        return this.getItem(key);
    }      private PyObject __setitem__(PyObject key, PyObject value) {
        this.setItem(key, value);
        return PyNone.INSTANCE;
    }
    
    private PyObject __getslice__(PyObject start, PyObject stop, PyObject step) {
        return this.getSlice(start, stop, step);
    }
    
    private PyObject __setslice__(PyObject start, PyObject stop, PyObject step, PyObject value) {
        this.setSlice(start, stop, step, value);
        return PyNone.INSTANCE;
    }
    
    private PyObject __delitem__(PyObject key) {
        this.delItem(key);
        return PyNone.INSTANCE;
    }
    
    private PyObject __iter__() {
        return new PyIterator(elements.iterator(), "list");
    }
    
    private PyObject __contains__(PyObject item) {
        return PyBool.valueOf(this.contains(item));
    }
    
    private PyObject __reversed__() {
        List<PyObject> reversedElements = new ArrayList<>(this.elements);
        Collections.reverse(reversedElements);
        return new PyIterator(reversedElements.iterator(), "list_reverseiterator");
    }
    
    private PyObject __iadd__(PyObject other, Interpreter interpreter) {
        if (other instanceof PyList) {
            this.elements.addAll(((PyList) other).getElements());
            return this;
        } else {
            // Try to iterate over the object
            try {
                Iterator<PyObject> iterator = other.iterator(interpreter);
                while (iterator.hasNext()) {
                    this.elements.add(iterator.next());
                }
                return this;
            } catch (Exception e) {
                throw new RuntimeException("can only concatenate list (not \"" + other.getTypeName() + "\") to list");
            }
        }
    }
      private PyObject __imul__(PyObject other) {
        if (other instanceof PyInt) {
            long times = ((PyInt) other).getValue();
            if (times <= 0) {
                this.elements.clear();
            } else {
                List<PyObject> originalElements = new ArrayList<>(this.elements);
                this.elements.clear();
                for (int i = 0; i < times; i++) {
                    this.elements.addAll(originalElements);
                }
            }
            return this;
        } else {
            throw new RuntimeException("can't multiply sequence by non-int of type '" + other.getTypeName() + "'");
        }
    }
    
    // List method implementations
    private PyObject append(PyObject item) {
        elements.add(item);
        return PyNone.INSTANCE;
    }
    
    private PyObject extend(PyObject iterable, Interpreter interpreter) {
        Iterator<PyObject> iterator = iterable.iterator(interpreter);
        while (iterator.hasNext()) {
            elements.add(iterator.next());
        }
        return PyNone.INSTANCE;
    }    private PyObject insert(PyObject index, PyObject item) {
        if (!(index instanceof PyInt)) {
            throw new RuntimeException("list indices must be integers");
        }
        int idx = (int) ((PyInt) index).getValue();
        if (idx < 0) idx += elements.size();
        if (idx < 0) idx = 0;
        if (idx > elements.size()) idx = elements.size();
        elements.add(idx, item);
        return PyNone.INSTANCE;
    }
    
    private PyObject remove(PyObject value) {
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i).equals(value)) {
                elements.remove(i);
                return PyNone.INSTANCE;
            }
        }
        throw new RuntimeException("list.remove(x): x not in list");
    }
    
    private PyObject pop(List<PyObject> args) {
        if (args.size() > 1) {
            throw new RuntimeException("pop() takes at most 1 argument (" + args.size() + " given)");
        }
        if (elements.isEmpty()) {
            throw new RuntimeException("pop from empty list");
        }
        
        int index;
        if (args.isEmpty()) {
            index = elements.size() - 1;        } else {
            if (!(args.get(0) instanceof PyInt)) {
                throw new RuntimeException("list indices must be integers");
            }
            index = (int) ((PyInt) args.get(0)).getValue();
            if (index < 0) index += elements.size();
            if (index < 0 || index >= elements.size()) {
                throw new RuntimeException("pop index out of range");
            }
        }
        return elements.remove(index);
    }
    
    private PyObject clear() {
        elements.clear();
        return PyNone.INSTANCE;
    }
    
    private PyObject index(List<PyObject> args) {
        if (args.isEmpty() || args.size() > 3) {
            throw new RuntimeException("index() takes from 1 to 3 positional arguments but " + args.size() + " were given");
        }
        PyObject value = args.get(0);
        int start = 0;
        int end = elements.size();
          if (args.size() >= 2) {
            if (!(args.get(1) instanceof PyInt)) {
                throw new RuntimeException("list indices must be integers");
            }
            start = (int) ((PyInt) args.get(1)).getValue();
            if (start < 0) start += elements.size();
            if (start < 0) start = 0;
        }
          if (args.size() == 3) {
            if (!(args.get(2) instanceof PyInt)) {
                throw new RuntimeException("list indices must be integers");
            }
            end = (int) ((PyInt) args.get(2)).getValue();
            if (end < 0) end += elements.size();
            if (end > elements.size()) end = elements.size();
        }
        
        for (int i = start; i < end; i++) {
            if (elements.get(i).equals(value)) {
                return new PyInt(i);
            }
        }
        throw new RuntimeException(value.toString() + " is not in list");
    }
    
    private PyObject count(PyObject value) {
        int count = 0;
        for (PyObject element : elements) {
            if (element.equals(value)) {
                count++;
            }
        }
        return new PyInt(count);
    }
    
    private PyObject sort(List<PyObject> args) {
        if (args.size() > 2) {
            throw new RuntimeException("sort() takes at most 2 arguments (" + args.size() + " given)");
        }
        PyObject key = !args.isEmpty() ? args.get(0) : null;
        boolean reverse = false;
        if (args.size() > 1) {
            if (args.get(1) instanceof PyBool) {
                reverse = ((PyBool) args.get(1)).getValue();
            }
        }
        this.sortInPlace(key, reverse);
        return PyNone.INSTANCE;
    }
    
    private PyObject copy() {
        return new PyList(new ArrayList<>(elements));
    }
    
    private PyObject reverse() {
        this.reverseInPlace();
        return PyNone.INSTANCE;
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
    
    public void delItem(PyObject key) {
        if (!(key instanceof PyInt)) {
            throw new RuntimeException("list indices must be integers");
        }
        int index = (int) ((PyInt) key).getValue();
        if (index < 0) index += elements.size();
        if (index < 0 || index >= elements.size()) {
            throw new RuntimeException("list assignment index out of range");
        }
        elements.remove(index);
    }
    
    /**
     * Sort the list in place
     */
    public void sortInPlace() {
        sortInPlace(null, false);
    }
    
    /**
     * Sort the list in place with optional key function and reverse flag
     */
    public void sortInPlace(PyObject key, boolean reverse) {
        if (key == null) {
            // Simple sort - compare elements directly
            try {
                elements.sort((a, b) -> {
                    // Simple comparison for basic types
                    if (a instanceof PyInt && b instanceof PyInt) {
                        return Long.compare(((PyInt) a).getValue(), ((PyInt) b).getValue());
                    } else if (a instanceof PyFloat && b instanceof PyFloat) {
                        return Double.compare(((PyFloat) a).getValue(), ((PyFloat) b).getValue());
                    } else if (a instanceof PyString && b instanceof PyString) {
                        return a.toString().compareTo(b.toString());
                    } else {
                        // For mixed types, convert to string and compare
                        return a.toString().compareTo(b.toString());
                    }
                });
                if (reverse) {
                    Collections.reverse(elements);
                }
            } catch (Exception e) {
                throw new RuntimeException("TypeError: '<' not supported between instances");
            }
        } else {
            throw new RuntimeException("TypeError: sort() with key function not yet implemented");
        }
    }
      /**
     * Reverse the list in place
     */
    public void reverseInPlace() {
        Collections.reverse(elements);
    }
    
    /**
     * Helper method to compare two lists element by element
     */
    private int compareElements(List<PyObject> list1, List<PyObject> list2) {
        int minSize = Math.min(list1.size(), list2.size());
        
        for (int i = 0; i < minSize; i++) {
            PyObject elem1 = list1.get(i);
            PyObject elem2 = list2.get(i);
            
            // Try to compare elements using their comparison methods
            try {
                if (elem1 instanceof PyInt && elem2 instanceof PyInt) {
                    long val1 = ((PyInt) elem1).getValue();
                    long val2 = ((PyInt) elem2).getValue();
                    int cmp = Long.compare(val1, val2);
                    if (cmp != 0) return cmp;
                } else if (elem1 instanceof PyString && elem2 instanceof PyString) {
                    String str1 = ((PyString) elem1).getValue();
                    String str2 = ((PyString) elem2).getValue();
                    int cmp = str1.compareTo(str2);
                    if (cmp != 0) return cmp;
                } else {
                    // For other types, use equals check
                    if (!elem1.equals(elem2)) {
                        // If not equal, try to determine order based on hash codes
                        return Integer.compare(elem1.hashCode(), elem2.hashCode());
                    }
                }
            } catch (Exception e) {
                // If comparison fails, consider elements equal
            }
        }
        
        // If all compared elements are equal, compare sizes
        return Integer.compare(list1.size(), list2.size());
    }
}

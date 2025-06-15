package edu.carole.runtime;

import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.base.PyObjectWithMethods;
import edu.carole.runtime.registry.MethodBuilder;

import java.util.*;

/**
 * Python集合对象的实现
 */
public class PySet extends PyObjectWithMethods {
    private Set<PyObject> elements;
    
    public PySet(Set<PyObject> elements) {
        this.elements = elements;
    }
    
    public Set<PyObject> getElements() {
        return elements;
    }
    
    @Override
    public String getTypeName() {
        return "set";
    }
    
    @Override
    public String toString() {
        if (elements.isEmpty()) {
            return "set()";
        }
        
        StringBuilder builder = new StringBuilder("{");
        Iterator<PyObject> iterator = elements.iterator();
        while (iterator.hasNext()) {
            builder.append(iterator.next().toString());
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
        builder.append("}");
        return builder.toString();
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
    public Iterator<PyObject> iterator(Interpreter interpreter) {
        return elements.iterator();
    }

    @Override
    protected void registerMethods() {
        methodRegistry.registerMethod("__iter__", MethodBuilder.noArgs(this::iter));
        methodRegistry.registerMethod("__repr__", MethodBuilder.noArgs(() -> new PyString(toString())));
        methodRegistry.registerMethod("__str__", MethodBuilder.noArgs(() -> new PyString(toString())));
        methodRegistry.registerMethod("add", MethodBuilder.oneArg(this::add));
        methodRegistry.registerMethod("clear", MethodBuilder.noArgs(this::clear));
        methodRegistry.registerMethod("remove", MethodBuilder.oneArg(this::remove));
        methodRegistry.registerMethod("discard", MethodBuilder.oneArg(this::discard));
        methodRegistry.registerMethod("pop", MethodBuilder.noArgs(this::pop));
        methodRegistry.registerMethod("copy", MethodBuilder.noArgs(this::copy));
        methodRegistry.registerMethod("issubset", MethodBuilder.oneArg(this::issubset));
        methodRegistry.registerMethod("issuperset", MethodBuilder.oneArg(this::issuperset));
        methodRegistry.registerMethod("union", MethodBuilder.varArgs(this::union));
        methodRegistry.registerMethod("intersection", MethodBuilder.varArgs(this::intersection));
        methodRegistry.registerMethod("difference", MethodBuilder.varArgs(this::difference));
        methodRegistry.registerMethod("__eq__", MethodBuilder.oneArg(this::eq));
        methodRegistry.registerMethod("__ne__", MethodBuilder.oneArg(this::ne));
        methodRegistry.registerMethod("__contains__", MethodBuilder.oneArg(this::contains));
        methodRegistry.registerMethod("__or__", MethodBuilder.oneArg(this::or));
        methodRegistry.registerMethod("__and__", MethodBuilder.oneArg(this::and));
        methodRegistry.registerMethod("__sub__", MethodBuilder.oneArg(this::sub));
        methodRegistry.registerMethod("__xor__", MethodBuilder.oneArg(this::xor));
        methodRegistry.registerMethod("symmetric_difference", MethodBuilder.oneArg(this::symmetricDifference));
        methodRegistry.registerMethod("isdisjoint", MethodBuilder.oneArg(this::isdisjoint));
        methodRegistry.registerMethod("update", MethodBuilder.varArgs(this::update));
        methodRegistry.registerMethod("intersection_update", MethodBuilder.varArgs(this::intersectionUpdate));
        methodRegistry.registerMethod("difference_update", MethodBuilder.varArgs(this::differenceUpdate));
        methodRegistry.registerMethod("symmetric_difference_update", MethodBuilder.oneArg(this::symmetricDifferenceUpdate));
        methodRegistry.registerMethod("__hash__", MethodBuilder.noArgs(this::hash));
    }
    
    public PyObject hash() {
        return new PyInt(elements.hashCode());
    }
    
    public PyObject iter() {
        return new PyIterator(elements.iterator(), "set_iterator");
    }
    
    public PyObject add(PyObject item) {
        elements.add(item);
        return PyNone.INSTANCE;
    }
    
    public PyObject clear() {
        elements.clear();
        return PyNone.INSTANCE;
    }
    
    public PyObject remove(PyObject item) {
        if (!elements.remove(item)) {
            throw new RuntimeException("KeyError: " + item.toString());
        }
        return PyNone.INSTANCE;
    }

    private PyObject discard(PyObject item) {
        elements.remove(item); // No exception if not found
        return PyNone.INSTANCE;
    }

    private PyObject pop() {
        if (elements.isEmpty()) {
            throw new RuntimeException("KeyError: 'pop from an empty set'");
        }
        Iterator<PyObject> it = elements.iterator();
        PyObject item = it.next();
        it.remove();
        return item;
    }

    private PyObject copy() {
        return new PySet(new HashSet<>(elements));
    }

    private PyObject issubset(PyObject other) {
        if (!(other instanceof PySet || other instanceof PyFrozenSet)) {
            throw new RuntimeException("'issubset' requires a set as an argument");
        }

        Set<PyObject> otherSet;
        if (other instanceof PySet) {
            otherSet = ((PySet) other).elements;
        } else {
            otherSet = ((PyFrozenSet) other).getElements();
        }

        return PyBool.valueOf(otherSet.containsAll(elements));
    }

    private PyObject issuperset(PyObject other) {
        if (!(other instanceof PySet || other instanceof PyFrozenSet)) {
            throw new RuntimeException("'issuperset' requires a set as an argument");
        }

        Set<PyObject> otherSet;
        if (other instanceof PySet) {
            otherSet = ((PySet) other).elements;
        } else {
            otherSet = ((PyFrozenSet) other).getElements();
        }

        return PyBool.valueOf(elements.containsAll(otherSet));
    }

    private PyObject union(List<PyObject> args, Interpreter interpreter) {
        Set<PyObject> result = new HashSet<>(elements);
        for (PyObject arg : args) {
            Iterator<PyObject> iterator = arg.iterator(interpreter);
            while (iterator.hasNext()) {
                result.add(iterator.next());
            }
        }
        return new PySet(result);
    }

    private PyObject intersection(List<PyObject> args, Interpreter interpreter) {
        Set<PyObject> result = new HashSet<>(elements);
        for (PyObject arg : args) {
            Set<PyObject> argSet = new HashSet<>();
            Iterator<PyObject> iterator = arg.iterator(interpreter);
            while (iterator.hasNext()) {
                argSet.add(iterator.next());
            }

            result.retainAll(argSet);
        }

        return new PySet(result);
    }

    private PyObject difference(List<PyObject> args, Interpreter interpreter) {
        Set<PyObject> result = new HashSet<>(elements);
        for (PyObject arg : args) {
            Iterator<PyObject> iterator = arg.iterator(interpreter);
            while (iterator.hasNext()) {
                result.remove(iterator.next());
            }
        }
        return new PySet(result);
    }

    private PyObject eq(PyObject other) {
        if (other instanceof PySet otherSet) {
            return PyBool.valueOf(elements.equals(otherSet.elements));
        } else if (other instanceof PyFrozenSet otherSet) {
            return PyBool.valueOf(elements.equals(otherSet.getElements()));
        } else {
            return PyBool.FALSE;
        }
    }

    private PyObject ne(PyObject object) {
        return ((PyBool)eq(object)).reverse();
    }

    private PyObject contains(PyObject arg) {
        return PyBool.valueOf(this.elements.contains(arg));
    }

    private PyObject or(PyObject arg, Interpreter interpreter) {
        return union(List.of(arg), interpreter);
    }

    private PyObject and(PyObject arg, Interpreter interpreter) {
        return intersection(List.of(arg), interpreter);
    }

    private PyObject sub(PyObject arg, Interpreter interpreter) {
        return difference(List.of(arg), interpreter);
    }

    private PyObject xor(PyObject other, Interpreter interpreter) {
        return symmetricDifference(other, interpreter);
    }

    private PyObject symmetricDifference(PyObject other, Interpreter interpreter) {
        Set<PyObject> result = new HashSet<>(elements);
        Set<PyObject> otherSet = new HashSet<>();

        Iterator<PyObject> iterator = other.iterator(interpreter);
        while (iterator.hasNext()) {
            otherSet.add(iterator.next());
        }

        // Remove common elements
        Set<PyObject> intersection = new HashSet<>(result);
        intersection.retainAll(otherSet);
        result.removeAll(intersection);
        otherSet.removeAll(intersection);

        // Add remaining elements from other set
        result.addAll(otherSet);

        return new PySet(result);
    }

    private PyObject isdisjoint(PyObject other, Interpreter interpreter) {
        Iterator<PyObject> iterator = other.iterator(interpreter);
        while (iterator.hasNext()) {
            if (elements.contains(iterator.next())) {
                return PyBool.FALSE;
            }
        }
        return PyBool.TRUE;
    }

    private PyObject update(List<PyObject> args, Interpreter interpreter) {
        for (PyObject arg : args) {
            Iterator<PyObject> iterator = arg.iterator(interpreter);
            while (iterator.hasNext()) {
                elements.add(iterator.next());
            }
        }
        return PyNone.INSTANCE;
    }

    private PyObject intersectionUpdate(List<PyObject> args, Interpreter interpreter) {
        for (PyObject arg : args) {
            Set<PyObject> argSet = new HashSet<>();
            Iterator<PyObject> iterator = arg.iterator(interpreter);
            while (iterator.hasNext()) {
                argSet.add(iterator.next());
            }
            elements.retainAll(argSet);
        }
        return PyNone.INSTANCE;
    }

    private PyObject differenceUpdate(List<PyObject> args, Interpreter interpreter) {
        for (PyObject arg : args) {
            Iterator<PyObject> iterator = arg.iterator(interpreter);
            while (iterator.hasNext()) {
                elements.remove(iterator.next());
            }
        }
        return PyNone.INSTANCE;
    }

    private PyObject symmetricDifferenceUpdate(PyObject other, Interpreter interpreter) {
        Set<PyObject> otherSet = new HashSet<>();

        Iterator<PyObject> iterator = other.iterator(interpreter);
        while (iterator.hasNext()) {
            otherSet.add(iterator.next());
        }

        // Find intersection
        Set<PyObject> intersection = new HashSet<>(elements);
        intersection.retainAll(otherSet);

        // Remove intersection from both sets
        elements.removeAll(intersection);
        otherSet.removeAll(intersection);

        // Add remaining elements from other set
        elements.addAll(otherSet);

        return PyNone.INSTANCE;
    }
}

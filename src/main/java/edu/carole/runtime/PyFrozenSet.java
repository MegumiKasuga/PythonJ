package edu.carole.runtime;

import edu.carole.runtime.base.PyObjectWithMethods;
import edu.carole.runtime.registry.MethodBuilder;
import edu.carole.runtime.registry.MethodRegistry;

import java.util.*;

/**
 * Python frozenset object using method registration system
 */
public class PyFrozenSet extends PyObjectWithMethods {
    private final Set<PyObject> elements;
    
    public PyFrozenSet(Set<PyObject> elements) {
        this.elements = Collections.unmodifiableSet(new HashSet<>(elements));
    }
    
    public PyFrozenSet() {
        this.elements = Collections.unmodifiableSet(new HashSet<>());
    }
    
    @Override
    protected void registerMethods() {
        MethodRegistry methodRegistry = getMethodRegistry();
        
        // Magic methods
        methodRegistry.registerMethod("__iter__", MethodBuilder.noArgs(this::__iter__));
        methodRegistry.registerMethod("__eq__", MethodBuilder.oneArg(this::__eq__));
        methodRegistry.registerMethod("__ne__", MethodBuilder.oneArg(this::__ne__));
        methodRegistry.registerMethod("__len__", MethodBuilder.noArgs(this::__len__));
        methodRegistry.registerMethod("__contains__", MethodBuilder.oneArg(this::__contains__));
        methodRegistry.registerMethod("__or__", MethodBuilder.oneArg(this::__or__));
        methodRegistry.registerMethod("__and__", MethodBuilder.oneArg(this::__and__));
        methodRegistry.registerMethod("__sub__", MethodBuilder.oneArg(this::__sub__));
        methodRegistry.registerMethod("__xor__", MethodBuilder.oneArg(this::__xor__));
        methodRegistry.registerMethod("__repr__", MethodBuilder.noArgs(this::__repr__));
        methodRegistry.registerMethod("__str__", MethodBuilder.noArgs(this::__str__));
        methodRegistry.registerMethod("__hash__", MethodBuilder.noArgs(this::__hash__));
        
        // Set operation methods (frozenset is immutable, so no modification methods)
        methodRegistry.registerMethod("copy", MethodBuilder.noArgs(this::copy));
        methodRegistry.registerMethod("union", MethodBuilder.varArgs(this::union));
        methodRegistry.registerMethod("intersection", MethodBuilder.varArgs(this::intersection));
        methodRegistry.registerMethod("difference", MethodBuilder.varArgs(this::difference));
        methodRegistry.registerMethod("symmetric_difference", MethodBuilder.oneArg(this::symmetric_difference));
        methodRegistry.registerMethod("issubset", MethodBuilder.oneArg(this::issubset));
        methodRegistry.registerMethod("issuperset", MethodBuilder.oneArg(this::issuperset));
        methodRegistry.registerMethod("isdisjoint", MethodBuilder.oneArg(this::isdisjoint));
    }
    
    // Accessors
    public Set<PyObject> getElements() { 
        return elements; 
    }
    
    @Override
    public String getTypeName() { 
        return "frozenset"; 
    }
    
    @Override
    public String toString() {
        if (elements.isEmpty()) {
            return "frozenset()";
        }
        
        StringBuilder builder = new StringBuilder("frozenset({");
        Iterator<PyObject> iterator = elements.iterator();
        while (iterator.hasNext()) {
            builder.append(iterator.next().toString());
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
        builder.append("})");
        return builder.toString();
    }
    
    @Override
    public boolean isTruthy() { 
        return !elements.isEmpty(); 
    }
      public PyObject len() {
        return new PyInt(elements.size());
    }
    
    public Iterator<PyObject> iterator() {
        return elements.iterator();
    }
    
    // Magic method implementations
    private PyObject __iter__() {
        return new PyIterator(elements.iterator(), "frozenset_iterator");
    }
    
    private PyObject __eq__(PyObject other) {
        if (other instanceof PyFrozenSet otherSet) {
            return PyBool.valueOf(elements.equals(otherSet.elements));
        } else if (other instanceof PySet otherSet) {
            return PyBool.valueOf(elements.equals(otherSet.getElements()));
        } else {
            return PyBool.FALSE;
        }
    }
    
    private PyObject __ne__(PyObject other) {
        PyObject eqResult = __eq__(other);
        return PyBool.valueOf(!((PyBool) eqResult).getValue());
    }
      private PyObject __len__() {
        return new PyInt(this.elements.size());
    }
    
    private PyObject __contains__(PyObject item) {
        return PyBool.valueOf(this.elements.contains(item));
    }
    
    private PyObject __or__(PyObject other) {
        return union(List.of(other));
    }
    
    private PyObject __and__(PyObject other) {
        return intersection(List.of(other));
    }
    
    private PyObject __sub__(PyObject other) {
        return difference(List.of(other));
    }
    
    private PyObject __xor__(PyObject other) {
        return symmetric_difference(other);
    }
      private PyObject __repr__() {
        return new PyString(this.toString());
    }
    
    private PyObject __str__() {
        return new PyString(this.toString());
    }
    
    private PyObject __hash__() {
        return new PyInt(this.hashCode());
    }
    
    // Set operation methods (all return new frozensets since this is immutable)
    private PyObject copy() {
        return this; // Since it's immutable, return self
    }
    
    private PyObject union(List<PyObject> args) {
        Set<PyObject> result = new HashSet<>(elements);
        for (PyObject arg : args) {
            Iterator<PyObject> iterator = arg.iterator();
            while (iterator.hasNext()) {
                result.add(iterator.next());
            }
        }
        return new PyFrozenSet(result);
    }
    
    private PyObject intersection(List<PyObject> args) {
        if (args.isEmpty()) {
            return new PyFrozenSet(new HashSet<>(elements));
        }
        
        Set<PyObject> result = new HashSet<>(elements);
        for (PyObject arg : args) {
            Set<PyObject> argSet = new HashSet<>();
            Iterator<PyObject> iterator = arg.iterator();
            while (iterator.hasNext()) {
                argSet.add(iterator.next());
            }
            
            result.retainAll(argSet);
        }
        
        return new PyFrozenSet(result);
    }
    
    private PyObject difference(List<PyObject> args) {
        Set<PyObject> result = new HashSet<>(elements);
        for (PyObject arg : args) {
            Iterator<PyObject> iterator = arg.iterator();
            while (iterator.hasNext()) {
                result.remove(iterator.next());
            }
        }
        return new PyFrozenSet(result);
    }
    
    private PyObject symmetric_difference(PyObject other) {
        Set<PyObject> result = new HashSet<>(elements);
        Set<PyObject> otherSet = new HashSet<>();
        
        Iterator<PyObject> iterator = other.iterator();
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
        
        return new PyFrozenSet(result);
    }
    
    private PyObject issubset(PyObject other) {
        if (!(other instanceof PyFrozenSet || other instanceof PySet)) {
            throw new RuntimeException("'issubset' requires a set as an argument");
        }
        
        Set<PyObject> otherSet;
        if (other instanceof PyFrozenSet) {
            otherSet = ((PyFrozenSet) other).elements;
        } else {
            otherSet = ((PySet) other).getElements();
        }
        
        return PyBool.valueOf(otherSet.containsAll(elements));
    }
    
    private PyObject issuperset(PyObject other) {
        if (!(other instanceof PyFrozenSet || other instanceof PySet)) {
            throw new RuntimeException("'issuperset' requires a set as an argument");
        }
        
        Set<PyObject> otherSet;
        if (other instanceof PyFrozenSet) {
            otherSet = ((PyFrozenSet) other).elements;
        } else {
            otherSet = ((PySet) other).getElements();
        }
        
        return PyBool.valueOf(elements.containsAll(otherSet));
    }
    
    private PyObject isdisjoint(PyObject other) {
        Iterator<PyObject> iterator = other.iterator();
        while (iterator.hasNext()) {
            if (elements.contains(iterator.next())) {
                return PyBool.FALSE;
            }
        }
        return PyBool.TRUE;
    }
    
    // Helper methods
    public boolean contains(PyObject item) {
        return elements.contains(item);
    }
    
    // Java Object.equals() and hashCode() for proper HashMap functionality
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PyFrozenSet that = (PyFrozenSet) obj;
        return elements.equals(that.elements);
    }
    
    @Override
    public int hashCode() {
        return elements.hashCode();
    }
}

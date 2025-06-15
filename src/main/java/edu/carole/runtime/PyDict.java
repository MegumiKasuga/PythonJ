package edu.carole.runtime;

import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.base.PyObjectWithMethods;
import edu.carole.runtime.registry.MethodRegistry;
import edu.carole.runtime.registry.MethodBuilder;

import java.util.*;

/**
 * Python dict type using method registration system
 */
public class PyDict extends PyObjectWithMethods {
    private final Map<PyObject, PyObject> entries;
    
    public PyDict(Map<PyObject, PyObject> entries) {
        this.entries = new HashMap<>(entries);
    }
    
    public PyDict() {
        this.entries = new HashMap<>();
    }
    
    @Override
    protected void registerMethods() {
        MethodRegistry methodRegistry = getMethodRegistry();
        
        // Magic methods
        methodRegistry.registerMethod("__eq__", MethodBuilder.oneArg(this::__eq__));
        methodRegistry.registerMethod("__ne__", MethodBuilder.oneArg(this::__ne__));
        methodRegistry.registerMethod("__len__", MethodBuilder.noArgs(this::__len__));
        methodRegistry.registerMethod("__getitem__", MethodBuilder.oneArg(this::__getitem__));
        methodRegistry.registerMethod("__setitem__", MethodBuilder.twoArgs(args -> {
            this.setItem(args[0], args[1]);
            return PyNone.INSTANCE;
        }));
        methodRegistry.registerMethod("__contains__", MethodBuilder.oneArg(this::__contains__));
        methodRegistry.registerMethod("__iter__", MethodBuilder.noArgs(this::__iter__));
        methodRegistry.registerMethod("__delitem__", MethodBuilder.oneArg(this::__delitem__));
        methodRegistry.registerMethod("__repr__", MethodBuilder.noArgs(this::__repr__));
        methodRegistry.registerMethod("__str__", MethodBuilder.noArgs(this::__str__));
        
        // Dict methods
        methodRegistry.registerMethod("keys", MethodBuilder.noArgs(this::keys));
        methodRegistry.registerMethod("values", MethodBuilder.noArgs(this::values));
        methodRegistry.registerMethod("items", MethodBuilder.noArgs(this::items));
        methodRegistry.registerMethod("get", MethodBuilder.varArgs(args -> this.get(args)));
        methodRegistry.registerMethod("pop", MethodBuilder.varArgs(args -> this.pop(args)));
        methodRegistry.registerMethod("popitem", MethodBuilder.noArgs(this::popitem));
        methodRegistry.registerMethod("clear", MethodBuilder.noArgs(this::clear));
        methodRegistry.registerMethod("update", MethodBuilder.oneArg(this::update));
        methodRegistry.registerMethod("setdefault", MethodBuilder.varArgs(args -> this.setdefault(args)));
        methodRegistry.registerMethod("copy", MethodBuilder.noArgs(this::copy));
        
        // Static methods
        methodRegistry.registerStaticMethod("fromkeys", MethodBuilder.varArgs(PyDict::fromkeys));
    }
    
    // Static method implementation
    private static PyObject fromkeys(List<PyObject> args, Interpreter interpreter) {
        if (args.size() < 1 || args.size() > 2) {
            throw new RuntimeException("fromkeys() takes 1 or 2 positional arguments but " + args.size() + " were given");
        }
        
        PyObject iterable = args.get(0);
        PyObject defaultValue = args.size() > 1 ? args.get(1) : PyNone.INSTANCE;
        
        PyDict result = new PyDict();
        Iterator<PyObject> iterator = iterable.iterator(interpreter);
        while (iterator.hasNext()) {
            PyObject key = iterator.next();
            result.entries.put(key, defaultValue);
        }
        
        return result;
    }
    
    // Accessors
    public Map<PyObject, PyObject> getEntries() { 
        return entries; 
    }
    
    @Override
    public String getTypeName() { 
        return "dict"; 
    }
    
    @Override
    public String toString() {
        if (entries.isEmpty()) return "{}";
        
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<PyObject, PyObject> entry : entries.entrySet()) {
            if (!first) sb.append(", ");
            first = false;
            
            PyObject key = entry.getKey();
            PyObject value = entry.getValue();
            
            if (key instanceof PyString) {
                sb.append("'").append(key.toString()).append("'");
            } else {
                sb.append(key.toString());
            }
            
            sb.append(": ");
            
            if (value instanceof PyString) {
                sb.append("'").append(value.toString()).append("'");
            } else {
                sb.append(value.toString());
            }
        }
        sb.append("}");
        return sb.toString();
    }
    
    @Override
    public boolean isTruthy() { 
        return !entries.isEmpty(); 
    }
    
    public PyObject len() {
        return new PyInt(entries.size());
    }    public PyObject getItem(PyObject key) {
        // Try direct lookup
        PyObject value = entries.get(key);
        
        // If not found, try string comparison for PyStringNew objects
        if (value == null && key instanceof PyString) {
            String keyStr = ((PyString) key).getValue();
            for (Map.Entry<PyObject, PyObject> entry : entries.entrySet()) {
                if (entry.getKey() instanceof PyString) {
                    String existingKey = ((PyString) entry.getKey()).getValue();
                    if (existingKey.equals(keyStr)) {
                        return entry.getValue();
                    }
                }
            }
            throw new RuntimeException("KeyError: " + key.toString());
        }
        
        if (value == null) {
            throw new RuntimeException("KeyError: " + key.toString());
        }
        return value;
    }    public void setItem(PyObject key, PyObject value) {
        entries.put(key, value);
    }
    
    public Iterator<PyObject> iterator() {
        return entries.keySet().iterator();
    }
      // Magic method implementations
    private PyObject __eq__(PyObject other) {
        if (other instanceof PyDict) {
            Map<PyObject, PyObject> otherEntries = ((PyDict) other).getEntries();
            if (this.entries.size() != otherEntries.size()) {
                return PyBool.FALSE;
            }
            
            // Check each key-value pair
            for (Map.Entry<PyObject, PyObject> entry : this.entries.entrySet()) {
                PyObject key = entry.getKey();
                PyObject value = entry.getValue();
                
                // Try to find matching key in other dict
                boolean found = false;
                
                // Direct lookup first
                PyObject otherValue = otherEntries.get(key);
                if (otherValue != null && value.equals(otherValue)) {
                    found = true;
                } 
                
                // If not found and key is string, try string comparison
                if (!found && key instanceof PyString) {
                    String keyStr = ((PyString) key).getValue();
                    for (Map.Entry<PyObject, PyObject> otherEntry : otherEntries.entrySet()) {
                        if (otherEntry.getKey() instanceof PyString) {
                            String otherKeyStr = ((PyString) otherEntry.getKey()).getValue();
                            if (keyStr.equals(otherKeyStr)) {
                                if (value.equals(otherEntry.getValue())) {
                                    found = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                
                if (!found) {
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
    
    private PyObject __len__() {
        return new PyInt(this.entries.size());
    }    private PyObject __getitem__(PyObject key) {
        return this.getItem(key);
    }
    
    private PyObject __contains__(PyObject key) {
        return PyBool.valueOf(this.entries.containsKey(key));
    }
    
    private PyObject __iter__() {
        return new PyIterator(entries.keySet().iterator(), "dict");
    }    private PyObject __delitem__(PyObject key) {
        this.delItem(key);
        return PyNone.INSTANCE;
    }
    
    private PyObject __repr__() {
        return new PyString(this.toString());
    }    private PyObject __str__() {
        return new PyString(this.toString());
    }
    
    // Dict method implementations
    public PyObject keys() {
        return new PyList(new ArrayList<>(entries.keySet()));
    }
    
    public PyObject values() {
        return new PyList(new ArrayList<>(entries.values()));
    }
    
    public PyObject items() {
        List<PyObject> items = new ArrayList<>();
        for (Map.Entry<PyObject, PyObject> entry : entries.entrySet()) {
            List<PyObject> pair = new ArrayList<>();
            pair.add(entry.getKey());
            pair.add(entry.getValue());
            items.add(new PyTuple(pair));
        }
        return new PyList(items);
    }
    
    private PyObject get(List<PyObject> args) {
        if (args.size() == 1) {
            return this.get(args.get(0), null);
        } else if (args.size() == 2) {
            return this.get(args.get(0), args.get(1));
        } else {
            throw new RuntimeException("get() takes 1 or 2 arguments (" + args.size() + " given)");
        }
    }
    
    public PyObject get(PyObject key, PyObject defaultValue) {
        PyObject value = entries.get(key);
        if (value == null) {
            return defaultValue != null ? defaultValue : PyNone.INSTANCE;
        }
        return value;
    }
    
    private PyObject pop(List<PyObject> args) {
        if (args.size() == 1) {
            return this.pop(args.get(0), null);
        } else if (args.size() == 2) {
            return this.pop(args.get(0), args.get(1));
        } else {
            throw new RuntimeException("pop() takes 1 or 2 arguments (" + args.size() + " given)");
        }
    }
    
    public PyObject pop(PyObject key, PyObject defaultValue) {
        if (entries.containsKey(key)) {
            return entries.remove(key);
        } else if (defaultValue != null) {
            return defaultValue;
        } else {
            throw new RuntimeException("KeyError: " + key.toString());
        }
    }
    
    public PyObject popitem() {
        if (entries.isEmpty()) {
            throw new RuntimeException("KeyError: 'popitem(): dictionary is empty'");
        }
        Map.Entry<PyObject, PyObject> entry = entries.entrySet().iterator().next();
        PyObject key = entry.getKey();
        PyObject value = entry.getValue();
        entries.remove(key);
        
        List<PyObject> pair = new ArrayList<>();
        pair.add(key);
        pair.add(value);
        return new PyTuple(pair);
    }    private PyObject clear() {
        entries.clear();
        return PyNone.INSTANCE;
    }
    
    private PyObject update(PyObject other, Interpreter interpreter) {
        if (other instanceof PyDict) {
            PyDict otherDict = (PyDict) other;
            entries.putAll(otherDict.getEntries());
        } else {
            // Handle iterable of key-value pairs
            Iterator<PyObject> iterator = other.iterator(interpreter);
            while (iterator.hasNext()) {
                PyObject item = iterator.next();
                if (item instanceof PyTuple) {
                    PyTuple tuple = (PyTuple) item;
                    List<PyObject> elements = tuple.getElements();
                    if (elements.size() == 2) {
                        entries.put(elements.get(0), elements.get(1));
                    } else {
                        throw new RuntimeException("ValueError: dictionary update sequence element must have length 2");
                    }
                } else {
                    throw new RuntimeException("TypeError: cannot convert dictionary update sequence element to a sequence");
                }
            }
        }
        return PyNone.INSTANCE;
    }
    
    private PyObject setdefault(List<PyObject> args) {
        if (args.size() == 1) {
            return this.setdefault(args.get(0), null);
        } else if (args.size() == 2) {
            return this.setdefault(args.get(0), args.get(1));
        } else {
            throw new RuntimeException("setdefault() takes 1 or 2 arguments (" + args.size() + " given)");
        }
    }
    
    public PyObject setdefault(PyObject key, PyObject defaultValue) {
        if (entries.containsKey(key)) {
            return entries.get(key);
        } else {
            PyObject value = defaultValue != null ? defaultValue : PyNone.INSTANCE;
            entries.put(key, value);
            return value;
        }
    }
    
    public PyObject copy() {
        return new PyDict(new HashMap<>(entries));
    }
    
    // Helper methods
    public boolean contains(PyObject item) {
        return entries.containsKey(item);
    }
    
    public void delItem(PyObject key) {
        if (!entries.containsKey(key)) {
            throw new RuntimeException("KeyError: " + key.toString());
        }
        entries.remove(key);
    }
}

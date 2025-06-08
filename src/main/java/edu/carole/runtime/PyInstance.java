package edu.carole.runtime;

import java.util.*;

/**
 * Python实例对象
 */
public class PyInstance extends PyObject {
    private final PyClass pyClass;
    private final Map<String, PyObject> attributes;
    
    public PyInstance(PyClass pyClass) {
        this.pyClass = pyClass;
        this.attributes = new HashMap<>();
    }
    
    @Override
    public String getTypeName() { return pyClass.getName(); }
    
    @Override
    public String toString() { 
        return "<" + pyClass.getName() + " object>"; 
    }
    
    @Override
    public boolean isTruthy() { return true; }

    public PyObject getMethod(String name) {
        return pyClass.findMethod(name);
    }

    public Map<String, PyObject> getAttributes() {
        return attributes;
    }

    @Override
    public PyObject getAttribute(String name) {
        // 首先检查实例属性
        PyObject attribute = attributes.get(name);
        if (attribute != null) {
            return attribute;
        }
        
        // 然后按MRO顺序检查类方法
        PyObject method = pyClass.findMethod(name);
        if (method != null) {
            // If it's a function, bind it to this instance
            if (method instanceof PyFunction) {
                return ((PyFunction) method).bindToInstance(this);
            }
            // For other method types (including decorated methods)
            return new PyBoundMethod(this, method);
        }
        
        throw new RuntimeException("'" + pyClass.getName() + "' object has no attribute '" + name + "'");
    }
    
    @Override
    public void setAttribute(String name, PyObject value) {
        attributes.put(name, value);
    }
    
    public PyClass getPyClass() { return pyClass; }
}

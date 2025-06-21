package edu.carole.runtime.instance;

import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.InstanceBindable;
import edu.carole.runtime.clazz.PyClass;
import edu.carole.runtime.PyObject;
import lombok.Getter;

import java.util.*;

/**
 * Python实例对象
 */
public class PyInstance extends PyObject {

    private final PyClass pyClass;
    
    public PyInstance(PyClass pyClass) {
        super();
        this.pyClass = pyClass;
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
    public PyObject getAttribute(Interpreter interpreter, String name) {
        // 首先检查实例属性
        PyObject attribute = attributes.get(name);
        if (attribute != null) {
            return attribute;
        }
        
        // 然后按MRO顺序检查类方法
        PyObject method = pyClass.findMethod(name);
        if (method != null) {
            // If it's a function, bind it to this instance
            if (method instanceof InstanceBindable pf) {
                return pf.bindToInstance(interpreter, this);
            }
            return method;
        }
        
        throw new RuntimeException("'" + pyClass.getName() + "' object has no attribute '" + name + "'");
    }
    
    @Override
    public void setAttribute(Interpreter interpreter, String name, PyObject value) {
        attributes.put(name, value);
    }
    
    public PyClass getPyClass() { return pyClass; }
}

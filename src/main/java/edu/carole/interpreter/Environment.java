package edu.carole.interpreter;

import edu.carole.runtime.PyObject;
import edu.carole.runtime.PyClass;
import edu.carole.runtime.PyInstance;

import java.util.HashMap;
import java.util.Map;

/**
 * 环境类，管理变量的作用域
 */
public class Environment {
    private final Environment enclosing;
    private final Map<String, PyObject> values = new HashMap<>();
    private PyClass currentClass; // 当前类上下文（用于super()）
    private PyInstance currentInstance; // 当前实例上下文（用于super()）
    
    public Environment() {
        this.enclosing = null;
    }
    
    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }
    
    /**
     * 定义变量
     */
    public void define(String name, PyObject value) {
        values.put(name, value);
    }
    
    /**
     * 获取变量
     */
    public PyObject get(String name) {
        if (values.containsKey(name)) {
            return values.get(name);
        }
          if (enclosing != null) {
            return enclosing.get(name);
        }
        
        throw new Interpreter.PyExceptionWrapper(
            edu.carole.runtime.PyException.nameError("Undefined variable '" + name + "'")
        );
    }
    
    /**
     * 赋值给已存在的变量
     */
    public void assign(String name, PyObject value) {
        if (values.containsKey(name)) {
            values.put(name, value);
            return;
        }
          if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        
        throw new Interpreter.PyExceptionWrapper(
            edu.carole.runtime.PyException.nameError("Undefined variable '" + name + "'")
        );
    }
    
    /**
     * 检查变量是否存在
     */
    public boolean isDefined(String name) {
        if (values.containsKey(name)) {
            return true;
        }
        
        if (enclosing != null) {
            return enclosing.isDefined(name);
        }
        
        return false;
    }
      /**
     * 获取所有变量名
     */
    public Map<String, PyObject> getValues() {
        return new HashMap<>(values);
    }
    
    /**
     * 设置当前类上下文（用于super()）
     */
    public void setCurrentClass(PyClass currentClass) {
        this.currentClass = currentClass;
    }
    
    /**
     * 设置当前实例上下文（用于super()）
     */
    public void setCurrentInstance(PyInstance currentInstance) {
        this.currentInstance = currentInstance;
    }
    
    /**
     * 获取当前类上下文（用于super()）
     */
    public PyClass getCurrentClass() {
        if (currentClass != null) {
            return currentClass;
        }
        if (enclosing != null) {
            return enclosing.getCurrentClass();
        }
        return null;
    }
    
    /**
     * 获取当前实例上下文（用于super()）
     */
    public PyInstance getCurrentInstance() {
        if (currentInstance != null) {
            return currentInstance;
        }
        if (enclosing != null) {
            return enclosing.getCurrentInstance();
        }
        return null;
    }
}

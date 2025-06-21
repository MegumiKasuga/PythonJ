package edu.carole.runtime;

import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.exception.ExceptionWrapper;
import edu.carole.runtime.func.PyBuiltinFunction;
import edu.carole.runtime.instance.PyInstance;

import java.util.*;

/**
 * Python对象的基类
 */
public abstract class PyObject {

    public abstract String getTypeName();
    public abstract String toString();
    public abstract boolean isTruthy();

    public Map<String, PyObject> attributes;

    public PyObject() {
        attributes = new HashMap<>();
        initAttributes(attributes);
    }

    public void initAttributes(Map<String, PyObject> attr) {
        attr.put("__str__", new PyBuiltinFunction("__str__", (args, kwargs, interpreter) -> new PyString(this.toString())));
        attr.put("__repr__", new PyBuiltinFunction("__repr__", (args, kwargs, interpreter) -> new PyString(this.toString())));
        attr.put("__bool__", new PyBuiltinFunction("__bool__", (args, kwargs, interpreter) -> PyBool.valueOf(this.isTruthy())));
    }

      /**
     * 获取属性
     */
    public PyObject getAttribute(Interpreter interpreter, String name) {
        // 提供一些默认的魔术方法
        PyObject result = attributes.getOrDefault(name, null);
        if (result == null) {
            PyInstance ins = (PyInstance) interpreter.getExceptions().
                    createExceptionInstance("KeyError", List.of());
            ExceptionWrapper wrapper = new ExceptionWrapper(ins);
            wrapper.addNote(interpreter, "'" + getTypeName() + "' object has no attribute '" + name + "'");
            throw wrapper;
        }
        return result;
    }
    
    /**
     * 设置属性
     */
    public void setAttribute(Interpreter interpreter, String name, PyObject value) {
        throw new RuntimeException("'" + getTypeName() + "' object has no attribute '" + name + "'");
    }
    
    /**
     * 调用对象（带解释器上下文）
     */
    public PyObject call(List<PyObject> arguments, edu.carole.interpreter.Interpreter interpreter) {
        // Default implementation falls back to regular call
        return call(arguments, null, interpreter);
    }
    
    /**
     * 调用对象（带关键字参数和解释器上下文）
     */
    public PyObject call(List<PyObject> arguments, Map<String, PyObject> keywordArguments, edu.carole.interpreter.Interpreter interpreter) {
        throw new RuntimeException("'" + getTypeName() + "' object doesn't support keyword arguments");
    }
    
    /**
     * 获取索引
     */
    public PyObject getItem(PyObject key) {
        throw new RuntimeException("'" + getTypeName() + "' object is not subscriptable");
    }
      /**
     * 设置索引
     */
    public void setItem(PyObject key, PyObject value) {
        throw new RuntimeException("'" + getTypeName() + "' object does not support item assignment");
    }
    
    /**
     * 获取切片
     */
    public PyObject getSlice(PyObject start, PyObject stop, PyObject step) {
        throw new RuntimeException("'" + getTypeName() + "' object is not subscriptable");
    }
    
    /**
     * 设置切片
     */
    public void setSlice(PyObject start, PyObject stop, PyObject step, PyObject value) {
        throw new RuntimeException("'" + getTypeName() + "' object does not support slice assignment");
    }
    
    /**
     * 获取长度
     */
    public PyObject len() {
        throw new RuntimeException("object of type '" + getTypeName() + "' has no len()");
    }
    
    /**
     * 迭代器
     */
    public Iterator<PyObject> iterator(Interpreter interpreter) {
        try {
            getAttribute(interpreter, "__iter__");
            getAttribute(interpreter, "__next__");
        } catch (RuntimeException e) {
            // 如果没有__iter__或__next__方法，抛出异常
            PyInstance ins = (PyInstance) interpreter.getExceptions().
                    createExceptionInstance("TypeError", List.of());
            ExceptionWrapper wrapper = new ExceptionWrapper(ins);
            wrapper.addNote(interpreter, "'" + getTypeName() + "' object is not iterable");
            throw wrapper;
        }
        return new PyIterator(interpreter, this, getTypeName());
    }
      /**
     * 相等性比较
     */
    public boolean equals(PyObject other) {
        return this == other;
    }
    
    /**
     * 上下文管理器协议：进入上下文
     */
    public PyObject contextEnter(Interpreter interpreter) {
        PyObject enterFunc = getAttribute(interpreter, "__enter__");
        return enterFunc.call(List.of(), interpreter);
    }


    public PyObject contextExit(Interpreter interpreter) {
        throw new RuntimeException("'" + getTypeName() + "' object does not support the context manager protocol");
    }
}

package edu.carole.runtime;

import java.util.*;

/**
 * Python对象的基类
 */
public abstract class PyObject {
    public abstract String getTypeName();
    public abstract String toString();
    public abstract boolean isTruthy();
      /**
     * 获取属性
     */
    public PyObject getAttribute(String name) {
        // 提供一些默认的魔术方法
        return switch (name) {
            case "__str__" -> new PyBuiltinFunction("__str__", (args, kwargs) -> new PyString(this.toString()));
            case "__repr__" -> new PyBuiltinFunction("__repr__", (args, kwargs) -> new PyString(this.toString()));
            case "__bool__" -> new PyBuiltinFunction("__bool__", (args, kwargs) -> PyBool.valueOf(this.isTruthy()));
            default -> throw new RuntimeException("'" + getTypeName() + "' object has no attribute '" + name + "'");
        };
    }
    
    /**
     * 设置属性
     */
    public void setAttribute(String name, PyObject value) {
        throw new RuntimeException("'" + getTypeName() + "' object has no attribute '" + name + "'");
    }
      /**
     * 调用对象
     */
    public PyObject call(List<PyObject> arguments) {
        throw new RuntimeException("'" + getTypeName() + "' object is not callable");
    }
    
    /**
     * 调用对象（带解释器上下文）
     */
    public PyObject call(List<PyObject> arguments, edu.carole.interpreter.Interpreter interpreter) {
        // Default implementation falls back to regular call
        return call(arguments);
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
    public Iterator<PyObject> iterator() {
        try {
            getAttribute("__iter__");
            getAttribute("__next__");
        } catch (RuntimeException e) {
            // 如果没有__iter__或__next__方法，抛出异常
            throw new RuntimeException("'" + getTypeName() + "' object is not iterable");
        }
        return new PyIterator(this, getTypeName());
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
    public PyObject contextEnter() {
        throw new RuntimeException("'" + getTypeName() + "' object does not support the context manager protocol");
    }
    
    /**
     * 上下文管理器协议：退出上下文
     * @param exceptionType 异常类型（如果有）
     * @param exceptionValue 异常值（如果有）
     * @param traceback 异常追踪（如果有）
     * @return 是否抑制异常（True表示抑制，False表示传播）
     */
    public PyObject contextExit(PyObject exceptionType, PyObject exceptionValue, PyObject traceback) {
        throw new RuntimeException("'" + getTypeName() + "' object does not support the context manager protocol");
    }
}

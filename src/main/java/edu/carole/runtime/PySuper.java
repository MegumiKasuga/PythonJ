package edu.carole.runtime;

import java.util.*;

/**
 * Python super()对象
 * 用于访问父类方法
 */
public class PySuper extends PyObject {
    private final PyClass targetClass; // 目标类
    private final PyInstance instance;  // 实例对象
    private final List<PyClass> mro;    // 方法解析顺序
    private final int startIndex;       // 从MRO的哪个位置开始查找
    
    public PySuper(PyClass targetClass, PyInstance instance) {
        this(targetClass, instance, null);
    }
    
    public PySuper(PyClass targetClass, PyInstance instance, String specificClassName) {
        this.targetClass = targetClass;
        this.instance = instance;
        this.mro = instance.getPyClass().getMRO();
        
        // 找到目标类在MRO中的位置，从下一个位置开始查找
        this.startIndex = findStartIndex(specificClassName);
    }
      private int findStartIndex(String specificClassName) {
        if (specificClassName != null) {
            // If a specific class name is provided, find that class in MRO
            for (int i = 0; i < mro.size(); i++) {
                if (mro.get(i).getName().equals(specificClassName)) {
                    return i + 1; // Start from the next class
                }
            }
            throw new RuntimeException("Class '" + specificClassName + "' not found in method resolution order");
        } else {
            // Default behavior: find the target class in MRO
            for (int i = 0; i < mro.size(); i++) {
                if (mro.get(i) == targetClass) {
                    return i + 1; // 从下一个类开始
                }
            }
            return mro.size(); // 如果没找到，从末尾开始（即不查找）
        }
    }
    
    @Override
    public String getTypeName() { 
        return "super"; 
    }
    
    @Override
    public String toString() { 
        return "<super: " + targetClass.getName() + ", " + instance.toString() + ">"; 
    }
    
    @Override
    public boolean isTruthy() { 
        return true; 
    }
    
    @Override
    public PyObject getAttribute(String name) {
        // 从startIndex开始在MRO中查找方法
        for (int i = startIndex; i < mro.size(); i++) {
            PyClass cls = mro.get(i);
            PyObject method = cls.getMethods().get(name);
            if (method != null) {
                // 返回绑定到原实例的方法
                return new PyBoundMethod(instance, method);
            }
        }
        
        throw new RuntimeException("'super' object has no attribute '" + name + "'");
    }
}

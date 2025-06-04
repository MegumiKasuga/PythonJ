package edu.carole.runtime;

import java.util.*;

/**
 * Python类对象
 */
public class PyClass extends PyObject {
    private final String name;
    private final Map<String, PyObject> methods;
    private final List<PyClass> baseClasses; // 直接父类列表
    private List<PyClass> mro; // 方法解析顺序 (Method Resolution Order)
    
    public PyClass(String name, Map<String, PyObject> methods) {
        this.name = name;
        this.methods = new HashMap<>(methods);
        this.baseClasses = new ArrayList<>();
        computeMRO();
    }
    
    public PyClass(String name, Map<String, PyObject> methods, List<PyClass> baseClasses) {
        this.name = name;
        this.methods = new HashMap<>(methods);
        this.baseClasses = new ArrayList<>(baseClasses);
        computeMRO();
    }
    
    /**
     * 计算方法解析顺序 (MRO)
     * 使用简化的C3线性化算法
     */
    private void computeMRO() {
        mro = new ArrayList<>();
        mro.add(this); // 自己首先
        
        // 深度优先搜索添加父类
        Set<PyClass> visited = new HashSet<>();
        for (PyClass baseClass : baseClasses) {
            addToMRO(baseClass, visited);
        }
    }
    
    private void addToMRO(PyClass cls, Set<PyClass> visited) {
        if (visited.contains(cls)) {
            return; // 避免循环
        }
        visited.add(cls);
        
        // 先添加父类的父类
        for (PyClass baseClass : cls.baseClasses) {
            addToMRO(baseClass, visited);
        }
        
        // 然后添加自己(如果还没添加)
        if (!mro.contains(cls)) {
            mro.add(cls);
        }
    }
    
    @Override
    public String getTypeName() { return "type"; }
    
    @Override
    public String toString() { return "<class '" + name + "'>"; }
    
    @Override
    public boolean isTruthy() { return true; }      @Override
    public PyObject getAttribute(String attributeName) {
        // 按MRO顺序查找方法
        for (PyClass cls : mro) {
            PyObject method = cls.methods.get(attributeName);
            if (method != null) {
                return method;
            }
        }
        throw new RuntimeException("type object '" + name + "' has no attribute '" + attributeName + "'");
    }
    
    /**
     * Finds a method in this class or its parent classes
     * Does not throw exception if not found
     */
    public PyObject findMethod(String methodName) {
        // Look in the method resolution order
        for (PyClass cls : mro) {
            PyObject method = cls.methods.get(methodName);
            if (method != null) {
                return method;
            }
        }
        return null;
    }
    
    @Override
    public PyObject call(List<PyObject> arguments) {
        return call(arguments, null);
    }
      @Override
    public PyObject call(List<PyObject> arguments, edu.carole.interpreter.Interpreter interpreter) {
        // 创建实例
        PyInstance instance = new PyInstance(this);
        
        // 调用__init__方法（如果存在）
        PyObject initMethod = findMethod("__init__");
        if (initMethod != null) {
            // Create a bound method to ensure proper context setting
            PyBoundMethod boundInit = new PyBoundMethod(instance, initMethod);
            
            // Use interpreter-aware call if available
            if (interpreter != null) {
                boundInit.call(arguments, interpreter);
            } else {
                boundInit.call(arguments);
            }
        }
        
        return instance;
    }
    
    public String getName() { return name; }
    public Map<String, PyObject> getMethods() { return methods; }
    public List<PyClass> getBaseClasses() { return baseClasses; }
    public List<PyClass> getMRO() { return mro; }
    
    /**
     * 检查是否是指定类的实例或子类
     */
    public boolean isSubclassOf(PyClass other) {
        return mro.contains(other);
    }
}

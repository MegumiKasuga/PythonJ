package edu.carole.runtime;

import edu.carole.interpreter.Environment;
import edu.carole.runtime.base.PyObjectWithMethods;
import edu.carole.runtime.registry.MethodRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Python模块类型 - 表示一个Python模块
 */
public class PyModule extends PyObjectWithMethods {
    private final String name;
    private final String doc;
    private final Map<String, PyObject> attributes = new HashMap<>();
    
    /**
     * 创建一个新的Python模块
     * 
     * @param name 模块名称
     * @param doc 模块文档字符串
     */
    public PyModule(String name, String doc) {
        this.name = name;
        this.doc = doc;
        
        // 设置标准属性
        attributes.put("__name__", new PyString(name));
        attributes.put("__doc__", new PyString(doc));
    }
      /**
     * 创建一个没有文档字符串的新模块
     * 
     * @param name 模块名称
     */
    public PyModule(String name) {
        this(name, "");
    }
    
    /**
     * 创建一个从环境中导入属性的模块
     * 
     * @param name 模块名称
     * @param environment 环境对象，包含模块的属性
     */
    public PyModule(String name, Environment environment) {
        this.name = name;
        this.doc = "";
        
        // 设置标准属性
        attributes.put("__name__", new PyString(name));
        attributes.put("__doc__", new PyString(doc));
          // 从环境中复制所有变量到模块属性
        if (environment != null) {
            Map<String, PyObject> envVars = environment.getValues();
            attributes.putAll(envVars);
        }
    }
    
    @Override
    protected void registerMethods() {
        MethodRegistry methodRegistry = getMethodRegistry();
        
        // 添加标准模块方法
        methodRegistry.registerMethod("__repr__", (Function<List<PyObject>, PyObject>) args -> new PyString("<module '" + name + "'>"));
        methodRegistry.registerMethod("__str__", (Function<List<PyObject>, PyObject>) args -> new PyString("<module '" + name + "'>"));
    }
    
    @Override
    public String getTypeName() {
        return "module";
    }
    
    @Override
    public String toString() {
        return "<module '" + name + "'>";
    }
    
    @Override
    public boolean isTruthy() {
        return true;
    }
    
    /**
     * 获取模块名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取模块文档字符串
     */
    public String getDoc() {
        return doc;
    }
    
    @Override
    public PyObject getAttribute(String name) {
        // 首先检查属性字典
        if (attributes.containsKey(name)) {
            return attributes.get(name);
        }
        
        // 然后检查方法注册表
        return super.getAttribute(name);
    }
    
    @Override
    public void setAttribute(String name, PyObject value) {
        attributes.put(name, value);
    }
    
    /**
     * 获取模块所有属性
     */
    public Map<String, PyObject> getAttributes() {
        return new HashMap<>(attributes);
    }
}

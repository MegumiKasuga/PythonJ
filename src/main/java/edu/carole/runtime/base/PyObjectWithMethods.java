package edu.carole.runtime.base;

import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.PyObject;
import edu.carole.runtime.registry.MethodRegistry;

/**
 * 带有方法注册功能的Python对象基类
 */
public abstract class PyObjectWithMethods extends PyObject {
    protected final MethodRegistry methodRegistry;
    
    public PyObjectWithMethods() {
        this.methodRegistry = new MethodRegistry();
        registerMethods();
    }
    
    /**
     * 子类需要实现此方法来注册自己的方法
     */
    protected abstract void registerMethods();
    
    @Override
    public PyObject getAttribute(Interpreter interpreter, String name) {
        // 首先尝试从方法注册表中获取
        if (methodRegistry.hasMethod(name)) {
            return methodRegistry.getMethod(name);
        }
        
        // 如果注册表中没有，则调用父类方法
        return super.getAttribute(interpreter, name);
    }
    
    /**
     * 获取方法注册表（用于调试或扩展）
     * @return 方法注册表
     */
    public MethodRegistry getMethodRegistry() {
        return methodRegistry;
    }
}

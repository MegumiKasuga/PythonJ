package edu.carole.runtime;

import edu.carole.interpreter.Interpreter;

import java.util.function.Function;
import java.util.List;

/**
 * Python的@staticmethod装饰器实现
 * 用于将方法标记为静态方法，可以直接通过类调用，无需实例
 */
public class PyStaticMethod extends PyObject {
    private final PyBuiltinFunction function;
    
    /**
     * 构造函数
     * @param function 被装饰的函数
     */
    public PyStaticMethod(PyBuiltinFunction function) {
        this.function = function;
    }
    
    /**
     * 构造函数，从函数对象和实现创建
     * @param methodName 方法名
     * @param implementation 方法实现
     */
    public PyStaticMethod(String methodName, Function<List<PyObject>, PyObject> implementation) {
        this.function = new PyBuiltinFunction(methodName, (args, kwargs, inter) -> implementation.apply(args));
    }
    
    /**
     * 获取被装饰的函数
     * @return PyBuiltinFunction对象
     */
    public PyBuiltinFunction getFunction() {
        return function;
    }
    
    /**
     * 调用静态方法
     * @param args 参数列表
     * @return 方法返回值
     */
    public PyObject call(List<PyObject> args, Interpreter interpreter) {
        return function.call(args, interpreter);
    }
    
    /**
     * 获取描述器，用于类属性访问
     * 对于静态方法，直接返回函数本身
     * @param obj 实例对象（对于静态方法忽略）
     * @param type 类对象
     * @return 函数对象
     */
    public PyObject __get__(PyObject obj, PyObject type) {
        return function;
    }
    
    @Override
    public String toString() {
        return "<staticmethod(" + function.toString() + ")>";
    }
    
    @Override
    public String getTypeName() {
        return "staticmethod";
    }
    
    @Override
    public boolean isTruthy() {
        return true; // 装饰器对象本身总是真值
    }
}

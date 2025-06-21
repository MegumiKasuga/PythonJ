package edu.carole.runtime;

import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.func.PyBuiltinFunction;

import java.util.Map;
import java.util.function.Function;
import java.util.List;

/**
 * Python的@abstractmethod装饰器实现
 * 用于将方法标记为抽象方法，必须在子类中实现
 */
public class PyAbstractMethod extends PyObject {
    private final PyBuiltinFunction function;
    private final boolean isAbstract;
    
    /**
     * 构造函数
     * @param function 被装饰的函数
     */
    public PyAbstractMethod(PyBuiltinFunction function) {
        this.function = function;
        this.isAbstract = true;
    }
    
    /**
     * 构造函数，创建一个默认的抽象方法（抛出NotImplementedError）
     * @param methodName 方法名
     */
    public PyAbstractMethod(String methodName) {
        this.function = new PyBuiltinFunction(methodName, (args, kwargs, inter) -> {
            throw new RuntimeException("NotImplementedError: Abstract method '" + methodName + "' must be implemented by subclass");
        });
        this.isAbstract = true;
    }
    
    /**
     * 构造函数，从函数对象和实现创建
     * @param methodName 方法名
     * @param implementation 方法实现（可以为null，表示纯抽象方法）
     */
    public PyAbstractMethod(String methodName, Function<List<PyObject>, PyObject> implementation) {
        if (implementation != null) {
            this.function = new PyBuiltinFunction(methodName, (args, kwargs, inter) -> implementation.apply(args));
        } else {
            this.function = new PyBuiltinFunction(methodName, (args, kwargs, inter) -> {
                throw new RuntimeException("NotImplementedError: Abstract method '" + methodName + "' must be implemented by subclass");
            });
        }
        this.isAbstract = true;
    }
    
    /**
     * 获取被装饰的函数
     * @return PyBuiltinFunction对象
     */
    public PyBuiltinFunction getFunction() {
        return function;
    }
    
    /**
     * 检查是否为抽象方法
     * @return 如果是抽象方法返回true
     */
    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public PyObject call(List<PyObject> arguments, Interpreter interpreter) {
        return function.call(arguments, interpreter);
    }

    @Override
    public PyObject call(List<PyObject> arguments, Map<String, PyObject> keywordArguments, Interpreter interpreter) {
        return function.call(arguments, keywordArguments, interpreter);
    }

    /**
     * 获取描述器，用于类属性访问
     * @param obj 实例对象
     * @param type 类对象
     * @return 函数对象
     */
    public PyObject __get__(PyObject obj, PyObject type) {
        return function;
    }
    
    @Override
    public String toString() {
        return "<abstractmethod(" + function.toString() + ")>";
    }
    
    @Override
    public String getTypeName() {
        return "abstractmethod";
    }
    
    @Override
    public boolean isTruthy() {
        return true; // 装饰器对象本身总是真值
    }
}

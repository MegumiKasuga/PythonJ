package edu.carole.runtime.registry;

import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.func.PyBuiltinFunction;
import edu.carole.runtime.PyObject;
import edu.carole.runtime.PyStaticMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.List;

/**
 * 方法注册表，用于管理Python对象的方法
 */
public class MethodRegistry {
    private final Map<String, PyBuiltinFunction> methods;
    private final Map<String, PyBuiltinFunction> staticMethods;
    private final Map<String, PyBuiltinFunction> abstractMethods;
    
    public MethodRegistry() {
        this.methods = new HashMap<>();
        this.staticMethods = new HashMap<>();
        this.abstractMethods = new HashMap<>();
    }
    
    /**
     * 注册一个方法
     * @param methodName 方法名
     * @param implementation 方法实现
     */
    public void registerMethod(String methodName, Function<List<PyObject>, PyObject> implementation) {
        methods.put(methodName, new PyBuiltinFunction(methodName, (args, kwargs, inter) -> implementation.apply(args)));
    }

    public void registerMethod(String methodName, FunctionSupplier implementation) {
        methods.put(methodName, new PyBuiltinFunction(methodName, (args, kwargs, inter) ->
                implementation.apply(new FunctionParams(args, kwargs, inter))
        ));
    }
    
    /**
     * 注册一个方法（直接传入PyBuiltinFunction）
     * @param methodName 方法名
     * @param function PyBuiltinFunction对象
     */
    public void registerMethod(String methodName, PyBuiltinFunction function) {
        methods.put(methodName, function);
    }
      /**
     * 注册一个静态方法
     * @param methodName 方法名
     * @param implementation 方法实现
     */
    public void registerStaticMethod(String methodName, Function<List<PyObject>, PyObject> implementation) {
        staticMethods.put(methodName, new PyBuiltinFunction(methodName, (args, kwargs, inter) -> implementation.apply(args)));
    }
    
    /**
     * 注册一个静态方法（直接传入PyBuiltinFunction）
     * @param methodName 方法名
     * @param function PyBuiltinFunction对象
     */
    public void registerStaticMethod(String methodName, PyBuiltinFunction function) {
        staticMethods.put(methodName, function);
    }

    public void registerStaticMethod(String methodName, FunctionSupplier supplier) {
        staticMethods.put(methodName, new PyBuiltinFunction(methodName, (args, kwargs, inter) -> {
            return supplier.apply(new FunctionParams(args, kwargs, inter));
        }));
    }
    
    /**
     * 注册一个抽象方法
     * @param methodName 方法名
     * @param implementation 方法实现（如果提供）
     */
    public void registerAbstractMethod(String methodName, Function<List<PyObject>, PyObject> implementation) {
        if (implementation != null) {
            abstractMethods.put(methodName, new PyBuiltinFunction(methodName, (args, kwargs, inter) -> implementation.apply(args)));
        } else {
            // 抽象方法的默认实现，抛出NotImplementedError
            abstractMethods.put(methodName, new PyBuiltinFunction(methodName, (args, kwargs, inter) -> {
                throw new RuntimeException("NotImplementedError: Abstract method '" + methodName + "' must be implemented by subclass");
            }));
        }
    }
    
    /**
     * 注册一个抽象方法（直接传入PyBuiltinFunction）
     * @param methodName 方法名
     * @param function PyBuiltinFunction对象
     */
    public void registerAbstractMethod(String methodName, PyBuiltinFunction function) {
        abstractMethods.put(methodName, function);
    }
    
    /**
     * 注册一个@staticmethod装饰的方法
     * @param methodName 方法名
     * @param staticMethod PyStaticMethod对象
     */
    public void registerStaticMethodDecorator(String methodName, PyStaticMethod staticMethod) {
        staticMethods.put(methodName, staticMethod.getFunction());
    }
    
//    /**
//     * 注册一个@abstractmethod装饰的方法
//     * @param methodName 方法名
//     * @param abstractMethod PyAbstractMethod对象
//     */
//    public void registerAbstractMethodDecorator(String methodName, PyAbstractMethod abstractMethod) {
//        abstractMethods.put(methodName, abstractMethod.getFunction());
//    }
    
    /**
     * 获取静态方法
     * @param methodName 方法名
     * @return PyBuiltinFunction对象，如果不存在则返回null
     */
    public PyBuiltinFunction getStaticMethod(String methodName) {
        return staticMethods.get(methodName);
    }
    
    /**
     * 获取抽象方法
     * @param methodName 方法名
     * @return PyBuiltinFunction对象，如果不存在则返回null
     */
    public PyBuiltinFunction getAbstractMethod(String methodName) {
        return abstractMethods.get(methodName);
    }
    
    /**
     * 获取方法，按优先级查找：实例方法 > 静态方法 > 抽象方法
     * @param methodName 方法名
     * @return PyBuiltinFunction对象，如果不存在则返回null
     */
    public PyBuiltinFunction getMethod(String methodName) {
        // 1. 首先查找实例方法
        PyBuiltinFunction method = methods.get(methodName);
        if (method != null) {
            return method;
        }
        
        // 2. 然后查找静态方法
        method = staticMethods.get(methodName);
        if (method != null) {
            return method;
        }
        
        // 3. 最后查找抽象方法
        return abstractMethods.get(methodName);
    }
    
    /**
     * 检查静态方法是否存在
     * @param methodName 方法名
     * @return 如果静态方法存在返回true，否则返回false
     */
    public boolean hasStaticMethod(String methodName) {
        return staticMethods.containsKey(methodName);
    }
    
    /**
     * 检查抽象方法是否存在
     * @param methodName 方法名
     * @return 如果抽象方法存在返回true，否则返回false
     */
    public boolean hasAbstractMethod(String methodName) {
        return abstractMethods.containsKey(methodName);
    }
    
    /**
     * 检查方法是否存在
     * @param methodName 方法名
     * @return 如果方法存在返回true，否则返回false
     */
    public boolean hasMethod(String methodName) {
        return methods.containsKey(methodName);
    }
    
    /**
     * 获取所有注册的方法名
     * @return 方法名集合
     */
    public java.util.Set<String> getMethodNames() {
        return methods.keySet();
    }
    
    /**
     * 清空所有方法
     */
    public void clear() {
        methods.clear();
    }
    
    /**
     * 复制另一个注册表的所有方法到当前注册表
     * @param other 另一个方法注册表
     */
    public void copyFrom(MethodRegistry other) {
        this.methods.putAll(other.methods);
    }
    
    /**
     * 合并另一个注册表的方法到当前注册表（不会覆盖已存在的方法）
     * @param other 另一个方法注册表
     */
    public void mergeFrom(MethodRegistry other) {
        for (Map.Entry<String, PyBuiltinFunction> entry : other.methods.entrySet()) {
            if (!this.methods.containsKey(entry.getKey())) {
                this.methods.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public record FunctionParams(List<PyObject> positionArgs,
                                 Map<String, PyObject> keywordArgs,
                                 Interpreter interpreter) {}

    @FunctionalInterface
    public interface FunctionSupplier {
        PyObject apply(FunctionParams params);
    }
}

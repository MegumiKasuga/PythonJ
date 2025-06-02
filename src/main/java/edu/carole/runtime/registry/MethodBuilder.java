package edu.carole.runtime.registry;

import edu.carole.runtime.*;
import java.util.List;
import java.util.function.Function;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * 方法构建器，提供便捷的方法创建和参数检查
 */
public class MethodBuilder {
    
    /**
     * 创建一个不接受参数的方法
     */
    public static Function<List<PyObject>, PyObject> noArgs(Supplier<PyObject> impl) {
        return args -> {
            if (!args.isEmpty()) {
                throw new RuntimeException("method takes no arguments (" + args.size() + " given)");
            }
            return impl.get();
        };
    }
    
    /**
     * 创建一个接受一个参数的方法
     */
    public static Function<List<PyObject>, PyObject> oneArg(Function<PyObject, PyObject> impl) {
        return args -> {
            if (args.size() != 1) {
                throw new RuntimeException("method takes exactly 1 argument (" + args.size() + " given)");
            }
            return impl.apply(args.get(0));
        };
    }
    
    /**
     * 创建一个接受两个参数的方法
     */
    public static Function<List<PyObject>, PyObject> twoArgs(Function<PyObject[], PyObject> impl) {
        return args -> {
            if (args.size() != 2) {
                throw new RuntimeException("method takes exactly 2 arguments (" + args.size() + " given)");
            }
            return impl.apply(new PyObject[]{args.get(0), args.get(1)});
        };
    }

    public static Function<List<PyObject>, PyObject> multiArgs(
            final int argSize, Function<PyObject[], PyObject> impl1) {
        return args -> {
            if (args.size() != argSize) {
                throw new RuntimeException("method takes exactly 2 arguments (" + args.size() + " given)");
            }
            PyObject[] arg2 = new PyObject[argSize];
            for (int i = 0; i < argSize; i++) {
                arg2[i] = args.get(i);
            }
            return impl1.apply(arg2);
        };
    }
    
    /**
     * 创建一个接受可变参数的方法
     */
    public static Function<List<PyObject>, PyObject> varArgs(Function<List<PyObject>, PyObject> impl) {
        return impl;
    }
    
    /**
     * 创建一个接受指定数量参数的方法
     */
    public static Function<List<PyObject>, PyObject> exactArgs(int count, Function<List<PyObject>, PyObject> impl) {
        return args -> {
            if (args.size() != count) {
                throw new RuntimeException("method takes exactly " + count + " arguments (" + args.size() + " given)");
            }
            return impl.apply(args);
        };
    }
    
    /**
     * 创建一个接受指定范围参数的方法
     */
    public static Function<List<PyObject>, PyObject> rangeArgs(int min, int max, Function<List<PyObject>, PyObject> impl) {
        return args -> {
            if (args.size() < min || args.size() > max) {
                throw new RuntimeException("method takes from " + min + " to " + max + " arguments (" + args.size() + " given)");
            }
            return impl.apply(args);
        };
    }
    
    /**
     * 创建一个接受可选参数的方法
     */
    public static Function<List<PyObject>, PyObject> optionalArgs(int required, Function<List<PyObject>, PyObject> impl) {
        return args -> {
            if (args.size() < required) {
                throw new RuntimeException("method takes at least " + required + " arguments (" + args.size() + " given)");
            }
            return impl.apply(args);
        };
    }
    
    /**
     * 类型检查辅助方法
     */
    public static void checkType(PyObject obj, Class<?> expectedType, String paramName) {
        if (!expectedType.isInstance(obj)) {
            throw new RuntimeException(paramName + " must be " + expectedType.getSimpleName() + 
                                     ", not " + obj.getTypeName());
        }
    }
    
    /**
     * 检查参数是否为指定类型
     */
    public static <T extends PyObject> T requireType(PyObject obj, Class<T> expectedType, String paramName) {
        if (!expectedType.isInstance(obj)) {
            throw new RuntimeException(paramName + " must be " + expectedType.getSimpleName() + 
                                     ", not " + obj.getTypeName());
        }
        return expectedType.cast(obj);
    }
}

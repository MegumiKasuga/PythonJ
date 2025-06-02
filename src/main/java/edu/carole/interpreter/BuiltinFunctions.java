package edu.carole.interpreter;

import edu.carole.runtime.*;

import java.io.*;
import java.util.*;

/**
 * Python内置函数和全局对象
 */
public class BuiltinFunctions {
    
    /**
     * 创建包含所有内置函数的全局环境
     */
    public static Environment createGlobalEnvironment() {
        Environment globals = new Environment();
          // print函数
        globals.define("print", new PyBuiltinFunction("print", args -> {
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < args.size(); i++) {
                if (i > 0) output.append(" ");
                output.append(args.get(i).toString());
            }
            
            // 使用IOManager的控制台输出流
            PrintStream outputStream = edu.carole.runtime.io.IOManager.getInstance().getConsoleOutputStream();
            outputStream.println(output.toString());
            return PyNone.INSTANCE;
        }));
        
        // len函数
        globals.define("len", new PyBuiltinFunction("len", args -> {
            if (args.size() != 1) {
                throw new RuntimeException("len() takes exactly one argument (" + args.size() + " given)");
            }
            return args.get(0).len();
        }));
        
        // type函数
        globals.define("type", new PyBuiltinFunction("type", args -> {
            if (args.size() != 1) {
                throw new RuntimeException("type() takes exactly one argument (" + args.size() + " given)");
            }
            return new PyString(args.get(0).getTypeName());
        }));
        
        // str函数
        globals.define("str", new PyBuiltinFunction("str", args -> {
            if (args.size() == 0) {
                return new PyString("");
            } else if (args.size() == 1) {
                return new PyString(args.get(0).toString());
            } else {
                throw new RuntimeException("str() takes at most 1 argument (" + args.size() + " given)");
            }
        }));
        
        // int函数
        globals.define("int", new PyBuiltinFunction("int", args -> {
            if (args.size() == 0) {
                return new PyInt(0);
            } else if (args.size() == 1) {
                PyObject arg = args.get(0);
                if (arg instanceof PyInt) {
                    return arg;
                } else if (arg instanceof PyFloat) {
                    return new PyInt((long) ((PyFloat) arg).getValue());
                } else if (arg instanceof PyString) {
                    try {
                        return new PyInt(Long.parseLong(((PyString) arg).getValue().trim()));
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("invalid literal for int(): '" + ((PyString) arg).getValue() + "'");
                    }
                } else if (arg instanceof PyBool) {
                    return new PyInt(((PyBool) arg).getValue() ? 1 : 0);
                } else {
                    throw new RuntimeException("int() argument must be a string or a number, not '" + arg.getTypeName() + "'");
                }
            } else {
                throw new RuntimeException("int() takes at most 1 argument (" + args.size() + " given)");
            }
        }));
        
        // float函数
        globals.define("float", new PyBuiltinFunction("float", args -> {
            if (args.size() == 0) {
                return new PyFloat(0.0);
            } else if (args.size() == 1) {
                PyObject arg = args.get(0);
                if (arg instanceof PyFloat) {
                    return arg;
                } else if (arg instanceof PyInt) {
                    return new PyFloat(((PyInt) arg).getValue());
                } else if (arg instanceof PyString) {
                    try {
                        return new PyFloat(Double.parseDouble(((PyString) arg).getValue().trim()));
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("could not convert string to float: '" + ((PyString) arg).getValue() + "'");
                    }
                } else if (arg instanceof PyBool) {
                    return new PyFloat(((PyBool) arg).getValue() ? 1.0 : 0.0);
                } else {
                    throw new RuntimeException("float() argument must be a string or a number, not '" + arg.getTypeName() + "'");
                }
            } else {
                throw new RuntimeException("float() takes at most 1 argument (" + args.size() + " given)");
            }
        }));
        
        // bool函数
        globals.define("bool", new PyBuiltinFunction("bool", args -> {
            if (args.size() == 0) {
                return PyBool.FALSE;
            } else if (args.size() == 1) {
                return PyBool.valueOf(args.get(0).isTruthy());
            } else {
                throw new RuntimeException("bool() takes at most 1 argument (" + args.size() + " given)");
            }
        }));
        
        // list函数
        globals.define("list", new PyBuiltinFunction("list", args -> {
            if (args.size() == 0) {
                return new PyList(new ArrayList<>());
            } else if (args.size() == 1) {
                PyObject arg = args.get(0);
                List<PyObject> elements = new ArrayList<>();
                Iterator<PyObject> iterator = arg.iterator();
                while (iterator.hasNext()) {
                    elements.add(iterator.next());
                }
                return new PyList(elements);
            } else {
                throw new RuntimeException("list() takes at most 1 argument (" + args.size() + " given)");
            }
        }));
          // dict函数
        globals.define("dict", new PyBuiltinFunction("dict", args -> {
            if (args.size() == 0) {
                return new PyDict(new HashMap<>());
            } else if (args.size() == 1) {
                PyObject arg = args.get(0);
                
                // Handle dict from mapping (like another dict)
                if (arg instanceof PyDict) {
                    return new PyDict(((PyDict) arg).getEntries());
                }
                
                // Handle dict from iterable of key-value pairs
                Map<PyObject, PyObject> entries = new HashMap<>();
                Iterator<PyObject> iterator = arg.iterator();
                try {
                    while (iterator.hasNext()) {
                        PyObject pair = iterator.next();
                        if (pair instanceof PyTuple) {
                            List<PyObject> elements = ((PyTuple) pair).getElements();
                            if (elements.size() == 2) {
                                entries.put(elements.get(0), elements.get(1));
                            } else {
                                throw new RuntimeException("ValueError: dictionary update sequence element has length " + elements.size() + "; 2 is required");
                            }
                        } else {
                            throw new RuntimeException("TypeError: cannot convert dictionary update sequence element to a sequence");
                        }
                    }
                    return new PyDict(entries);
                } catch (Exception e) {
                    if (e instanceof RuntimeException) {
                        throw e;
                    }
                    throw new RuntimeException("TypeError: '" + arg.getTypeName() + "' object is not iterable");
                }
            } else {
                throw new RuntimeException("dict() takes at most 1 argument (" + args.size() + " given)");
            }
        }));
        
        // range函数
        globals.define("range", new PyBuiltinFunction("range", args -> {
            if (args.size() == 1) {
                if (!(args.get(0) instanceof PyInt)) {
                    throw new RuntimeException("'int' object cannot be interpreted as an integer");
                }
                long stop = ((PyInt) args.get(0)).getValue();
                return new PyRange(0, stop, 1);
            } else if (args.size() == 2) {
                if (!(args.get(0) instanceof PyInt) || !(args.get(1) instanceof PyInt)) {
                    throw new RuntimeException("'int' object cannot be interpreted as an integer");
                }
                long start = ((PyInt) args.get(0)).getValue();
                long stop = ((PyInt) args.get(1)).getValue();
                return new PyRange(start, stop, 1);
            } else if (args.size() == 3) {
                if (!(args.get(0) instanceof PyInt) || !(args.get(1) instanceof PyInt) || !(args.get(2) instanceof PyInt)) {
                    throw new RuntimeException("'int' object cannot be interpreted as an integer");
                }
                long start = ((PyInt) args.get(0)).getValue();
                long stop = ((PyInt) args.get(1)).getValue();
                long step = ((PyInt) args.get(2)).getValue();
                if (step == 0) {
                    throw new RuntimeException("range() arg 3 must not be zero");
                }
                return new PyRange(start, stop, step);
            } else {
                throw new RuntimeException("range expected at most 3 arguments, got " + args.size());
            }
        }));          // input函数
        globals.define("input", new PyBuiltinFunction("input", args -> {
            if (args.size() > 1) {
                throw new RuntimeException("input expected at most 1 arguments, got " + args.size());
            }
            
            if (args.size() == 1) {
                // 使用IOManager的控制台输出流打印提示
                PrintStream outputStream = edu.carole.runtime.io.IOManager.getInstance().getConsoleOutputStream();
                outputStream.print(args.get(0).toString());
                outputStream.flush(); // 确保提示立即显示
            }
            
            try {
                // 使用IOManager的专用方法读取控制台输入
                String line = edu.carole.runtime.io.IOManager.getInstance().readConsoleLine();
                return new PyString(line != null ? line : "");
            } catch (IOException e) {
                throw new RuntimeException("Error reading input: " + e.getMessage());
            }
        }));
        
        // abs函数
        globals.define("abs", new PyBuiltinFunction("abs", args -> {
            if (args.size() != 1) {
                throw new RuntimeException("abs() takes exactly one argument (" + args.size() + " given)");
            }
            
            PyObject arg = args.get(0);
            if (arg instanceof PyInt) {
                long value = ((PyInt) arg).getValue();
                return new PyInt(Math.abs(value));
            } else if (arg instanceof PyFloat) {
                double value = ((PyFloat) arg).getValue();
                return new PyFloat(Math.abs(value));
            } else {
                throw new RuntimeException("bad operand type for abs(): '" + arg.getTypeName() + "'");
            }
        }));
        
        // min函数
        globals.define("min", new PyBuiltinFunction("min", args -> {
            if (args.size() == 0) {
                throw new RuntimeException("min expected at least 1 argument, got 0");
            }
            
            PyObject min = args.get(0);
            for (int i = 1; i < args.size(); i++) {
                // 简单的数值比较
                if (compareObjects(args.get(i), min) < 0) {
                    min = args.get(i);
                }
            }
            return min;
        }));
        
        // max函数
        globals.define("max", new PyBuiltinFunction("max", args -> {
            if (args.size() == 0) {
                throw new RuntimeException("max expected at least 1 argument, got 0");
            }
            
            PyObject max = args.get(0);
            for (int i = 1; i < args.size(); i++) {
                // 简单的数值比较
                if (compareObjects(args.get(i), max) > 0) {
                    max = args.get(i);
                }
            }
            return max;        }));        // next函数
        globals.define("next", new PyBuiltinFunction("next", args -> {
            if (args.size() < 1 || args.size() > 2) {
                throw new RuntimeException("next() takes from 1 to 2 positional arguments but " + args.size() + " were given");
            }
            
            PyObject iterator = args.get(0);
            
            // Special handling for generators
            if (iterator instanceof PyGenerator) {
                PyGenerator generator = (PyGenerator) iterator;
                if (generator.isExhausted()) {
                    if (args.size() == 2) {
                        return args.get(1); // Return default value
                    } else {
                        throw new RuntimeException("StopIteration");
                    }
                }
                
                Iterator<PyObject> iter = generator.getCurrentIterator();
                if (iter.hasNext()) {
                    return iter.next();
                } else {
                    if (args.size() == 2) {
                        return args.get(1); // Return default value
                    } else {
                        throw new RuntimeException("StopIteration");
                    }
                }
            }
            
            // For PyIterator objects, call their __next__ method
            if (iterator instanceof PyIterator) {
                try {
                    return ((PyIterator) iterator).next();
                } catch (RuntimeException e) {
                    if ("StopIteration".equals(e.getMessage()) && args.size() == 2) {
                        return args.get(1); // Return default value
                    } else {
                        throw e;
                    }
                }
            }
            
            // Try to call the object's __next__ method
            try {
                PyObject nextMethod = iterator.getAttribute("__next__");
                return nextMethod.call(new ArrayList<>());
            } catch (RuntimeException e) {
                if (args.size() == 2 && e.getMessage().contains("StopIteration")) {
                    return args.get(1); // Return default value
                } else {
                    throw new RuntimeException("'" + iterator.getTypeName() + "' object is not an iterator");
                }
            }
        }));
        
        // iter函数 - 调用对象的__iter__方法
        globals.define("iter", new PyBuiltinFunction("iter", args -> {
            if (args.size() != 1) {
                throw new RuntimeException("iter() takes exactly one argument (" + args.size() + " given)");
            }
            
            PyObject obj = args.get(0);
            
            // 调用对象的__iter__方法
            try {
                return obj.getAttribute("__iter__").call(new ArrayList<>());
            } catch (RuntimeException e) {
                throw new RuntimeException("'" + obj.getTypeName() + "' object is not iterable");
            }
        }));
          // open函数 - 创建真正的文件对象用于文件I/O操作
        globals.define("open", new PyBuiltinFunction("open", args -> {
            if (args.size() < 1 || args.size() > 2) {
                throw new RuntimeException("open() takes from 1 to 2 positional arguments but " + args.size() + " were given");
            }
            
            PyObject filename = args.get(0);
            if (!(filename instanceof PyString)) {
                throw new RuntimeException("open() argument 1 must be a string");
            }
            
            String mode = "r"; // Default mode
            if (args.size() == 2) {
                PyObject modeArg = args.get(1);
                if (!(modeArg instanceof PyString)) {
                    throw new RuntimeException("open() argument 2 must be a string");
                }
                mode = ((PyString) modeArg).getValue();
            }          return new PyFileContext(((PyString) filename).getValue(), mode);
        }));
        
        // sorted函数 - 返回排序后的新列表
        globals.define("sorted", new PyBuiltinFunction("sorted", args -> {
            if (args.size() < 1 || args.size() > 1) {
                // 目前仅实现基本版本，不支持key参数和reverse参数
                throw new RuntimeException("sorted() takes 1 positional argument but " + args.size() + " were given");
            }
            
            PyObject iterable = args.get(0);
            List<PyObject> elements = new ArrayList<>();
            
            // 获取可迭代对象的所有元素
            try {
                Iterator<PyObject> iterator = iterable.iterator();
                while (iterator.hasNext()) {
                    elements.add(iterator.next());
                }
            } catch (Exception e) {
                throw new RuntimeException("'" + iterable.getTypeName() + "' object is not iterable");
            }
            
            // 对元素进行排序
            elements.sort((o1, o2) -> compareObjects(o1, o2));
            
            // 返回新的排序列表
            return new PyList(elements);
        }));
        
        // zip函数 - 将多个可迭代对象的对应元素打包成元组
        globals.define("zip", new PyBuiltinFunction("zip", args -> {
            if (args.size() == 0) {
                return new PyList(new ArrayList<>());
            }
            
            List<Iterator<PyObject>> iterators = new ArrayList<>();
            for (PyObject arg : args) {
                try {
                    iterators.add(arg.iterator());
                } catch (Exception e) {
                    throw new RuntimeException("'" + arg.getTypeName() + "' object is not iterable");
                }
            }
            
            List<PyObject> result = new ArrayList<>();
            
            while (true) {
                List<PyObject> tuple = new ArrayList<>();
                boolean allHaveNext = true;
                
                for (Iterator<PyObject> it : iterators) {
                    if (it.hasNext()) {
                        tuple.add(it.next());
                    } else {
                        allHaveNext = false;
                        break;
                    }
                }
                
                if (!allHaveNext) {
                    break;
                }
                
                result.add(new PyTuple(tuple));
            }
            
            return new PyList(result);
        }));
        
        // filter函数 - 过滤可迭代对象中的元素
        globals.define("filter", new PyBuiltinFunction("filter", args -> {
            if (args.size() != 2) {
                throw new RuntimeException("filter() takes exactly 2 arguments (" + args.size() + " given)");
            }
            
            PyObject function = args.get(0);
            PyObject iterable = args.get(1);
            List<PyObject> result = new ArrayList<>();
            
            try {
                Iterator<PyObject> iterator = iterable.iterator();
                while (iterator.hasNext()) {
                    PyObject element = iterator.next();
                    List<PyObject> functionArgs = new ArrayList<>();
                    functionArgs.add(element);
                    
                    PyObject predicate = function;
                    if (predicate == PyNone.INSTANCE) {
                        // None作为函数时，相当于identity函数，保留真值的元素
                        if (element.isTruthy()) {
                            result.add(element);
                        }
                    } else {
                        PyObject callResult = predicate.call(functionArgs);
                        if (callResult.isTruthy()) {
                            result.add(element);
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("'" + iterable.getTypeName() + "' object is not iterable");
            }
            
            return new PyList(result);
        }));
        
        // map函数 - 对可迭代对象的每个元素应用函数
        globals.define("map", new PyBuiltinFunction("map", args -> {
            if (args.size() < 2) {
                throw new RuntimeException("map() requires at least 2 arguments");
            }
            
            PyObject function = args.get(0);
            List<Iterator<PyObject>> iterators = new ArrayList<>();
            
            // 获取所有可迭代对象的迭代器
            for (int i = 1; i < args.size(); i++) {
                try {
                    iterators.add(args.get(i).iterator());
                } catch (Exception e) {
                    throw new RuntimeException("'" + args.get(i).getTypeName() + "' object is not iterable");
                }
            }
            
            List<PyObject> result = new ArrayList<>();
            
            while (true) {
                List<PyObject> functionArgs = new ArrayList<>();
                boolean allHaveNext = true;
                
                for (Iterator<PyObject> it : iterators) {
                    if (it.hasNext()) {
                        functionArgs.add(it.next());
                    } else {
                        allHaveNext = false;
                        break;
                    }
                }
                
                if (!allHaveNext) {
                    break;
                }
                
                PyObject callResult = function.call(functionArgs);
                result.add(callResult);
            }
            
            return new PyList(result);
        }));
        
        // any函数 - 如果可迭代对象中任一元素为真，返回True
        globals.define("any", new PyBuiltinFunction("any", args -> {
            if (args.size() != 1) {
                throw new RuntimeException("any() takes exactly one argument (" + args.size() + " given)");
            }
            
            PyObject iterable = args.get(0);
            
            try {
                Iterator<PyObject> iterator = iterable.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().isTruthy()) {
                        return PyBool.TRUE;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("'" + iterable.getTypeName() + "' object is not iterable");
            }
            
            return PyBool.FALSE;
        }));
        
        // all函数 - 如果可迭代对象中所有元素都为真，返回True
        globals.define("all", new PyBuiltinFunction("all", args -> {
            if (args.size() != 1) {
                throw new RuntimeException("all() takes exactly one argument (" + args.size() + " given)");
            }
            
            PyObject iterable = args.get(0);
            
            try {
                Iterator<PyObject> iterator = iterable.iterator();
                while (iterator.hasNext()) {
                    if (!iterator.next().isTruthy()) {
                        return PyBool.FALSE;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("'" + iterable.getTypeName() + "' object is not iterable");
            }
            
            return PyBool.TRUE;
        }));
          // sum函数 - 对可迭代对象的元素求和
        globals.define("sum", new PyBuiltinFunction("sum", args -> {
            if (args.size() < 1 || args.size() > 2) {
                throw new RuntimeException("sum() takes at most 2 arguments (" + args.size() + " given)");
            }
            
            PyObject iterable = args.get(0);
            PyObject start = (args.size() == 2) ? args.get(1) : new PyInt(0);
            
            try {
                // 获取迭代器
                Iterator<PyObject> iterator = iterable.iterator();
                PyObject result = start;
                
                // 迭代并累加
                while (iterator.hasNext()) {
                    PyObject element = iterator.next();
                    
                    if (result instanceof PyInt && element instanceof PyInt) {
                        // 整数相加
                        long sum = ((PyInt)result).getValue() + ((PyInt)element).getValue();
                        result = new PyInt(sum);
                    } else if ((result instanceof PyInt || result instanceof PyFloat) &&
                               (element instanceof PyInt || element instanceof PyFloat)) {
                        // 浮点数相加
                        double sum;
                        if (result instanceof PyInt) {
                            sum = ((PyInt)result).getValue();
                        } else {
                            sum = ((PyFloat)result).getValue();
                        }
                        
                        if (element instanceof PyInt) {
                            sum += ((PyInt)element).getValue();
                        } else {
                            sum += ((PyFloat)element).getValue();
                        }
                        
                        result = new PyFloat(sum);
                    } else {
                        // 对于其他类型，尝试调用加法方法
                        List<PyObject> addArgs = new ArrayList<>();
                        addArgs.add(element);
                        result = result.getAttribute("__add__").call(addArgs);
                    }
                }
                
                return result;
            } catch (Exception e) {
                throw new RuntimeException("'" + iterable.getTypeName() + "' object is not iterable");
            }
        }));
        
        // round函数 - 对数字进行四舍五入
        globals.define("round", new PyBuiltinFunction("round", args -> {
            if (args.size() < 1 || args.size() > 2) {
                throw new RuntimeException("round() takes at most 2 arguments (" + args.size() + " given)");
            }
            
            PyObject number = args.get(0);
            int ndigits = 0;
            
            if (args.size() == 2) {
                PyObject ndigitsObj = args.get(1);
                if (!(ndigitsObj instanceof PyInt)) {
                    throw new RuntimeException("'" + ndigitsObj.getTypeName() + "' object cannot be interpreted as an integer");
                }
                ndigits = (int)((PyInt)ndigitsObj).getValue();
            }
            
            if (number instanceof PyInt) {
                return number; // 整数四舍五入仍为自身
            } else if (number instanceof PyFloat) {
                double value = ((PyFloat)number).getValue();
                double factor = Math.pow(10, ndigits);
                return new PyFloat(Math.round(value * factor) / factor);
            } else {
                throw new RuntimeException("type '" + number.getTypeName() + "' doesn't define __round__ method");
            }
        }));
          // pow函数 - 返回x的y次幂
        globals.define("pow", new PyBuiltinFunction("pow", args -> {
            if (args.size() < 2 || args.size() > 3) {
                throw new RuntimeException("pow() takes 2 or 3 arguments (" + args.size() + " given)");
            }
            
            PyObject base = args.get(0);
            PyObject exponent = args.get(1);
            PyObject modulus = args.size() == 3 ? args.get(2) : null;
            
            // 获取base和exponent的数值
            double baseValue;
            double exponentValue;
            
            if (base instanceof PyInt) {
                baseValue = ((PyInt) base).getValue();
            } else if (base instanceof PyFloat) {
                baseValue = ((PyFloat) base).getValue();
            } else if (base instanceof PyBool) {
                baseValue = ((PyBool) base).getValue() ? 1.0 : 0.0;
            } else {
                throw new RuntimeException("unsupported operand type(s) for ** or pow(): '" + 
                                         base.getTypeName() + "' and '" + exponent.getTypeName() + "'");
            }
            
            if (exponent instanceof PyInt) {
                exponentValue = ((PyInt) exponent).getValue();
            } else if (exponent instanceof PyFloat) {
                exponentValue = ((PyFloat) exponent).getValue();
            } else if (exponent instanceof PyBool) {
                exponentValue = ((PyBool) exponent).getValue() ? 1.0 : 0.0;
            } else {
                throw new RuntimeException("unsupported operand type(s) for ** or pow(): '" + 
                                         base.getTypeName() + "' and '" + exponent.getTypeName() + "'");
            }
            
            // 计算结果
            double result = Math.pow(baseValue, exponentValue);
            
            // 处理取模运算 pow(base, exp, mod)
            if (modulus != null) {
                if (!(modulus instanceof PyInt)) {
                    throw new RuntimeException("pow() 3rd argument must be an integer");
                }
                
                long modulusValue = ((PyInt) modulus).getValue();
                if (modulusValue == 0) {
                    throw new RuntimeException("pow() 3rd argument cannot be 0");
                }
                
                // 对于三参数pow，需要base和exponent都是整数
                if (!(base instanceof PyInt) || !(exponent instanceof PyInt)) {
                    throw new RuntimeException("pow() with 3 arguments requires integer arguments");
                }
                
                // 使用模幂运算
                long baseInt = ((PyInt) base).getValue();
                long expInt = ((PyInt) exponent).getValue();
                
                if (expInt < 0) {
                    throw new RuntimeException("pow() 2nd argument cannot be negative when 3rd argument specified");
                }
                
                // 计算模幂运算 (base^exp) % mod
                long modResult = modPow(baseInt, expInt, modulusValue);
                return new PyInt(modResult);
            }
            
            // 如果输入都是整数且指数非负，尝试返回整数结果
            if (base instanceof PyInt && exponent instanceof PyInt && exponentValue >= 0) {
                long expLong = ((PyInt) exponent).getValue();
                if (expLong >= 0 && result == Math.floor(result) && result <= Long.MAX_VALUE && result >= Long.MIN_VALUE) {
                    return new PyInt((long) result);
                }
            }
          // 否则返回浮点数
            return new PyFloat(result);
        }));
          // dir函数 - 返回对象的属性列表
        globals.define("dir", new PyBuiltinFunction("dir", args -> {
            if (args.size() > 1) {
                throw new RuntimeException("dir() takes at most 1 argument (" + args.size() + " given)");
            }
            
            List<PyObject> attributes = new ArrayList<>();
              if (args.size() == 0) {
                // 没有参数时，返回当前作用域的名字
                // 通过闭包访问当前环境
                Set<String> allNames = new HashSet<>();
                
                // 遍历当前环境链获取所有可见的变量名
                Environment currentEnv = globals; // 在实际使用时需要获取当前环境
                while (currentEnv != null) {
                    allNames.addAll(currentEnv.getValues().keySet());
                    // 获取父环境需要反射或增加方法
                    break; // 暂时只获取当前层
                }
                
                // 转换为PyStringNew列表
                for (String name : allNames) {
                    attributes.add(new PyString(name));
                }
            } else {
                PyObject obj = args.get(0);
                
                // 对于不同类型返回其方法和属性
                if (obj instanceof PyString) {
                    String[] stringMethods = {"upper", "lower", "strip", "startswith", "endswith", "replace", 
                                            "split", "join", "format", "isalpha", "isdigit", "isnumeric", 
                                            "islower", "isupper", "isspace", "find", "count", "index"};
                    for (String method : stringMethods) {
                        attributes.add(new PyString(method));
                    }
                } else if (obj instanceof PyList) {
                    String[] listMethods = {"append", "extend", "insert", "remove", "pop", "clear", 
                                          "index", "count", "sort", "reverse", "copy"};
                    for (String method : listMethods) {
                        attributes.add(new PyString(method));
                    }
                } else if (obj instanceof PyDict) {
                    String[] dictMethods = {"get", "keys", "values", "items", "update", "pop", "clear", 
                                          "copy", "setdefault", "popitem"};
                    for (String method : dictMethods) {
                        attributes.add(new PyString(method));
                    }
                } else {
                    // 默认属性
                    String[] commonAttrs = {"__class__", "__str__", "__repr__"};
                    for (String attr : commonAttrs) {
                        attributes.add(new PyString(attr));
                    }
                }
            }
            
            return new PyList(attributes);
        }));
          // isinstance函数 - 检查对象是否是指定类型的实例
        globals.define("isinstance", new PyBuiltinFunction("isinstance", args -> {
            if (args.size() != 2) {
                throw new RuntimeException("isinstance() takes exactly 2 arguments (" + args.size() + " given)");
            }
            
            PyObject obj = args.get(0);
            PyObject classinfo = args.get(1);
            
            // 支持元组形式的多类型检查
            if (classinfo instanceof PyTuple) {
                List<PyObject> types = ((PyTuple) classinfo).getElements();
                for (PyObject type : types) {
                    if (isInstanceOfType(obj, type)) {
                        return PyBool.TRUE;
                    }
                }
                return PyBool.FALSE;
            }
              return PyBool.valueOf(isInstanceOfType(obj, classinfo));
        }));
        
        // id函数 - 返回对象的唯一标识符
        globals.define("id", new PyBuiltinFunction("id", args -> {
            if (args.size() != 1) {
                throw new RuntimeException("id() takes exactly one argument (" + args.size() + " given)");
            }
            
            PyObject obj = args.get(0);
            // 使用对象的hashCode作为简化的id实现
            return new PyInt(System.identityHashCode(obj));
        }));
        
        // callable函数 - 检查对象是否可调用
        globals.define("callable", new PyBuiltinFunction("callable", args -> {
            if (args.size() != 1) {
                throw new RuntimeException("callable() takes exactly one argument (" + args.size() + " given)");
            }
            
            PyObject obj = args.get(0);
            
            // 检查是否是函数或可调用对象
            if (obj instanceof PyFunction || obj instanceof PyBuiltinFunction) {
                return PyBool.TRUE;
            }
            
            // 检查是否有__call__方法
            try {
                obj.getAttribute("__call__");
                return PyBool.TRUE;
            } catch (RuntimeException e) {
                return PyBool.FALSE;
            }
        }));
          // complex函数 - 创建复数
        globals.define("complex", new PyBuiltinFunction("complex", args -> {
            if (args.size() > 2) {
                throw new RuntimeException("complex() takes at most 2 arguments (" + args.size() + " given)");
            }
            
            double real = 0.0;
            double imag = 0.0;
            
            if (args.size() >= 1) {
                PyObject realArg = args.get(0);
                if (realArg instanceof PyInt) {
                    real = ((PyInt) realArg).getValue();
                } else if (realArg instanceof PyFloat) {
                    real = ((PyFloat) realArg).getValue();                } else if (realArg instanceof PyString) {
                    // 解析字符串形式的复数，如 "3+4j"、"5j"、"2.5"
                    String complexStr = ((PyString) realArg).getValue().trim();
                    try {
                        System.out.println("DEBUG: Parsing complex string: '" + complexStr + "'");
                        PyComplex parsed = parseComplexString(complexStr);
                        real = parsed.getReal();
                        imag = parsed.getImag();
                        System.out.println("DEBUG: Parsed successfully: " + real + " + " + imag + "j");
                    } catch (Exception e) {
                        System.out.println("DEBUG: Parsing failed: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException("complex() arg is a malformed string");
                    }
                } else if (realArg instanceof PyComplex) {
                    // 如果第一个参数已经是复数
                    PyComplex complex = (PyComplex) realArg;
                    real = complex.getReal();
                    imag = complex.getImag();
                } else {
                    throw new RuntimeException("complex() argument must be a string or a number, not '" + realArg.getTypeName() + "'");
                }
            }
            
            if (args.size() == 2) {
                PyObject imagArg = args.get(1);
                if (imagArg instanceof PyInt) {
                    imag += ((PyInt) imagArg).getValue();
                } else if (imagArg instanceof PyFloat) {
                    imag += ((PyFloat) imagArg).getValue();
                } else {
                    throw new RuntimeException("complex() argument must be a string or a number, not '" + imagArg.getTypeName() + "'");
                }            }
            
            // 返回真正的复数对象
            return new PyComplex(real, imag);
        }));
          // set函数 - 创建集合
        globals.define("set", new PyBuiltinFunction("set", args -> {
            if (args.size() > 1) {
                throw new RuntimeException("set() takes at most 1 argument (" + args.size() + " given)");
            }
            
            Set<PyObject> elements = new HashSet<>();
            
            if (args.size() == 1) {
                PyObject iterable = args.get(0);
                try {
                    Iterator<PyObject> iterator = iterable.iterator();
                    while (iterator.hasNext()) {
                        elements.add(iterator.next());
                    }
                } catch (Exception e) {
                    throw new RuntimeException("'" + iterable.getTypeName() + "' object is not iterable");
                }
            }
              // 返回真正的集合对象
            return new PySet(elements);
        }));        // bytes函数 - 创建字节对象
        globals.define("bytes", new PyBuiltinFunction("bytes", args -> {
            if (args.size() == 0) {
                return new PyBytes(new byte[0]);
            } else if (args.size() == 1) {
                PyObject arg = args.get(0);
                if (arg instanceof PyString) {
                    String str = ((PyString) arg).getValue();
                    return new PyBytes(str.getBytes());
                } else if (arg instanceof PyInt) {
                    int size = (int) ((PyInt) arg).getValue();
                    if (size < 0) {
                        throw new RuntimeException("negative count");
                    }
                    return new PyBytes(new byte[size]);
                } else if (arg instanceof PyList) {
                    List<PyObject> elements = ((PyList) arg).getElements();
                    byte[] bytes = new byte[elements.size()];
                    for (int i = 0; i < elements.size(); i++) {
                        PyObject element = elements.get(i);
                        if (element instanceof PyInt) {
                            int value = (int) ((PyInt) element).getValue();
                            if (value < 0 || value > 255) {
                                throw new RuntimeException("bytes must be in range(0, 256)");
                            }
                            bytes[i] = (byte) value;
                        } else {
                            throw new RuntimeException("'" + element.getTypeName() + "' object cannot be interpreted as an integer");
                        }
                    }
                    return new PyBytes(bytes);
                } else {
                    throw new RuntimeException("'" + arg.getTypeName() + "' object is not iterable");
                }
            } else if (args.size() == 2) {
                // bytes(string, encoding) form
                PyObject first = args.get(0);
                PyObject second = args.get(1);
                
                if (first instanceof PyString && second instanceof PyString) {
                    String str = ((PyString) first).getValue();
                    String encoding = ((PyString) second).getValue();
                    
                    try {
                        return new PyBytes(str.getBytes(encoding));
                    } catch (Exception e) {
                        // If encoding is not supported, use default encoding
                        return new PyBytes(str.getBytes());
                    }
                } else {
                    throw new RuntimeException("bytes() argument 1 must be a string when argument 2 is provided");
                }
            } else if (args.size() == 3) {
                // bytes(string, encoding, errors) form - simplified implementation
                PyObject first = args.get(0);
                PyObject second = args.get(1);
                // PyObject third = args.get(2); // errors parameter (ignored for simplicity)
                
                if (first instanceof PyString && second instanceof PyString) {
                    String str = ((PyString) first).getValue();
                    String encoding = ((PyString) second).getValue();
                    
                    try {
                        return new PyBytes(str.getBytes(encoding));
                    } catch (Exception e) {
                        return new PyBytes(str.getBytes());
                    }
                } else {
                    throw new RuntimeException("bytes() argument 1 must be a string when argument 2 is provided");
                }
            } else {
                throw new RuntimeException("bytes() takes at most 3 arguments (" + args.size() + " given)");
            }
        }));

        // bytearray函数 - 创建可变字节数组对象
        globals.define("bytearray", new PyBuiltinFunction("bytearray", args -> {
            if (args.size() == 0) {
                return new PyByteArray(new byte[0]);
            } else if (args.size() == 1) {
                PyObject arg = args.get(0);
                if (arg instanceof PyString) {
                    String str = ((PyString) arg).getValue();
                    return new PyByteArray(str.getBytes());
                } else if (arg instanceof PyInt) {
                    int size = (int) ((PyInt) arg).getValue();
                    if (size < 0) {
                        throw new RuntimeException("negative count");
                    }
                    return new PyByteArray(new byte[size]);
                } else if (arg instanceof PyList) {
                    List<PyObject> elements = ((PyList) arg).getElements();
                    byte[] bytes = new byte[elements.size()];
                    for (int i = 0; i < elements.size(); i++) {
                        PyObject element = elements.get(i);
                        if (element instanceof PyInt) {
                            int value = (int) ((PyInt) element).getValue();
                            if (value < 0 || value > 255) {
                                throw new RuntimeException("bytearray must be in range(0, 256)");
                            }
                            bytes[i] = (byte) value;
                        } else {
                            throw new RuntimeException("'" + element.getTypeName() + "' object cannot be interpreted as an integer");
                        }
                    }
                    return new PyByteArray(bytes);
                } else {
                    throw new RuntimeException("'" + arg.getTypeName() + "' object is not iterable");
                }
            } else {
                throw new RuntimeException("bytearray() takes at most 1 argument (" + args.size() + " given)");
            }
        }));        // vars函数 - 返回对象的__dict__属性或当前作用域的局部变量
        globals.define("vars", new PyBuiltinFunction("vars", args -> {
            if (args.size() > 1) {
                throw new RuntimeException("vars() takes at most 1 argument (" + args.size() + " given)");
            }
              if (args.size() == 0) {
                // 返回当前作用域的局部变量
                Map<PyObject, PyObject> localVars = new HashMap<>();
                
                // 获取当前环境的所有变量
                // 注意：在实际使用时需要传递当前环境
                Environment currentEnv = globals; // 这里应该是当前环境，而非全局环境
                if (currentEnv != null) {
                    for (Map.Entry<String, PyObject> entry : currentEnv.getValues().entrySet()) {
                        localVars.put(new PyString(entry.getKey()), entry.getValue());
                    }
                }
                
                // 添加一些标准的内置变量
                if (!localVars.containsKey(new PyString("__name__"))) {
                    localVars.put(new PyString("__name__"), new PyString("__main__"));
                }
                if (!localVars.containsKey(new PyString("__doc__"))) {
                    localVars.put(new PyString("__doc__"), PyNone.INSTANCE);
                }
                
                return new PyDict(localVars);
            } else {
                PyObject obj = args.get(0);
                
                // 尝试获取对象的__dict__属性
                try {
                    PyObject dict = obj.getAttribute("__dict__");
                    if (dict instanceof PyDict) {
                        return dict;
                    }
                } catch (RuntimeException e) {
                    // 如果没有__dict__属性，返回空字典
                }
                
                // 对于没有__dict__的对象，返回空字典
                return new PyDict(new HashMap<>());
            }
        }));

        // tuple函数 - 创建元组
        globals.define("tuple", new PyBuiltinFunction("tuple", args -> {
            if (args.size() == 0) {
                return new PyTuple(new ArrayList<>());
            } else if (args.size() == 1) {
                PyObject arg = args.get(0);
                List<PyObject> elements = new ArrayList<>();
                try {
                    Iterator<PyObject> iterator = arg.iterator();
                    while (iterator.hasNext()) {
                        elements.add(iterator.next());
                    }
                } catch (Exception e) {
                    throw new RuntimeException("'" + arg.getTypeName() + "' object is not iterable");
                }
                return new PyTuple(elements);
            } else {
                throw new RuntimeException("tuple() takes at most 1 argument (" + args.size() + " given)");
            }
        }));        // frozenset函数 - 创建不可变集合
        globals.define("frozenset", new PyBuiltinFunction("frozenset", args -> {
            if (args.size() > 1) {
                throw new RuntimeException("frozenset() takes at most 1 argument (" + args.size() + " given)");
            }
            
            Set<PyObject> elements = new HashSet<>();
            
            if (args.size() == 1) {
                PyObject iterable = args.get(0);
                try {
                    Iterator<PyObject> iterator = iterable.iterator();
                    while (iterator.hasNext()) {
                        elements.add(iterator.next());
                    }
                } catch (Exception e) {
                    throw new RuntimeException("'" + iterable.getTypeName() + "' object is not iterable");
                }
            }
            
            // 返回真正的不可变集合对象
            return new PyFrozenSet(elements);
        }));

        // enumerate函数 - 返回枚举对象
        globals.define("enumerate", new PyBuiltinFunction("enumerate", args -> {
            if (args.size() < 1 || args.size() > 2) {
                throw new RuntimeException("enumerate() takes from 1 to 2 positional arguments but " + args.size() + " were given");
            }
            
            PyObject iterable = args.get(0);
            int start = 0;
            
            if (args.size() == 2) {
                PyObject startArg = args.get(1);
                if (startArg instanceof PyInt) {
                    start = (int) ((PyInt) startArg).getValue();
                } else {
                    throw new RuntimeException("'" + startArg.getTypeName() + "' object cannot be interpreted as an integer");
                }
            }
            
            List<PyObject> result = new ArrayList<>();
            
            try {
                Iterator<PyObject> iterator = iterable.iterator();
                int index = start;
                while (iterator.hasNext()) {
                    List<PyObject> tupleElements = new ArrayList<>();
                    tupleElements.add(new PyInt(index));
                    tupleElements.add(iterator.next());
                    result.add(new PyTuple(tupleElements));
                    index++;
                }
            } catch (Exception e) {
                throw new RuntimeException("'" + iterable.getTypeName() + "' object is not iterable");
            }
            
            return new PyList(result);
        }));

        // reversed函数 - 返回反向迭代器
        globals.define("reversed", new PyBuiltinFunction("reversed", args -> {
            if (args.size() != 1) {
                throw new RuntimeException("reversed() takes exactly one argument (" + args.size() + " given)");
            }
            
            PyObject seq = args.get(0);
            
            if (seq instanceof PyList) {
                List<PyObject> elements = ((PyList) seq).getElements();
                List<PyObject> reversed = new ArrayList<>();
                for (int i = elements.size() - 1; i >= 0; i--) {
                    reversed.add(elements.get(i));
                }
                return new PyList(reversed);
            } else if (seq instanceof PyString) {
                String str = ((PyString) seq).getValue();
                StringBuilder reversed = new StringBuilder(str).reverse();
                return new PyString(reversed.toString());
            } else if (seq instanceof PyTuple) {
                List<PyObject> elements = ((PyTuple) seq).getElements();
                List<PyObject> reversed = new ArrayList<>();
                for (int i = elements.size() - 1; i >= 0; i--) {
                    reversed.add(elements.get(i));
                }
                return new PyTuple(reversed);
            } else {
                throw new RuntimeException("'" + seq.getTypeName() + "' object is not reversible");
            }
        }));

        return globals;
    }
    
    /**
     * 比较两个对象
     */
    private static int compareObjects(PyObject a, PyObject b) {
        if (a instanceof PyInt && b instanceof PyInt) {
            return Long.compare(((PyInt) a).getValue(), ((PyInt) b).getValue());
        } else if (a instanceof PyFloat && b instanceof PyFloat) {
            return Double.compare(((PyFloat) a).getValue(), ((PyFloat) b).getValue());
        } else if (a instanceof PyInt && b instanceof PyFloat) {
            return Double.compare(((PyInt) a).getValue(), ((PyFloat) b).getValue());
        } else if (a instanceof PyFloat && b instanceof PyInt) {
            return Double.compare(((PyFloat) a).getValue(), ((PyInt) b).getValue());
        } else if (a instanceof PyString && b instanceof PyString) {
            return ((PyString) a).getValue().compareTo(((PyString) b).getValue());
        } else {
            throw new RuntimeException("unorderable types: " + a.getTypeName() + " and " + b.getTypeName());
        }
    }
    
    /**
     * 计算模幂运算 (base^exp) % mod 使用快速幂算法
     */
    private static long modPow(long base, long exponent, long modulus) {
        if (modulus == 1) return 0;
        
        long result = 1;
        base = base % modulus;
        
        if (base < 0) {
            base += modulus;
        }
        
        while (exponent > 0) {
            // 如果指数是奇数，将base乘到结果中
            if (exponent % 2 == 1) {
                result = (result * base) % modulus;
            }
            
            // 指数除以2，base平方
            exponent = exponent >> 1;
            base = (base * base) % modulus;
        }
        
        return result;
    }
      /**
     * 检查对象是否是指定类型的实例，支持继承关系
     */
    private static boolean isInstanceOfType(PyObject obj, PyObject classinfo) {
        // 基本类型名比较
        String objTypeName = obj.getTypeName();
        
        if (classinfo instanceof PyString) {
            String className = ((PyString) classinfo).getValue();
            
            // 检查直接类型匹配
            if (objTypeName.equals(className)) {
                return true;
            }
            
            // 检查继承关系 - 支持collection hierarchy
            return checkInheritanceChain(obj, className);
        }
        
        // 处理内置类型函数（如int, str, list等）
        if (classinfo instanceof PyBuiltinFunction) {
            PyBuiltinFunction typeFunc = (PyBuiltinFunction) classinfo;
            String typeName = typeFunc.toString();
            
            // 从函数字符串中提取类型名，例如 "<built-in function int>" -> "int"
            if (typeName.startsWith("<built-in function ") && typeName.endsWith(">")) {
                String className = typeName.substring(19, typeName.length() - 1);
                
                // 检查直接类型匹配
                if (objTypeName.equals(className)) {
                    return true;
                }
                
                // 检查继承关系
                return checkInheritanceChain(obj, className);
            }
        }
        
        // 对于类对象的直接比较
        if (classinfo instanceof PyClass) {
            return obj instanceof PyInstance && 
                   ((PyInstance) obj).getPyClass().equals(classinfo);
        }
        
        // 对于内置类型，比较Java类型
        return obj.getClass().equals(classinfo.getClass());
    }
    
    /**
     * 检查对象是否符合指定类型的继承链
     */
    private static boolean checkInheritanceChain(PyObject obj, String className) {
        // 检查集合类型的继承关系
        if (obj instanceof edu.carole.runtime.collections.PyIterable) {
            if ("Iterable".equals(className) || "collections.abc.Iterable".equals(className)) {
                return true;
            }
        }
        
        if (obj instanceof edu.carole.runtime.collections.PyCollection) {
            if ("Collection".equals(className) || "collections.abc.Collection".equals(className)) {
                return true;
            }
        }
        
        if (obj instanceof edu.carole.runtime.collections.PySequence) {
            if ("Sequence".equals(className) || "collections.abc.Sequence".equals(className)) {
                return true;
            }
        }
        
        if (obj instanceof edu.carole.runtime.collections.PyMutableSequence) {
            if ("MutableSequence".equals(className) || "collections.abc.MutableSequence".equals(className)) {
                return true;
            }
        }
        
        if (obj instanceof edu.carole.runtime.collections.PyMapping) {
            if ("Mapping".equals(className) || "collections.abc.Mapping".equals(className)) {
                return true;
            }
        }
        
        if (obj instanceof edu.carole.runtime.collections.PyMutableMapping) {
            if ("MutableMapping".equals(className) || "collections.abc.MutableMapping".equals(className)) {
                return true;
            }
        }
        
        return false;
    }
      /**
     * Helper method to parse complex number strings like "3+4j", "5j", "2.5"
     */
    private static PyComplex parseComplexString(String complexStr) {
        complexStr = complexStr.replaceAll("\\s+", ""); // Remove all whitespace
        
        if (complexStr.isEmpty()) {
            throw new NumberFormatException("Empty string");
        }
        
        // First, check for complex numbers like "3+4j" or "3-4j" (contains both real and imaginary parts)
        if ((complexStr.contains("+") || (complexStr.length() > 1 && complexStr.substring(1).contains("-"))) 
            && (complexStr.endsWith("j") || complexStr.endsWith("J"))) {
            
            int lastPlusIndex = complexStr.lastIndexOf('+');
            int lastMinusIndex = complexStr.lastIndexOf('-');
            
            // If minus is at the beginning, find the next minus
            if (lastMinusIndex == 0) {
                lastMinusIndex = complexStr.indexOf('-', 1);
                if (lastMinusIndex == -1) {
                    lastMinusIndex = 0; // Reset if no other minus found
                }
            }
            
            int splitIndex = Math.max(lastPlusIndex, lastMinusIndex);
            
            // If valid split found and not at beginning
            if (splitIndex > 0) {
                String realPart = complexStr.substring(0, splitIndex);
                String imagPart = complexStr.substring(splitIndex);
                
                if (!imagPart.endsWith("j") && !imagPart.endsWith("J")) {
                    throw new NumberFormatException("Missing 'j' in imaginary part: " + imagPart);
                }
                
                imagPart = imagPart.substring(0, imagPart.length() - 1); // Remove 'j'
                
                if (imagPart.equals("+")) {
                    imagPart = "1";
                } else if (imagPart.equals("-")) {
                    imagPart = "-1";
                }
                
                try {
                    double real = Double.parseDouble(realPart);
                    double imag = Double.parseDouble(imagPart);
                    return new PyComplex(real, imag);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Invalid complex format: " + complexStr);
                }
            }
        }
        
        // Handle pure imaginary numbers like "5j" or "j"
        if (complexStr.endsWith("j") || complexStr.endsWith("J")) {
            String imagPart = complexStr.substring(0, complexStr.length() - 1);
            if (imagPart.isEmpty() || imagPart.equals("+")) {
                return new PyComplex(0.0, 1.0);
            } else if (imagPart.equals("-")) {
                return new PyComplex(0.0, -1.0);
            } else {
                try {
                    double imag = Double.parseDouble(imagPart);
                    return new PyComplex(0.0, imag);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Invalid imaginary part: " + imagPart);
                }
            }
        }
        
        // Handle pure real numbers
        try {
            double real = Double.parseDouble(complexStr);
            return new PyComplex(real, 0.0);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid real number: " + complexStr);        }
    }
}

/**
 * Python range对象
 */
class PyRange extends PyObject implements Iterable<PyObject> {
    private final long start;
    private final long stop;
    private final long step;
    
    public PyRange(long start, long stop, long step) {
        this.start = start;
        this.stop = stop;
        this.step = step;
    }
    
    @Override
    public String getTypeName() { return "range"; }
    
    @Override
    public String toString() { 
        if (start == 0 && step == 1) {
            return "range(" + stop + ")";
        } else if (step == 1) {
            return "range(" + start + ", " + stop + ")";
        } else {
            return "range(" + start + ", " + stop + ", " + step + ")";
        }
    }
    
    @Override
    public boolean isTruthy() { 
        if (step > 0) {
            return start < stop;
        } else {
            return start > stop;
        }
    }
    
    @Override
    public PyObject len() {
        if (step > 0) {
            if (start >= stop) return new PyInt(0);
            return new PyInt((stop - start + step - 1) / step);
        } else {
            if (start <= stop) return new PyInt(0);
            return new PyInt((start - stop - step - 1) / (-step));
        }
    }
      @Override
    public Iterator<PyObject> iterator() {
        return new Iterator<PyObject>() {
            private long current = start;
            
            @Override
            public boolean hasNext() {
                if (step > 0) {
                    return current < stop;
                } else {
                    return current > stop;
                }
            }
            
            @Override
            public PyObject next() {
                if (!hasNext()) throw new NoSuchElementException();
                long value = current;
                current += step;
                return new PyInt(value);
            }
        };
    }
    
    @Override
    public PyObject getAttribute(String name) {
        switch (name) {
            case "__iter__":
                return new PyBuiltinFunction("__iter__", args -> {
                    if (args.size() != 0) {
                        throw new RuntimeException("__iter__() takes no arguments (" + args.size() + " given)");
                    }
                    return new PyIterator(iterator(), "range");
                });
                
            default:
                return super.getAttribute(name);
        }
    }
}

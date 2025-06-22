package edu.carole.runtime.clazz.builtins;

import edu.carole.ast.expressions.Literal;
import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.PyObject;
import edu.carole.runtime.clazz.BuiltinClass;
import edu.carole.runtime.clazz.PyClass;
import edu.carole.runtime.clazz.helper.Slice;
import edu.carole.runtime.instance.BuiltinInstance;
import edu.carole.runtime.exception.ExceptionWrapper;

import java.util.*;
import java.util.function.Function;

public class PyTuple extends BuiltinClass<PyObject[]> {
    public PyTuple(String name, Map<String, PyObject> methods) {
        super(name, methods, PyObject[].class);
    }

    public PyTuple(String name, Map<String, PyObject> methods, List<PyClass> baseClasses) {
        super(name, methods, baseClasses, PyObject[].class);
    }

    public PyTuple(String name, String modulePath, Map<String, PyObject> methods) {
        super(name, modulePath, methods, PyObject[].class);
    }

    public PyTuple(String name, String modulePath, Map<String, PyObject> methods, List<PyClass> baseClasses) {
        super(name, modulePath, methods, baseClasses, PyObject[].class);
    }

    @Override
    public void initMethods(Map<String, PyObject> methods, Map<String, PyObject> classAttr) {
        // 基础方法
        registerNArgs(methods, "__len__", 0, (args, kwargs, inter) -> {
            return len(inter, args.get(0));
        });
        registerNArgs(methods, "__str__", 0, (args, kwargs, inter) -> {
            return str(inter, args.get(0));
        });
        registerNArgs(methods, "__repr__", 0, (args, kwargs, inter) -> {
            return str(inter, args.get(0));
        });
        registerNArgs(methods, "__getitem__", 1, (args, kwargs, inter) -> {
            return getItem(inter, args.get(0), args.get(1));
        });
        registerMethod(methods, "__getslice__", (args, kwargs, inter) -> {
            return getSlice(inter, args, kwargs);
        });
        registerNArgs(methods, "__contains__", 1, (args, kwargs, inter) -> {
            return contains(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__iter__", 0, (args, kwargs, inter) -> {
            return iter(inter, args.get(0));
        });
        registerNArgs(methods, "__hash__", 0, (args, kwargs, inter) -> {
            return hash(inter, args.get(0));
        });

        // 算术运算
        registerNArgs(methods, "__add__", 1, (args, kwargs, inter) -> {
            return add(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__mul__", 1, (args, kwargs, inter) -> {
            return mul(inter, args.get(0), args.get(1));
        });

        // 比较运算
        registerNArgs(methods, "__eq__", 1, (args, kwargs, inter) -> {
            return eq(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__ne__", 1, (args, kwargs, inter) -> {
            return PyBool.reverse(inter, eq(inter, args.get(0), args.get(1)));
        });
        registerNArgs(methods, "__lt__", 1, (args, kwargs, inter) -> {
            return lt(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__le__", 1, (args, kwargs, inter) -> {
            return le(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__gt__", 1, (args, kwargs, inter) -> {
            return gt(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__ge__", 1, (args, kwargs, inter) -> {
            return ge(inter, args.get(0), args.get(1));
        });

        // 元组方法
        registerNArgs(methods, "count", 1, (args, kwargs, inter) -> {
            return count(inter, args.get(0), args.get(1));
        });
        registerMethod(methods, "index", (args, kwargs, inter) -> {
            return index(inter, args, kwargs);
        });
    }

    @Override
    public BuiltinClass<PyObject[]> getPyClass(Interpreter interpreter) {
        return interpreter.getMemoryModel().getTUPLE();
    }

    @Override
    public boolean isValidLiteral(Literal literal) {
        return literal.getValue() instanceof PyObject[];
    }

    @Override
    public int hashCode(BuiltinInstance<PyObject[]> instance) {
        return Objects.hashCode(instance.getValue());
    }

    // 基础方法实现
    public BuiltinInstance<Long> len(Interpreter interpreter, PyObject self) {
        PyObject[] elements = getValue(interpreter, self);
        return interpreter.getInteger(elements.length);
    }

    public BuiltinInstance<String> str(Interpreter interpreter, PyObject self) {
        PyObject[] elements = getValue(interpreter, self);
        
        if (elements.length == 1) {
            // Single element tuple displays with trailing comma
            PyObject element = elements[0];
            if (interpreter.isStr(element)) {
                return interpreter.createString("('" + element.toString() + "',)");
            } else {
                return interpreter.createString("(" + element.toString() + ",)");
            }
        }
        
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < elements.length; i++) {
            if (i > 0) sb.append(", ");
            PyObject element = elements[i];
            if (interpreter.isStr(element)) {
                sb.append("'").append(element.toString()).append("'");
            } else {
                sb.append(element.toString());
            }
        }
        sb.append(")");
        return interpreter.createString(sb.toString());
    }

    public PyObject getItem(Interpreter interpreter, PyObject self, PyObject key) {
        if (!interpreter.isInt(key)) {
            throw badType(interpreter, "tuple indices must be integers");
        }
        BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
        PyObject[] elements = getValue(interpreter, self);
        long index = INTEGER.getValue(interpreter, key);
        if (index < 0) index += elements.length;
        if (index < 0 || index >= elements.length) {
            throw badValue(interpreter, "tuple index out of range");
        }
        return elements[(int) index];
    }

    public BuiltinInstance<PyObject[]> getSlice(Interpreter interpreter, List<PyObject> args, Map<String, PyObject> kwargs) {
        if (args.isEmpty() || !(args.get(0) instanceof BuiltinInstance ins)) {
            throw badType(interpreter, "'__getslice__' is not a static method");
        } else if (!ins.is(this)) {
            throw badType(interpreter, "'__getslice__' is not a static method");
        }
        
        boolean hasKwarg = kwargs != null;
        int argSize = args.size() + (hasKwarg ? kwargs.size() : 0);
        if (argSize > 4) {
            throw badValue(interpreter, "__getslice__ takes at most 3 arguments");
        }
        
        PyObject start = interpreter.none();
        PyObject stop = interpreter.none();
        PyObject step = interpreter.getInteger(1);

        // Parse arguments
        boolean onlyPositionArg = argSize == args.size();
        if (args.size() == 1 && hasKwarg) {
            if (kwargs.containsKey("start")) start = kwargs.get("start");
            if (kwargs.containsKey("stop")) stop = kwargs.get("stop");
            if (kwargs.containsKey("step")) step = kwargs.get("step");
        } else if (args.size() == 2) {
            start = args.get(1);
            if (hasKwarg) {
                if (kwargs.containsKey("stop")) stop = kwargs.get("stop");
                if (kwargs.containsKey("step")) step = kwargs.get("step");
            }
        } else if (args.size() == 3) {
            start = args.get(1);
            stop = args.get(2);
            if (hasKwarg && kwargs.containsKey("step")) {
                step = kwargs.get("step");
            }
        } else if (args.size() == 4) {
            start = args.get(1);
            stop = args.get(2);
            step = args.get(3);
        }
        
        return getSliceImpl(interpreter, ins, start, stop, step);
    }

    public BuiltinInstance<PyObject[]> getSliceImpl(Interpreter interpreter, PyObject self, PyObject start, PyObject stop, PyObject step) {
        PyObject[] elements = getValue(interpreter, self);
        Slice slice = new Slice(interpreter, start, stop, step);
        long[] indices = slice.indices(elements.length);

        long startIdx = indices[0];
        long stopIdx = indices[1];
        long stepVal = indices[2];
        
        List<PyObject> result = new ArrayList<>();
        for (long i = startIdx; stepVal > 0 ? (i < stopIdx) : (i > stopIdx); i += stepVal) {
            result.add(elements[(int) i]);
        }
        return interpreter.getMemoryModel().createTuple(result.toArray(new PyObject[0]));
    }

    public BuiltinInstance<Boolean> contains(Interpreter interpreter, PyObject self, PyObject item) {
        PyObject[] elements = getValue(interpreter, self);
        for (PyObject element : elements) {
            if (element.equals(item)) {
                return interpreter.boolTrue();
            }
        }
        return interpreter.boolFalse();
    }

    // TODO: 实现 iter 方法，需要 PyIterator 类
    public PyObject iter(Interpreter interpreter, PyObject self) {
        // 暂时返回 None，等待 PyIterator 实现
        return interpreter.none();
    }

    public BuiltinInstance<Long> hash(Interpreter interpreter, PyObject self) {
        PyObject[] elements = getValue(interpreter, self);
        int hash = 1;
        for (PyObject element : elements) {
            // Try to get hash of each element
            try {
                // For basic types, use their hashCode directly
                if (interpreter.isInt(element)) {
                    BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
                    long val = INTEGER.getValue(interpreter, element);
                    hash = 31 * hash + Long.hashCode(val);
                } else if (interpreter.isStr(element)) {
                    BuiltinClass<String> STRING = interpreter.getMemoryModel().getSTR();
                    String val = STRING.getValue(interpreter, element);
                    hash = 31 * hash + val.hashCode();
                } else if (interpreter.isFloat(element)) {
                    BuiltinClass<Double> FLOAT = interpreter.getMemoryModel().getFLOAT();
                    double val = FLOAT.getValue(interpreter, element);
                    hash = 31 * hash + Double.hashCode(val);
                } else if (interpreter.isBool(element)) {
                    BuiltinClass<Boolean> BOOL = interpreter.getMemoryModel().getBOOL();
                    boolean val = BOOL.getValue(interpreter, element);
                    hash = 31 * hash + Boolean.hashCode(val);
                } else {
                    hash = 31 * hash + element.hashCode();
                }
            } catch (Exception e) {
                throw badType(interpreter, "unhashable type: '" + element.getTypeName() + "'");
            }
        }
        return interpreter.getInteger(hash);
    }

    // 算术运算实现
    public BuiltinInstance<PyObject[]> add(Interpreter interpreter, PyObject self, PyObject other) {
        if (!is(other)) {
            throw badType(interpreter, "can only concatenate tuple (not \"" + other.getTypeName() + "\") to tuple");
        }
        PyObject[] elements1 = getValue(interpreter, self);
        PyObject[] elements2 = getValue(interpreter, other);
        
        PyObject[] result = new PyObject[elements1.length + elements2.length];
        System.arraycopy(elements1, 0, result, 0, elements1.length);
        System.arraycopy(elements2, 0, result, elements1.length, elements2.length);
        
        return interpreter.getMemoryModel().createTuple(result);
    }

    public BuiltinInstance<PyObject[]> mul(Interpreter interpreter, PyObject self, PyObject other) {
        if (!interpreter.isInt(other)) {
            throw badType(interpreter, "can't multiply sequence by non-int of type '" + other.getTypeName() + "'");
        }
        BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
        PyObject[] elements = getValue(interpreter, self);
        long times = INTEGER.getValue(interpreter, other);
        
        if (times <= 0) {
            return interpreter.getMemoryModel().createTuple(new PyObject[0]);
        }
        
        List<PyObject> result = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            Collections.addAll(result, elements);
        }
        return interpreter.getMemoryModel().createTuple(result.toArray(new PyObject[0]));
    }

    // 比较运算实现
    public BuiltinInstance<Boolean> eq(Interpreter interpreter, PyObject self, PyObject other) {
        if (!is(other)) {
            return interpreter.boolFalse();
        }
        PyObject[] elements1 = getValue(interpreter, self);
        PyObject[] elements2 = getValue(interpreter, other);
        
        if (elements1.length != elements2.length) {
            return interpreter.boolFalse();
        }
        
        for (int i = 0; i < elements1.length; i++) {
            if (!elements1[i].equals(elements2[i])) {
                return interpreter.boolFalse();
            }
        }
        return interpreter.boolTrue();
    }

    public BuiltinInstance<Boolean> lt(Interpreter interpreter, PyObject self, PyObject other) {
        return compare(interpreter, self, other, r -> r < 0);
    }

    public BuiltinInstance<Boolean> le(Interpreter interpreter, PyObject self, PyObject other) {
        return compare(interpreter, self, other, r -> r <= 0);
    }

    public BuiltinInstance<Boolean> gt(Interpreter interpreter, PyObject self, PyObject other) {
        return compare(interpreter, self, other, r -> r > 0);
    }

    public BuiltinInstance<Boolean> ge(Interpreter interpreter, PyObject self, PyObject other) {
        return compare(interpreter, self, other, r -> r >= 0);
    }

    private BuiltinInstance<Boolean> compare(Interpreter interpreter, PyObject self, PyObject other, 
                                           Function<Integer, Boolean> op) {
        if (!is(other)) {
            throw badType(interpreter, "'<' not supported between instances of 'tuple' and '" + other.getTypeName() + "'");
        }
        PyObject[] elements1 = getValue(interpreter, self);
        PyObject[] elements2 = getValue(interpreter, other);
        int result = compareElements(interpreter, elements1, elements2);
        return interpreter.boolValue(op.apply(result));
    }

    private int compareElements(Interpreter interpreter, PyObject[] elements1, PyObject[] elements2) {
        int minSize = Math.min(elements1.length, elements2.length);
        
        for (int i = 0; i < minSize; i++) {
            PyObject elem1 = elements1[i];
            PyObject elem2 = elements2[i];
            
            try {
                if (interpreter.isInt(elem1) && interpreter.isInt(elem2)) {
                    BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
                    long val1 = INTEGER.getValue(interpreter, elem1);
                    long val2 = INTEGER.getValue(interpreter, elem2);
                    int cmp = Long.compare(val1, val2);
                    if (cmp != 0) return cmp;
                } else if (interpreter.isStr(elem1) && interpreter.isStr(elem2)) {
                    BuiltinClass<String> STRING = interpreter.getMemoryModel().getSTR();
                    String str1 = STRING.getValue(interpreter, elem1);
                    String str2 = STRING.getValue(interpreter, elem2);
                    int cmp = str1.compareTo(str2);
                    if (cmp != 0) return cmp;
                } else if (interpreter.isFloat(elem1) && interpreter.isFloat(elem2)) {
                    BuiltinClass<Double> FLOAT = interpreter.getMemoryModel().getFLOAT();
                    double val1 = FLOAT.getValue(interpreter, elem1);
                    double val2 = FLOAT.getValue(interpreter, elem2);
                    int cmp = Double.compare(val1, val2);
                    if (cmp != 0) return cmp;
                } else {
                    if (!elem1.equals(elem2)) {
                        return Integer.compare(elem1.hashCode(), elem2.hashCode());
                    }
                }
            } catch (Exception e) {
                // If comparison fails, consider elements equal
            }
        }
        
        return Integer.compare(elements1.length, elements2.length);
    }

    // 元组方法实现
    public BuiltinInstance<Long> count(Interpreter interpreter, PyObject self, PyObject item) {
        PyObject[] elements = getValue(interpreter, self);
        int count = 0;
        for (PyObject element : elements) {
            if (element.equals(item)) {
                count++;
            }
        }
        return interpreter.getInteger(count);
    }

    public BuiltinInstance<Long> index(Interpreter interpreter, List<PyObject> args, Map<String, PyObject> kwargs) {
        if (args.isEmpty() || !(args.get(0) instanceof BuiltinInstance ins)) {
            throw badType(interpreter, "'index' is not a static method");
        } else if (!ins.is(this)) {
            throw badType(interpreter, "'index' is not a static method");
        }
        
        if (args.size() < 2 || args.size() > 4) {
            throw badValue(interpreter, "index() takes from 1 to 3 positional arguments but " + (args.size() - 1) + " were given");
        }
        
        PyObject[] elements = getValue(interpreter, ins);
        PyObject value = args.get(1);
        int start = 0;
        int end = elements.length;
        
        if (args.size() >= 3) {
            if (args.get(2) != interpreter.none()) {
                if (!interpreter.isInt(args.get(2))) {
                    throw badType(interpreter, "tuple indices must be integers");
                }
                BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
                long startIdx = INTEGER.getValue(interpreter, args.get(2));
                if (startIdx < 0) startIdx += elements.length;
                if (startIdx < 0) startIdx = 0;
                start = (int) startIdx;
            }
        }
        
        if (args.size() == 4) {
            if (args.get(3) != interpreter.none()) {
                if (!interpreter.isInt(args.get(3))) {
                    throw badType(interpreter, "tuple indices must be integers");
                }
                BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
                long endIdx = INTEGER.getValue(interpreter, args.get(3));
                if (endIdx < 0) endIdx += elements.length;
                if (endIdx > elements.length) endIdx = elements.length;
                end = (int) endIdx;
            }
        }
        
        for (int i = start; i < end; i++) {
            if (elements[i].equals(value)) {
                return interpreter.getInteger(i);
            }
        }
        throw badValue(interpreter, "tuple.index(x): x not in tuple");
    }
}

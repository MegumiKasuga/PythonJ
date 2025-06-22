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
import java.util.function.BiFunction;
import java.util.function.Function;

public class PyList extends BuiltinClass<ArrayList> {
    public PyList(String name, Map<String, PyObject> methods) {
        super(name, methods, ArrayList.class);
    }

    public PyList(String name, Map<String, PyObject> methods, List<PyClass> baseClasses) {
        super(name, methods, baseClasses, ArrayList.class);
    }

    public PyList(String name, String modulePath, Map<String, PyObject> methods) {
        super(name, modulePath, methods, ArrayList.class);
    }

    public PyList(String name, String modulePath, Map<String, PyObject> methods, List<PyClass> baseClasses) {
        super(name, modulePath, methods, baseClasses, ArrayList.class);
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
        registerNArgs(methods, "__setitem__", 2, (args, kwargs, inter) -> {
            setItem(inter, args.get(0), args.get(1), args.get(2));
            return inter.none();
        });
        registerMethod(methods, "__setslice__", (args, kwargs, inter) -> {
            setSlice(inter, args, kwargs);
            return inter.none();
        });
        registerNArgs(methods, "__delitem__", 1, (args, kwargs, inter) -> {
            delItem(inter, args.get(0), args.get(1));
            return inter.none();
        });
        registerNArgs(methods, "__iter__", 0, (args, kwargs, inter) -> {
            return iter(inter, args.get(0));
        });
        registerNArgs(methods, "__contains__", 1, (args, kwargs, inter) -> {
            return contains(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__reversed__", 0, (args, kwargs, inter) -> {
            return reversed(inter, args.get(0));
        });

        // 算术运算
        registerNArgs(methods, "__add__", 1, (args, kwargs, inter) -> {
            return add(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__mul__", 1, (args, kwargs, inter) -> {
            return mul(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__iadd__", 1, (args, kwargs, inter) -> {
            return iadd(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__imul__", 1, (args, kwargs, inter) -> {
            return imul(inter, args.get(0), args.get(1));
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

        // 列表方法
        registerNArgs(methods, "append", 1, (args, kwargs, inter) -> {
            append(inter, args.get(0), args.get(1));
            return inter.none();
        });
        registerNArgs(methods, "extend", 1, (args, kwargs, inter) -> {
            extend(inter, args.get(0), args.get(1));
            return inter.none();
        });
        registerNArgs(methods, "insert", 2, (args, kwargs, inter) -> {
            insert(inter, args.get(0), args.get(1), args.get(2));
            return inter.none();
        });
        registerNArgs(methods, "remove", 1, (args, kwargs, inter) -> {
            remove(inter, args.get(0), args.get(1));
            return inter.none();
        });
        registerMethod(methods, "pop", (args, kwargs, inter) -> {
            return pop(inter, args, kwargs);
        });
        registerNArgs(methods, "clear", 0, (args, kwargs, inter) -> {
            clear(inter, args.get(0));
            return inter.none();
        });
        registerMethod(methods, "index", (args, kwargs, inter) -> {
            return index(inter, args, kwargs);
        });
        registerNArgs(methods, "count", 1, (args, kwargs, inter) -> {
            return count(inter, args.get(0), args.get(1));
        });
        registerMethod(methods, "sort", (args, kwargs, inter) -> {
            sort(inter, args, kwargs);
            return inter.none();
        });
        registerNArgs(methods, "copy", 0, (args, kwargs, inter) -> {
            return copy(inter, args.get(0));
        });
        registerNArgs(methods, "reverse", 0, (args, kwargs, inter) -> {
            reverse(inter, args.get(0));
            return inter.none();
        });
    }

    @Override
    public BuiltinClass<ArrayList> getPyClass(Interpreter interpreter) {
        return interpreter.getMemoryModel().getLIST();
    }

    @Override
    public boolean isValidLiteral(Literal literal) {
        return literal.getValue() instanceof ArrayList;
    }

    @Override
    public int hashCode(BuiltinInstance<ArrayList> instance) {
        return Objects.hashCode(instance.getValue());
    }

    // 基础方法实现
    public BuiltinInstance<Long> len(Interpreter interpreter, PyObject self) {
        ArrayList<PyObject> value = getValue(interpreter, self);
        return interpreter.getInteger(value.size());
    }

    public BuiltinInstance<String> str(Interpreter interpreter, PyObject self) {
        ArrayList<PyObject> elements = getValue(interpreter, self);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) sb.append(", ");
            PyObject element = elements.get(i);
            if (interpreter.isStr(element)) {
                sb.append("'").append(element.toString()).append("'");
            } else {
                sb.append(element.toString());
            }
        }
        sb.append("]");
        return interpreter.createString(sb.toString());
    }

    public PyObject getItem(Interpreter interpreter, PyObject self, PyObject key) {
        if (!interpreter.isInt(key)) {
            throw badType(interpreter, "list indices must be integers");
        }
        BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
        ArrayList<PyObject> elements = getValue(interpreter, self);
        long index = INTEGER.getValue(interpreter, key);
        if (index < 0) index += elements.size();
        if (index < 0 || index >= elements.size()) {
            throw badValue(interpreter, "list index out of range");
        }
        return elements.get((int) index);
    }

    public BuiltinInstance<ArrayList> getSlice(Interpreter interpreter, List<PyObject> args, Map<String, PyObject> kwargs) {
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

        // Parse arguments (similar to PyString implementation)
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

    public BuiltinInstance<ArrayList> getSliceImpl(Interpreter interpreter, PyObject self, PyObject start, PyObject stop, PyObject step) {
        ArrayList<PyObject> elements = getValue(interpreter, self);
        Slice slice = new Slice(interpreter, start, stop, step);
        long[] indices = slice.indices(elements.size());

        long startIdx = indices[0];
        long stopIdx = indices[1];
        long stepVal = indices[2];
        
        ArrayList<PyObject> result = new ArrayList<>();
        for (long i = startIdx; stepVal > 0 ? (i < stopIdx) : (i > stopIdx); i += stepVal) {
            result.add(elements.get((int) i));
        }
        return interpreter.getMemoryModel().createList(result);
    }

    public void setItem(Interpreter interpreter, PyObject self, PyObject key, PyObject value) {
        if (!interpreter.isInt(key)) {
            throw badType(interpreter, "list indices must be integers");
        }
        BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
        ArrayList<PyObject> elements = getValue(interpreter, self);
        long index = INTEGER.getValue(interpreter, key);
        if (index < 0) index += elements.size();
        if (index < 0 || index >= elements.size()) {
            throw badValue(interpreter, "list assignment index out of range");
        }
        elements.set((int) index, value);
    }

    public void setSlice(Interpreter interpreter, List<PyObject> args, Map<String, PyObject> kwargs) {
        if (args.isEmpty() || !(args.get(0) instanceof BuiltinInstance ins)) {
            throw badType(interpreter, "'__setslice__' is not a static method");
        } else if (!ins.is(this)) {
            throw badType(interpreter, "'__setslice__' is not a static method");
        }
        
        // Parse arguments similar to getSlice
        PyObject start = interpreter.none();
        PyObject stop = interpreter.none();
        PyObject step = interpreter.getInteger(1);
        PyObject value = interpreter.none();
        
        if (args.size() >= 2) {
            value = args.get(args.size() - 1); // Value is always the last argument
        }
        
        // Simplified argument parsing for setSlice
        if (args.size() == 2) {
            // setSlice(self, value) - replace entire list
            stop = interpreter.getInteger(getValue(interpreter, ins).size());
        } else if (args.size() == 3) {
            start = args.get(1);
            stop = interpreter.getInteger(getValue(interpreter, ins).size());
        } else if (args.size() == 4) {
            start = args.get(1);
            stop = args.get(2);
        } else if (args.size() == 5) {
            start = args.get(1);
            stop = args.get(2);
            step = args.get(3);
        }
        
        setSliceImpl(interpreter, ins, start, stop, step, value);
    }

    public void setSliceImpl(Interpreter interpreter, PyObject self, PyObject start, PyObject stop, PyObject step, PyObject value) {
        ArrayList<PyObject> elements = getValue(interpreter, self);
        Slice slice = new Slice(interpreter, start, stop, step);
        long[] indices = slice.indices(elements.size());

        long startIdx = indices[0];
        long stopIdx = indices[1];
        long stepVal = indices[2];
        
        if (!is(value)) {
            throw badType(interpreter, "can only assign an iterable");
        }
        
        ArrayList<PyObject> newValues = getValue(interpreter, value);
        
        if (stepVal != 1) {
            // Extended slice assignment
            List<Integer> sliceIndices = new ArrayList<>();
            for (long i = startIdx; stepVal > 0 ? (i < stopIdx) : (i > stopIdx); i += stepVal) {
                sliceIndices.add((int) i);
            }
            
            if (sliceIndices.size() != newValues.size()) {
                throw badValue(interpreter, "attempt to assign sequence of size " + newValues.size() + 
                              " to extended slice of size " + sliceIndices.size());
            }
            
            for (int i = 0; i < sliceIndices.size(); i++) {
                elements.set(sliceIndices.get(i), newValues.get(i));
            }
        } else {
            // Simple slice assignment
            for (int i = (int) stopIdx - 1; i >= startIdx; i--) {
                if (i < elements.size()) {
                    elements.remove(i);
                }
            }
            
            for (int i = 0; i < newValues.size(); i++) {
                elements.add((int) startIdx + i, newValues.get(i));
            }
        }
    }

    public void delItem(Interpreter interpreter, PyObject self, PyObject key) {
        if (!interpreter.isInt(key)) {
            throw badType(interpreter, "list indices must be integers");
        }
        BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
        ArrayList<PyObject> elements = getValue(interpreter, self);
        long index = INTEGER.getValue(interpreter, key);
        if (index < 0) index += elements.size();
        if (index < 0 || index >= elements.size()) {
            throw badValue(interpreter, "list assignment index out of range");
        }
        elements.remove((int) index);
    }

    // TODO: 实现 iter 和 reversed 方法，需要 PyIterator 类
    public PyObject iter(Interpreter interpreter, PyObject self) {
        // 暂时返回 None，等待 PyIterator 实现
        return interpreter.none();
    }

    public PyObject reversed(Interpreter interpreter, PyObject self) {
        // 暂时返回 None，等待 PyIterator 实现
        return interpreter.none();
    }

    public BuiltinInstance<Boolean> contains(Interpreter interpreter, PyObject self, PyObject item) {
        ArrayList<PyObject> elements = getValue(interpreter, self);
        for (PyObject element : elements) {
            if (element.equals(item)) {
                return interpreter.boolTrue();
            }
        }
        return interpreter.boolFalse();
    }

    // 算术运算实现
    public BuiltinInstance<ArrayList> add(Interpreter interpreter, PyObject self, PyObject other) {
        if (!is(other)) {
            throw badType(interpreter, "can only concatenate list (not \"" + other.getTypeName() + "\") to list");
        }
        ArrayList<PyObject> elements1 = getValue(interpreter, self);
        ArrayList<PyObject> elements2 = getValue(interpreter, other);
        ArrayList<PyObject> result = new ArrayList<>(elements1);
        result.addAll(elements2);
        return interpreter.getMemoryModel().createList(result);
    }

    public BuiltinInstance<ArrayList> mul(Interpreter interpreter, PyObject self, PyObject other) {
        if (!interpreter.isInt(other)) {
            throw badType(interpreter, "can't multiply sequence by non-int of type '" + other.getTypeName() + "'");
        }
        BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
        ArrayList<PyObject> elements = getValue(interpreter, self);
        long times = INTEGER.getValue(interpreter, other);
        
        if (times <= 0) {
            return interpreter.getMemoryModel().createList(new ArrayList<>());
        }
        
        ArrayList<PyObject> result = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            result.addAll(elements);
        }
        return interpreter.getMemoryModel().createList(result);
    }

    public PyObject iadd(Interpreter interpreter, PyObject self, PyObject other) {
        ArrayList<PyObject> elements = getValue(interpreter, self);
        if (is(other)) {
            ArrayList<PyObject> otherElements = getValue(interpreter, other);
            elements.addAll(otherElements);
        } else {
            // Try to iterate over the object
            try {
                Iterator<PyObject> iterator = other.iterator(interpreter);
                while (iterator.hasNext()) {
                    elements.add(iterator.next());
                }
            } catch (Exception e) {
                throw badType(interpreter, "can only concatenate list (not \"" + other.getTypeName() + "\") to list");
            }
        }
        return self;
    }

    public PyObject imul(Interpreter interpreter, PyObject self, PyObject other) {
        if (!interpreter.isInt(other)) {
            throw badType(interpreter, "can't multiply sequence by non-int of type '" + other.getTypeName() + "'");
        }
        BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
        ArrayList<PyObject> elements = getValue(interpreter, self);
        long times = INTEGER.getValue(interpreter, other);
        
        if (times <= 0) {
            elements.clear();
        } else {
            ArrayList<PyObject> originalElements = new ArrayList<>(elements);
            elements.clear();
            for (int i = 0; i < times; i++) {
                elements.addAll(originalElements);
            }
        }
        return self;
    }

    // 比较运算实现
    public BuiltinInstance<Boolean> eq(Interpreter interpreter, PyObject self, PyObject other) {
        if (!is(other)) {
            return interpreter.boolFalse();
        }
        ArrayList<PyObject> elements1 = getValue(interpreter, self);
        ArrayList<PyObject> elements2 = getValue(interpreter, other);
        
        if (elements1.size() != elements2.size()) {
            return interpreter.boolFalse();
        }
        
        for (int i = 0; i < elements1.size(); i++) {
            if (!elements1.get(i).equals(elements2.get(i))) {
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
            throw badType(interpreter, "'<' not supported between instances of 'list' and '" + other.getTypeName() + "'");
        }
        ArrayList<PyObject> elements1 = getValue(interpreter, self);
        ArrayList<PyObject> elements2 = getValue(interpreter, other);
        int result = compareElements(interpreter, elements1, elements2);
        return interpreter.boolValue(op.apply(result));
    }

    private int compareElements(Interpreter interpreter, ArrayList<PyObject> list1, ArrayList<PyObject> list2) {
        int minSize = Math.min(list1.size(), list2.size());
        
        for (int i = 0; i < minSize; i++) {
            PyObject elem1 = list1.get(i);
            PyObject elem2 = list2.get(i);
            
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
        
        return Integer.compare(list1.size(), list2.size());
    }

    // 列表方法实现
    public void append(Interpreter interpreter, PyObject self, PyObject item) {
        ArrayList<PyObject> elements = getValue(interpreter, self);
        elements.add(item);
    }

    public void extend(Interpreter interpreter, PyObject self, PyObject iterable) {
        ArrayList<PyObject> elements = getValue(interpreter, self);
        try {
            Iterator<PyObject> iterator = iterable.iterator(interpreter);
            while (iterator.hasNext()) {
                elements.add(iterator.next());
            }
        } catch (Exception e) {
            throw badType(interpreter, "'" + iterable.getTypeName() + "' object is not iterable");
        }
    }

    public void insert(Interpreter interpreter, PyObject self, PyObject indexObj, PyObject item) {
        if (!interpreter.isInt(indexObj)) {
            throw badType(interpreter, "list indices must be integers");
        }
        BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
        ArrayList<PyObject> elements = getValue(interpreter, self);
        long idx = INTEGER.getValue(interpreter, indexObj);
        if (idx < 0) idx += elements.size();
        if (idx < 0) idx = 0;
        if (idx > elements.size()) idx = elements.size();
        elements.add((int) idx, item);
    }

    public void remove(Interpreter interpreter, PyObject self, PyObject value) {
        ArrayList<PyObject> elements = getValue(interpreter, self);
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i).equals(value)) {
                elements.remove(i);
                return;
            }
        }
        throw badValue(interpreter, "list.remove(x): x not in list");
    }

    public PyObject pop(Interpreter interpreter, List<PyObject> args, Map<String, PyObject> kwargs) {
        if (args.isEmpty() || !(args.get(0) instanceof BuiltinInstance ins)) {
            throw badType(interpreter, "'pop' is not a static method");
        } else if (!ins.is(this)) {
            throw badType(interpreter, "'pop' is not a static method");
        }
        
        ArrayList<PyObject> elements = getValue(interpreter, ins);
        if (elements.isEmpty()) {
            throw badValue(interpreter, "pop from empty list");
        }
        
        int index;
        if (args.size() == 1) {
            index = elements.size() - 1;
        } else if (args.size() == 2) {
            if (!interpreter.isInt(args.get(1))) {
                throw badType(interpreter, "list indices must be integers");
            }
            BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
            long idx = INTEGER.getValue(interpreter, args.get(1));
            if (idx < 0) idx += elements.size();
            if (idx < 0 || idx >= elements.size()) {
                throw badValue(interpreter, "pop index out of range");
            }
            index = (int) idx;
        } else {
            throw badValue(interpreter, "pop() takes at most 1 argument (" + (args.size() - 1) + " given)");
        }
        
        return elements.remove(index);
    }

    public void clear(Interpreter interpreter, PyObject self) {
        ArrayList<PyObject> elements = getValue(interpreter, self);
        elements.clear();
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
        
        ArrayList<PyObject> elements = getValue(interpreter, ins);
        PyObject value = args.get(1);
        int start = 0;
        int end = elements.size();
        
        if (args.size() >= 3) {
            if (!interpreter.isInt(args.get(2))) {
                throw badType(interpreter, "list indices must be integers");
            }
            BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
            long startIdx = INTEGER.getValue(interpreter, args.get(2));
            if (startIdx < 0) startIdx += elements.size();
            if (startIdx < 0) startIdx = 0;
            start = (int) startIdx;
        }
        
        if (args.size() == 4) {
            if (!interpreter.isInt(args.get(3))) {
                throw badType(interpreter, "list indices must be integers");
            }
            BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
            long endIdx = INTEGER.getValue(interpreter, args.get(3));
            if (endIdx < 0) endIdx += elements.size();
            if (endIdx > elements.size()) endIdx = elements.size();
            end = (int) endIdx;
        }
        
        for (int i = start; i < end; i++) {
            if (elements.get(i).equals(value)) {
                return interpreter.getInteger(i);
            }
        }
        throw badValue(interpreter, value.toString() + " is not in list");
    }

    public BuiltinInstance<Long> count(Interpreter interpreter, PyObject self, PyObject value) {
        ArrayList<PyObject> elements = getValue(interpreter, self);
        int count = 0;
        for (PyObject element : elements) {
            if (element.equals(value)) {
                count++;
            }
        }
        return interpreter.getInteger(count);
    }

    public void sort(Interpreter interpreter, List<PyObject> args, Map<String, PyObject> kwargs) {
        if (args.isEmpty() || !(args.get(0) instanceof BuiltinInstance ins)) {
            throw badType(interpreter, "'sort' is not a static method");
        } else if (!ins.is(this)) {
            throw badType(interpreter, "'sort' is not a static method");
        }
        
        ArrayList<PyObject> elements = getValue(interpreter, ins);
        PyObject key = null;
        boolean reverse = false;
        
        if (args.size() > 1) {
            key = args.get(1);
        }
        if (args.size() > 2) {
            if (interpreter.isBool(args.get(2))) {
                BuiltinClass<Boolean> BOOL = interpreter.getMemoryModel().getBOOL();
                reverse = BOOL.getValue(interpreter, args.get(2));
            }
        }
        
        sortInPlace(interpreter, elements, key, reverse);
    }

    public BuiltinInstance<ArrayList> copy(Interpreter interpreter, PyObject self) {
        ArrayList<PyObject> elements = getValue(interpreter, self);
        return interpreter.getMemoryModel().createList(new ArrayList<>(elements));
    }

    public void reverse(Interpreter interpreter, PyObject self) {
        ArrayList<PyObject> elements = getValue(interpreter, self);
        Collections.reverse(elements);
    }

    // 辅助方法
    private void sortInPlace(Interpreter interpreter, ArrayList<PyObject> elements, PyObject key, boolean reverse) {
        if (key == null) {
            try {
                elements.sort((a, b) -> {
                    if (interpreter.isInt(a) && interpreter.isInt(b)) {
                        BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
                        long val1 = INTEGER.getValue(interpreter, a);
                        long val2 = INTEGER.getValue(interpreter, b);
                        return Long.compare(val1, val2);
                    } else if (interpreter.isFloat(a) && interpreter.isFloat(b)) {
                        BuiltinClass<Double> FLOAT = interpreter.getMemoryModel().getFLOAT();
                        double val1 = FLOAT.getValue(interpreter, a);
                        double val2 = FLOAT.getValue(interpreter, b);
                        return Double.compare(val1, val2);
                    } else if (interpreter.isStr(a) && interpreter.isStr(b)) {
                        BuiltinClass<String> STRING = interpreter.getMemoryModel().getSTR();
                        String str1 = STRING.getValue(interpreter, a);
                        String str2 = STRING.getValue(interpreter, b);
                        return str1.compareTo(str2);
                    } else {
                        return a.toString().compareTo(b.toString());
                    }
                });
                if (reverse) {
                    Collections.reverse(elements);
                }
            } catch (Exception e) {
                throw badType(interpreter, "'<' not supported between instances");
            }
        } else {
            throw badType(interpreter, "sort() with key function not yet implemented");
        }
    }
}

package edu.carole.runtime.clazz.builtins;

import edu.carole.ast.expressions.Literal;
import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.PyObject;
import edu.carole.runtime.clazz.BuiltinClass;
import edu.carole.runtime.clazz.PyClass;
import edu.carole.runtime.instance.BuiltinInstance;
import edu.carole.runtime.exception.ExceptionWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Iterator;

public class PyDict extends BuiltinClass<HashMap> {
    public PyDict(String name, Map<String, PyObject> methods) {
        super(name, methods, HashMap.class);
    }

    public PyDict(String name, Map<String, PyObject> methods, List<PyClass> baseClasses) {
        super(name, methods, baseClasses, HashMap.class);
    }

    public PyDict(String name, String modulePath, Map<String, PyObject> methods) {
        super(name, modulePath, methods, HashMap.class);
    }

    public PyDict(String name, String modulePath, Map<String, PyObject> methods, List<PyClass> baseClasses) {
        super(name, modulePath, methods, baseClasses, HashMap.class);
    }    @Override
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
        registerNArgs(methods, "__setitem__", 2, (args, kwargs, inter) -> {
            setItem(inter, args.get(0), args.get(1), args.get(2));
            return inter.none();
        });
        registerNArgs(methods, "__delitem__", 1, (args, kwargs, inter) -> {
            delItem(inter, args.get(0), args.get(1));
            return inter.none();
        });
        registerNArgs(methods, "__contains__", 1, (args, kwargs, inter) -> {
            return contains(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__iter__", 0, (args, kwargs, inter) -> {
            return iter(inter, args.get(0));
        });

        // 比较运算
        registerNArgs(methods, "__eq__", 1, (args, kwargs, inter) -> {
            return eq(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__ne__", 1, (args, kwargs, inter) -> {
            return PyBool.reverse(inter, eq(inter, args.get(0), args.get(1)));
        });

        // 字典方法
        registerNArgs(methods, "keys", 0, (args, kwargs, inter) -> {
            return keys(inter, args.get(0));
        });
        registerNArgs(methods, "values", 0, (args, kwargs, inter) -> {
            return values(inter, args.get(0));
        });
        registerNArgs(methods, "items", 0, (args, kwargs, inter) -> {
            return items(inter, args.get(0));
        });
        registerMethod(methods, "get", (args, kwargs, inter) -> {
            return get(inter, args, kwargs);
        });
        registerMethod(methods, "pop", (args, kwargs, inter) -> {
            return pop(inter, args, kwargs);
        });
        registerNArgs(methods, "popitem", 0, (args, kwargs, inter) -> {
            return popitem(inter, args.get(0));
        });
        registerNArgs(methods, "clear", 0, (args, kwargs, inter) -> {
            clear(inter, args.get(0));
            return inter.none();
        });
        registerNArgs(methods, "update", 1, (args, kwargs, inter) -> {
            update(inter, args.get(0), args.get(1));
            return inter.none();
        });
        registerMethod(methods, "setdefault", (args, kwargs, inter) -> {
            return setdefault(inter, args, kwargs);
        });
        registerNArgs(methods, "copy", 0, (args, kwargs, inter) -> {
            return copy(inter, args.get(0));
        });

        // 静态方法
        registerStaticMethod(classAttr, "fromkeys", (args, kwargs, inter) -> {
            return fromkeys(inter, args, kwargs);
        });
    }

    @Override
    public BuiltinClass<HashMap> getPyClass(Interpreter interpreter) {
        return interpreter.getMemoryModel().getDICT();
    }

    @Override
    public boolean isValidLiteral(Literal literal) {
        return literal.getValue() instanceof HashMap;
    }

    @Override
    public int hashCode(BuiltinInstance<HashMap> instance) {
        return instance.getValue().hashCode();
    }

    // 基础方法实现
    public BuiltinInstance<Long> len(Interpreter interpreter, PyObject self) {
        HashMap<PyObject, PyObject> entries = getValue(interpreter, self);
        return interpreter.getInteger(entries.size());
    }

    public BuiltinInstance<String> str(Interpreter interpreter, PyObject self) {
        HashMap<PyObject, PyObject> entries = getValue(interpreter, self);
        
        if (entries.isEmpty()) {
            return interpreter.createString("{}");
        }
        
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<PyObject, PyObject> entry : entries.entrySet()) {
            if (!first) sb.append(", ");
            first = false;
            
            PyObject key = entry.getKey();
            PyObject value = entry.getValue();
            
            if (interpreter.isStr(key)) {
                sb.append("'").append(key.toString()).append("'");
            } else {
                sb.append(key.toString());
            }
            
            sb.append(": ");
            
            if (interpreter.isStr(value)) {
                sb.append("'").append(value.toString()).append("'");
            } else {
                sb.append(value.toString());
            }
        }
        sb.append("}");
        return interpreter.createString(sb.toString());
    }

    public PyObject getItem(Interpreter interpreter, PyObject self, PyObject key) {
        HashMap<PyObject, PyObject> entries = getValue(interpreter, self);
        
        // Try direct lookup
        PyObject value = entries.get(key);
        if (value != null) {
            return value;
        }
        
        // If not found and key is string, try string comparison
        if (interpreter.isStr(key)) {
            BuiltinClass<String> STRING = interpreter.getMemoryModel().getSTR();
            String keyStr = STRING.getValue(interpreter, key);
            for (Map.Entry<PyObject, PyObject> entry : entries.entrySet()) {
                if (interpreter.isStr(entry.getKey())) {
                    String existingKey = STRING.getValue(interpreter, entry.getKey());
                    if (existingKey.equals(keyStr)) {
                        return entry.getValue();
                    }
                }
            }
        }
        
        throw badValue(interpreter, "KeyError: " + key.toString());
    }

    public void setItem(Interpreter interpreter, PyObject self, PyObject key, PyObject value) {
        HashMap<PyObject, PyObject> entries = getValue(interpreter, self);
        entries.put(key, value);
    }

    public void delItem(Interpreter interpreter, PyObject self, PyObject key) {
        HashMap<PyObject, PyObject> entries = getValue(interpreter, self);
        if (!entries.containsKey(key)) {
            throw badValue(interpreter, "KeyError: " + key.toString());
        }
        entries.remove(key);
    }

    public BuiltinInstance<Boolean> contains(Interpreter interpreter, PyObject self, PyObject key) {
        HashMap<PyObject, PyObject> entries = getValue(interpreter, self);
        return interpreter.boolValue(entries.containsKey(key));
    }

    // TODO: 实现 iter 方法，需要 PyIterator 类
    public PyObject iter(Interpreter interpreter, PyObject self) {
        // 暂时返回 None，等待 PyIterator 实现
        return interpreter.none();
    }

    // 比较运算实现
    public BuiltinInstance<Boolean> eq(Interpreter interpreter, PyObject self, PyObject other) {
        if (!is(other)) {
            return interpreter.boolFalse();
        }
        
        HashMap<PyObject, PyObject> entries1 = getValue(interpreter, self);
        HashMap<PyObject, PyObject> entries2 = getValue(interpreter, other);
        
        if (entries1.size() != entries2.size()) {
            return interpreter.boolFalse();
        }
        
        // Check each key-value pair
        for (Map.Entry<PyObject, PyObject> entry : entries1.entrySet()) {
            PyObject key = entry.getKey();
            PyObject value = entry.getValue();
            
            // Try direct lookup first
            PyObject otherValue = entries2.get(key);
            boolean found = (otherValue != null && value.equals(otherValue));
            
            // If not found and key is string, try string comparison
            if (!found && interpreter.isStr(key)) {
                BuiltinClass<String> STRING = interpreter.getMemoryModel().getSTR();
                String keyStr = STRING.getValue(interpreter, key);
                for (Map.Entry<PyObject, PyObject> otherEntry : entries2.entrySet()) {
                    if (interpreter.isStr(otherEntry.getKey())) {
                        String otherKeyStr = STRING.getValue(interpreter, otherEntry.getKey());
                        if (keyStr.equals(otherKeyStr)) {
                            if (value.equals(otherEntry.getValue())) {
                                found = true;
                                break;
                            }
                        }
                    }
                }
            }
            
            if (!found) {
                return interpreter.boolFalse();
            }
        }
        return interpreter.boolTrue();
    }

    // 字典方法实现
    public BuiltinInstance<ArrayList> keys(Interpreter interpreter, PyObject self) {
        HashMap<PyObject, PyObject> entries = getValue(interpreter, self);
        return interpreter.getMemoryModel().createList(new ArrayList<>(entries.keySet()));
    }

    public BuiltinInstance<ArrayList> values(Interpreter interpreter, PyObject self) {
        HashMap<PyObject, PyObject> entries = getValue(interpreter, self);
        return interpreter.getMemoryModel().createList(new ArrayList<>(entries.values()));
    }

    public BuiltinInstance<ArrayList> items(Interpreter interpreter, PyObject self) {
        HashMap<PyObject, PyObject> entries = getValue(interpreter, self);
        ArrayList<PyObject> items = new ArrayList<>();
        for (Map.Entry<PyObject, PyObject> entry : entries.entrySet()) {
            PyObject[] pair = new PyObject[]{entry.getKey(), entry.getValue()};
            items.add(interpreter.getMemoryModel().createTuple(pair));
        }
        return interpreter.getMemoryModel().createList(items);
    }

    public PyObject get(Interpreter interpreter, List<PyObject> args, Map<String, PyObject> kwargs) {
        if (args.isEmpty() || !(args.get(0) instanceof BuiltinInstance ins)) {
            throw badType(interpreter, "'get' is not a static method");
        } else if (!ins.is(this)) {
            throw badType(interpreter, "'get' is not a static method");
        }
        
        if (args.size() == 2) {
            return get(interpreter, ins, args.get(1), interpreter.none());
        } else if (args.size() == 3) {
            return get(interpreter, ins, args.get(1), args.get(2));
        } else {
            throw badValue(interpreter, "get() takes 1 or 2 arguments (" + (args.size() - 1) + " given)");
        }
    }

    public PyObject get(Interpreter interpreter, PyObject self, PyObject key, PyObject defaultValue) {
        HashMap<PyObject, PyObject> entries = getValue(interpreter, self);
        PyObject value = entries.get(key);
        return value != null ? value : defaultValue;
    }

    public PyObject pop(Interpreter interpreter, List<PyObject> args, Map<String, PyObject> kwargs) {
        if (args.isEmpty() || !(args.get(0) instanceof BuiltinInstance ins)) {
            throw badType(interpreter, "'pop' is not a static method");
        } else if (!ins.is(this)) {
            throw badType(interpreter, "'pop' is not a static method");
        }
        
        if (args.size() == 2) {
            return pop(interpreter, ins, args.get(1), null);
        } else if (args.size() == 3) {
            return pop(interpreter, ins, args.get(1), args.get(2));
        } else {
            throw badValue(interpreter, "pop() takes 1 or 2 arguments (" + (args.size() - 1) + " given)");
        }
    }

    public PyObject pop(Interpreter interpreter, PyObject self, PyObject key, PyObject defaultValue) {
        HashMap<PyObject, PyObject> entries = getValue(interpreter, self);
        if (entries.containsKey(key)) {
            return entries.remove(key);
        } else if (defaultValue != null) {
            return defaultValue;
        } else {
            throw badValue(interpreter, "KeyError: " + key.toString());
        }
    }

    public BuiltinInstance<PyObject[]> popitem(Interpreter interpreter, PyObject self) {
        HashMap<PyObject, PyObject> entries = getValue(interpreter, self);
        if (entries.isEmpty()) {
            throw badValue(interpreter, "popitem(): dictionary is empty");
        }
        
        Map.Entry<PyObject, PyObject> entry = entries.entrySet().iterator().next();
        PyObject key = entry.getKey();
        PyObject value = entry.getValue();
        entries.remove(key);
        
        PyObject[] pair = new PyObject[]{key, value};
        return interpreter.getMemoryModel().createTuple(pair);
    }

    public void clear(Interpreter interpreter, PyObject self) {
        HashMap<PyObject, PyObject> entries = getValue(interpreter, self);
        entries.clear();
    }

    public void update(Interpreter interpreter, PyObject self, PyObject other) {
        HashMap<PyObject, PyObject> entries = getValue(interpreter, self);
        
        if (is(other)) {
            HashMap<PyObject, PyObject> otherEntries = getValue(interpreter, other);
            entries.putAll(otherEntries);
        } else {
            // Handle iterable of key-value pairs
            try {
                Iterator<PyObject> iterator = other.iterator(interpreter);
                while (iterator.hasNext()) {
                    PyObject item = iterator.next();
                    if (interpreter.getMemoryModel().isTuple(item)) {
                        BuiltinClass<PyObject[]> TUPLE = interpreter.getMemoryModel().getTUPLE();
                        PyObject[] elements = TUPLE.getValue(interpreter, item);
                        if (elements.length == 2) {
                            entries.put(elements[0], elements[1]);
                        } else {
                            throw badValue(interpreter, "dictionary update sequence element must have length 2");
                        }
                    } else {
                        throw badType(interpreter, "cannot convert dictionary update sequence element to a sequence");
                    }
                }
            } catch (Exception e) {
                throw badType(interpreter, "'" + other.getTypeName() + "' object is not iterable");
            }
        }
    }

    public PyObject setdefault(Interpreter interpreter, List<PyObject> args, Map<String, PyObject> kwargs) {
        if (args.isEmpty() || !(args.get(0) instanceof BuiltinInstance ins)) {
            throw badType(interpreter, "'setdefault' is not a static method");
        } else if (!ins.is(this)) {
            throw badType(interpreter, "'setdefault' is not a static method");
        }
        
        if (args.size() == 2) {
            return setdefault(interpreter, ins, args.get(1), interpreter.none());
        } else if (args.size() == 3) {
            return setdefault(interpreter, ins, args.get(1), args.get(2));
        } else {
            throw badValue(interpreter, "setdefault() takes 1 or 2 arguments (" + (args.size() - 1) + " given)");
        }
    }

    public PyObject setdefault(Interpreter interpreter, PyObject self, PyObject key, PyObject defaultValue) {
        HashMap<PyObject, PyObject> entries = getValue(interpreter, self);
        if (entries.containsKey(key)) {
            return entries.get(key);
        } else {
            entries.put(key, defaultValue);
            return defaultValue;
        }
    }

    public BuiltinInstance<HashMap> copy(Interpreter interpreter, PyObject self) {
        HashMap<PyObject, PyObject> entries = getValue(interpreter, self);
        return interpreter.getMemoryModel().createDict(new HashMap<>(entries));
    }

    // 静态方法实现
    public static BuiltinInstance<HashMap> fromkeys(Interpreter interpreter, List<PyObject> args, Map<String, PyObject> kwargs) {
        if (args.size() < 1 || args.size() > 2) {
            throw badValue(interpreter, "fromkeys() takes 1 or 2 positional arguments but " + args.size() + " were given");
        }
        
        PyObject iterable = args.get(0);
        PyObject defaultValue = args.size() > 1 ? args.get(1) : interpreter.none();
        
        HashMap<PyObject, PyObject> result = new HashMap<>();
        try {
            Iterator<PyObject> iterator = iterable.iterator(interpreter);
            while (iterator.hasNext()) {
                PyObject key = iterator.next();
                result.put(key, defaultValue);
            }
        } catch (Exception e) {
            throw badType(interpreter, "'" + iterable.getTypeName() + "' object is not iterable");
        }
        
        return interpreter.getMemoryModel().createDict(result);
    }
}

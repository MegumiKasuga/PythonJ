package edu.carole.runtime.clazz.builtins;

import edu.carole.ast.expressions.Literal;
import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.PyObject;
import edu.carole.runtime.clazz.BuiltinClass;
import edu.carole.runtime.clazz.PyClass;
import edu.carole.runtime.instance.BuiltinInstance;
import edu.carole.runtime.exception.ExceptionWrapper;
import edu.carole.runtime.clazz.builtins.PyBool;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;

public class PySet extends BuiltinClass<HashSet> {
    public PySet(String name, Map<String, PyObject> methods) {
        super(name, methods, HashSet.class);
    }

    public PySet(String name, Map<String, PyObject> methods, List<PyClass> baseClasses) {
        super(name, methods, baseClasses, HashSet.class);
    }

    public PySet(String name, String modulePath, Map<String, PyObject> methods) {
        super(name, modulePath, methods, HashSet.class);
    }

    public PySet(String name, String modulePath, Map<String, PyObject> methods, List<PyClass> baseClasses) {
        super(name, modulePath, methods, baseClasses, HashSet.class);
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

        // 集合运算符
        registerNArgs(methods, "__or__", 1, (args, kwargs, inter) -> {
            return union(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__and__", 1, (args, kwargs, inter) -> {
            return intersection(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__sub__", 1, (args, kwargs, inter) -> {
            return difference(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__xor__", 1, (args, kwargs, inter) -> {
            return symmetricDifference(inter, args.get(0), args.get(1));
        });

        // 集合方法
        registerNArgs(methods, "add", 1, (args, kwargs, inter) -> {
            add(inter, args.get(0), args.get(1));
            return inter.none();
        });
        registerNArgs(methods, "clear", 0, (args, kwargs, inter) -> {
            clear(inter, args.get(0));
            return inter.none();
        });
        registerNArgs(methods, "remove", 1, (args, kwargs, inter) -> {
            remove(inter, args.get(0), args.get(1));
            return inter.none();
        });
        registerNArgs(methods, "discard", 1, (args, kwargs, inter) -> {
            discard(inter, args.get(0), args.get(1));
            return inter.none();
        });
        registerNArgs(methods, "pop", 0, (args, kwargs, inter) -> {
            return pop(inter, args.get(0));
        });
        registerNArgs(methods, "copy", 0, (args, kwargs, inter) -> {
            return copy(inter, args.get(0));
        });
        registerNArgs(methods, "issubset", 1, (args, kwargs, inter) -> {
            return issubset(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "issuperset", 1, (args, kwargs, inter) -> {
            return issuperset(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "isdisjoint", 1, (args, kwargs, inter) -> {
            return isdisjoint(inter, args.get(0), args.get(1));
        });
        registerMethod(methods, "union", (args, kwargs, inter) -> {
            return unionVarArgs(inter, args, kwargs);
        });
        registerMethod(methods, "intersection", (args, kwargs, inter) -> {
            return intersectionVarArgs(inter, args, kwargs);
        });
        registerMethod(methods, "difference", (args, kwargs, inter) -> {
            return differenceVarArgs(inter, args, kwargs);
        });
        registerNArgs(methods, "symmetric_difference", 1, (args, kwargs, inter) -> {
            return symmetricDifference(inter, args.get(0), args.get(1));
        });
        registerMethod(methods, "update", (args, kwargs, inter) -> {
            update(inter, args, kwargs);
            return inter.none();
        });
        registerMethod(methods, "intersection_update", (args, kwargs, inter) -> {
            intersectionUpdate(inter, args, kwargs);
            return inter.none();
        });
        registerMethod(methods, "difference_update", (args, kwargs, inter) -> {
            differenceUpdate(inter, args, kwargs);
            return inter.none();
        });
        registerNArgs(methods, "symmetric_difference_update", 1, (args, kwargs, inter) -> {
            symmetricDifferenceUpdate(inter, args.get(0), args.get(1));
            return inter.none();
        });
    }

    @Override
    public BuiltinClass<HashSet> getPyClass(Interpreter interpreter) {
        return interpreter.getMemoryModel().getSET();
    }

    @Override
    public boolean isValidLiteral(Literal literal) {
        return literal.getValue() instanceof HashSet;
    }

    @Override
    public int hashCode(BuiltinInstance<HashSet> instance) {
        return Objects.hashCode(instance.getValue());
    }

    // 基础方法实现
    public BuiltinInstance<Long> len(Interpreter interpreter, PyObject self) {
        HashSet<PyObject> elements = getValue(interpreter, self);
        return interpreter.getInteger(elements.size());
    }

    public BuiltinInstance<String> str(Interpreter interpreter, PyObject self) {
        HashSet<PyObject> elements = getValue(interpreter, self);
        
        if (elements.isEmpty()) {
            return interpreter.createString("set()");
        }
        
        StringBuilder sb = new StringBuilder("{");
        Iterator<PyObject> iterator = elements.iterator();
        while (iterator.hasNext()) {
            PyObject element = iterator.next();
            if (interpreter.isStr(element)) {
                sb.append("'").append(element.toString()).append("'");
            } else {
                sb.append(element.toString());
            }
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append("}");
        return interpreter.createString(sb.toString());
    }

    public BuiltinInstance<Boolean> contains(Interpreter interpreter, PyObject self, PyObject item) {
        HashSet<PyObject> elements = getValue(interpreter, self);
        return interpreter.boolValue(elements.contains(item));
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
        HashSet<PyObject> elements1 = getValue(interpreter, self);
        HashSet<PyObject> elements2 = getValue(interpreter, other);
        return interpreter.boolValue(elements1.equals(elements2));
    }

    // 集合运算实现
    public BuiltinInstance<HashSet> union(Interpreter interpreter, PyObject self, PyObject other) {
        HashSet<PyObject> elements = getValue(interpreter, self);
        HashSet<PyObject> result = new HashSet<>(elements);
        
        try {
            Iterator<PyObject> iterator = other.iterator(interpreter);
            while (iterator.hasNext()) {
                result.add(iterator.next());
            }
        } catch (Exception e) {
            throw badType(interpreter, "'" + other.getTypeName() + "' object is not iterable");
        }
        
        return interpreter.getMemoryModel().createSet(result);
    }

    public BuiltinInstance<HashSet> intersection(Interpreter interpreter, PyObject self, PyObject other) {
        HashSet<PyObject> elements = getValue(interpreter, self);
        HashSet<PyObject> result = new HashSet<>(elements);
        
        try {
            HashSet<PyObject> otherSet = new HashSet<>();
            Iterator<PyObject> iterator = other.iterator(interpreter);
            while (iterator.hasNext()) {
                otherSet.add(iterator.next());
            }
            result.retainAll(otherSet);
        } catch (Exception e) {
            throw badType(interpreter, "'" + other.getTypeName() + "' object is not iterable");
        }
        
        return interpreter.getMemoryModel().createSet(result);
    }

    public BuiltinInstance<HashSet> difference(Interpreter interpreter, PyObject self, PyObject other) {
        HashSet<PyObject> elements = getValue(interpreter, self);
        HashSet<PyObject> result = new HashSet<>(elements);
        
        try {
            Iterator<PyObject> iterator = other.iterator(interpreter);
            while (iterator.hasNext()) {
                result.remove(iterator.next());
            }
        } catch (Exception e) {
            throw badType(interpreter, "'" + other.getTypeName() + "' object is not iterable");
        }
        
        return interpreter.getMemoryModel().createSet(result);
    }

    public BuiltinInstance<HashSet> symmetricDifference(Interpreter interpreter, PyObject self, PyObject other) {
        HashSet<PyObject> elements = getValue(interpreter, self);
        HashSet<PyObject> result = new HashSet<>(elements);
        
        try {
            HashSet<PyObject> otherSet = new HashSet<>();
            Iterator<PyObject> iterator = other.iterator(interpreter);
            while (iterator.hasNext()) {
                otherSet.add(iterator.next());
            }
            
            // Find intersection
            HashSet<PyObject> intersection = new HashSet<>(result);
            intersection.retainAll(otherSet);
            
            // Remove intersection from both sets
            result.removeAll(intersection);
            otherSet.removeAll(intersection);
            
            // Add remaining elements from other set
            result.addAll(otherSet);
        } catch (Exception e) {
            throw badType(interpreter, "'" + other.getTypeName() + "' object is not iterable");
        }
        
        return interpreter.getMemoryModel().createSet(result);
    }

    // 集合方法实现
    public void add(Interpreter interpreter, PyObject self, PyObject item) {
        HashSet<PyObject> elements = getValue(interpreter, self);
        elements.add(item);
    }

    public void clear(Interpreter interpreter, PyObject self) {
        HashSet<PyObject> elements = getValue(interpreter, self);
        elements.clear();
    }

    public void remove(Interpreter interpreter, PyObject self, PyObject item) {
        HashSet<PyObject> elements = getValue(interpreter, self);
        if (!elements.remove(item)) {
            throw badValue(interpreter, "KeyError: " + item.toString());
        }
    }

    public void discard(Interpreter interpreter, PyObject self, PyObject item) {
        HashSet<PyObject> elements = getValue(interpreter, self);
        elements.remove(item); // No exception if not found
    }

    public PyObject pop(Interpreter interpreter, PyObject self) {
        HashSet<PyObject> elements = getValue(interpreter, self);
        if (elements.isEmpty()) {
            throw badValue(interpreter, "pop from an empty set");
        }
        Iterator<PyObject> iterator = elements.iterator();
        PyObject item = iterator.next();
        iterator.remove();
        return item;
    }

    public BuiltinInstance<HashSet> copy(Interpreter interpreter, PyObject self) {
        HashSet<PyObject> elements = getValue(interpreter, self);
        return interpreter.getMemoryModel().createSet(new HashSet<>(elements));
    }

    public BuiltinInstance<Boolean> issubset(Interpreter interpreter, PyObject self, PyObject other) {
        HashSet<PyObject> elements = getValue(interpreter, self);
        
        try {
            HashSet<PyObject> otherSet = new HashSet<>();
            Iterator<PyObject> iterator = other.iterator(interpreter);
            while (iterator.hasNext()) {
                otherSet.add(iterator.next());
            }
            return interpreter.boolValue(otherSet.containsAll(elements));
        } catch (Exception e) {
            throw badType(interpreter, "'" + other.getTypeName() + "' object is not iterable");
        }
    }

    public BuiltinInstance<Boolean> issuperset(Interpreter interpreter, PyObject self, PyObject other) {
        HashSet<PyObject> elements = getValue(interpreter, self);
        
        try {
            HashSet<PyObject> otherSet = new HashSet<>();
            Iterator<PyObject> iterator = other.iterator(interpreter);
            while (iterator.hasNext()) {
                otherSet.add(iterator.next());
            }
            return interpreter.boolValue(elements.containsAll(otherSet));
        } catch (Exception e) {
            throw badType(interpreter, "'" + other.getTypeName() + "' object is not iterable");
        }
    }

    public BuiltinInstance<Boolean> isdisjoint(Interpreter interpreter, PyObject self, PyObject other) {
        HashSet<PyObject> elements = getValue(interpreter, self);
        
        try {
            Iterator<PyObject> iterator = other.iterator(interpreter);
            while (iterator.hasNext()) {
                if (elements.contains(iterator.next())) {
                    return interpreter.boolFalse();
                }
            }
            return interpreter.boolTrue();
        } catch (Exception e) {
            throw badType(interpreter, "'" + other.getTypeName() + "' object is not iterable");
        }
    }

    // 变长参数方法实现
    public BuiltinInstance<HashSet> unionVarArgs(Interpreter interpreter, List<PyObject> args, Map<String, PyObject> kwargs) {
        if (args.isEmpty() || !(args.get(0) instanceof BuiltinInstance ins)) {
            throw badType(interpreter, "'union' is not a static method");
        } else if (!ins.is(this)) {
            throw badType(interpreter, "'union' is not a static method");
        }
        
        HashSet<PyObject> elements = getValue(interpreter, ins);
        HashSet<PyObject> result = new HashSet<>(elements);
        
        for (int i = 1; i < args.size(); i++) {
            PyObject arg = args.get(i);
            try {
                Iterator<PyObject> iterator = arg.iterator(interpreter);
                while (iterator.hasNext()) {
                    result.add(iterator.next());
                }
            } catch (Exception e) {
                throw badType(interpreter, "'" + arg.getTypeName() + "' object is not iterable");
            }
        }
        
        return interpreter.getMemoryModel().createSet(result);
    }

    public BuiltinInstance<HashSet> intersectionVarArgs(Interpreter interpreter, List<PyObject> args, Map<String, PyObject> kwargs) {
        if (args.isEmpty() || !(args.get(0) instanceof BuiltinInstance ins)) {
            throw badType(interpreter, "'intersection' is not a static method");
        } else if (!ins.is(this)) {
            throw badType(interpreter, "'intersection' is not a static method");
        }
        
        HashSet<PyObject> elements = getValue(interpreter, ins);
        HashSet<PyObject> result = new HashSet<>(elements);
        
        for (int i = 1; i < args.size(); i++) {
            PyObject arg = args.get(i);
            try {
                HashSet<PyObject> argSet = new HashSet<>();
                Iterator<PyObject> iterator = arg.iterator(interpreter);
                while (iterator.hasNext()) {
                    argSet.add(iterator.next());
                }
                result.retainAll(argSet);
            } catch (Exception e) {
                throw badType(interpreter, "'" + arg.getTypeName() + "' object is not iterable");
            }
        }
        
        return interpreter.getMemoryModel().createSet(result);
    }

    public BuiltinInstance<HashSet> differenceVarArgs(Interpreter interpreter, List<PyObject> args, Map<String, PyObject> kwargs) {
        if (args.isEmpty() || !(args.get(0) instanceof BuiltinInstance ins)) {
            throw badType(interpreter, "'difference' is not a static method");
        } else if (!ins.is(this)) {
            throw badType(interpreter, "'difference' is not a static method");
        }
        
        HashSet<PyObject> elements = getValue(interpreter, ins);
        HashSet<PyObject> result = new HashSet<>(elements);
        
        for (int i = 1; i < args.size(); i++) {
            PyObject arg = args.get(i);
            try {
                Iterator<PyObject> iterator = arg.iterator(interpreter);
                while (iterator.hasNext()) {
                    result.remove(iterator.next());
                }
            } catch (Exception e) {
                throw badType(interpreter, "'" + arg.getTypeName() + "' object is not iterable");
            }
        }
        
        return interpreter.getMemoryModel().createSet(result);
    }

    // 更新方法实现
    public void update(Interpreter interpreter, List<PyObject> args, Map<String, PyObject> kwargs) {
        if (args.isEmpty() || !(args.get(0) instanceof BuiltinInstance ins)) {
            throw badType(interpreter, "'update' is not a static method");
        } else if (!ins.is(this)) {
            throw badType(interpreter, "'update' is not a static method");
        }
        
        HashSet<PyObject> elements = getValue(interpreter, ins);
        
        for (int i = 1; i < args.size(); i++) {
            PyObject arg = args.get(i);
            try {
                Iterator<PyObject> iterator = arg.iterator(interpreter);
                while (iterator.hasNext()) {
                    elements.add(iterator.next());
                }
            } catch (Exception e) {
                throw badType(interpreter, "'" + arg.getTypeName() + "' object is not iterable");
            }
        }
    }

    public void intersectionUpdate(Interpreter interpreter, List<PyObject> args, Map<String, PyObject> kwargs) {
        if (args.isEmpty() || !(args.get(0) instanceof BuiltinInstance ins)) {
            throw badType(interpreter, "'intersection_update' is not a static method");
        } else if (!ins.is(this)) {
            throw badType(interpreter, "'intersection_update' is not a static method");
        }
        
        HashSet<PyObject> elements = getValue(interpreter, ins);
        
        for (int i = 1; i < args.size(); i++) {
            PyObject arg = args.get(i);
            try {
                HashSet<PyObject> argSet = new HashSet<>();
                Iterator<PyObject> iterator = arg.iterator(interpreter);
                while (iterator.hasNext()) {
                    argSet.add(iterator.next());
                }
                elements.retainAll(argSet);
            } catch (Exception e) {
                throw badType(interpreter, "'" + arg.getTypeName() + "' object is not iterable");
            }
        }
    }

    public void differenceUpdate(Interpreter interpreter, List<PyObject> args, Map<String, PyObject> kwargs) {
        if (args.isEmpty() || !(args.get(0) instanceof BuiltinInstance ins)) {
            throw badType(interpreter, "'difference_update' is not a static method");
        } else if (!ins.is(this)) {
            throw badType(interpreter, "'difference_update' is not a static method");
        }
        
        HashSet<PyObject> elements = getValue(interpreter, ins);
        
        for (int i = 1; i < args.size(); i++) {
            PyObject arg = args.get(i);
            try {
                Iterator<PyObject> iterator = arg.iterator(interpreter);
                while (iterator.hasNext()) {
                    elements.remove(iterator.next());
                }
            } catch (Exception e) {
                throw badType(interpreter, "'" + arg.getTypeName() + "' object is not iterable");
            }
        }
    }

    public void symmetricDifferenceUpdate(Interpreter interpreter, PyObject self, PyObject other) {
        HashSet<PyObject> elements = getValue(interpreter, self);
        
        try {
            HashSet<PyObject> otherSet = new HashSet<>();
            Iterator<PyObject> iterator = other.iterator(interpreter);
            while (iterator.hasNext()) {
                otherSet.add(iterator.next());
            }
            
            // Find intersection
            HashSet<PyObject> intersection = new HashSet<>(elements);
            intersection.retainAll(otherSet);
            
            // Remove intersection from both sets
            elements.removeAll(intersection);
            otherSet.removeAll(intersection);
            
            // Add remaining elements from other set
            elements.addAll(otherSet);
        } catch (Exception e) {
            throw badType(interpreter, "'" + other.getTypeName() + "' object is not iterable");
        }
    }
}

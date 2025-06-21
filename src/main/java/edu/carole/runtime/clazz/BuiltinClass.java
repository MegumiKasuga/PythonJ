package edu.carole.runtime.clazz;

import edu.carole.ast.expressions.Literal;
import edu.carole.interpreter.BuiltinFunctions;
import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.InstanceBindable;
import edu.carole.runtime.PyObject;
import edu.carole.runtime.exception.ExceptionWrapper;
import edu.carole.runtime.func.PyBuiltinFunction;
import edu.carole.runtime.instance.BuiltinInstance;
import edu.carole.runtime.instance.PyInstance;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public abstract class BuiltinClass<T> extends PyClass {

    @Getter
    private final Class<T> valueClass;
    public BuiltinClass(String name, Map<String, PyObject> methods, Class<T> valueClass) {
        super(name, methods);
        this.valueClass = valueClass;
        initMethods(methods, getClassAttributes());
    }

    public BuiltinClass(String name, Map<String, PyObject> methods, List<PyClass> baseClasses,
                        Class<T> valueClass) {
        super(name, methods, baseClasses);
        this.valueClass = valueClass;
        initMethods(methods, getClassAttributes());
    }

    public BuiltinClass(String name, String modulePath, Map<String, PyObject> methods,
                        Class<T> valueClass) {
        super(name, modulePath, methods);
        this.valueClass = valueClass;
        initMethods(methods, getClassAttributes());
    }

    public BuiltinClass(String name, String modulePath, Map<String, PyObject> methods,
                        List<PyClass> baseClasses, Class<T> valueClass) {
        super(name, modulePath, methods, baseClasses);
        this.valueClass = valueClass;
        initMethods(methods, getClassAttributes());
    }

    public static PyBuiltinFunction createMethod(String name, PyBuiltinFunction.BuiltinFunction func) {
        return new PyBuiltinFunction(name, func);
    }

    public static void registerMethod(Map<String, PyObject> method, String name, PyBuiltinFunction.BuiltinFunction func) {
        method.put(name, createMethod(name, func));
    }

    public boolean is(PyObject obj) {
        return (obj instanceof BuiltinInstance ins) && ins.is(this);
    }

    public abstract void initMethods(Map<String, PyObject> methods, Map<String, PyObject> classAttr);

    public abstract BuiltinClass<T> getPyClass(Interpreter interpreter);

    public abstract boolean isValidLiteral(Literal literal);

    public BuiltinInstance<T> fromLiteral(Interpreter interpreter, Literal literal) {
        if (!isValidLiteral(literal)) return null;
        BuiltinInstance<T> instance = new BuiltinInstance<T>(getPyClass(interpreter), valueClass);
        instance.setValue((T) literal.getValue());
        bindAttrToInstance(interpreter, instance);
        return instance;
    }

    public BuiltinInstance<T> fromValue(Interpreter interpreter, T value) {
        BuiltinInstance<T> instance = new BuiltinInstance<T>(getPyClass(interpreter), valueClass);
        instance.setValue(value);
        bindAttrToInstance(interpreter, instance);
        return instance;
    }

    public void registerNArgs(Map<String, PyObject> methods, String name, int argCount, PyBuiltinFunction.BuiltinFunction func) {
        registerMethod(methods, name, (args, kwargs, inter) -> {
            if (args.isEmpty() || !(args.get(0) instanceof BuiltinInstance ins)) {
                throw BuiltinFunctions.typeError(inter, "'" + name + "' is not an static method");
            } else if (!ins.is(this)) {
                throw BuiltinFunctions.typeError(inter, "'" + name + "' is not an static method");
            }
            if (args.size() != argCount + 1) {
                if (argCount <= 0) {
                    throw BuiltinFunctions.exactlyNArgs(inter, name, 0, args.size() - 1);
                } else {
                    throw BuiltinFunctions.exactlyNArgs(inter, name, argCount, args.size() - 1);
                }
            }
            return func.call(args, kwargs, inter);
        });
    }

    private void bindAttrToInstance(Interpreter interpreter, BuiltinInstance<T> instance) {
        getMethods().forEach((name, method) -> {
            if (method instanceof InstanceBindable bindable) {
                instance.setAttribute(interpreter, name, bindable.bindToInstance(interpreter, instance));
            }
        });
        getProperties().forEach(
            (name, prop) -> {
                instance.setAttribute(interpreter, name, prop.boundToInstance(interpreter, instance));
            }
        );
    }

    public static ExceptionWrapper badType(Interpreter interpreter, String funcName, String needType, String givenType) {
        return badType(interpreter, funcName + " expected return type '" + needType + "' (got '" + givenType + "')");
    }

    public static ExceptionWrapper badType(Interpreter interpreter, String note) {
        PyInstance ins = (PyInstance) interpreter.getExceptions().
                createExceptionInstance("TypeError", List.of());
        ExceptionWrapper wrapper = new ExceptionWrapper(ins);
        wrapper.addNote(interpreter, note);
        return wrapper;
    }

    public static ExceptionWrapper badValue(Interpreter interpreter, String note) {
        PyInstance ins = (PyInstance) interpreter.getExceptions().
                createExceptionInstance("ValueError", List.of());
        ExceptionWrapper wrapper = new ExceptionWrapper(ins);
        wrapper.addNote(interpreter, note);
        return wrapper;
    }

    public static ExceptionWrapper zeroDivisionError(Interpreter interpreter) {
        PyInstance ins = (PyInstance) interpreter.getExceptions().
                createExceptionInstance("ZeroDivisionError", List.of());
        return new ExceptionWrapper(ins);
    }

    public BuiltinInstance biOperate(Interpreter interpreter, String funcName, PyObject self, PyObject other,
                                         BiFunction<T, T, BuiltinInstance<T>> operation) {
        BuiltinInstance<T> me = (BuiltinInstance<T>) self;
        if (is(other)) {
            T b1 = me.getValue();
            T b2 = ((BuiltinInstance<T>) other).getValue();
            return operation.apply(b1, b2);
        } else {
            PyObject func = other.getAttribute(interpreter, "__" + getName() + "__");
            PyObject result = func.call(List.of(), interpreter);
            if (!is(result)) {
                throw badType(interpreter, funcName, getName(), other.getTypeName());
            }
            T b1 = me.getValue();
            T b2 = ((BuiltinInstance<T>) result).getValue();
            return operation.apply(b1, b2);
        }
    }

    public T getValue(Interpreter interpreter, PyObject value) {
        if (!is(value)) {
            throw BuiltinFunctions.typeError(interpreter,
                    "required type '" + this.getTypeName() + "'(got '" + value.getTypeName() + "')");
        }
        return ((BuiltinInstance<T>) value).getValue();
    }

    public abstract int hashCode(BuiltinInstance<T> instance);
}

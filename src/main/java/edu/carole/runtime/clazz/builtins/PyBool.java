package edu.carole.runtime.clazz.builtins;

import edu.carole.ast.expressions.Literal;
import edu.carole.interpreter.BuiltinFunctions;
import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.InstanceBindable;
import edu.carole.runtime.PyObject;
import edu.carole.runtime.clazz.BuiltinClass;
import edu.carole.runtime.clazz.PyClass;
import edu.carole.runtime.exception.BuiltinExceptions;
import edu.carole.runtime.exception.ExceptionWrapper;
import edu.carole.runtime.func.PyBuiltinFunction;
import edu.carole.runtime.instance.BuiltinInstance;
import edu.carole.runtime.instance.PyInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class PyBool extends BuiltinClass<Boolean> {

    public PyBool(String name, Map<String, PyObject> methods) {
        super(name, methods, Boolean.class);
    }

    public PyBool(String name, Map<String, PyObject> methods, List<PyClass> baseClasses) {
        super(name, methods, baseClasses, Boolean.class);
    }

    public PyBool(String name, String modulePath, Map<String, PyObject> methods) {
        super(name, modulePath, methods, Boolean.class);
    }

    public PyBool(String name, String modulePath, Map<String, PyObject> methods, List<PyClass> baseClasses) {
        super(name, modulePath, methods, baseClasses, Boolean.class);
    }

    @Override
    public void initMethods(Map<String, PyObject> methods, Map<String, PyObject> classAttr) {
        // 运算部分
        registerNArgs(methods, "__and__", 1, (args, kwargs, inter) -> {
            return and(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__or__", 1, (args, kwargs, inter) -> {
            return or(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__xor__", 1, (args, kwargs, inter) -> {
            return xor(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__eq__", 1, (args, kwargs, inter) -> {
            return eq(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__ne__", 1, (args, kwargs, inter) -> {
            return ne(inter, args.get(0), args.get(1));
        });


        // 附加
        registerNArgs(methods, "__hash__", 0, (args, kwargs, inter) -> {
            return inter.getInteger(args.get(0).hashCode());
        });
        registerNArgs(methods, "__int__", 0, (args, kwargs, inter) -> {
            boolean value = getValue(inter, args.get(0));
            return inter.getInteger(value ? 1 : 0);
        });
        registerNArgs(methods, "__str__", 0, (args, kwargs, inter) -> {
            boolean value = getValue(inter, args.get(0));
            return inter.createString(value ? "True" : "False");
        });
        registerNArgs(methods, "__repr__", 0, (args, kwargs, inter) -> {
            boolean value = getValue(inter, args.get(0));
            return inter.createString(value ? "True" : "False");
        });
    }

    public static BuiltinInstance<Boolean> reverse(Interpreter interpreter, BuiltinInstance<Boolean> input) {
        return input == interpreter.boolTrue() ? interpreter.boolFalse() : interpreter.boolTrue();
    }

    public static ExceptionWrapper unsupportedOperand(Interpreter interpreter, String syntax, String needType, String type) {
        PyInstance ins = (PyInstance) interpreter.getExceptions().
                createExceptionInstance("SyntaxError", List.of());
        ExceptionWrapper wrapper = new ExceptionWrapper(ins);
        wrapper.addNote(interpreter, "unsupported operand type(s) for " + syntax + ": '" + needType + "' and '" + type + "'");
        throw wrapper;
    }

    public BuiltinInstance<Boolean> and(Interpreter interpreter, PyObject self, PyObject other) {
        return biOperate(interpreter, "__and__", self, other, (a, b) -> fromValue(interpreter, a & b));
    }

    public BuiltinInstance<Boolean> or(Interpreter interpreter, PyObject self, PyObject other) {
        return biOperate(interpreter, "__or__", self, other, (a, b) -> fromValue(interpreter, a | b));
    }

    public BuiltinInstance<Boolean> xor(Interpreter interpreter, PyObject self, PyObject other) {
        return biOperate(interpreter, "__xor__", self, other, (a, b) -> fromValue(interpreter, a ^ b));
    }

    public BuiltinInstance<Boolean> eq(Interpreter interpreter, PyObject self, PyObject other) {
        return biOperate(interpreter, "__eq__", self, other, (a, b) -> fromValue(interpreter, a == b));
    }

    public BuiltinInstance<Boolean> ne(Interpreter interpreter, PyObject self, PyObject other) {
        return biOperate(interpreter, "__ne__", self, other, (a, b) -> fromValue(interpreter, a != b));
    }

    @Override
    public BuiltinClass<Boolean> getPyClass(Interpreter interpreter) {
        return interpreter.getMemoryModel().getBOOL();
    }

    @Override
    public boolean isValidLiteral(Literal literal) {
        return literal.getValue() instanceof Boolean;
    }

    @Override
    public int hashCode(BuiltinInstance<Boolean> instance) {
        return Boolean.hashCode(instance.getValue());
    }
}

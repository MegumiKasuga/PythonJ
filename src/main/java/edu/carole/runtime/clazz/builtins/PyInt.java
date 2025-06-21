package edu.carole.runtime.clazz.builtins;

import edu.carole.ast.expressions.Literal;
import edu.carole.interpreter.BuiltinFunctions;
import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.PyObject;
import edu.carole.runtime.clazz.BuiltinClass;
import edu.carole.runtime.clazz.PyClass;
import edu.carole.runtime.exception.ExceptionWrapper;
import edu.carole.runtime.instance.BuiltinInstance;
import edu.carole.runtime.instance.PyInstance;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class PyInt extends BuiltinClass<Long> {

    public PyInt(String name, Map<String, PyObject> methods) {
        super(name, methods, Long.class);
    }

    public PyInt(String name, Map<String, PyObject> methods, List<PyClass> baseClasses) {
        super(name, methods, baseClasses, Long.class);
    }

    public PyInt(String name, String modulePath, Map<String, PyObject> methods) {
        super(name, modulePath, methods, Long.class);
    }

    public PyInt(String name, String modulePath, Map<String, PyObject> methods, List<PyClass> baseClasses) {
        super(name, modulePath, methods, baseClasses, Long.class);
    }

    @Override
    public void initMethods(Map<String, PyObject> methods, Map<String, PyObject> classAttr) {
        // 运算部分
        registerNArgs(methods, "__add__", 1, (args, kwargs, inter) -> {
            return add(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__sub__", 1, (args, kwargs, inter) -> {
            return sub(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__mul__", 1, (args, kwargs, inter) -> {
            return mul(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__truediv__", 1, (args, kwargs, inter) -> {
            return trueDiv(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__floordiv__", 1, (args, kwargs, inter) -> {
            return floorDiv(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__mod__", 1, (args, kwargs, inter) -> {
            return mod(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__pow__", 1, (args, kwargs, inter) -> {
            return pow(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__neg__", 0, (args, kwargs, inter) -> {
            return neg(inter, args.get(0));
        });
        registerNArgs(methods, "__abs__", 0, (args, kwargs, inter) -> {
            return abs(inter, args.get(0));
        });


        // 比较部分
        registerNArgs(methods, "__eq__", 1, (args, kwargs, inter) -> {
            return eq(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__ne__", 1, (args, kwargs, inter) -> {
            return ne(inter, args.get(0), args.get(1));
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

        // 位操作
        registerNArgs(methods, "__lshift__", 1, (args, kwargs, inter) -> {
            return lShift(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__rshift__", 1, (args, kwargs, inter) -> {
            return rShift(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__and__", 1, (args, kwargs, inter) -> {
            return bitwiseAnd(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__or__", 1, (args, kwargs, inter) -> {
            return bitwiseOr(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__xor__", 1, (args, kwargs, inter) -> {
            return bitwiseXor(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__invert__", 0, (args, kwargs, inter) -> {
            return bitwiseInvert(inter, args.get(0));
        });
        registerNArgs(methods, "bit_length", 0, (args, kwargs, inter) -> {
            return bitLength(inter, args.get(0));
        });
        registerNArgs(methods, "bit_count", 0, (args, kwargs, inter) -> {
            return bitCount(inter, args.get(0));
        });
        registerNArgs(methods, "conjugate", 0, (args, kwargs, inter) -> {
            return args.get(0);
        });


        // 附加
        registerNArgs(methods, "real", 0, (args, kwargs, inter) -> {
            return args.get(0);
        });
        registerNArgs(methods, "imag", 0, (args, kwargs, inter) -> {
            return inter.getInteger(0);
        });
        registerNArgs(methods, "numerator", 0, (args, kwargs, inter) -> {
            return args.get(0);
        });
        registerNArgs(methods, "denominator", 0, (args, kwargs, inter) -> {
            return inter.getInteger(1);
        });
        registerNArgs(methods, "__hash__", 0, (args, kwargs, inter) -> {
            return inter.getInteger(args.get(0).hashCode());
        });
        registerNArgs(methods, "__pos__", 0, (args, kwargs, inter) -> {
            return args.get(0);
        });


        // 类型转换
        registerNArgs(methods, "__bool__", 0, (args, kwargs, inter) -> {
            return bool(inter, args.get(0));
        });
        registerNArgs(methods, "__int__", 0, (args, kwargs, inter) -> {
            return args.get(0);
        });
        registerNArgs(methods, "__float__", 0, (args, kwargs, inter) -> {
            return args.get(0);
        });
        registerNArgs(methods, "__str__", 0, (args, kwargs, inter) -> {
            long value = getValue(inter, args.get(0));
            return inter.createString(String.valueOf(value));
        });
        // TODO: 实现toBytes()
        // TODO: 实现fromBytes()
    }

    public BuiltinInstance add(Interpreter interpreter, PyObject self, PyObject others) {
        return operate(interpreter, "__add__", self, others, Long::sum, (a, b) -> (double) a + b);
    }

    public BuiltinInstance sub(Interpreter interpreter, PyObject self, PyObject others) {
        return operate(interpreter, "__sub__", self, others, (a, b) -> a - b, (a, b) -> (double) a - b);
    }

    public BuiltinInstance mul(Interpreter interpreter, PyObject self, PyObject others) {
        return operate(interpreter, "__mul__", self, others, (a, b) -> a * b, (a, b) -> (double) a * b);
    }

    public BuiltinInstance trueDiv(Interpreter interpreter, PyObject self, PyObject others) {
        return divide(interpreter, "__truediv__", true, self, others);
    }

    public BuiltinInstance floorDiv(Interpreter interpreter, PyObject self, PyObject others) {
        return divide(interpreter, "__floordiv__", false, self, others);
    }

    public BuiltinInstance<Boolean> eq(Interpreter interpreter, PyObject self, PyObject others) {
        return compareOperate(interpreter, self, others,
                Objects::equals, (a, b) -> a <= b && a >= b,
                () -> false);
    }

    public BuiltinInstance<Boolean> ne(Interpreter interpreter, PyObject self, PyObject others) {
        return compareOperate(interpreter, self, others,
                (a, b) -> !Objects.equals(a, b), (a, b) -> !(a <= b && a >= b),
                () -> true);
    }

    public BuiltinInstance<Boolean> lt(Interpreter interpreter, PyObject self, PyObject others) {
        return compareOperate(interpreter, "<", self, others,
                (a, b) -> a < b, (a, b) -> a < b);
    }

    public BuiltinInstance<Boolean> le(Interpreter interpreter, PyObject self, PyObject others) {
        return compareOperate(interpreter, "<=", self, others,
                (a, b) -> a <= b, (a, b) -> a <= b);
    }

    public BuiltinInstance<Boolean> gt(Interpreter interpreter, PyObject self, PyObject others) {
        return compareOperate(interpreter, ">", self, others,
                (a, b) -> a > b, (a, b) -> a > b);
    }

    public BuiltinInstance<Boolean> ge(Interpreter interpreter, PyObject self, PyObject others) {
        return compareOperate(interpreter, ">=", self, others,
                (a, b) -> a >= b, (a, b) -> a >= b);
    }

    public BuiltinInstance<Long> neg(Interpreter interpreter, PyObject self) {
        long value = getValue(interpreter, self);
        return interpreter.getInteger(- value);
    }

    public BuiltinInstance<Long> abs(Interpreter interpreter, PyObject self) {
        long value = getValue(interpreter, self);
        return interpreter.getInteger(Math.abs(value));
    }

    public BuiltinInstance<Long> lShift(Interpreter interpreter, PyObject self, PyObject others) {
        if (!is(others)) {
            throw badType(interpreter, "__lshift__", "int", others.getTypeName());
        }
        long b1 = getValue(interpreter, self);
        long b2 = getValue(interpreter, others);
        if (b2 < 0) {
            throw badValue(interpreter, "shift count could not be negative");
        } else if (b2 >= 64) {
            throw badValue(interpreter, "shift count too large");
        }
        return interpreter.getInteger(b1 << b2);
    }

    public BuiltinInstance<Long> rShift(Interpreter interpreter, PyObject self, PyObject others) {
        if (!is(others)) {
            throw badType(interpreter, "__lshift__", "int", others.getTypeName());
        }
        long b1 = getValue(interpreter, self);
        long b2 = getValue(interpreter, others);
        if (b2 < 0) {
            throw badValue(interpreter, "shift count could not be negative");
        } else if (b2 >= 64) {
            return interpreter.getInteger(b1 < 0 ? -1 : 0);
        }
        return interpreter.getInteger(b1 >> b2);
    }

    public BuiltinInstance<Long> bitwiseAnd(Interpreter interpreter, PyObject self, PyObject others) {
        if (!is(others)) {
            throw badType(interpreter, "__and__", "int", others.getTypeName());
        }
        long b1 = getValue(interpreter, self);
        long b2 = getValue(interpreter, others);
        return interpreter.getInteger(b1 & b2);
    }

    public BuiltinInstance<Long> bitwiseOr(Interpreter interpreter, PyObject self, PyObject others) {
        if (!is(others)) {
            throw badType(interpreter, "__or__", "int", others.getTypeName());
        }
        long b1 = getValue(interpreter, self);
        long b2 = getValue(interpreter, others);
        return interpreter.getInteger(b1 | b2);
    }

    public BuiltinInstance<Long> bitwiseXor(Interpreter interpreter, PyObject self, PyObject others) {
        if (!is(others)) {
            throw badType(interpreter, "__xor__", "int", others.getTypeName());
        }
        long b1 = getValue(interpreter, self);
        long b2 = getValue(interpreter, others);
        return interpreter.getInteger(b1 ^ b2);
    }

    public BuiltinInstance<Long> bitwiseInvert(Interpreter interpreter, PyObject self) {
        long b1 = getValue(interpreter, self);
        return interpreter.getInteger(~ b1);
    }

    public BuiltinInstance<Long> bitLength(Interpreter interpreter, PyObject self) {
        long b1 = getValue(interpreter, self);
        if (b1 == 0) {
            return interpreter.getInteger(0);
        }
        return interpreter.getInteger(Long.SIZE - Long.numberOfLeadingZeros(b1));
    }

    public BuiltinInstance<Long> bitCount(Interpreter interpreter, PyObject self) {
        long value = getValue(interpreter, self);
        if (value < 0) {
            throw badValue(interpreter, "bit_count() only supports non-negative integers");
        }
        return interpreter.getInteger(Long.bitCount(value));
    }

    public BuiltinInstance<Boolean> bool(Interpreter interpreter, PyObject self) {
        long value = getValue(interpreter, self);
        return interpreter.boolValue(value != 0);
    }


    public BuiltinInstance pow(Interpreter interpreter, PyObject self, PyObject others) {
        BuiltinClass<Double> FLOAT = interpreter.getMemoryModel().getFLOAT();
        long b1 = getValue(interpreter, self);
        if (is(others)) {
            long b2 = getValue(interpreter, others);
            return interpreter.getFloat(Math.pow(b1, b2));
        } else if (FLOAT.is(others)) {
            double b2 = FLOAT.getValue(interpreter, others);
            return interpreter.getFloat(Math.pow((double) b1, b2));
        } else {
            throw PyBool.unsupportedOperand(interpreter, "** or pow()", "int", others.getTypeName());
        }
    }

    public BuiltinInstance mod(Interpreter interpreter, PyObject self, PyObject others) {
        BuiltinClass<Double> FLOAT = interpreter.getMemoryModel().getFLOAT();
        if (is(others)) {
            long b1 = getValue(interpreter, self);
            long b2 = getValue(interpreter, others);
            return interpreter.getInteger(b1 % b2);
        } else if (FLOAT.is(others)) {
            double b1 = (double) getValue(interpreter, self);
            double b2 = FLOAT.getValue(interpreter, others);
            if (b2 == 0) {
                throw zeroDivisionError(interpreter);
            }
            return interpreter.getFloat(b1 % b2);
        } else {
            throw badType(interpreter, "__mod__()", "int", others.getTypeName());
        }
    }

    public BuiltinInstance divide(Interpreter interpreter, String funcName, boolean isTrueDivide,
                                  PyObject self, PyObject other) {
        BuiltinClass<Double> FLOAT = interpreter.getMemoryModel().getFLOAT();
        if (is(other)) {
            long b1 = getValue(interpreter, self);
            long b2 = getValue(interpreter, other);
            return isTrueDivide ? interpreter.getFloat((double) b1 / (double) b2) :
                    interpreter.getInteger(Math.floorDiv(b1, b2));
        } else if (FLOAT.is(other)) {
            double b1 = getValue(interpreter, self);
            double b2 = FLOAT.getValue(interpreter, other);
            return isTrueDivide ? interpreter.getFloat(b1 / b2) :
                    interpreter.getInteger(((Double) Math.floor(b1 / b2)).longValue());
        } else {
            throw  BuiltinFunctions.typeError(interpreter, funcName, "'int' or 'float'", other.getTypeName());
        }
    }

    public static ExceptionWrapper notSupported(Interpreter interpreter, String syntax, String type1, String type2) {
        PyInstance ins = (PyInstance) interpreter.getExceptions().
                createExceptionInstance("TypeError", List.of());
        ExceptionWrapper wrapper = new ExceptionWrapper(ins);
        wrapper.addNote(interpreter, "'" + syntax + "' not supported between instances of '" + type1 + "' and '" + type2 + "'");
        return wrapper;
    }

    public BuiltinInstance<Boolean> compareOperate(Interpreter interpreter, String syntax, PyObject self, PyObject others,
                                          BiFunction<Long, Long, Boolean> compare,
                                          BiFunction<Long, Double, Boolean> compare2) {
        BuiltinClass<Double> FLOAT = interpreter.getMemoryModel().getFLOAT();
        long b1 = getValue(interpreter, self);
        if (is(others)) {
            long b2 = getValue(interpreter, others);
            return interpreter.boolValue(compare.apply(b1, b2));
        } else if (FLOAT.is(others)) {
            double b2 = FLOAT.getValue(interpreter, others);
            return interpreter.boolValue(compare2.apply(b1, b2));
        } else {
            throw notSupported(interpreter, syntax, "int", others.getTypeName());
        }
    }

    public BuiltinInstance<Boolean> compareOperate(Interpreter interpreter,PyObject self, PyObject others,
                                          BiFunction<Long, Long, Boolean> compare,
                                          BiFunction<Long, Double, Boolean> compare2,
                                          Supplier<Boolean> defaultValue) {
        BuiltinClass<Double> FLOAT = interpreter.getMemoryModel().getFLOAT();
        long b1 = getValue(interpreter, self);
        if (is(others)) {
            long b2 = getValue(interpreter, others);
            return interpreter.boolValue(compare.apply(b1, b2));
        } else if (FLOAT.is(others)) {
            double b2 = FLOAT.getValue(interpreter, others);
            return interpreter.boolValue(compare2.apply(b1, b2));
        } else {
            return interpreter.boolValue(defaultValue.get());
        }
    }

    public BuiltinInstance operate(Interpreter interpreter, String funcName, PyObject self, PyObject other,
                                            BiFunction<Long, Long, Long> operation,
                                            BiFunction<Long, Double, Double> floatOperation) {
        BuiltinClass<Double> FLOAT = interpreter.getMemoryModel().getFLOAT();
        long b1 = getValue(interpreter, self);
        if (is(other)) {
            long b2 = getValue(interpreter, other);
            return interpreter.getInteger(operation.apply(b1, b2));
        } else if (FLOAT.is(other)) {
            double b2 = FLOAT.getValue(interpreter, other);
            return FLOAT.fromValue(interpreter, floatOperation.apply(b1, b2));
        } else {
            PyObject func = other.getAttribute(interpreter, "__int__");
            PyObject result = func.call(List.of(), interpreter);
            if (!is(result)) {
                throw badType(interpreter, funcName, "int", other.getTypeName());
            }
            long b2 = getValue(interpreter, result);
            return interpreter.getInteger(operation.apply(b1, b2));
        }
    }

    @Override
    public BuiltinClass<Long> getPyClass(Interpreter interpreter) {
        return interpreter.getMemoryModel().getINTEGER();
    }

    @Override
    public boolean isValidLiteral(Literal literal) {
        return literal.getValue() instanceof Long;
    }

    public int hashCode(BuiltinInstance<Long> value) {
        return Long.hashCode(value.getValue());
    }
}

package edu.carole.runtime.clazz.builtins;

import edu.carole.ast.expressions.Literal;
import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.PyInt;
import edu.carole.runtime.PyObject;
import edu.carole.runtime.PyTuple;
import edu.carole.runtime.clazz.BuiltinClass;
import edu.carole.runtime.clazz.PyClass;
import edu.carole.runtime.instance.BuiltinInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class PyFloat extends BuiltinClass<Double> {

    public PyFloat(String name, Map<String, PyObject> methods) {
        super(name, methods, Double.class);
    }

    public PyFloat(String name, Map<String, PyObject> methods, List<PyClass> baseClasses) {
        super(name, methods, baseClasses, Double.class);
    }

    public PyFloat(String name, String modulePath, Map<String, PyObject> methods) {
        super(name, modulePath, methods, Double.class);
    }

    public PyFloat(String name, String modulePath, Map<String, PyObject> methods, List<PyClass> baseClasses) {
        super(name, modulePath, methods, baseClasses, Double.class);
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
            return PyBool.reverse(inter, eq(inter, args.get(0), args.get(1)));
        });
        registerNArgs(methods, "__lt__", 1, (args, kwargs, inter) -> {
            return compare(inter, args.get(0), args.get(1), (a, b) -> a < b);
        });
        registerNArgs(methods, "__le__", 1, (args, kwargs, inter) -> {
            return compare(inter, args.get(0), args.get(1), (a, b) -> a <= b);
        });
        registerNArgs(methods, "__gt__", 1, (args, kwargs, inter) -> {
            return compare(inter, args.get(0), args.get(1), (a, b) -> a > b);
        });
        registerNArgs(methods, "__ge__", 1, (args, kwargs, inter) -> {
            return compare(inter, args.get(0), args.get(1), (a, b) -> a >= b);
        });


        // 浮点数专有
        registerNArgs(methods, "is_integer", 0, (args, kwargs, inter) -> {
            return isInteger(inter, args.get(0));
        });
        registerNArgs(methods, "as_integer_ratio", 0, (args, kwargs, inter) -> {
            return asIntegerRatio(inter, args.get(0));
        });
        registerNArgs(methods, "conjugate", 0, (args, kwargs, inter) -> {
            return args.get(0);
        });
        registerNArgs(methods, "real", 0, (args, kwargs, inter) -> {
            return args.get(0);
        });
        registerNArgs(methods, "imag", 0, (args, kwargs, inter) -> {
            return this.fromValue(inter, 0.0);
        });


        // 类型转换
        registerNArgs(methods, "__bool__", 0, (args, kwargs, inter) -> {
            double value = getValue(inter, args.get(0));
            return inter.boolValue(value != 0.0);
        });
        registerNArgs(methods, "__int__", 0, (args, kwargs, inter) -> {
            double value = getValue(inter, args.get(0));
            return inter.getInteger(((Double) value).longValue());
        });
        registerNArgs(methods, "__hash__", 0, (args, kwargs, inter) -> {
            return inter.getInteger(args.get(0).hashCode());
        });
        registerNArgs(methods, "__float__", 0, (args, kwargs, inter) -> {
            return args.get(0);
        });
        registerNArgs(methods, "__pos__", 0, (args, kwargs, inter) -> {
            return args.get(0);
        });
        registerNArgs(methods, "__str__", 0, (args, kwargs, inter) -> {
            double value = getValue(inter, args.get(0));
            return inter.createString(String.valueOf(value));
        });
        registerNArgs(methods, "__repr__", 0, (args, kwargs, inter) -> {
            double value = getValue(inter, args.get(0));
            return inter.createString(String.valueOf(value));
        });
        // TODO: 待PyString完成后，完成hex和fromHex方法
    }



    public BuiltinInstance<Long> asIntegerRatio(Interpreter interpreter, PyObject self) {
        double value = getValue(interpreter, self);
        if (Double.isNaN(value)) {
            throw badValue(interpreter, "cannot convert NaN to integer ratio");
        } else if (Double.isInfinite(value)) {
            throw badValue(interpreter, "cannot convert infinity to integer ratio");
        }
        // Use binary representation to get exact ratio
        long bits = Double.doubleToLongBits(value);
        boolean negative = (bits & 0x8000000000000000L) != 0;
        int exponent = (int) ((bits & 0x7FF0000000000000L) >>> 52) - 1023;
        long mantissa = bits & 0x000FFFFFFFFFFFFFL;

        if (exponent == -1023) { // Subnormal number
            exponent = -1022;
        } else {
            mantissa |= 0x0010000000000000L; // Add implicit leading 1
        }

        // Calculate numerator and denominator
        long numerator = mantissa;
        long denominator = 1L << 52;

        if (exponent > 0) {
            numerator <<= exponent;
        } else if (exponent < 0) {
            denominator <<= -exponent;
        }

        if (negative) {
            numerator = -numerator;
        }

        // Reduce to lowest terms using GCD
        long gcd = gcd(Math.abs(numerator), denominator);
        numerator /= gcd;
        denominator /= gcd;
        List<PyObject> result = new ArrayList<>();
        result.add(interpreter.getInteger(numerator));
        result.add(interpreter.getInteger(denominator));
        // TODO: 待PyTuple完成后，完成它
        return interpreter.getInteger(0);
    }

    private static long gcd(long a, long b) {
        while (b != 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    public BuiltinInstance<Boolean> isInteger(Interpreter interpreter, PyObject self) {
        double value = getValue(interpreter, self);
        return interpreter.boolValue(Math.floor(value) == value && !Double.isInfinite(value));
    }

    public BuiltinInstance<Boolean> compare(Interpreter interpreter, PyObject self, PyObject other,
                                            BiFunction<Double, Double, Boolean> operation) {
        BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
        double b1 = getValue(interpreter, self);
        double b2;
        if (is(other)) {
            b2 = getValue(interpreter, other);
        } else if (INTEGER.is(other)) {
            b2 = (double) INTEGER.getValue(interpreter, other);
        } else {
            throw badType(interpreter, "__lt__", "'float' or 'int'", other.getTypeName());
        }
        return interpreter.boolValue(operation.apply(b1, b2));
    }

    public BuiltinInstance<Boolean> eq(Interpreter interpreter, PyObject self, PyObject other) {
        BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
        double b1 = getValue(interpreter, self);
        double b2;
        if (is(other)) {
            b2 = getValue(interpreter, other);
        } else if (INTEGER.is(other)) {
            b2 = (double) INTEGER.getValue(interpreter, other);
        } else {
            return interpreter.boolFalse();
        }
        return interpreter.boolValue(b1 == b2);
    }

    public BuiltinInstance<Double> neg(Interpreter interpreter, PyObject self) {
        return this.fromValue(interpreter, - getValue(interpreter, self));
    }

    public BuiltinInstance<Double> abs(Interpreter interpreter, PyObject self) {
        return this.fromValue(interpreter, Math.abs(getValue(interpreter, self)));
    }

    public BuiltinInstance<Double> add(Interpreter interpreter, PyObject self, PyObject others) {
        return operate(interpreter, "__add__", self, others, Double::sum);
    }

    public BuiltinInstance<Double> sub(Interpreter interpreter, PyObject self, PyObject others) {
        return operate(interpreter, "__sub__", self, others, (a, b) -> a - b);
    }

    public BuiltinInstance<Double> mul(Interpreter interpreter, PyObject self, PyObject others) {
        return operate(interpreter, "__mul__", self, others, (a, b) -> a * b);
    }

    public BuiltinInstance<Double> trueDiv(Interpreter interpreter, PyObject self, PyObject others) {
        return operate(interpreter, "__truediv__", self, others, (a, b) -> a / b);
    }

    public BuiltinInstance floorDiv(Interpreter interpreter, PyObject self, PyObject others) {
        BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
        double b1 = getValue(interpreter, self);
        double b2;
        if (is(others)) {
            b2 = getValue(interpreter, others);
        } else if (INTEGER.is(others)) {
            b2 = (double) INTEGER.getValue(interpreter, others);
        } else {
            throw badType(interpreter, "__floordiv__", "'float' or 'int'", others.getTypeName());
        }
        return interpreter.getInteger(((Double) Math.floor(b1 / b2)).longValue());
    }

    public BuiltinInstance<Double> pow(Interpreter interpreter, PyObject self, PyObject others) {
        return operate(interpreter, "__pow__", self, others, Math::pow);
    }

    public BuiltinInstance<Double> mod(Interpreter interpreter, PyObject self, PyObject others) {
        BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
        double b1 = getValue(interpreter, self);
        double b2;
        if (is(others)) {
            b2 = getValue(interpreter, others);
        } else if (INTEGER.is(others)) {
            b2 = (double) INTEGER.getValue(interpreter, others);
        } else {
            throw badType(interpreter, "__mod__", "'float' or 'int'", others.getTypeName());
        }
        if (b2 == 0.0) {
            throw zeroDivisionError(interpreter);
        }
        return this.fromValue(interpreter, b1 % b2);
    }

    public BuiltinInstance<Double> operate(Interpreter interpreter, String funcName, PyObject self, PyObject others,
                                   BiFunction<Double, Double, Double> operation) {
        BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
        double b1 = getValue(interpreter, self);
        double b2;
        if (is(others)) {
            b2 = getValue(interpreter, others);
        } else if (INTEGER.is(others)) {
            b2 = (double) INTEGER.getValue(interpreter, others);
        } else {
            throw badType(interpreter, funcName, "'float' or 'int'", others.getTypeName());
        }
        return this.fromValue(interpreter, operation.apply(b1, b2));
    }

    @Override
    public BuiltinClass<Double> getPyClass(Interpreter interpreter) {
        return null;
    }

    @Override
    public boolean isValidLiteral(Literal literal) {
        return literal.getValue() instanceof Double;
    }

    @Override
    public int hashCode(BuiltinInstance<Double> instance) {
        return Double.hashCode(instance.getValue());
    }
}

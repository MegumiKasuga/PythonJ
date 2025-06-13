package edu.carole.runtime;

import edu.carole.runtime.base.PyObjectWithMethods;
import edu.carole.runtime.registry.MethodBuilder;

/**
 * Python浮点数类型 - 使用方法注册系统重构版本
 */
public class PyFloat extends PyObjectWithMethods {
    private final double value;
    
    public PyFloat(double value) {
        this.value = value;
        registerMethods();
    }
    
    public double getValue() { return value; }
    
    @Override
    public String getTypeName() { return "float"; }
    
    @Override
    public String toString() { return String.valueOf(value); }
    
    @Override
    public boolean isTruthy() { return value != 0.0; }
      @Override
    protected void registerMethods() {
        // 基本方法
        methodRegistry.registerMethod("__str__", MethodBuilder.noArgs(() -> new PyString(toString())));
        methodRegistry.registerMethod("__repr__", MethodBuilder.noArgs(() -> new PyString(toString())));
        methodRegistry.registerMethod("__bool__", MethodBuilder.noArgs(() -> PyBool.valueOf(isTruthy())));
        methodRegistry.registerMethod("__int__", MethodBuilder.noArgs(() -> new PyInt((long)value)));
        methodRegistry.registerMethod("__float__", MethodBuilder.noArgs(() -> this));
        methodRegistry.registerMethod("__hash__", MethodBuilder.noArgs(() -> new PyInt(hashCode())));
        methodRegistry.registerMethod("__pos__", MethodBuilder.noArgs(() -> this));
        
        // 算术运算方法
        methodRegistry.registerMethod("__add__", MethodBuilder.oneArg(this::add));
        methodRegistry.registerMethod("__sub__", MethodBuilder.oneArg(this::subtract));
        methodRegistry.registerMethod("__mul__", MethodBuilder.oneArg(this::multiply));
        methodRegistry.registerMethod("__truediv__", MethodBuilder.oneArg(this::trueDivide));
        methodRegistry.registerMethod("__floordiv__", MethodBuilder.oneArg(this::floorDivide));
        methodRegistry.registerMethod("__mod__", MethodBuilder.oneArg(this::modulo));
        methodRegistry.registerMethod("__pow__", MethodBuilder.oneArg(this::power));
        methodRegistry.registerMethod("__neg__", MethodBuilder.noArgs(this::negate));
        methodRegistry.registerMethod("__abs__", MethodBuilder.noArgs(this::absolute));
        
        // 比较运算方法
        methodRegistry.registerMethod("__eq__", MethodBuilder.oneArg(this::equals_));
        methodRegistry.registerMethod("__ne__", MethodBuilder.oneArg(this::notEquals));
        methodRegistry.registerMethod("__lt__", MethodBuilder.oneArg(this::lessThan));
        methodRegistry.registerMethod("__le__", MethodBuilder.oneArg(this::lessEqual));
        methodRegistry.registerMethod("__gt__", MethodBuilder.oneArg(this::greaterThan));
        methodRegistry.registerMethod("__ge__", MethodBuilder.oneArg(this::greaterEqual));
        
        // 浮点数特有方法
        methodRegistry.registerMethod("is_integer", MethodBuilder.noArgs(this::isInteger));
        methodRegistry.registerMethod("as_integer_ratio", MethodBuilder.noArgs(this::asIntegerRatio));
        methodRegistry.registerMethod("hex", MethodBuilder.noArgs(this::hex));
        methodRegistry.registerMethod("conjugate", MethodBuilder.noArgs(this::conjugate));
          // 属性
        methodRegistry.registerMethod("real", MethodBuilder.noArgs(this::getReal));
        methodRegistry.registerMethod("imag", MethodBuilder.noArgs(this::getImag));
        
        // 静态方法
        methodRegistry.registerMethod("fromhex", MethodBuilder.oneArg(this::fromhex));
    }
      // 算术运算方法实现
    private PyObject add(PyObject other) {
        if (other instanceof PyInt) {
            return new PyFloat(this.value + ((PyInt) other).getValue());
        } else if (other instanceof PyFloat) {
            return new PyFloat(this.value + ((PyFloat) other).getValue());
        } else if (other instanceof PyComplex otherComplex) {
            return new PyComplex(this.value + otherComplex.getReal(), otherComplex.getImag());
        } else {
            throw new RuntimeException("unsupported operand type(s) for +: 'float' and '" + other.getTypeName() + "'");
        }
    }

    private PyObject subtract(PyObject other) {
        if (other instanceof PyInt) {
            return new PyFloat(this.value - ((PyInt) other).getValue());
        } else if (other instanceof PyFloat) {
            return new PyFloat(this.value - ((PyFloat) other).getValue());
        } else if (other instanceof PyComplex otherComplex) {
            return new PyComplex(this.value - otherComplex.getReal(), -otherComplex.getImag());
        } else {
            throw new RuntimeException("unsupported operand type(s) for -: 'float' and '" + other.getTypeName() + "'");
        }
    }

    private PyObject multiply(PyObject other) {
        if (other instanceof PyInt) {
            return new PyFloat(this.value * ((PyInt) other).getValue());
        } else if (other instanceof PyFloat) {
            return new PyFloat(this.value * ((PyFloat) other).getValue());
        } else if (other instanceof PyComplex otherComplex) {
            return new PyComplex(this.value * otherComplex.getReal(), this.value * otherComplex.getImag());
        } else {
            throw new RuntimeException("unsupported operand type(s) for *: 'float' and '" + other.getTypeName() + "'");
        }
    }

    private PyObject trueDivide(PyObject other) {
        if (other instanceof PyInt) {
            long otherValue = ((PyInt) other).getValue();
            if (otherValue == 0) {
                throw new RuntimeException("float division by zero");
            }
            return new PyFloat(this.value / otherValue);
        } else if (other instanceof PyFloat) {
            double otherValue = ((PyFloat) other).getValue();
            if (otherValue == 0.0) {
                throw new RuntimeException("float division by zero");
            }
            return new PyFloat(this.value / otherValue);
        } else {
            throw new RuntimeException("unsupported operand type(s) for /: 'float' and '" + other.getTypeName() + "'");
        }
    }

    private PyObject floorDivide(PyObject other) {
        if (other instanceof PyInt) {
            long otherValue = ((PyInt) other).getValue();
            if (otherValue == 0) {
                throw new RuntimeException("float floor division by zero");
            }
            return new PyFloat(Math.floor(this.value / otherValue));
        } else if (other instanceof PyFloat) {
            double otherValue = ((PyFloat) other).getValue();
            if (otherValue == 0.0) {
                throw new RuntimeException("float floor division by zero");
            }
            return new PyFloat(Math.floor(this.value / otherValue));
        } else {
            throw new RuntimeException("unsupported operand type(s) for //: 'float' and '" + other.getTypeName() + "'");
        }
    }

    private PyObject modulo(PyObject other) {
        if (other instanceof PyInt) {
            long otherValue = ((PyInt) other).getValue();
            if (otherValue == 0) {
                throw new RuntimeException("float modulo");
            }
            return new PyFloat(this.value % otherValue);
        } else if (other instanceof PyFloat) {
            double otherValue = ((PyFloat) other).getValue();
            if (otherValue == 0.0) {
                throw new RuntimeException("float modulo");
            }
            return new PyFloat(this.value % otherValue);
        } else {
            throw new RuntimeException("unsupported operand type(s) for %: 'float' and '" + other.getTypeName() + "'");
        }
    }

    private PyObject power(PyObject other) {
        if (other instanceof PyInt) {
            long otherValue = ((PyInt) other).getValue();
            return new PyFloat(Math.pow(this.value, otherValue));
        } else if (other instanceof PyFloat) {
            double otherValue = ((PyFloat) other).getValue();
            return new PyFloat(Math.pow(this.value, otherValue));
        } else {
            throw new RuntimeException("unsupported operand type(s) for ** or pow(): 'float' and '" + other.getTypeName() + "'");
        }
    }
    
    private PyObject negate() {
        return new PyFloat(-this.value);
    }
    
    private PyObject absolute() {
        return new PyFloat(Math.abs(this.value));
    }
    
    // 比较运算方法实现
    private PyObject equals_(PyObject other) {
        return PyBool.valueOf(this.equals(other));
    }
    
    private PyObject notEquals(PyObject other) {
        return PyBool.valueOf(!this.equals(other));
    }

    private PyObject lessThan(PyObject other) {
        if (other instanceof PyInt) {
            return PyBool.valueOf(this.value < ((PyInt) other).getValue());
        } else if (other instanceof PyFloat) {
            return PyBool.valueOf(this.value < ((PyFloat) other).getValue());
        } else {
            throw new RuntimeException("'<' not supported between instances of 'float' and '" + other.getTypeName() + "'");
        }
    }

    private PyObject lessEqual(PyObject other) {
        if (other instanceof PyInt) {
            return PyBool.valueOf(this.value <= ((PyInt) other).getValue());
        } else if (other instanceof PyFloat) {
            return PyBool.valueOf(this.value <= ((PyFloat) other).getValue());
        } else {
            throw new RuntimeException("'<=' not supported between instances of 'float' and '" + other.getTypeName() + "'");
        }
    }

    private PyObject greaterThan(PyObject other) {
        if (other instanceof PyInt) {
            return PyBool.valueOf(this.value > ((PyInt) other).getValue());
        } else if (other instanceof PyFloat) {
            return PyBool.valueOf(this.value > ((PyFloat) other).getValue());
        } else {
            throw new RuntimeException("'>' not supported between instances of 'float' and '" + other.getTypeName() + "'");
        }
    }

    private PyObject greaterEqual(PyObject other) {
        if (other instanceof PyInt) {
            return PyBool.valueOf(this.value >= ((PyInt) other).getValue());
        } else if (other instanceof PyFloat) {
            return PyBool.valueOf(this.value >= ((PyFloat) other).getValue());
        } else {
            throw new RuntimeException("'>=' not supported between instances of 'float' and '" + other.getTypeName() + "'");
        }
    }
    
    // 浮点数特有方法实现
    private PyObject isInteger() {
        return PyBool.valueOf(this.value == Math.floor(this.value) && !Double.isInfinite(this.value));
    }
    
    private PyObject asIntegerRatio() {
        if (Double.isNaN(this.value)) {
            throw new RuntimeException("cannot convert NaN to integer ratio");
        }
        if (Double.isInfinite(this.value)) {
            throw new RuntimeException("cannot convert infinity to integer ratio");
        }
        
        // Use binary representation to get exact ratio
        long bits = Double.doubleToLongBits(this.value);
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
        denominator /= gcd;        java.util.List<PyObject> result = new java.util.ArrayList<>();
        result.add(new PyInt(numerator));
        result.add(new PyInt(denominator));
        return new PyTuple(result);
    }

    private PyObject hex() {
        if (Double.isNaN(this.value)) {
            return new PyString("nan");
        }
        if (this.value == Double.POSITIVE_INFINITY) {
            return new PyString("inf");
        }
        if (this.value == Double.NEGATIVE_INFINITY) {
            return new PyString("-inf");
        }
        return new PyString(Double.toHexString(this.value));
    }
    
    private PyObject conjugate() {
        return new PyFloat(this.value); // For real numbers, conjugate is itself
    }
    
    // 属性方法实现
    private PyObject getReal() {
        return new PyFloat(this.value);
    }
    
    private PyObject getImag() {
        return new PyFloat(0.0);
    }
    
    // Helper method to calculate GCD
    private static long gcd(long a, long b) {
        while (b != 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
    
    // Static method for fromhex (would be accessible as float.fromhex in Python)
    public static PyFloat fromHex(String hexString) {
        try {
            // Handle special cases
            String s = hexString.trim().toLowerCase();
            switch (s) {
                case "nan" -> {
                    return new PyFloat(Double.NaN);
                }
                case "inf", "+inf", "infinity", "+infinity" -> {
                    return new PyFloat(Double.POSITIVE_INFINITY);
                }
                case "-inf", "-infinity" -> {
                    return new PyFloat(Double.NEGATIVE_INFINITY);
                }
            }

            // Parse hex float
            if (s.startsWith("0x") || s.startsWith("-0x") || s.startsWith("+0x")) {
                return new PyFloat(Double.parseDouble(s));
            } else {
                // Add 0x prefix if not present
                if (s.startsWith("-")) {
                    return new PyFloat(Double.parseDouble("-0x" + s.substring(1)));
                } else if (s.startsWith("+")) {
                    return new PyFloat(Double.parseDouble("0x" + s.substring(1)));
                } else {
                    return new PyFloat(Double.parseDouble("0x" + s));
                }
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("invalid hexadecimal floating-point string");
        }
    }

    @Override
    public boolean equals(PyObject other) {
        if (other instanceof PyInt) {
            return ((PyInt) other).getValue() == value;
        }
        if (other instanceof PyFloat) {
            return ((PyFloat) other).getValue() == value;
        }
        return false;
    }
    
    // fromhex 静态方法实现
    private PyObject fromhex(PyObject hexStr) {
        if (!(hexStr instanceof PyString)) {
            throw new RuntimeException("fromhex() argument must be a string");
        }
        
        String hexString = ((PyString) hexStr).getValue().trim();
        try {
            // 简化实现：使用Java的Double.parseDouble处理十六进制字符串
            if (hexString.toLowerCase().startsWith("0x")) {
                hexString = hexString.substring(2);
            }
            
            // 特殊情况处理
            if ("inf".equalsIgnoreCase(hexString) || "infinity".equalsIgnoreCase(hexString)) {
                return new PyFloat(Double.POSITIVE_INFINITY);
            } else if ("-inf".equalsIgnoreCase(hexString) || "-infinity".equalsIgnoreCase(hexString)) {
                return new PyFloat(Double.NEGATIVE_INFINITY);
            } else if ("nan".equalsIgnoreCase(hexString)) {
                return new PyFloat(Double.NaN);
            }
            
            // 尝试解析十六进制浮点数
            double result = Double.parseDouble(hexString);
            return new PyFloat(result);
        } catch (NumberFormatException e) {
            throw new RuntimeException("invalid hexadecimal floating-point string: '" + hexString + "'");
        }
    }

    // Java Object.equals() and hashCode() for proper HashMap functionality
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PyFloat pyFloat = (PyFloat) obj;
        return Double.compare(pyFloat.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }
}

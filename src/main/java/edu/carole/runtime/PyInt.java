package edu.carole.runtime;

import edu.carole.runtime.base.PyObjectWithMethods;
import edu.carole.runtime.registry.MethodBuilder;

/**
 * Python整数类型 - 使用方法注册系统重构版本
 */
public class PyInt extends PyObjectWithMethods {
    private final long value;
    
    public PyInt(long value) {
        this.value = value;
        registerMethods();
    }
    
    public long getValue() { return value; }
    
    @Override
    public String getTypeName() { return "int"; }
    
    @Override
    public String toString() { return String.valueOf(value); }
    
    @Override
    public boolean isTruthy() { return value != 0; }

    @Override
    protected void registerMethods() {
        // 基本方法
        methodRegistry.registerMethod("__str__", MethodBuilder.noArgs(() -> new PyString(toString())));
        methodRegistry.registerMethod("__repr__", MethodBuilder.noArgs(() -> new PyString(toString())));
        methodRegistry.registerMethod("__bool__", MethodBuilder.noArgs(() -> PyBool.valueOf(isTruthy())));
        methodRegistry.registerMethod("__int__", MethodBuilder.noArgs(() -> this));
        methodRegistry.registerMethod("__float__", MethodBuilder.noArgs(() -> new PyFloat(value)));
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
        
        // 位运算方法
        methodRegistry.registerMethod("__lshift__", MethodBuilder.oneArg(this::leftShift));
        methodRegistry.registerMethod("__rshift__", MethodBuilder.oneArg(this::rightShift));
        methodRegistry.registerMethod("__and__", MethodBuilder.oneArg(this::bitwiseAnd));
        methodRegistry.registerMethod("__or__", MethodBuilder.oneArg(this::bitwiseOr));
        methodRegistry.registerMethod("__xor__", MethodBuilder.oneArg(this::bitwiseXor));
        methodRegistry.registerMethod("__invert__", MethodBuilder.noArgs(this::bitwiseInvert));
        
        // 整数特有方法
        methodRegistry.registerMethod("bit_length", MethodBuilder.noArgs(this::bitLength));
        methodRegistry.registerMethod("bit_count", MethodBuilder.noArgs(this::bitCount));
        methodRegistry.registerMethod("to_bytes", MethodBuilder.exactArgs(2, this::toBytes));
        methodRegistry.registerMethod("conjugate", MethodBuilder.noArgs(this::conjugate));
          // 属性
        methodRegistry.registerMethod("real", MethodBuilder.noArgs(this::getReal));
        methodRegistry.registerMethod("imag", MethodBuilder.noArgs(this::getImag));
        methodRegistry.registerMethod("numerator", MethodBuilder.noArgs(this::getNumerator));
        methodRegistry.registerMethod("denominator", MethodBuilder.noArgs(this::getDenominator));
        
        // 静态方法
        methodRegistry.registerMethod("from_bytes", MethodBuilder.varArgs(this::fromBytes));
    }
    
    // 算术运算方法实现
    private PyObject add(PyObject other) {
        if (other instanceof PyInt) {
            return new PyInt(this.value + ((PyInt) other).getValue());
        } else if (other instanceof PyFloat) {
            return new PyFloat(this.value + ((PyFloat) other).getValue());
        } else if (other instanceof PyComplex otherComplex) {
            return new PyComplex(this.value + otherComplex.getReal(), otherComplex.getImag());
        } else {
            throw new RuntimeException("unsupported operand type(s) for +: 'int' and '" + other.getTypeName() + "'");
        }
    }
    
    private PyObject subtract(PyObject other) {
        if (other instanceof PyInt) {
            return new PyInt(this.value - ((PyInt) other).getValue());
        } else if (other instanceof PyFloat) {
            return new PyFloat(this.value - ((PyFloat) other).getValue());
        } else if (other instanceof PyComplex otherComplex) {
            return new PyComplex(this.value - otherComplex.getReal(), -otherComplex.getImag());
        } else {
            throw new RuntimeException("unsupported operand type(s) for -: 'int' and '" + other.getTypeName() + "'");
        }
    }
    
    private PyObject multiply(PyObject other) {
        if (other instanceof PyInt) {
            return new PyInt(this.value * ((PyInt) other).getValue());
        } else if (other instanceof PyFloat) {
            return new PyFloat(this.value * ((PyFloat) other).getValue());
        } else if (other instanceof PyComplex otherComplex) {
            return new PyComplex(this.value * otherComplex.getReal(), this.value * otherComplex.getImag());
        } else if (other instanceof PyString str) {
            // String repetition
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < this.value; i++) {
                result.append(str.getValue());
            }
            return new PyString(result.toString());
        } else {
            throw new RuntimeException("unsupported operand type(s) for *: 'int' and '" + other.getTypeName() + "'");
        }
    }
    
    private PyObject trueDivide(PyObject other) {
        if (other instanceof PyInt) {
            long otherValue = ((PyInt) other).getValue();
            if (otherValue == 0) {
                throw new RuntimeException("division by zero");
            }
            return new PyFloat((double) this.value / otherValue);
        } else if (other instanceof PyFloat) {
            double otherValue = ((PyFloat) other).getValue();
            if (otherValue == 0.0) {
                throw new RuntimeException("float division by zero");
            }
            return new PyFloat(this.value / otherValue);
        } else {
            throw new RuntimeException("unsupported operand type(s) for /: 'int' and '" + other.getTypeName() + "'");
        }
    }
    
    private PyObject floorDivide(PyObject other) {
        if (other instanceof PyInt) {
            long otherValue = ((PyInt) other).getValue();
            if (otherValue == 0) {
                throw new RuntimeException("integer division or modulo by zero");
            }
            return new PyInt(Math.floorDiv(this.value, otherValue));
        } else if (other instanceof PyFloat) {
            double otherValue = ((PyFloat) other).getValue();
            if (otherValue == 0.0) {
                throw new RuntimeException("float floor division by zero");
            }
            return new PyFloat(Math.floor(this.value / otherValue));
        } else {
            throw new RuntimeException("unsupported operand type(s) for //: 'int' and '" + other.getTypeName() + "'");
        }
    }
    
    private PyObject modulo(PyObject other) {
        if (other instanceof PyInt) {
            long otherValue = ((PyInt) other).getValue();
            if (otherValue == 0) {
                throw new RuntimeException("integer division or modulo by zero");
            }
            return new PyInt(this.value % otherValue);
        } else if (other instanceof PyFloat) {
            double otherValue = ((PyFloat) other).getValue();
            if (otherValue == 0.0) {
                throw new RuntimeException("float modulo");
            }
            return new PyFloat(this.value % otherValue);
        } else {
            throw new RuntimeException("unsupported operand type(s) for %: 'int' and '" + other.getTypeName() + "'");
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
            throw new RuntimeException("unsupported operand type(s) for ** or pow(): 'int' and '" + other.getTypeName() + "'");
        }
    }
    
    private PyObject negate() {
        return new PyInt(-this.value);
    }
    
    private PyObject absolute() {
        return new PyInt(Math.abs(this.value));
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
            throw new RuntimeException("'<' not supported between instances of 'int' and '" + other.getTypeName() + "'");
        }
    }
    
    private PyObject lessEqual(PyObject other) {
        if (other instanceof PyInt) {
            return PyBool.valueOf(this.value <= ((PyInt) other).getValue());
        } else if (other instanceof PyFloat) {
            return PyBool.valueOf(this.value <= ((PyFloat) other).getValue());
        } else {
            throw new RuntimeException("'<=' not supported between instances of 'int' and '" + other.getTypeName() + "'");
        }
    }
    
    private PyObject greaterThan(PyObject other) {
        if (other instanceof PyInt) {
            return PyBool.valueOf(this.value > ((PyInt) other).getValue());
        } else if (other instanceof PyFloat) {
            return PyBool.valueOf(this.value > ((PyFloat) other).getValue());
        } else {
            throw new RuntimeException("'>' not supported between instances of 'int' and '" + other.getTypeName() + "'");
        }
    }
    
    private PyObject greaterEqual(PyObject other) {
        if (other instanceof PyInt) {
            return PyBool.valueOf(this.value >= ((PyInt) other).getValue());
        } else if (other instanceof PyFloat) {
            return PyBool.valueOf(this.value >= ((PyFloat) other).getValue());
        } else {
            throw new RuntimeException("'>=' not supported between instances of 'int' and '" + other.getTypeName() + "'");
        }
    }
    
    // 位运算方法实现
    private PyObject leftShift(PyObject other) {
        if (other instanceof PyInt) {
            long shiftCount = ((PyInt) other).getValue();
            if (shiftCount < 0) {
                throw new RuntimeException("negative shift count");
            }
            if (shiftCount >= 64) {
                throw new RuntimeException("shift count too large");
            }
            return new PyInt(this.value << shiftCount);
        } else {
            throw new RuntimeException("unsupported operand type(s) for <<: 'int' and '" + other.getTypeName() + "'");
        }
    }
    
    private PyObject rightShift(PyObject other) {
        if (other instanceof PyInt) {
            long shiftCount = ((PyInt) other).getValue();
            if (shiftCount < 0) {
                throw new RuntimeException("negative shift count");
            }
            if (shiftCount >= 64) {
                return new PyInt(this.value < 0 ? -1 : 0);
            }
            return new PyInt(this.value >> shiftCount);
        } else {
            throw new RuntimeException("unsupported operand type(s) for >>: 'int' and '" + other.getTypeName() + "'");
        }
    }
    
    private PyObject bitwiseAnd(PyObject other) {
        if (other instanceof PyInt) {
            return new PyInt(this.value & ((PyInt) other).getValue());
        } else {
            throw new RuntimeException("unsupported operand type(s) for &: 'int' and '" + other.getTypeName() + "'");
        }
    }
    
    private PyObject bitwiseOr(PyObject other) {
        if (other instanceof PyInt) {
            return new PyInt(this.value | ((PyInt) other).getValue());
        } else {
            throw new RuntimeException("unsupported operand type(s) for |: 'int' and '" + other.getTypeName() + "'");
        }
    }
    
    private PyObject bitwiseXor(PyObject other) {
        if (other instanceof PyInt) {
            return new PyInt(this.value ^ ((PyInt) other).getValue());
        } else {
            throw new RuntimeException("unsupported operand type(s) for ^: 'int' and '" + other.getTypeName() + "'");
        }
    }
    
    private PyObject bitwiseInvert() {
        return new PyInt(~this.value);
    }
    
    // 整数特有方法实现
    private PyObject bitLength() {
        if (value == 0) {
            return new PyInt(0);
        }
        long absValue = Math.abs(value);
        return new PyInt(Long.SIZE - Long.numberOfLeadingZeros(absValue));
    }
    
    private PyObject bitCount() {
        if (value < 0) {
            throw new RuntimeException("bit_count() is only supported for non-negative integers");
        }
        return new PyInt(Long.bitCount(value));
    }
    
    private PyObject toBytes(java.util.List<PyObject> args) {
        if (!(args.get(0) instanceof PyInt)) {
            throw new RuntimeException("length must be an int");
        }
        if (!(args.get(1) instanceof PyString)) {
            throw new RuntimeException("byteorder must be str");
        }
        
        int length = (int) ((PyInt) args.get(0)).getValue();
        String byteorder = ((PyString) args.get(1)).getValue();
        
        if (length < 0) {
            throw new RuntimeException("length must be non-negative");
        }
        
        if (!byteorder.equals("big") && !byteorder.equals("little")) {
            throw new RuntimeException("byteorder must be 'big' or 'little'");
        }
        
        if (value < 0) {
            throw new RuntimeException("can't convert negative int to bytes");
        }
        
        // Check if value fits in specified length
        long maxValue = (1L << (length * 8)) - 1;
        if (value > maxValue) {
            throw new RuntimeException("int too big to convert to " + length + " bytes");
        }
        
        byte[] bytes = new byte[length];
        long temp = value;
        
        if (byteorder.equals("big")) {
            for (int i = length - 1; i >= 0; i--) {
                bytes[i] = (byte) (temp & 0xFF);
                temp >>= 8;
            }
        } else { // little endian
            for (int i = 0; i < length; i++) {
                bytes[i] = (byte) (temp & 0xFF);
                temp >>= 8;
            }
        }
        
        return new PyBytes(bytes);
    }
    
    private PyObject conjugate() {
        return new PyInt(this.value); // For integers, conjugate is itself
    }
    
    // 属性方法实现
    private PyObject getReal() {
        return new PyInt(this.value);
    }
    
    private PyObject getImag() {
        return new PyInt(0);
    }
    
    private PyObject getNumerator() {
        return new PyInt(this.value);
    }
    
    private PyObject getDenominator() {
        return new PyInt(1);
    }
    
    @Override
    public boolean equals(PyObject other) {
        if (other instanceof PyInt) {
            return ((PyInt) other).getValue() == value;
        }
        if (other instanceof PyFloat) {
            return ((PyFloat) other).getValue() == value;
        }        return false;
    }
    
    // from_bytes 静态方法实现
    private PyObject fromBytes(java.util.List<PyObject> args) {
        if (args.size() < 2) {
            throw new RuntimeException("from_bytes() missing required argument: 'byteorder'");
        }
        
        // 简化实现：假设第一个参数是bytes，第二个是byteorder
        PyObject bytesObj = args.get(0);
        String byteorder = args.get(1).toString();
        
        if (bytesObj instanceof PyBytes) {
            java.util.List<PyObject> bytesList = ((PyBytes) bytesObj).getItems();
            long result = 0;
              if ("big".equals(byteorder)) {
                for (PyObject byteObj : bytesList) {
                    if (byteObj instanceof PyInt) {
                        result = (result << 8) | (((PyInt) byteObj).getValue() & 0xFF);
                    }
                }
            } else if ("little".equals(byteorder)) {
                for (int i = bytesList.size() - 1; i >= 0; i--) {
                    PyObject byteObj = bytesList.get(i);
                    if (byteObj instanceof PyInt) {
                        result = (result << 8) | (((PyInt) byteObj).getValue() & 0xFF);
                    }
                }
            } else {
                throw new RuntimeException("byteorder must be either 'little' or 'big'");
            }
            
            return new PyInt(result);
        } else {
            throw new RuntimeException("from_bytes() argument 1 must be bytes-like object");
        }
    }

    // Java Object.equals() and hashCode() for proper HashMap functionality
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PyInt pyInt = (PyInt) obj;
        return value == pyInt.value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }
}

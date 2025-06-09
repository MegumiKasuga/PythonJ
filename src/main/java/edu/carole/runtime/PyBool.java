package edu.carole.runtime;

import edu.carole.runtime.base.PyObjectWithMethods;
import edu.carole.runtime.registry.MethodBuilder;

/**
 * Python布尔类型
 */
public class PyBool extends PyObjectWithMethods {
    public static final PyBool TRUE = new PyBool(true);
    public static final PyBool FALSE = new PyBool(false);
    
    private final boolean value;

    private PyBool(boolean value) {
        super();
        this.value = value;
    }

    public static PyBool fromValue(boolean value) {
        return value ? TRUE : FALSE;
    }

    @Override
    protected void registerMethods() {
        // Logical operations
        methodRegistry.registerMethod("__and__", MethodBuilder.oneArg(this::and));
        methodRegistry.registerMethod("__or__", MethodBuilder.oneArg(this::or));
        methodRegistry.registerMethod("__xor__", MethodBuilder.oneArg(this::xor));
        
        // Comparison operations
        methodRegistry.registerMethod("__eq__", MethodBuilder.oneArg(this::eq));
        methodRegistry.registerMethod("__ne__", MethodBuilder.oneArg(this::ne));
        
        // Type conversions
        methodRegistry.registerMethod("__bool__", MethodBuilder.noArgs(() -> this));
        methodRegistry.registerMethod("__int__", MethodBuilder.noArgs(() -> new PyInt(this.value ? 1 : 0)));
        methodRegistry.registerMethod("__str__", MethodBuilder.noArgs(() -> new PyString(this.toString())));
        methodRegistry.registerMethod("__repr__", MethodBuilder.noArgs(() -> new PyString(this.toString())));
        methodRegistry.registerMethod("__hash__", MethodBuilder.noArgs(() -> new PyInt(this.hashCode())));
    }

    public PyBool reverse() {
        return value ? FALSE : TRUE;
    }
    
    // 逻辑运算方法实现
    private PyObject and(PyObject other) {
        if (other instanceof PyBool) {
            return PyBool.valueOf(this.value && ((PyBool) other).value);
        } else if (other instanceof PyInt) {
            return new PyInt(this.value ? ((PyInt) other).getValue() : 0);
        }
        throw new RuntimeException("unsupported operand type(s) for &: 'bool' and '" + other.getTypeName() + "'");
    }
    
    private PyObject or(PyObject other) {        if (other instanceof PyBool) {
            return PyBool.valueOf(this.value || ((PyBool) other).value);
        } else if (other instanceof PyInt) {
            return new PyInt(this.value ? 1 | ((PyInt) other).getValue() : ((PyInt) other).getValue());
        }
        throw new RuntimeException("unsupported operand type(s) for |: 'bool' and '" + other.getTypeName() + "'");
    }
    
    private PyObject xor(PyObject other) {        if (other instanceof PyBool) {
            return PyBool.valueOf(this.value ^ ((PyBool) other).value);
        } else if (other instanceof PyInt) {
            return new PyInt((this.value ? 1 : 0) ^ ((PyInt) other).getValue());
        }
        throw new RuntimeException("unsupported operand type(s) for ^: 'bool' and '" + other.getTypeName() + "'");
    }
    
    // 比较运算方法实现
    private PyObject eq(PyObject other) {
        if (other instanceof PyBool) {
            return PyBool.valueOf(this.value == ((PyBool) other).value);
        }
        return PyBool.FALSE;
    }
    
    private PyObject ne(PyObject other) {
        PyObject eqResult = eq(other);
        return PyBool.valueOf(!((PyBool) eqResult).getValue());
    }
    
    public static PyBool valueOf(boolean value) {
        return value ? TRUE : FALSE;
    }
    
    public boolean getValue() { return value; }
    
    @Override
    public String getTypeName() { return "bool"; }
    
    @Override
    public String toString() { return value ? "True" : "False"; }
    
    @Override
    public boolean isTruthy() { return value; }
    
    @Override
    public boolean equals(PyObject other) {
        return other instanceof PyBool && ((PyBool) other).value == value;
    }
    
    @Override
    public PyObject getAttribute(String name) {
        /*
          switch (name) {
            case "__and__":
                return new PyBuiltinFunction("__and__", args -> {                    if (args.size() != 1) {
                        throw new RuntimeException("__and__() takes exactly 1 argument (" + args.size() + " given)");
                    }
                    PyObject other = args.get(0);                    if (other instanceof PyBool) {
                        return PyBool.valueOf(this.value && ((PyBool) other).value);                    } else if (other instanceof PyIntNew) {
                        return new PyIntNew(this.value ? ((PyIntNew) other).getValue() : 0);
                    }
                    throw new RuntimeException("unsupported operand type(s) for &: 'bool' and '" + other.getTypeName() + "'");
                });
                
            case "__or__":
                return new PyBuiltinFunction("__or__", args -> {
                    if (args.size() != 1) {
                        throw new RuntimeException("__or__() takes exactly 1 argument (" + args.size() + " given)");                    }
                    PyObject other = args.get(0);                    if (other instanceof PyBool) {
                        return PyBool.valueOf(this.value || ((PyBool) other).value);
                    } else if (other instanceof PyIntNew) {
                        return new PyIntNew(this.value ? 1 | ((PyIntNew) other).getValue() : ((PyIntNew) other).getValue());
                    } else if (other instanceof PyIntNew) {
                        return new PyIntNew(this.value ? 1 | ((PyIntNew) other).getValue() : ((PyIntNew) other).getValue());
                    }
                    throw new RuntimeException("unsupported operand type(s) for |: 'bool' and '" + other.getTypeName() + "'");
                });
                
            case "__xor__":
                return new PyBuiltinFunction("__xor__", args -> {
                    if (args.size() != 1) {
                        throw new RuntimeException("__xor__() takes exactly 1 argument (" + args.size() + " given)");
                    }
                    PyObject other = args.get(0);                    if (other instanceof PyBool) {
                        return PyBool.valueOf(this.value ^ ((PyBool) other).value);                    } else if (other instanceof PyIntNew) {
                        return new PyIntNew((this.value ? 1 : 0) ^ ((PyIntNew) other).getValue());
                    }
                    throw new RuntimeException("unsupported operand type(s) for ^: 'bool' and '" + other.getTypeName() + "'");
                });
                
            case "__eq__":
                return new PyBuiltinFunction("__eq__", args -> {
                    if (args.size() != 1) {
                        throw new RuntimeException("__eq__() takes exactly 1 argument (" + args.size() + " given)");
                    }
                    PyObject other = args.get(0);
                    if (other instanceof PyBool) {
                        return PyBool.valueOf(this.value == ((PyBool) other).value);
                    }
                    return PyBool.FALSE;
                });
                
            case "__ne__":
                return new PyBuiltinFunction("__ne__", args -> {
                    if (args.size() != 1) {
                        throw new RuntimeException("__ne__() takes exactly 1 argument (" + args.size() + " given)");
                    }
                    PyObject eqResult = this.getAttribute("__eq__").call(args);
                    return PyBool.valueOf(!((PyBool) eqResult).getValue());
                });
                
            case "__bool__":
                return new PyBuiltinFunction("__bool__", args -> {
                    if (args.size() != 0) {
                        throw new RuntimeException("__bool__() takes no arguments (" + args.size() + " given)");
                    }
                    return this;
                });
                
            case "__int__":
                return new PyBuiltinFunction("__int__", args -> {
                    if (args.size() != 0) {
                        throw new RuntimeException("__int__() takes no arguments (" + args.size() + " given)");
                    }
                    return new PyIntNew(this.value ? 1 : 0);
                });
                  case "__str__":
                return new PyBuiltinFunction("__str__", args -> {
                    if (args.size() != 0) {
                        throw new RuntimeException("__str__() takes no arguments (" + args.size() + " given)");
                    }
                    return new PyStringNew(this.toString());
                });
                  case "__repr__":
                return new PyBuiltinFunction("__repr__", args -> {
                    if (args.size() != 0) {
                        throw new RuntimeException("__repr__() takes no arguments (" + args.size() + " given)");
                    }
                    return new PyStringNew(this.toString());
                });
                  case "__hash__":
                return new PyBuiltinFunction("__hash__", args -> {
                    if (args.size() != 0) {
                        throw new RuntimeException("__hash__() takes no arguments (" + args.size() + " given)");
                    }
                    return new PyIntNew(this.hashCode());
                });
                
            default:
                return super.getAttribute(name);
        }

         */
        return super.getAttribute(name);
    }
    
    // Java Object.equals() and hashCode() for proper HashMap functionality
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PyBool pyBool = (PyBool) obj;
        return value == pyBool.value;
    }
    
    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }
}

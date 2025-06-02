package edu.carole.runtime;

/**
 * Python异常对象
 */
public class PyException extends PyObject {
    private final String message;
    private final String exceptionType;
    
    public PyException(String exceptionType, String message) {
        this.exceptionType = exceptionType;
        this.message = message;
    }
    
    @Override
    public String getTypeName() { 
        return exceptionType; 
    }
    
    @Override
    public String toString() { 
        return exceptionType + ": " + message; 
    }
    
    @Override
    public boolean isTruthy() { 
        return true; 
    }
    
    public String getMessage() { 
        return message; 
    }
    
    public String getExceptionType() { 
        return exceptionType; 
    }
    
    // 静态方法创建常见异常类型
    public static PyException nameError(String message) {
        return new PyException("NameError", message);
    }
    
    public static PyException typeError(String message) {
        return new PyException("TypeError", message);
    }
    
    public static PyException valueError(String message) {
        return new PyException("ValueError", message);
    }
    
    public static PyException attributeError(String message) {
        return new PyException("AttributeError", message);
    }
    
    public static PyException indexError(String message) {
        return new PyException("IndexError", message);
    }
    
    public static PyException keyError(String message) {
        return new PyException("KeyError", message);
    }
    
    public static PyException zeroDivisionError(String message) {
        return new PyException("ZeroDivisionError", message);
    }
    
    public static PyException runtimeError(String message) {
        return new PyException("RuntimeError", message);
    }
}

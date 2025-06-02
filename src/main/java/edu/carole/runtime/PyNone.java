package edu.carole.runtime;

/**
 * Python None类型
 */
public class PyNone extends PyObject {
    public static final PyNone INSTANCE = new PyNone();
    
    private PyNone() {}
    
    @Override
    public String getTypeName() { return "NoneType"; }
    
    @Override
    public String toString() { return "None"; }
    
    @Override
    public boolean isTruthy() { return false; }
    
    @Override
    public boolean equals(PyObject other) { return other instanceof PyNone; }
}

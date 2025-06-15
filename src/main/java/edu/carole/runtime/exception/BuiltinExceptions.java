package edu.carole.runtime.exception;

import edu.carole.runtime.PyClass;

import java.util.HashMap;

public class BuiltinExceptions {
    private static HashMap<String, PyClass> exceptions = new HashMap<>();

    private static final BuiltinExceptions INSTANCE = new BuiltinExceptions();

    private BuiltinExceptions() {
        exceptions.put("BaseException", BaseException.getInstance());
    }
    public static BuiltinExceptions getInstance() {
        return INSTANCE;
    }
}

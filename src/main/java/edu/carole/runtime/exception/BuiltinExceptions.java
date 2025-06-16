package edu.carole.runtime.exception;

import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.PyClass;
import edu.carole.runtime.PyInstance;
import edu.carole.runtime.PyObject;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class BuiltinExceptions {

    private final HashMap<String, PyClass> exceptions;

    private final Interpreter interpreter;

    private final BaseException base;
    private BaseException systemExit;
    private BaseException keyboardInterrupt;
    private BaseException generatorExit;
    private BaseException stopIteration;
    private BaseException systemError;

    public BuiltinExceptions(Interpreter interpreter) {
        this.exceptions = new HashMap<>();
        this.interpreter = interpreter;
        base = new BaseException("BaseException");
        exceptions.put("BaseException", base);
        initBuiltinExceptions();
    }

    private void initBuiltinExceptions() {
        generatorExit = registerException("GeneratorExit");
        keyboardInterrupt = registerException("KeyboardInterrupt");
        systemExit = registerException("SystemExit");
        BaseException exception = registerException("Exception");
        {
            BaseException arithmeticError = registerException("ArithmeticError", exception);
            {
                registerException("FloatingPointError", arithmeticError);
                registerException("OverflowError", arithmeticError);
                registerException("ZeroDivisionError", arithmeticError);
            }
            registerException("AssertionError", exception);
            registerException("AttributeError", exception);
            registerException("BufferError", exception);
            registerException("EOFError", exception);
            BaseException importError = registerException("ImportError", exception);
            {
                registerException("ModuleNotFoundError", importError);
            }
            BaseException lookupError = registerException("LookupError", exception);
            {
                registerException("IndexError", lookupError);
                registerException("KeyError", lookupError);
            }
            registerException("MemoryError", exception);
            BaseException nameError = registerException("NameError", exception);
            {
                registerException("UnboundLocalError", nameError);
            }
            BaseException osError = registerException("OSError", exception);
            {
                registerException("BlockingIOError", osError);
                registerException("ChildProcessError", osError);
                BaseException connectionError = registerException("ConnectionError", osError);
                {
                    registerException("BrokenPipeError", connectionError);
                    registerException("ConnectionAbortedError", connectionError);
                    registerException("ConnectionRefusedError", connectionError);
                    registerException("ConnectionResetError", connectionError);
                }
                registerException("FileExistsError", osError);
                registerException("FileNotFoundError", osError);
                registerException("InterruptedError", osError);
                registerException("IsADirectoryError", osError);
                registerException("NotADirectoryError", osError);
                registerException("PermissionError", osError);
                registerException("ProcessLookupError", osError);
                registerException("TimeoutError", osError);
            }
            registerException("ReferenceError", exception);
            BaseException runtimeError = registerException("RuntimeError", exception);
            {
                registerException("NotImplementedError", runtimeError);
                registerException("PythonFinalizationError", runtimeError);
                registerException("RecursionError", runtimeError);
            }
            registerException("StopAsyncIteration", exception);
            stopIteration = registerException("StopIteration", exception);
            BaseException syntaxError = registerException("SyntaxError", exception);
            {
                BaseException indentationError = registerException("IndentationError", syntaxError);
                {
                    registerException("TabError", indentationError);
                }
            }
            systemError = registerException("SystemError", exception);
            registerException("TypeError", exception);
            BaseException valueError = registerException("ValueError", exception);
            {
                BaseException unicodeError = registerException("UnicodeError", valueError);
                {
                    registerException("UnicodeDecodeError", unicodeError);
                    registerException("UnicodeEncodeError", unicodeError);
                    registerException("UnicodeTranslateError", unicodeError);
                }
            }
            BaseException warning = registerException("Warning", exception);
            {
                registerException("BytesWarning", warning);
                registerException("DeprecationWarning", warning);
                registerException("EncodingWarning", warning);
                registerException("FutureWarning", warning);
                registerException("ImportWarning", warning);
                registerException("PendingDeprecationWarning", warning);
                registerException("ResourceWarning", warning);
                registerException("RuntimeWarning", warning);
                registerException("SyntaxWarning", warning);
                registerException("UnicodeWarning", warning);
                registerException("UserWarning", warning);
            }
        }
    }

    public PyClass get(String name) {
        return exceptions.getOrDefault(name, null);
    }

//    public ExceptionWrapper packJavaException(Exception e) {
//        if (e instanceof ExceptionWrapper wrapper) return wrapper;
//        e.getStackTrace();
//    }

    public BaseException registerException(String name) {
        return registerException(name, null, null, (String[]) null);
    }

    public BaseException registerException(String name, String... baseClasses) {
        return registerException(name, null, null, baseClasses);
    }

    public BaseException registerException(String name, PyClass... baseClasses) {
        return registerException(name, null, null, baseClasses);
    }

    public BaseException registerException(String name, Consumer<Map<String, PyObject>> methodCustomizer) {
        return registerException(name, methodCustomizer, null, (String[]) null);
    }

    public BaseException registerException(String name, Consumer<Map<String, PyObject>> methodCustomizer, String... baseClasses) {
        return registerException(name, methodCustomizer, null, baseClasses);
    }

    public BaseException registerException(String name, Consumer<Map<String, PyObject>> methodCustomizer,
                                           Consumer<PyInstance> attributeCustomizer) {
        return registerException(name, methodCustomizer, attributeCustomizer, (String[]) null);
    }

    public BaseException registerException(String name, Consumer<Map<String, PyObject>> methodCustomizer,
                                           Consumer<PyInstance> attributeCustomizer, String[] baseClasses) {
        PyClass[] bases;
        if (baseClasses != null) {
            Map<String, PyClass> classes = getClasses(baseClasses);
            bases = new PyClass[classes.size()];
            int i = 0;
            for (PyClass obj : classes.values()) {
                bases[i] = obj;
                i++;
            }
        } else {
            bases = new PyClass[]{base};
        }
        return registerException(name, methodCustomizer, attributeCustomizer, bases);
    }

    public BaseException registerException(String name, Consumer<Map<String, PyObject>> methodCustomizer,
                                           Consumer<PyInstance> attributeCustomizer, PyClass[] baseClass) {
        BaseException exc = new BaseException(name, methodCustomizer, attributeCustomizer, baseClass);
        exceptions.put(name, exc);
        return exc;
    }

    public Map<String, PyClass> getClasses(String... classes) {
        Map<String, PyClass> result = new HashMap<>();
        for (String clazz : classes) {
            if (exceptions.containsKey(clazz)) {
                result.put(clazz, exceptions.get(clazz));
            }
        }
        return result;
    }



    public boolean isException(PyObject obj) {
        if (!(obj instanceof PyInstance instance)) return false;
        return instance.getPyClass().getMRO().contains(base);
    }
}

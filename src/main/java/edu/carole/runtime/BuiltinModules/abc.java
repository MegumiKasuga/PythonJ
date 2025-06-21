package edu.carole.runtime.BuiltinModules;

import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.*;
import edu.carole.runtime.clazz.PyClass;
import edu.carole.runtime.func.PyBuiltinFunction;
import edu.carole.runtime.func.PyFunction;
import edu.carole.runtime.property.PyProperty;

import java.util.HashMap;
import java.util.Map;

public class abc {

    public static PyModule createModule(Interpreter inter) {
        PyModule module = new PyModule("abc", "Abstract Base Classes (ABCs) for Python");

        // Add ABC class
        module.setAttribute(inter, "ABC", new ABC("ABC", new HashMap<>()));

        // Add abstractmethod decorator
        module.setAttribute(inter, "abstractmethod", new PyBuiltinFunction("abstractmethod",
                (args, kwargs, interpreter) -> {
                    if (args.size() != 1) {
                        throw new RuntimeException("@abstractmethod takes exactly 1 argument");
                    }
                    PyObject target = args.get(0);
                    if (!(target instanceof PyFunction func)) {
                        throw new RuntimeException("@abstractmethod decorator can only be applied to functions");
                    }
                    func.setAbstractMethod(true);
                    if (func.isStaticMethod()) {
                        throw new RuntimeException("@abstractmethod cannot be applied to static methods");
                    }
                    return func;
        }));

        // Add abstractproperty decorator
        module.setAttribute(inter, "abstractproperty", new PyBuiltinFunction("abstractproperty",
                (args, kwargs, interpreter) -> {
                    if (args.size() != 1) {
                        throw new RuntimeException("@abstractproperty takes exactly 1 argument");
                    }
                    PyObject target = args.get(0);
                    if (!(target instanceof PyFunction func)) {
                        throw new RuntimeException("@abstractproperty decorator can only be applied to functions");
                    }
                    func.setAbstractMethod(true);
                    if (func.isStaticMethod()) {
                        throw new RuntimeException("@abstractproperty cannot be applied to static methods");
                    }
                    func.setAttribute(interpreter, "__isproperty__", PyBool.TRUE);
                    return new PyProperty(func);
        }));

        return module;
    }

    public static void registerModule(ModuleLoader loader) {
        // modules.put("abc", createModule());
        loader.getLoadedModules().put("abc", createModule(loader.getInterpreter()));
    }

    public static class ABC extends PyClass {

        public ABC(String name, Map<String, PyObject> methods) {
            super(name, methods);
        }
        // Implementation of the ABC class
        // This class will define
        // the basic structure for abstract base classes in Python
    }
}

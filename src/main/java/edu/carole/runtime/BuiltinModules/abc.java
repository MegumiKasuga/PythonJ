package edu.carole.runtime.BuiltinModules;

import edu.carole.runtime.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class abc {

    public static PyModule createModule() {
        PyModule module = new PyModule("abc", "Abstract Base Classes (ABCs) for Python");

        // Add ABC class
        module.setAttribute("ABC", new ABC("ABC", new HashMap<>()));

        // Add abstractmethod decorator
        module.setAttribute("abstractmethod", new PyBuiltinFunction("abstractmethod",
                args -> {
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
        module.setAttribute("abstractproperty", new PyBuiltinFunction("abstractproperty",
                args -> {
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
                    return func;
        }));

        return module;
    }

    public static void registerModule(ModuleLoader loader) {
        // modules.put("abc", createModule());
        loader.getLoadedModules().put("abc", createModule());
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

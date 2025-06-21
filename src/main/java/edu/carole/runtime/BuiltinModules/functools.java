package edu.carole.runtime.BuiltinModules;

import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.*;
import edu.carole.runtime.func.PyBuiltinFunction;
import edu.carole.runtime.func.PyFunction;

/**
 * Implementation of the Python functools module
 * Provides higher-order functions and operations on callable objects.
 */
public class functools {

    /**
     * Create and initialize the functools module
     */
    public static PyModule createModule(Interpreter inter) {
        PyModule module = new PyModule("functools", "Higher-order functions and operations on callable objects");
        
        // Add wraps function
        module.setAttribute(inter, "wraps", new PyBuiltinFunction("wraps", (args, kwargs, interpreter) -> {
            if (args.size() != 1) {
                throw new RuntimeException("wraps() takes exactly 1 argument (" + args.size() + " given)");
            }

            if (!(args.get(0) instanceof PyFunction)) {
                throw new RuntimeException("wraps() requires a function as its argument");
            }

            PyFunction wrapped = (PyFunction) args.get(0);

            return new PyBuiltinFunction("wraps_decorator", (decoratorArgs, kwargs2, inter2) -> {
                if (decoratorArgs.size() != 1) {
                    throw new RuntimeException("decorator returned by wraps() takes exactly 1 argument");
                }

                if (!(decoratorArgs.get(0) instanceof PyFunction)) {
                    throw new RuntimeException("Function wrapper must be a function");
                }

                PyFunction wrapper = (PyFunction) decoratorArgs.get(0);

                // Copy metadata from wrapped to wrapper
                wrapper.setAttribute(inter2, "__name__", new PyString(wrapped.getName()));
                wrapper.setAttribute(inter2, "__doc__", wrapped.getAttribute(inter2, "__doc__"));
                wrapper.setAttribute(inter2, "__module__", wrapped.getAttribute(inter2 , "__module__"));
                wrapper.setAttribute(inter2, "__wrapped__", wrapped);

                return wrapper;
            });
        }));
        
        return module;
    }
    
    /**
     * Register the module in the global environment
     */
    public static void registerModule(ModuleLoader moduleLoader) {
        moduleLoader.getLoadedModules().put("functools", createModule(moduleLoader.getInterpreter()));
        // globals.put("functools", createModule());
    }
}

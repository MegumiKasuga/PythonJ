package edu.carole.runtime.BuiltinModules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.carole.ast.ASTNode;
import edu.carole.interpreter.Environment;
import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.*;

/**
 * Implementation of the Python functools module
 * Provides higher-order functions and operations on callable objects.
 */
public class functools {

    /**
     * Create and initialize the functools module
     */
    public static PyModule createModule() {
        PyModule module = new PyModule("functools", "Higher-order functions and operations on callable objects");
        
        // Add wraps function
        module.setAttribute("wraps", new PyBuiltinFunction("wraps", (args, kwargs, inter) -> {
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
                wrapper.setAttribute("__name__", new PyString(wrapped.getName()));
                wrapper.setAttribute("__doc__", wrapped.getAttribute("__doc__"));
                wrapper.setAttribute("__module__", wrapped.getAttribute("__module__"));
                wrapper.setAttribute("__wrapped__", wrapped);
                
                return wrapper;
            });
        }));
        
        return module;
    }
    
    /**
     * Register the module in the global environment
     */
    public static void registerModule(ModuleLoader moduleLoader) {
        moduleLoader.getLoadedModules().put("functools", createModule());
        // globals.put("functools", createModule());
    }
}

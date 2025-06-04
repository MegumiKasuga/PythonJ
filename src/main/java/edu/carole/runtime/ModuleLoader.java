package edu.carole.runtime;

import edu.carole.interpreter.Interpreter;
import edu.carole.interpreter.Environment;
import edu.carole.interpreter.BuiltinFunctions;
import edu.carole.lexer.Lexer;
import edu.carole.parser.Parser;
import edu.carole.ast.statements.Program;
import edu.carole.runtime.io.IOManager;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Handles module loading and management for Python imports
 */
public class ModuleLoader {
    private final Map<String, PyModule> loadedModules;
    private final List<String> modulePaths;
    private final Map<String, PyModule> builtinModules;
    private final IOManager io;
    
    public ModuleLoader(IOManager io) {
        this.io = io;
        this.loadedModules = new HashMap<>();
        this.modulePaths = new ArrayList<>();
        this.builtinModules = new HashMap<>();
        
        // Add current directory and common Python paths
        modulePaths.add(System.getProperty("user.dir"));
        
        // Initialize builtin modules
        initializeBuiltinModules();
    }
    
    /**
     * Import a module by name
     * 
     * @param moduleName The name of the module to import
     * @return The imported module
     * @throws RuntimeException if module cannot be found or loaded
     */
    public PyModule importModule(String moduleName) {
        // Check if already loaded
        if (loadedModules.containsKey(moduleName)) {
            return loadedModules.get(moduleName);
        }
        
        // Check builtin modules first
        if (builtinModules.containsKey(moduleName)) {
            PyModule module = builtinModules.get(moduleName);
            loadedModules.put(moduleName, module);
            return module;
        }
        
        // Try to load from file system
        PyModule module = loadModuleFromFile(moduleName);
        if (module != null) {
            loadedModules.put(moduleName, module);
            return module;
        }
        
        throw new RuntimeException("No module named '" + moduleName + "'");
    }
    
    /**
     * Import all public attributes from a module into an environment
     * 
     * @param module The module to import from
     * @param environment The environment to import into
     */
    public void importAllFromModule(PyModule module, Environment environment) {
        // Import all attributes that don't start with underscore
        for (Map.Entry<String, PyObject> entry : module.getAttributes().entrySet()) {
            String name = entry.getKey();
            if (!name.startsWith("_")) {
                environment.define(name, entry.getValue());
            }
        }
    }
    
    /**
     * Import a specific item from a module
     * 
     * @param module The module to import from
     * @param itemName The name of the item to import
     * @return The imported item, or null if not found
     */
    public PyObject importFromModule(PyModule module, String itemName) {
        return module.getAttribute(itemName);
    }
    
    /**
     * Add a module search path
     * 
     * @param path The path to add
     */
    public void addModulePath(String path) {
        if (!modulePaths.contains(path)) {
            modulePaths.add(path);
        }
    }
    
    /**
     * Load a module from file system
     * 
     * @param moduleName The name of the module
     * @return The loaded module, or null if not found
     */
    private PyModule loadModuleFromFile(String moduleName) {
        String fileName = moduleName.replace('.', File.separatorChar) + ".py";
        
        for (String basePath : modulePaths) {
            Path modulePath = Paths.get(basePath, fileName);
            
            if (Files.exists(modulePath)) {
                try {
                    return loadPythonFile(modulePath.toString(), moduleName);
                } catch (Exception e) {
                    System.err.println("Warning: Failed to load module " + moduleName + " from " + modulePath + ": " + e.getMessage());
                }
            }
            
            // Also check for package __init__.py
            Path packagePath = Paths.get(basePath, moduleName.replace('.', File.separatorChar), "__init__.py");
            if (Files.exists(packagePath)) {
                try {
                    return loadPythonFile(packagePath.toString(), moduleName);
                } catch (Exception e) {
                    System.err.println("Warning: Failed to load package " + moduleName + " from " + packagePath + ": " + e.getMessage());
                }
            }
        }
        
        return null;
    }

    public static String readFile(InputStream stream, Charset charset) throws IOException {
        String newLine = System.lineSeparator();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line;
        boolean flag = false;
        while ((line = reader.readLine()) != null) {
            builder.append(flag ? newLine : "").append(line);
            flag = true;
        }
        reader.close();
        return builder.toString();
    }
    
    /**
     * Load a Python file and create a module
     * 
     * @param filePath The path to the Python file
     * @param moduleName The name of the module
     * @return The created module
     * @throws Exception if loading fails
     */
    private PyModule loadPythonFile(String filePath, String moduleName) throws Exception {
        // String source = Files.readString(Paths.get(filePath));
        InputStream stream = io.createInputStream(filePath, "r");
        String source = readFile(stream, StandardCharsets.UTF_8);

        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer.tokenize());
        Program program = parser.parse();
        
        // Create module environment
        Environment moduleEnv = new Environment();
        
        // Set module attributes
        moduleEnv.define("__name__", new PyString(moduleName));
        moduleEnv.define("__file__", new PyString(filePath));
        
        // Execute module code
        Interpreter interpreter = new Interpreter();
        interpreter.setEnvironment(moduleEnv);
        interpreter.visitProgram(program);
        
        // Create module object
        PyModule module = new PyModule(moduleName, moduleEnv);
        
        return module;
    }
    
    /**
     * Initialize builtin modules
     */
    private void initializeBuiltinModules() {
        // Create functools module
        Environment functoolsEnv = createFunctoolsModule();
        PyModule functoolsModule = new PyModule("functools", functoolsEnv);
        builtinModules.put("functools", functoolsModule);
        
        // Add more builtin modules as needed
        Environment sysEnv = createSysModule();
        PyModule sysModule = new PyModule("sys", sysEnv);
        builtinModules.put("sys", sysModule);
        
        Environment osEnv = createOsModule();
        PyModule osModule = new PyModule("os", osEnv);
        builtinModules.put("os", osModule);
    }
      /**
     * Create the functools builtin module
     */
    private Environment createFunctoolsModule() {
        Environment env = new Environment();
        
        // Add functools.reduce - simplified implementation
        env.define("reduce", new PyBuiltinFunction("reduce", args -> {
            if (args.size() < 2 || args.size() > 3) {
                throw new RuntimeException("reduce expected 2 or 3 arguments, got " + args.size());
            }
            
            PyObject function = args.get(0);
            PyObject iterable = args.get(1);
            PyObject initializer = args.size() == 3 ? args.get(2) : null;
            
            // Simple reduce implementation
            PyObject result = initializer;
            boolean first = true;
            
            try {
                java.util.Iterator<PyObject> iterator = iterable.iterator();
                while (iterator.hasNext()) {
                    PyObject element = iterator.next();
                    if (first && result == null) {
                        result = element;
                        first = false;
                    } else {
                        java.util.List<PyObject> funcArgs = new java.util.ArrayList<>();
                        funcArgs.add(result);
                        funcArgs.add(element);
                        result = function.call(funcArgs);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("reduce of empty sequence with no initial value");
            }
            
            return result;
        }));
        
        // Add functools.partial (placeholder implementation)
        env.define("partial", new PyBuiltinFunction("partial", args -> {
            if (args.size() < 1) {
                throw new RuntimeException("partial expected at least 1 argument, got " + args.size());
            }
            // Return a simple wrapper function
            return args.get(0); // Simplified implementation
        }));
        
        return env;
    }
    
    /**
     * Create the sys builtin module
     */
    private Environment createSysModule() {
        Environment env = new Environment();
        
        // Add sys.version
        env.define("version", new PyString("3.8.0 (JythonKernel)"));
          // Add sys.path (as a list)
        java.util.List<PyObject> pathElements = new java.util.ArrayList<>();
        for (String path : modulePaths) {
            pathElements.add(new PyString(path));
        }
        PyList pathList = new PyList(pathElements);
        env.define("path", pathList);
        
        return env;
    }
    
    /**
     * Create the os builtin module
     */
    private Environment createOsModule() {
        Environment env = new Environment();
          // Add os.getcwd
        env.define("getcwd", new PyBuiltinFunction("getcwd", args -> {
            if (args.size() != 0) {
                throw new RuntimeException("getcwd() takes no arguments");
            }
            return new PyString(System.getProperty("user.dir"));
        }));
        
        return env;
    }
    
    /**
     * Get all loaded modules
     */
    public Map<String, PyModule> getLoadedModules() {
        return Collections.unmodifiableMap(loadedModules);
    }
    
    /**
     * Clear loaded modules (for testing/reset)
     */
    public void clearLoadedModules() {
        loadedModules.clear();
    }
}

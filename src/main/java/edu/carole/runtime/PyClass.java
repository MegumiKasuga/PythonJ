package edu.carole.runtime;

import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.property.PyProperty;

import java.util.*;

/**
 * Python类对象
 */
public class PyClass extends PyObject {
    private final String name;
    private final Map<String, PyObject> methods;
    private final List<PyClass> baseClasses; // 直接父类列表
    private List<PyClass> mro; // 方法解析顺序 (Method Resolution Order)
    private PyClasspath classpath; // Class path information
    private final Map<String, PyObject> classAttributes; // Class attributes
    private final Map<String, PyProperty> properties = new HashMap<>(); // Class properties
    
    public PyClass(String name, Map<String, PyObject> methods) {
        this.name = name;
        this.methods = new HashMap<>(methods);
        this.baseClasses = new ArrayList<>();
        computeMRO();
        // Create default classpath in __main__ module
        this.classpath = new PyClasspath(this, name);
        classAttributes = new HashMap<>();
    }
    
    public PyClass(String name, Map<String, PyObject> methods, List<PyClass> baseClasses) {
        this.name = name;
        this.methods = new HashMap<>(methods);
        this.baseClasses = new ArrayList<>(baseClasses);
        computeMRO();
        
        // Create classpath with parent classpaths
        this.classpath = new PyClasspath(this, "__main__", name);
        
        // Add parent classpaths
        for (PyClass baseClass : baseClasses) {
            this.classpath.addParentClasspath(baseClass.getClasspath());
        }
        classAttributes = new HashMap<>();
    }
    
    /**
     * Create a Python class with explicit module path
     * 
     * @param name Class name
     * @param modulePath Module path (e.g., "package.subpackage")
     * @param methods Class methods
     */
    public PyClass(String name, String modulePath, Map<String, PyObject> methods) {
        this.name = name;
        this.methods = new HashMap<>(methods);
        this.baseClasses = new ArrayList<>();
        computeMRO();
        this.classpath = new PyClasspath(this, modulePath, name);
        classAttributes = new HashMap<>();
    }
    
    /**
     * Create a Python class with explicit module path and base classes
     * 
     * @param name Class name
     * @param modulePath Module path (e.g., "package.subpackage")
     * @param methods Class methods
     * @param baseClasses Base classes
     */
    public PyClass(String name, String modulePath, Map<String, PyObject> methods, List<PyClass> baseClasses) {
        this.name = name;
        this.methods = new HashMap<>(methods);
        this.baseClasses = new ArrayList<>(baseClasses);
        computeMRO();
        
        // Create classpath with parent classpaths
        List<PyClasspath> parentPaths = new ArrayList<>();
        for (PyClass baseClass : baseClasses) {
            parentPaths.add(baseClass.getClasspath());
        }
        this.classpath = new PyClasspath(this, modulePath, name, parentPaths);
        classAttributes = new HashMap<>();
    }

    public void addClassAttribute(String name, PyObject value) {
        classAttributes.put(name, value);
    }

    public void addClassAttributes(Map<String, PyObject> attributes) {
        classAttributes.putAll(attributes);
    }

    public void addProperty(String name, PyProperty property) {
        properties.put(name, property);
    }

    public void addProperties(Map<String, PyProperty> properties) {
        this.properties.putAll(properties);
    }
    
    /**
     * 计算方法解析顺序 (MRO)
     * 使用简化的C3线性化算法
     */
    private void computeMRO() {
        mro = new ArrayList<>();
        mro.add(this); // 自己首先
        
        // 深度优先搜索添加父类
        Set<PyClass> visited = new HashSet<>();
        for (PyClass baseClass : baseClasses) {
            addToMRO(baseClass, visited);
        }
    }
    
    private void addToMRO(PyClass cls, Set<PyClass> visited) {
        if (visited.contains(cls)) {
            return; // 避免循环
        }
        visited.add(cls);
        
        // 先添加父类的父类
        for (PyClass baseClass : cls.baseClasses) {
            addToMRO(baseClass, visited);
        }
        
        // 然后添加自己(如果还没添加)
        if (!mro.contains(cls)) {
            mro.add(cls);
        }
    }

    public boolean isAbstractClass(Interpreter interpreter) {
        if (interpreter.getModuleLoader().getLoadedModules().containsKey("abc")) {
            // 如果已经加载了abc模块，直接使用其中的ABC类
            PyObject abcModule = interpreter.getModuleLoader().getLoadedModules().get("abc");
            if (abcModule instanceof PyModule pyModule) {
                PyObject abcClass = pyModule.getAttribute("ABC");
                return this == abcClass || baseClasses.contains(abcClass);
            }
        }
        return false;
    }

    public HashMap<String, PyFunction> scanMROForAbstractMethodsForImplementation(Interpreter interpreter) {
        if (isAbstractClass(interpreter)) {
            // 如果是抽象类，扫描MRO中的抽象方法
            return new HashMap<>();
        }

        HashMap<String, PyFunction> abstractMethods = new HashMap<>();
        for (int i = mro.size() - 1; i >= 0; i--) {
            PyClass cls = mro.get(i);
            for (Map.Entry<String, PyObject> entry : cls.methods.entrySet()) {
                String methodName = entry.getKey();
                PyObject method = entry.getValue();

                // 如果是抽象方法，记录下来
                if (method instanceof PyFunction pyFunc) {
                    if (pyFunc.isAbstractMethod()) {
                        if (!cls.isAbstractClass(interpreter)) {
                            throw new RuntimeException(
                                    "Cannot implement abstract method '" + methodName + "' in non-abstract class '" + cls.getName() + "'"
                            );
                        }
                        if (!abstractMethods.containsKey(methodName)) {
                            abstractMethods.putIfAbsent(methodName, pyFunc);
                        }
                    } else {
                        if (!abstractMethods.isEmpty() &&
                                abstractMethods.containsKey(methodName)) {
                            PyFunction absFunc = abstractMethods.get(methodName);
                            if (absFunc.hasSameFunctionHead(pyFunc)) {
                                abstractMethods.remove(methodName);
                            }
                        }
                    }
                }
            }
        }
        return abstractMethods;
    }
    
    @Override
    public String getTypeName() { return "type"; }
    
    @Override
    public String toString() { return "<class '" + name + "'>"; }
    
    @Override
    public boolean isTruthy() { return true; }

    @Override
    public PyObject getAttribute(String attributeName) {
        // 按MRO顺序查找方法
        for (PyClass cls : mro) {
            PyObject method = cls.methods.get(attributeName);
            if (method != null) {
                return method;
            }
        }
        throw new RuntimeException("type object '" + name + "' has no attribute '" + attributeName + "'");
    }

    public Map<String, PyObject> getAttributeEnv(String name) {
        // 按MRO顺序查找方法
        for (PyClass cls : mro) {
            if (cls.classAttributes.containsKey(name)) {
                return cls.classAttributes;
            }
        }
        return null;
    }
    
    /**
     * Finds a method in this class or its parent classes
     * Does not throw exception if not found
     */
    public PyObject findMethod(String methodName) {
        // Look in the method resolution order
        for (PyClass cls : mro) {
            PyObject method = cls.methods.get(methodName);
            if (method != null) {
                return method;
            }
        }
        return null;
    }

    public Map<String, PyObject> findMethodEnv(String methodName) {
        // Look in the method resolution order
        for (PyClass cls : mro) {
            if (cls.methods.containsKey(methodName)) {
                return cls.methods;
            }
        }
        return null;
    }
    
    @Override
    public PyObject call(List<PyObject> arguments) {
        return call(arguments, null);
    }

    @Override
    public PyObject call(List<PyObject> arguments, edu.carole.interpreter.Interpreter interpreter) {
        // 创建实例
        PyInstance instance = new PyInstance(this);
        properties.forEach(
                (name, prop) -> {
                    instance.setAttribute(name, prop.boundToInstance(instance));
                }
        );
        
        // 调用__init__方法（如果存在）
        PyObject initMethod = findMethod("__init__");
        if (initMethod != null) {
            // Create a bound method to ensure proper context setting
            PyBoundMethod boundInit = new PyBoundMethod(instance, initMethod);
            
            // Use interpreter-aware call if available
            if (interpreter != null) {
                boundInit.call(arguments, interpreter);
            } else {
                boundInit.call(arguments);
            }
        }
        
        return instance;
    }
    
    public String getName() { return name; }
    public Map<String, PyObject> getMethods() { return methods; }
    public List<PyClass> getBaseClasses() { return baseClasses; }
    public List<PyClass> getMRO() { return mro; }
    
    /**
     * Get the classpath of this class
     */
    public PyClasspath getClasspath() {
        return classpath;
    }

    public Map<String, PyObject> getClassAttributes() {
        return classAttributes;
    }

    /**
     * Set the classpath for this class
     * @param modulePath The module path (e.g., "package.subpackage")
     * @param className The class name (typically the same as this.name)
     */
    public void setClasspath(String modulePath, String className) {
        // Create new classpath with specified module path
        List<PyClasspath> parentPaths = new ArrayList<>();
        
        // Transfer parent classpaths from existing classpath if available
        if (classpath != null) {
            parentPaths.addAll(classpath.getParentClasspaths());
        } else {
            // Add parent classpaths based on base classes
            for (PyClass baseClass : baseClasses) {
                PyClasspath parentPath = baseClass.getClasspath();
                if (parentPath != null) {
                    parentPaths.add(parentPath);
                }
            }
        }
        
        classpath = new PyClasspath(this, modulePath, className, parentPaths);
    }
    
    /**
     * Check if this class is a subclass of another class by fully qualified name
     */
    public boolean isSubclassOfByPath(String fullyQualifiedName) {
        if (classpath != null) {
            return classpath.isSubclassOf(fullyQualifiedName);
        }
        return false;
    }
}

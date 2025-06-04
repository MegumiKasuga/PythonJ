package edu.carole.runtime;

import java.util.*;

/**
 * Represents the class path information for a Python class
 * Stores the module path and class hierarchy information
 */
public class PyClasspath {
    private final PyClass pyClass;
    private final String modulePath;
    private final String className;
    private final List<PyClasspath> parentClasspaths;
    
    /**
     * Create a classpath for a Python class
     *
     * @param pyClass The Python class this classpath belongs to
     * @param modulePath The module path (e.g., "package.subpackage")
     * @param className The class name
     */
    public PyClasspath(PyClass pyClass, String modulePath, String className) {
        this.pyClass = pyClass;
        this.modulePath = modulePath;
        this.className = className;
        this.parentClasspaths = new ArrayList<>();
    }
    
    /**
     * Create a classpath for a Python class with parent classpaths
     * 
     * @param pyClass The Python class this classpath belongs to
     * @param modulePath The module path (e.g., "package.subpackage")
     * @param className The class name
     * @param parentClasspaths List of parent class classpaths
     */
    public PyClasspath(PyClass pyClass, String modulePath, String className, List<PyClasspath> parentClasspaths) {
        this.pyClass = pyClass;
        this.modulePath = modulePath;
        this.className = className;
        this.parentClasspaths = new ArrayList<>(parentClasspaths);
    }
    
    /**
     * Create a classpath for a top-level class in the __main__ module
     * 
     * @param pyClass The Python class this classpath belongs to
     * @param className The class name
     */
    public PyClasspath(PyClass pyClass, String className) {
        this(pyClass, "__main__", className);
    }
      /**
     * Get the Python class associated with this classpath
     */
    public PyClass getPyClass() {
        return pyClass;
    }
    
    /**
     * Get the module path for this class (e.g., "package.subpackage")
     */
    public String getModulePath() {
        return modulePath;
    }
    
    /**
     * Get the class name
     */
    public String getClassName() {
        return className;
    }
    
    /**
     * Get the fully qualified name (module.class)
     */
    public String getFullyQualifiedName() {
        if ("__main__".equals(modulePath)) {
            return className;
        }
        return modulePath + "." + className;
    }
    
    /**
     * Get the parent classpaths
     */
    public List<PyClasspath> getParentClasspaths() {
        return Collections.unmodifiableList(parentClasspaths);
    }
    
    /**
     * Add a parent classpath
     */
    public void addParentClasspath(PyClasspath parent) {
        if (!parentClasspaths.contains(parent)) {
            parentClasspaths.add(parent);
        }
    }
    
    /**
     * Check if this class is a subclass of another class by path
     * 
     * @param modulePath The module path
     * @param className The class name
     * @return true if this class is a subclass of the specified class
     */
    public boolean isSubclassOf(String modulePath, String className) {
        // Check if this class matches
        if (this.modulePath.equals(modulePath) && this.className.equals(className)) {
            return true;
        }
        
        // Check parent classes recursively
        for (PyClasspath parent : parentClasspaths) {
            if (parent.isSubclassOf(modulePath, className)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if this class is a subclass of another class by full name
     * 
     * @param fullyQualifiedName The fully qualified name (module.class)
     * @return true if this class is a subclass of the specified class
     */
    public boolean isSubclassOf(String fullyQualifiedName) {
        // Split into module and class
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        if (lastDot == -1) {
            // No module path, assume __main__
            return isSubclassOf("__main__", fullyQualifiedName);
        }
        
        String modulePath = fullyQualifiedName.substring(0, lastDot);
        String className = fullyQualifiedName.substring(lastDot + 1);
        return isSubclassOf(modulePath, className);
    }
    
    /**
     * Get string representation
     */
    @Override
    public String toString() {
        return getFullyQualifiedName();
    }
    
    /**
     * Check if two classpaths are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        
        if (!(obj instanceof PyClasspath)) {
            return false;
        }
        
        PyClasspath other = (PyClasspath) obj;
        return modulePath.equals(other.modulePath) && className.equals(other.className);
    }
    
    /**
     * Generate hash code based on module path and class name
     */
    @Override
    public int hashCode() {
        return Objects.hash(modulePath, className);
    }
}

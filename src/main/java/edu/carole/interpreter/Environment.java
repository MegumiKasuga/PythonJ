package edu.carole.interpreter;

import edu.carole.runtime.*;
import edu.carole.runtime.clazz.PyClass;
import edu.carole.runtime.func.PyFunction;
import edu.carole.runtime.instance.PyInstance;
import edu.carole.runtime.property.PyProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 环境类，管理变量的作用域
 */
public class Environment {
    private final Environment enclosing;
    private final Map<String, PyObject> values = new HashMap<>();
    private PyClass currentClass; // 当前类上下文（用于super()）
    private PyInstance currentInstance; // 当前实例上下文（用于super()）
    private final Interpreter interpreter;
    
    public Environment(Interpreter interpreter) {
        this.interpreter = interpreter;
        this.enclosing = null;
    }
    
    public Environment(Interpreter interpreter, Environment enclosing) {
        this.interpreter = interpreter;
        this.enclosing = enclosing;
    }
    
    /**
     * 定义变量
     */
    public void define(String name, PyObject value) {
        if (name.contains(".")) {
            String[] path = name.split("\\.");
            Map<String, PyObject> attributes = getAttributeEnv(path, !path[0].equals("self"), false);
            if (attributes != null) {
                dealWithPropertySet(path[path.length - 1], value, attributes);
                return;
            }
        }
        dealWithPropertySet(name, value, values);
    }

    public void dealWithPropertySet(String name, PyObject value, Map<String, PyObject> values) {
        if (values.containsKey(name) &&
                values.get(name) instanceof PyProperty prop) {
            ArrayList<PyObject> args = new ArrayList<>();
            args.add(value);
            PyObject result = prop.call(args, interpreter);
            if (result == prop) {
                values.put(name, prop);
            }
        } else {
            values.put(name, value);
        }
    }

    /**
     * 获取变量
     */
    public PyObject get(String name, boolean allowFunc) {
        if (name.contains(".")) {
            String[] path = name.split("\\.");
            Map<String, PyObject> attributes = getAttributeEnv(path, true, allowFunc);
            if (attributes != null && attributes.containsKey(path[path.length - 1])) {
                return attributes.get(path[path.length - 1]);
            }
        } else if (values.containsKey(name)) {
            return values.get(name);
        } else if (enclosing != null) {
            return enclosing.get(name, allowFunc);
        }

        throw new Interpreter.PyExceptionWrapper(
            edu.carole.runtime.PyException.nameError("Undefined variable '" + name + "'")
        );
    }

    public PyObject set(String name, PyObject value) {
        if (name.contains(".")) {
            String[] path = name.split("\\.");
            Map<String, PyObject> attributes = getAttributeEnv(path, !path[0].equals("self"), false);
            if (attributes != null) {
                dealWithPropertySet(path[path.length - 1], value, attributes);
                return value;
            }
        } else if (values.containsKey(name)) {
            this.define(name, value);
            return value;
        } else if (enclosing != null) {
            enclosing.define(name, value);
            return value;
        }

        throw new Interpreter.PyExceptionWrapper(
            edu.carole.runtime.PyException.nameError("Undefined variable '" + name + "'")
        );
    }

    public Map<String, PyObject> getAttributeEnv(String[] path, boolean allowClass, boolean allowFunc) {
        Environment current = this;

        while (!current.values.containsKey(path[0])) {
            current = current.enclosing;
            if (current == null) {
                return null;
            }
        }

        Map<String, PyObject> currentValues = new HashMap<>(current.values);
        for (int i = 0; i < path.length; i++) {
            PyObject obj = currentValues.get(path[i]);
            if (obj == null) {
                return null; // 属性不存在
            }
            if (i == path.length - 1) {
                return currentValues;
            } else if (obj instanceof PyModule module) {
                if (module.getAttribute(interpreter, path[i + 1]) != null) {
                    currentValues = module.getAttributes();
                }
            } else if (obj instanceof PyClass cls) {
                Map<String, PyObject> cache = cls.findMethodEnv(path[i + 1]);
                if (allowFunc && cache != null) {
                    PyFunction func = ((PyFunction) cache.get(path[i + 1]));
                    if (!func.isStaticMethod()) {
                        throw new Interpreter.PyExceptionWrapper(
                                PyException.attributeError(
                                        "Cannot access non-static method '" + path[i + 1] + "' from class '" + cls.getName() + "'"
                                )
                        );
                    }
                    currentValues = Map.of(path[i + 1], func);
                } else {
                    cache = cls.getAttributeEnv(path[i + 1]);
                    if (cache != null) {
                        currentValues = cache;
                    } else {
                        return null; // 属性不存在
                    }
                }
            } else if (obj instanceof PyInstance instance) {
                if (!allowClass && i + 1 >= path.length - 1) {
                    return instance.getAttributes();
                } else {
                    if (instance.getAttributes().containsKey(path[i + 1])) {
                        currentValues = instance.getAttributes();
                    } else {
                        PyClass clazz = instance.getPyClass();
                        Map<String, PyObject> classAttributes = new HashMap<>();
                        classAttributes.putAll(clazz.getClassAttributes());
                        classAttributes.putAll(clazz.getMethods());
                        if (i + 1 >= path.length - 1) {
                            Map<String, PyObject> cache = (clazz.findMethodEnv(path[i + 1]));
                            if (cache != null) {
                                PyObject funcObj = cache.get(path[i + 1]);
                                if (funcObj instanceof InstanceBindable ib) {
                                    if (ib instanceof PyFunction func) {
                                        if (func.isAbstractMethod()) {
                                            throw new Interpreter.PyExceptionWrapper(
                                                    PyException.attributeError(
                                                            "method '" + path[i + 1] + "' in class '" + clazz.getName() +
                                                                    "' is abstract, cannot be called directly."
                                                    )
                                            );
                                        }
                                    }
                                    PyObject rst = ib.bindToInstance(interpreter, instance);
                                    return Map.of(path[i + 1], rst);
                                }
                            }
                            cache = clazz.getAttributeEnv(path[i + 1]);
                            return cache;
                        }
                        if (classAttributes.containsKey(path[i + 1])) {
                            currentValues = classAttributes;
                        } else {
                            return null; // 属性不存在
                        }
                    }
                }
            } else {
                try {
                    PyObject value = obj.getAttribute(interpreter, path[i + 1]);
                    currentValues = new HashMap<>();
                    currentValues.put(path[i + 1], value);
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }
    
    /**
     * 赋值给已存在的变量
     */
    public void assign(String name, PyObject value) {
        if (values.containsKey(name)) {
            values.put(name, value);
            return;
        }
          if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        
        throw new Interpreter.PyExceptionWrapper(
            edu.carole.runtime.PyException.nameError("Undefined variable '" + name + "'")
        );
    }
    
    /**
     * 检查变量是否存在
     */
    public boolean isDefined(String name) {
        if (values.containsKey(name)) {
            return true;
        }
        
        if (enclosing != null) {
            return enclosing.isDefined(name);
        }
        
        return false;
    }
      /**
     * 获取所有变量名
     */
    public Map<String, PyObject> getValues() {
        return new HashMap<>(values);
    }
    
    /**
     * 设置当前类上下文（用于super()）
     */
    public void setCurrentClass(PyClass currentClass) {
        this.currentClass = currentClass;
    }
    
    /**
     * 设置当前实例上下文（用于super()）
     */
    public void setCurrentInstance(PyInstance currentInstance) {
        this.currentInstance = currentInstance;
    }
    
    /**
     * 获取当前类上下文（用于super()）
     */
    public PyClass getCurrentClass() {
        if (currentClass != null) {
            return currentClass;
        }
        if (enclosing != null) {
            return enclosing.getCurrentClass();
        }
        return null;
    }
    
    /**
     * 获取当前实例上下文（用于super()）
     */
    public PyInstance getCurrentInstance() {
        if (currentInstance != null) {
            return currentInstance;
        }
        if (enclosing != null) {
            return enclosing.getCurrentInstance();
        }
        return null;
    }
    
    /**
     * 获取全局环境（遍历到最顶层）
     */
    public Environment getGlobal() {
        Environment current = this;
        while (current.enclosing != null) {
            current = current.enclosing;
        }
        return current;
    }
    
    /**
     * 在指定环境中定义变量（用于global和nonlocal）
     */
    public void defineInEnvironment(String name, PyObject value, Environment targetEnv) {
        targetEnv.define(name, value);
    }
    
    /**
     * 在指定环境中赋值变量（用于global和nonlocal）
     */
    public void assignInEnvironment(String name, PyObject value, Environment targetEnv) {
        if (targetEnv.values.containsKey(name)) {
            targetEnv.values.put(name, value);
        } else {
            targetEnv.define(name, value);
        }
    }
    
    /**
     * 获取外层非局部环境中的变量（用于nonlocal）
     */
    public Environment findNonlocalEnvironment(String name) {
        Environment current = this.enclosing;
        while (current != null) {
            if (current.values.containsKey(name)) {
                return current;
            }
            current = current.enclosing;
        }
        return null;
    }
}

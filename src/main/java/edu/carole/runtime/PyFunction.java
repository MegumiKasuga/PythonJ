package edu.carole.runtime;

import edu.carole.ast.ASTNode;
import edu.carole.ast.statements.Decorator;
import edu.carole.ast.statements.FunctionParameter;
import edu.carole.interpreter.Environment;
import edu.carole.interpreter.Interpreter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Python函数对象
 */
public class PyFunction extends PyObject {
    private final String name;
    private final List<String> parameters;  // 保留用于向后兼容
    private final List<FunctionParameter> functionParameters;  // 新的参数结构
    private final List<ASTNode> body;
    private final Environment closure;
    private final Map<String, PyObject> attributes = new HashMap<>();
    private final String varargsParam; // *args parameter name
    private final String kwargsParam; // **kwargs parameter name
    private final Map<String, ASTNode> defaultValues; // parameter name -> default value expression
    private boolean isBoundMethod = false;
    private PyObject boundInstance = null;

    private final int line, column;
    
    // 静态工厂方法用于向后兼容
    public static PyFunction fromParameterNames(String name, List<String> parameters, List<ASTNode> body, Environment closure, int line, int column) {
        List<FunctionParameter> functionParameters = parameters.stream()
                .map(paramName -> new FunctionParameter(paramName, line, column))
                .collect(java.util.stream.Collectors.toList());
        return new PyFunction(name, functionParameters, body, closure, line, column);
    }

    // 主要构造器，使用 FunctionParameter 结构
    public PyFunction(String name, List<FunctionParameter> functionParameters, List<ASTNode> body, Environment closure, int line, int column) {
        this.name = name;
        this.functionParameters = functionParameters;
        this.parameters = extractParameterNames(functionParameters);
        this.body = body;
        this.closure = closure;
        this.varargsParam = extractVarargsParam(functionParameters);
        this.kwargsParam = extractKwargsParam(functionParameters);
        this.defaultValues = extractDefaultValues(functionParameters);
        this.line = line;
        this.column = column;

        // Debug output
//        System.out.println("DEBUG: Function " + name + " created with:");
//        System.out.println("  Regular parameters: " + parameters);
//        System.out.println("  Varargs param: " + varargsParam);
//        System.out.println("  Default values: " + defaultValues.keySet());
    }

    public PyFunction(String name, List<String> parameters, List<ASTNode> body, Environment closure, String varargsParam, int line, int column) {
        this.name = name;
        this.parameters = parameters;
        this.functionParameters = convertToFunctionParameters(parameters, varargsParam, null);
        this.body = body;
        this.closure = closure;
        this.varargsParam = varargsParam;
        this.kwargsParam = null;
        this.defaultValues = new HashMap<>();
        this.line = line;
        this.column = column;
    }
    
    public PyFunction(String name, List<String> parameters, List<ASTNode> body, Environment closure, 
                      String varargsParam, String kwargsParam, Map<String, ASTNode> defaultValues,
                      int line, int column) {
        this.name = name;
        this.parameters = parameters;
        this.functionParameters = convertToFunctionParameters(parameters, varargsParam, kwargsParam, defaultValues);
        this.body = body;
        this.closure = closure;
        this.varargsParam = varargsParam;
        this.kwargsParam = kwargsParam;
        this.defaultValues = defaultValues != null ? defaultValues : new HashMap<>();
        this.line = line;
        this.column = column;
    }

    public boolean hasSameFunctionHead(PyFunction other) {
        if (other == null) return false;
        if (this == other) return true;

        // 比较函数名
        if (!this.name.equals(other.name)) return false;

        // 比较参数列表
        if (this.functionParameters.size() != other.functionParameters.size()) return false;

        // 比较可变参数
        if (this.varargsParam == null && other.varargsParam != null) return false;
        if (this.varargsParam != null && other.varargsParam == null) return false;

        // 比较关键字参数
        if (this.kwargsParam == null && other.kwargsParam != null) return false;
        return !(this.kwargsParam != null && other.kwargsParam == null);
    }

    // 转换工具方法
    private List<FunctionParameter> convertToFunctionParameters(List<String> parameterNames) {
        return parameterNames.stream()
                .map(paramName -> new FunctionParameter(paramName, line, column))
                .collect(Collectors.toList());
    }
    
    private List<FunctionParameter> convertToFunctionParameters(List<String> parameterNames, String varargsParam, String kwargsParam) {
        return convertToFunctionParameters(parameterNames, varargsParam, kwargsParam, null);
    }
    
    private List<FunctionParameter> convertToFunctionParameters(List<String> parameterNames, 
                                                               String varargsParam, String kwargsParam, 
                                                               Map<String, ASTNode> defaultValues) {
        List<FunctionParameter> result = new ArrayList<>();
        
        // 添加普通参数
        for (String paramName : parameterNames) {
            ASTNode defaultValue = (defaultValues != null) ? defaultValues.get(paramName) : null;
            result.add(new FunctionParameter(paramName, defaultValue, line, column));
        }
        
        // 添加 *args 参数
        if (varargsParam != null) {
            result.add(new FunctionParameter(varargsParam, FunctionParameter.ParameterType.VARARGS, line, column));
        }
        
        // 添加 **kwargs 参数
        if (kwargsParam != null) {
            result.add(new FunctionParameter(kwargsParam, FunctionParameter.ParameterType.KWARGS, line, column));
        }
        
        return result;
    }

    // 从 FunctionParameter 列表中提取信息的工具方法
    private List<String> extractParameterNames(List<FunctionParameter> functionParameters) {
        return functionParameters.stream()
                .filter(FunctionParameter::isNormal)
                .map(FunctionParameter::getIdentifier)
                .collect(Collectors.toList());
    }
    
    private String extractVarargsParam(List<FunctionParameter> functionParameters) {
        return functionParameters.stream()
                .filter(FunctionParameter::isVarargs)
                .map(FunctionParameter::getIdentifier)
                .findFirst()
                .orElse(null);
    }
    
    private String extractKwargsParam(List<FunctionParameter> functionParameters) {
        return functionParameters.stream()
                .filter(FunctionParameter::isKwargs)
                .map(FunctionParameter::getIdentifier)
                .findFirst()
                .orElse(null);
    }
    
    private Map<String, ASTNode> extractDefaultValues(List<FunctionParameter> functionParameters) {
        Map<String, ASTNode> result = new HashMap<>();
        for (FunctionParameter param : functionParameters) {
            if (param.hasDefaultValue()) {
                result.put(param.getIdentifier(), param.getDefaultValue());
            }
        }
        return result;
    }

    public boolean isStaticMethod() {
        return attributes.containsKey("__isstaticmethod__") &&
                attributes.get("__isstaticmethod__").isTruthy();
    }

    public void setStaticMethod(boolean isStaticMethod) {
        attributes.put("__isstaticmethod__", PyBool.fromValue(isStaticMethod));
    }

    public boolean isAbstractMethod() {
        return attributes.containsKey("__isabstractmethod__") &&
                attributes.get("__isabstractmethod__").isTruthy();
    }

    public boolean isSetterMethod() {
        return attributes.containsKey("__ispropertysetter__") &&
                attributes.get("__ispropertysetter__").isTruthy();
    }

    public void setAbstractMethod(boolean isAbstract) {
        attributes.put("__isabstractmethod__", PyBool.fromValue(isAbstract));
    }

    @Override
    public String getTypeName() { return "function"; }
    
    @Override
    public String toString() { return "<function " + name + ">"; }
    
    @Override
    public boolean isTruthy() { return true; }
    
    // Public getter methods for function properties
    public String getName() { return name; }
    public List<String> getParameters() { return new ArrayList<>(parameters); }
    public List<FunctionParameter> getFunctionParameters() { return new ArrayList<>(functionParameters); }
    public Map<String, ASTNode> getDefaultValues() { return new HashMap<>(defaultValues); }
    public String getVarargsParam() { return varargsParam; }
    public String getKwargsParam() { return kwargsParam; }
    public List<ASTNode> getBody() { return new ArrayList<>(body); }
    public Environment getClosure() { return closure; }

    @Override
    public PyObject getAttribute(String attributeName) {
        if (attributes.containsKey(attributeName)) {
            return attributes.get(attributeName);
        } else if ("__name__".equals(attributeName)) {
            return new PyString(name);
        } else if ("__doc__".equals(attributeName)) {
            // Return None if no docstring is set
            return PyNone.INSTANCE;
        } else if ("__module__".equals(attributeName)) {
            // Return None if no module is set
            return PyNone.INSTANCE;
        }
        return super.getAttribute(attributeName);
    }
    
    @Override
    public void setAttribute(String attributeName, PyObject value) {
        attributes.put(attributeName, value);
    }
    
    @Override
    public PyObject call(List<PyObject> positionalArguments, Map<String, PyObject> keywordArguments, Interpreter interpreter) {
        // Create new function scope
        Environment environment = new Environment(interpreter, closure);
        
        int regularParamCount = parameters.size();
        int posArgCount = positionalArguments.size();
        
        // Debug output
//        System.out.println("DEBUG: Calling function " + name + " with keywords");
//        System.out.println("DEBUG: regularParamCount = " + regularParamCount);
//        System.out.println("DEBUG: posArgCount = " + posArgCount);
//        System.out.println("DEBUG: keywordArgCount = " +
//                      (keywordArguments != null ? keywordArguments.size() : 0));
//        System.out.println("DEBUG: parameters = " + parameters);
//        System.out.println("DEBUG: varargsParam = " + varargsParam);
//        System.out.println("DEBUG: kwargsParam = " + kwargsParam);
        
        // 跟踪已经绑定的参数
        Set<String> boundParams = new HashSet<>();
        
        // 1. 绑定由关键字指定的参数
        if (keywordArguments != null && !keywordArguments.isEmpty()) {
            for (Map.Entry<String, PyObject> entry : keywordArguments.entrySet()) {
                String paramName = entry.getKey();
                PyObject value = entry.getValue();
                
                // 如果是函数声明中的参数，则直接绑定
                if (parameters.contains(paramName)) {
                    environment.define(paramName, value);
                    boundParams.add(paramName);
//                    System.out.println("DEBUG: Bound keyword parameter: " + paramName + " = " + value);
                } 
                // 否则，如果有**kwargs参数，则放入kwargs字典
                else if (kwargsParam != null) {
                    // 暂存，稍后会放入kwargs字典
//                    System.out.println("DEBUG: Found keyword for kwargs: " + paramName + " = " + value);
                } else {
                    throw new RuntimeException(name + "() got an unexpected keyword argument '" + paramName + "'");
                }
            }
        }
        
        // 2. 绑定位置参数
        int paramIndex = 0;
        int argIndex = 0;
        
        while (paramIndex < regularParamCount && argIndex < posArgCount) {
            String paramName = parameters.get(paramIndex);
            
            // 跳过已经由关键字绑定的参数
            if (boundParams.contains(paramName)) {
                paramIndex++;
                continue;
            }
            
            // 绑定位置参数
            environment.define(paramName, positionalArguments.get(argIndex));
            boundParams.add(paramName);
//            System.out.println("DEBUG: Bound positional parameter: " + paramName + " = " + positionalArguments.get(argIndex));
            paramIndex++;
            argIndex++;
        }
        
        // 3. 为未绑定的参数使用默认值
        while (paramIndex < regularParamCount) {
            String paramName = parameters.get(paramIndex);
            
            // 跳过已经由关键字绑定的参数
            if (boundParams.contains(paramName)) {
                paramIndex++;
                continue;
            }
            
            // 使用默认值
            if (defaultValues.containsKey(paramName)) {
                try {
                    PyObject defaultValue = evaluateDefaultValue(defaultValues.get(paramName), interpreter);
                    environment.define(paramName, defaultValue);
                    System.out.println("DEBUG: Using default value for: " + paramName + " = " + defaultValue);
                } catch (Exception e) {
                    throw new RuntimeException("Error evaluating default value for parameter '" + 
                                         paramName + "': " + e.getMessage());
                }
            } else {
                throw new RuntimeException(name + "() missing required positional argument: '" + paramName + "'");
            }
            
            paramIndex++;
        }
        
        // 4. 处理*args参数
        if (varargsParam != null) {
            List<PyObject> varargsValues = new ArrayList<>();
            while (argIndex < posArgCount) {
                varargsValues.add(positionalArguments.get(argIndex));
                argIndex++;
            }
            environment.define(varargsParam, new PyTuple(varargsValues));
            System.out.println("DEBUG: Bound varargs parameter: " + varargsParam + " = " + varargsValues);
        } else if (argIndex < posArgCount) {
            // 如果没有*args但还有额外的位置参数，则报错
            throw new RuntimeException(name + "() takes " + regularParamCount + 
                               " positional arguments but " + posArgCount + " were given");
        }
        
        // 5. 处理**kwargs参数
        if (kwargsParam != null) {
            Map<PyObject, PyObject> kwargsMap = new HashMap<>();
            
            if (keywordArguments != null) {
                for (Map.Entry<String, PyObject> entry : keywordArguments.entrySet()) {
                    String paramName = entry.getKey();
                    
                    // 只将未在参数列表中声明的关键字参数加入kwargs
                    if (!parameters.contains(paramName)) {
                        kwargsMap.put(new PyString(paramName), entry.getValue());
                    }
                }
            }
            
            environment.define(kwargsParam, new PyDict(kwargsMap));
//            System.out.println("DEBUG: Bound kwargs parameter: " + kwargsParam + " = " + kwargsMap);
        } else if (keywordArguments != null) {
            // 确保没有未处理的关键字参数
            for (String key : keywordArguments.keySet()) {
                if (!parameters.contains(key)) {
                    throw new RuntimeException(name + "() got an unexpected keyword argument '" + key + "'");
                }
            }
        }
        
        // 执行函数体
        return callWithInterpreter(environment, interpreter);
    }

    @Override
    public PyObject call(List<PyObject> arguments, Interpreter interpreter) {
        // Create new function scope
        Environment environment = new Environment(interpreter, closure);
        
        List<PyObject> actualArguments = new ArrayList<>(arguments);
        
        // Handle bound method behavior by injecting 'self' as first argument
        if (isBoundMethod && !isStaticMethod()) {
            // Insert the instance as the first argument (self)
            actualArguments.add(0, boundInstance);
            // System.out.println("DEBUG: Injecting self argument for bound method: " + boundInstance);
        }
        
        int regularParamCount = parameters.size();
        int argCount = actualArguments.size();

        // Debug output
//        System.out.println("DEBUG: Calling function " + name);
//        System.out.println("DEBUG: regularParamCount = " + regularParamCount);
//        System.out.println("DEBUG: argCount = " + argCount);
//        System.out.println("DEBUG: parameters = " + parameters);
//        System.out.println("DEBUG: varargsParam = " + varargsParam);
//        System.out.println("DEBUG: kwargsParam = " + kwargsParam);
//        System.out.println("DEBUG: isBoundMethod = " + isBoundMethod);
        
        // Handle parameter binding with default values
        if (varargsParam == null && kwargsParam == null) {
            // No varargs or kwargs - check bounds considering default values
            int requiredParams = 0;
            for (String param : parameters) {
                if (!defaultValues.containsKey(param)) {
                    requiredParams++;
                }
            }
            
            if (argCount < requiredParams || argCount > regularParamCount) {
                throw new RuntimeException(name + "() takes " + requiredParams + 
                    " to " + regularParamCount + " positional arguments but " + argCount + " were given");
            }
        } else if (varargsParam != null && kwargsParam == null) {
            // Has varargs but no kwargs
            int requiredParams = 0;
            for (String param : parameters) {
                if (!defaultValues.containsKey(param)) {
                    requiredParams++;
                }
            }
            
            if (argCount < requiredParams) {
                throw new RuntimeException(name + "() takes at least " + requiredParams + 
                    " positional arguments but " + argCount + " were given");
            }
        }        // Bind regular parameters
//        System.out.println("DEBUG: Binding parameters for " + name + "():");
//        System.out.println("  regularParamCount: " + regularParamCount);
//        System.out.println("  argCount: " + argCount);
//        System.out.println("  parameters: " + parameters);
//        System.out.println("  varargsParam: " + varargsParam);
        
        for (int i = 0; i < regularParamCount; i++) {
            String paramName = parameters.get(i);
//            System.out.println("  Processing parameter " + i + ": " + paramName);
            if (i < argCount) {
                // Use provided argument
//                System.out.println("    Using provided argument: " + actualArguments.get(i));
                environment.define(paramName, actualArguments.get(i));
            } else if (defaultValues.containsKey(paramName)) {
                // Use default value - evaluate it in the closure environment
//                System.out.println("    Using default value");
                try {
                    PyObject defaultValue = evaluateDefaultValue(defaultValues.get(paramName), interpreter);
                    environment.define(paramName, defaultValue);
                } catch (Exception e) {
                    throw new RuntimeException("Error evaluating default value for parameter '" + paramName + "': " + e.getMessage());
                }
            } else {
                // Missing required parameter
//                System.out.println("    ERROR: Missing required parameter");
                throw new RuntimeException(name + "() missing required positional argument: '" + paramName + "'");
            }
        }
          // Bind varargs parameter if present
        if (varargsParam != null) {
            List<PyObject> varargsValues = new ArrayList<>();
            for (int i = regularParamCount; i < argCount; i++) {
                varargsValues.add(actualArguments.get(i));
            }
            environment.define(varargsParam, new PyTuple(varargsValues));
        }
        
        // Bind kwargs parameter if present (empty for now - will be populated when we add keyword arg support)
        if (kwargsParam != null) {
            environment.define(kwargsParam, new PyDict(new HashMap<>()));
        }
        
        // Execute function body with interpreter context
        return callWithInterpreter(environment, interpreter);
    }

    private PyObject evaluateDefaultValue(ASTNode defaultExpr, Interpreter interpreter) {
        if (interpreter == null) {
            interpreter = new Interpreter();
        }
        
        // Evaluate default value in the closure environment
        Environment previousEnv = interpreter.getEnvironment();
        interpreter.setEnvironment(closure);
        
        try {
            return defaultExpr.accept(interpreter);
        } finally {
            interpreter.setEnvironment(previousEnv);
        }
    }

    private PyGenerator defineGenerator(FunctionParameterPacket packet) {
        return new PyGenerator(packet);
    }
    
    /**
     * 使用指定的解释器调用函数，如果没有提供解释器则创建新的
     * Enhanced to better handle closures and nested functions
     */
    public PyObject callWithInterpreter(Environment functionEnvironment, Interpreter interpreter) {
        if (interpreter == null) {
            interpreter = new Interpreter();
        }
        
        // For closures, ensure the function's original closure is part of the environment chain
        Environment executionEnvironment;
        
        if (this.closure != null && functionEnvironment != this.closure) {
            // For nested functions, we need to make sure the closure environment is in the chain
            // Create a synthetic environment that links both the call environment and the closure
            executionEnvironment = new Environment(interpreter, functionEnvironment);
            
            // Add closure's variables directly to the execution environment
            for (Map.Entry<String, PyObject> entry : closure.getValues().entrySet()) {
                // Important: Preserve special handling for varargs (*args) and kwargs (**kwargs)
                String varName = entry.getKey();
                PyObject varValue = entry.getValue();
                
                // Handle special case for varargs and kwargs parameters
                if (varName.equals(varargsParam) || varName.equals(kwargsParam)) {
                    // These will be properly handled in call() method, just pass through
//                    System.out.println("DEBUG: Preserving special parameter " + varName + " in closure");
                }
                
                executionEnvironment.define(varName, varValue);
            }
        } else {
            // Use the provided environment directly
            executionEnvironment = functionEnvironment;
        }
        
        // Save the original environment and set to the execution environment
        Environment previousEnv = interpreter.getEnvironment();
        interpreter.setEnvironment(executionEnvironment);
        
        // Add the function itself to its environment to support recursion
        executionEnvironment.define(name, this);

        try {
            for (ASTNode node : body) {
                // Debug output for each node execution
                node.accept(interpreter);
            }
        } catch (ReturnException returnException) {
            return returnException.getValue();
        } catch (YieldException yieldException) {
            FunctionParameterPacket packet = new FunctionParameterPacket(
                    this,
                    yieldException,
                    functionEnvironment,
                    interpreter
            );
            return defineGenerator(packet);
        } finally {
            // Restore the original environment
            interpreter.setEnvironment(previousEnv);
        }
        
        return PyNone.INSTANCE;
    }

    // We no longer need this method as we're handling closure variables directly
    // in the callWithInterpreter method

    /**
     * Creates a function that is bound to a specific object instance (for methods)
     *
     * @param instance The object instance to bind this function to
     * @return A new PyFunction that is bound to the specified instance
     */
    public PyFunction bindToInstance(PyObject instance) {
        PyFunction boundFunction = new PyFunction(
                name,
                functionParameters,
                body,
                closure,
                line,
                column
        );

        // Mark as bound method and keep reference to the instance
        boundFunction.isBoundMethod = true;
        boundFunction.boundInstance = instance;
        boundFunction.setStaticMethod(isStaticMethod()); // Preserve static method status

        // Copy attributes to the bound function
        for (Map.Entry<String, PyObject> entry : attributes.entrySet()) {
            boundFunction.setAttribute(entry.getKey(), entry.getValue());
        }

        return boundFunction;
    }

    /**
     * 返回异常，用于控制流
     */
    public static class ReturnException extends RuntimeException {
        private final PyObject value;

        public ReturnException(PyObject value) {
            this.value = value;
        }

        public PyObject getValue() {
            return value;
        }
    }

    public static class YieldException extends RuntimeException {
        private final PyObject value;
        private final List<YieldingClause> from;

        public YieldException(PyObject value) {
            this.value = value;
            this.from = new ArrayList<>();
        }

        public PyObject getValue() {
            return value;
        }

        public void addFrom(YieldingClause node) {
            this.from.add(node);
        }

        public List<YieldingClause> getFrom() {
            return from;
        }
    }

    public record FunctionParameterPacket(
            PyFunction func,
            YieldException lastYield,
            Environment environment,
            Interpreter interpreter
    ) {}

    public static class YieldingClause {

        private final ASTNode self; // 可能是一个函数或生成器
        private List<ASTNode> body;
        private final boolean isCirculate; // 是否循环
        private PyObject iterableCache; // 用于缓存迭代器

        public YieldingClause(
                ASTNode self,
                List<ASTNode> body,
                boolean isCirculate,
                PyObject iterableCache
        ) {
            this.self = self;
            this.body = body;
            this.isCirculate = isCirculate;
            this.iterableCache = iterableCache;
        }

        public ASTNode self() {
            return self;
        }

        public List<ASTNode> body() {
            return body;
        }

        public boolean isCirculate() {
            return isCirculate;
        }

        public PyObject iterableCache() {
            return iterableCache;
        }

        public void setBody(List<ASTNode> body) {
            this.body = body;
        }

        public void setIterableCache(PyObject iterableCache) {
            this.iterableCache = iterableCache;
        }
    }
}

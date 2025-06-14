package edu.carole.interpreter;

import edu.carole.ast.*;
import edu.carole.ast.ast.ASTVisitor;
import edu.carole.ast.statements.*;
import edu.carole.ast.expressions.*;
import edu.carole.runtime.*;
import edu.carole.runtime.io.IOManager;
import edu.carole.runtime.property.PyProperty;

import java.util.*;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/**
 * Python解释器
 */
public class Interpreter implements ASTVisitor<PyObject> {
    private final Environment globals;
    private Environment environment;
    private final Set<String> globalVariables = new HashSet<>(); // Track variables declared as global in current scope
    private final Map<String, Environment> nonlocalVariables = new HashMap<>(); // Track nonlocal variables and their target environments
    private final ModuleLoader moduleLoader; // Module loading system
    private final IOManager io;

    public Interpreter() {
        this(IOManager.getInstance());
    }
    
    public Interpreter(IOManager io) {
        this.io = io;
        this.moduleLoader = new ModuleLoader(io);
        this.globals = BuiltinFunctions.createGlobalEnvironment(io, moduleLoader);
        this.environment = globals;
    }
    
    // Getter and setter for environment access
    public Environment getEnvironment() {
        return environment;
    }

    public ModuleLoader getModuleLoader() {
        return moduleLoader;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
    
    public void interpret(Program program) {
        try {
            visitProgram(program);
        } catch (RuntimeException error) {
            if (error instanceof PyExceptionWrapper wrapper) {
                io.getConsoleErrStream().println(wrapper.pyException.toString());
            } else {
                io.getConsoleErrStream().println(error.getMessage());
            }
        }
    }

    public PyObject execute(ASTNode statement, Environment env) {
        Environment previous = this.environment;
        try {
            this.environment = env;
            return statement.accept(this);
        } finally {
            this.environment = previous;
        }
    }
    
    /**
     * Evaluate an expression node in the given environment
     * Used by F-string processor to evaluate embedded expressions
     */
    public Object evaluateExpression(ASTNode expression, Environment env) {
        Environment previous = this.environment;
        try {
            this.environment = env;
            PyObject result = expression.accept(this);
            
            // Convert PyObject to Java object for string formatting
            if (result instanceof PyString) {
                return ((PyString) result).getValue();
            } else if (result instanceof PyInt) {
                return ((PyInt) result).getValue();
            } else if (result instanceof PyFloat) {
                return ((PyFloat) result).getValue();
            } else if (result instanceof PyBool) {
                return ((PyBool) result).getValue();
            } else if (result == PyNone.INSTANCE) {
                return null;
            } else {
                return result.toString();
            }
        } finally {
            this.environment = previous;
        }
    }
    
    @Override
    public PyObject visitProgram(Program program) {
        PyObject result = PyNone.INSTANCE;
        for (ASTNode statement : program.getStatements()) {
            result = statement.accept(this);
        }
        return result;
    }

    @Override
    public PyObject visitAssignmentStatement(AssignmentStatement statement) {
        PyObject value = statement.getValue().accept(this);
        String target = statement.getTarget();
        
        // Check if this variable is declared as global
        if (globalVariables.contains(target)) {
            globals.define(target, value);
        }

        // Check if this variable is declared as nonlocal
        else if (nonlocalVariables.containsKey(target)) {
            Environment targetEnv = nonlocalVariables.get(target);
            targetEnv.define(target, value);
        }

        // Regular local assignment
        else {
            environment.define(target, value);
        }
        
        return value;
    }

    @Override
    public PyObject visitCompoundAssignmentStatement(CompoundAssignmentStatement statement) {
        // Get current value of the variable
        PyObject currentValue = environment.get(statement.getTarget(), false);
        
        // Get the right-hand side value
        PyObject rightValue = statement.getValue().accept(this);
          // Perform the compound operation
        PyObject newValue = switch (statement.getOperator()) {
            case PLUS_ASSIGN ->
                    callMagicMethod(currentValue, "__add__", rightValue, () -> add(currentValue, rightValue));
            case MINUS_ASSIGN ->
                    callMagicMethod(currentValue, "__sub__", rightValue, () -> subtract(currentValue, rightValue));
            case MULTIPLY_ASSIGN ->
                    callMagicMethod(currentValue, "__mul__", rightValue, () -> multiply(currentValue, rightValue));
            case DIVIDE_ASSIGN ->
                    callMagicMethod(currentValue, "__truediv__", rightValue, () -> divide(currentValue, rightValue));
            case MODULO_ASSIGN ->
                    callMagicMethod(currentValue, "__mod__", rightValue, () -> modulo(currentValue, rightValue));
            case POWER_ASSIGN ->
                    callMagicMethod(currentValue, "__pow__", rightValue, () -> power(currentValue, rightValue));
            case FLOOR_DIVIDE_ASSIGN ->
                    callMagicMethod(currentValue, "__floordiv__", rightValue, () -> floorDivide(currentValue, rightValue));
            case AND_ASSIGN ->
                    callMagicMethod(currentValue, "__and__", rightValue, () -> bitwiseAnd(currentValue, rightValue));
            case OR_ASSIGN ->
                    callMagicMethod(currentValue, "__or__", rightValue, () -> bitwiseOr(currentValue, rightValue));
            case XOR_ASSIGN ->
                    callMagicMethod(currentValue, "__xor__", rightValue, () -> bitwiseXor(currentValue, rightValue));
            case LEFT_SHIFT_ASSIGN ->
                    callMagicMethod(currentValue, "__lshift__", rightValue, () -> leftShift(currentValue, rightValue));
            case RIGHT_SHIFT_ASSIGN ->
                    callMagicMethod(currentValue, "__rshift__", rightValue, () -> rightShift(currentValue, rightValue));
            default -> throw new RuntimeException("Unknown compound assignment operator: " + statement.getOperator());
        };

        // Store the new value respecting global/nonlocal declarations
        String target = statement.getTarget();
        if (globalVariables.contains(target)) {
            globals.define(target, newValue);
        } else if (nonlocalVariables.containsKey(target)) {
            Environment targetEnv = nonlocalVariables.get(target);
            targetEnv.define(target, newValue);
        }else {
            environment.define(target, newValue);
        }
        return newValue;
    }

    @Override
    public PyObject visitAttributeAssignmentStatement(AttributeAssignmentStatement statement) {
        PyObject object = statement.getObject().accept(this);
        PyObject value = statement.getValue().accept(this);
        object.setAttribute(statement.getAttribute(), value);
        return value;
    }

    @Override
    public PyObject visitIndexAssignmentStatement(IndexAssignmentStatement statement) {
        PyObject object = statement.getObject().accept(this);
        PyObject value = statement.getValue().accept(this);
        
        // Check if this is a slice assignment
        if (statement.getIndex() instanceof SliceExpression) {
            SliceExpression slice = (SliceExpression) statement.getIndex();
            PyObject start = slice.getStart() != null ? slice.getStart().accept(this) : PyNone.INSTANCE;
            PyObject stop = slice.getStop() != null ? slice.getStop().accept(this) : PyNone.INSTANCE;
            PyObject step = slice.getStep() != null ? slice.getStep().accept(this) : PyNone.INSTANCE;
            
            object.setSlice(start, stop, step, value);
        } else {
            // Regular index assignment
            PyObject index = statement.getIndex().accept(this);
            object.setItem(index, value);
        }
        
        return value;
    }

    @Override
    public PyObject visitTupleUnpackingAssignment(TupleUnpackingAssignment statement) {
        PyObject value = statement.getValue().accept(this);
        List<String> targets = statement.getTargets();
        
        // Convert value to a list of elements for unpacking
        List<PyObject> elements = new ArrayList<>();
        
        if (value instanceof PyTuple) {
            elements = ((PyTuple) value).getElements();
        } else if (value instanceof PyList) {
            elements = ((PyList) value).getElements();
        } else {
            // Try to iterate through the value
            try {
                Iterator<PyObject> iterator = value.iterator();
                while (iterator.hasNext()) {
                    elements.add(iterator.next());
                }
            } catch (RuntimeException e) {
                throw new RuntimeException("cannot unpack non-sequence " + value.getTypeName());
            }
        }
        
        if (elements.size() != targets.size()) {
            if (elements.size() < targets.size()) {
                throw new RuntimeException("not enough values to unpack (expected " + targets.size() + ", got " + elements.size() + ")");
            } else {
                throw new RuntimeException("too many values to unpack (expected " + targets.size() + ")");
            }
        }
        
        // Assign each element to corresponding target variable
        for (int i = 0; i < targets.size(); i++) {
            environment.define(targets.get(i), elements.get(i));
        }
        return value;
    }
    
    @Override
    public PyObject visitExpressionStatement(ExpressionStatement statement) {
        return statement.getExpression().accept(this);
    }
    
    @Override
    public PyObject visitIfStatement(IfStatement statement) {
        for (Map.Entry<ASTNode, List<ASTNode>> branch : statement.getConditionBranches()) {
            // Evaluate the condition
            PyObject condition = branch.getKey().accept(this);
            if (condition.isTruthy()) {
                // If condition is true, execute the then branch
                for (ASTNode stmt : branch.getValue()) {
                    try {
                        stmt.accept(this);
                    } catch (PyFunction.YieldException e) {
                        // 如果在if分支中遇到yield，抛出异常以便外部处理
                        PyFunction.YieldingClause clause = new PyFunction.YieldingClause(
                                statement,
                                branch.getValue(),
                                false,
                                null
                        );
                        e.addFrom(clause);
                        throw e;
                    }
                }
                return PyNone.INSTANCE; // Exit after executing the first true branch
            }
        }
        if (!statement.getElseBranch().isEmpty()) {
            for (ASTNode stmt : statement.getElseBranch()) {
                try {
                    stmt.accept(this);
                } catch (PyFunction.YieldException e) {
                    // 如果在if分支中遇到yield，抛出异常以便外部处理
                    PyFunction.YieldingClause clause = new PyFunction.YieldingClause(
                            statement,
                            statement.getElseBranch(),
                            false,
                            null
                    );
                    e.addFrom(clause);
                    throw e;
                }
            }
        }
        return PyNone.INSTANCE;
    }

    @Override
    public PyObject visitWhileStatement(WhileStatement statement) {
        boolean brokeOut = false;
        
        try {
            while (statement.getCondition().accept(this).isTruthy()) {
                try {
                    for (ASTNode stmt : statement.getBody()) {
                        try {
                            stmt.accept(this);
                        } catch (PyFunction.YieldException yieldStat) {
                            PyFunction.YieldingClause clause = new PyFunction.YieldingClause(
                                    statement,
                                    statement.getBody(),
                                    true,
                                    null
                            );
                            yieldStat.addFrom(clause);
                            throw yieldStat;
                        }
                    }
                } catch (ContinueException e) {
                    continue;
                }
            }
        } catch (BreakException e) {
            // 通过break退出循环
            brokeOut = true;
        }
        
        // 只有在没有通过break退出循环时才执行else块
        if (!brokeOut && !statement.getElseBody().isEmpty()) {
            for (ASTNode stmt : statement.getElseBody()) {
                try {
                    stmt.accept(this);
                } catch (PyFunction.YieldException yieldStat) {
                    PyFunction.YieldingClause clause = new PyFunction.YieldingClause(
                            statement,
                            statement.getElseBody(),
                            false,
                            null
                    );
                    yieldStat.addFrom(clause);
                    throw yieldStat;
                }
            }
        }
        
        return PyNone.INSTANCE;
    }

    @Override
    public PyObject visitForStatement(ForStatement statement) {
        PyObject iterable = statement.getIterable().accept(this);
        return visitForStatement(statement, iterable);
    }

    public PyObject visitForStatement(ForStatement statement, PyObject iterable) {
        Iterator<PyObject> iterator = iterable.iterator();
        boolean brokeOut = false;
        Circle: try {
            while (iterator.hasNext()) {
                try {
                    PyObject value;
                    try {
                        value = iterator.next();
                    } catch (RuntimeException e) {
                        if (e.getMessage().equals("StopIteration")) {
                            break Circle;
                        } else {
                            throw e;
                        }
                    }
                    environment.define(statement.getVariable(), value);

                    for (ASTNode stmt : statement.getBody()) {
                        try {
                            stmt.accept(this);
                        } catch (PyFunction.YieldException yieldStat) {
                            PyFunction.YieldingClause clause = new PyFunction.YieldingClause(
                                    statement,
                                    statement.getBody(),
                                    true,
                                    iterable
                            );
                            yieldStat.addFrom(clause);
                            throw yieldStat;
                        }
                    }
                } catch (ContinueException e) {
                    continue;
                }
            }
        } catch (BreakException e) {
            // 通过break退出循环
            brokeOut = true;
        }

        // 只有在没有通过break退出循环时才执行else块
        if (!brokeOut && !statement.getElseBody().isEmpty()) {
            for (ASTNode stmt : statement.getElseBody()) {
                try {
                    stmt.accept(this);
                } catch (PyFunction.YieldException yieldStat) {
                    PyFunction.YieldingClause clause = new PyFunction.YieldingClause(
                            statement,
                            statement.getElseBody(),
                            false,
                            null
                    );
                    yieldStat.addFrom(clause);
                    throw yieldStat;
                }
            }
        }

        return PyNone.INSTANCE;
    }

    @Override
    public PyObject visitFunctionDef(FunctionDef function) {
        PyFunction pyFunction = new PyFunction(
            function.getName(),
            function.getParameters(),
            function.getBody(),
            environment,
            function.getLine(),
            function.getColumn()
        );
        pyFunction.setStaticMethod(function.isStaticMethod());
        environment.define(function.getName(), pyFunction);
        return pyFunction;
    }

    @Override
    public PyObject visitClassDef(ClassDef classDef) {
        Map<String, PyObject> methods = new HashMap<>();
        
        // 解析父类
        List<PyClass> baseClasses = new ArrayList<>();
        for (String baseClassName : classDef.getBaseClasses()) {
            PyObject baseObj = environment.get(baseClassName, true);
            if (baseObj instanceof PyClass) {
                baseClasses.add((PyClass) baseObj);
            } else {
                throw new RuntimeException("Base class '" + baseClassName + "' is not a class");
            }
        }
        
        // 创建类环境
        Environment classEnvironment = new Environment(environment);
        Environment previous = this.environment;
        Map<String, PyObject> classAttributes = new HashMap<>();
        Map<String, PyProperty> properties = new HashMap<>();
        
        try {
            this.environment = classEnvironment;
              // 执行类体
            for (ASTNode statement : classDef.getBody()) {
                if (statement instanceof FunctionDef method) {
                    PyFunction pyMethod = new PyFunction(
                        method.getName(),
                        method.getParameters(),
                        method.getBody(),
                        environment,
                        method.getLine(),
                        method.getColumn()
                    );
                    methods.put(method.getName(), pyMethod);
                } else if (statement instanceof Decorator decorator) {
                    PyObject result = visitDecorator(decorator, true);
                    if (result instanceof PyFunction func) {
                        if (func.isSetterMethod()) {
                            String funcName = func.getName();
                            if (!properties.containsKey(funcName)) {
                                throw new RuntimeException("No property found for setter method '" + funcName + "'");
                            }
                            PyProperty property = properties.get(funcName);
                            property.setSetter(func);
                        } else {
                            methods.put(func.getName(), func);
                        }
                    } else if (result instanceof PyProperty property) {
                        properties.put(property.getName(), property);
                    }
                } else if (statement instanceof AssignmentStatement assign) {
                    PyObject obj = assign.accept(this);
                    classAttributes.put(assign.getTarget(), obj);
                    classEnvironment.define(assign.getTarget(), obj);
                }
            }
        } finally {
            this.environment = previous;
        }
        
        PyClass pyClass = new PyClass(classDef.getName(), methods, baseClasses);
        pyClass.addClassAttributes(classAttributes);
        pyClass.addProperties(properties);
        environment.define(classDef.getName(), pyClass);
        HashMap<String, PyFunction> abstractMissImplementation =
                pyClass.scanMROForAbstractMethodsForImplementation(this);
        if (!abstractMissImplementation.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("class '").append(pyClass.getName()).append("' need to implement the following abstract methods: ");
            for (String methodName : abstractMissImplementation.keySet()) {
                sb.append(methodName).append(", ");
            }
            sb.setLength(sb.length() - 2); // 去掉最后的逗号和空格
            throw new PyExceptionWrapper(PyException.typeError(sb.toString()));
        }
        return pyClass;
    }
    
    @Override
    public PyObject visitReturnStatement(ReturnStatement statement) {
        PyObject value = PyNone.INSTANCE;
        if (statement.getValue() != null) {
            value = statement.getValue().accept(this);
        }
        throw new PyFunction.ReturnException(value);
    }

    @Override
    public PyObject visitYieldStatement(YieldStatement yieldStatement) {
        PyObject value = PyNone.INSTANCE;
        if (yieldStatement.getValue() != null) {
            value = yieldStatement.getValue().accept(this);
        }
        PyFunction.YieldException exception = new PyFunction.YieldException(value);
        PyFunction.YieldingClause clause = new PyFunction.YieldingClause(
                yieldStatement,
                null,
                false,
                null
        );
        exception.addFrom(clause);
        throw exception;
    }
    
    @Override
    public PyObject visitBreakStatement(BreakStatement statement) {
        throw new BreakException();
    }
    
    @Override
    public PyObject visitContinueStatement(ContinueStatement statement) {
        throw new ContinueException();
    }
      @Override
    public PyObject visitPassStatement(PassStatement statement) {
        return PyNone.INSTANCE;
    }
    
    @Override
    public PyObject visitGlobalStatement(GlobalStatement statement) {
        // Mark variables as global in the current scope
        for (String variable : statement.getVariables()) {
            globalVariables.add(variable);
        }
        return PyNone.INSTANCE;
    }
    
    @Override
    public PyObject visitNonlocalStatement(NonlocalStatement statement) {
        // Mark variables as nonlocal and find their target environments
        for (String variable : statement.getVariables()) {
            Environment nonlocalEnv = environment.findNonlocalEnvironment(variable);
            if (nonlocalEnv == null) {
                throw new RuntimeException("no binding for nonlocal '" + variable + "' found");
            }
            nonlocalVariables.put(variable, nonlocalEnv);
        }
        return PyNone.INSTANCE;
    }

    @Override
    public PyObject visitImportStatement(ImportStatement statement) {
        for (ImportStatement.ImportClause importClause : statement.getImports()) {
            String moduleName = importClause.getModuleName();
            String effectiveName = importClause.getEffectiveName();
            
            try {
                PyModule module = moduleLoader.importModule(moduleName);
                environment.define(effectiveName, module);
            } catch (Exception e) {
                throw new RuntimeException("ImportError: No module named '" + moduleName + "'");
            }
        }
        
        return PyNone.INSTANCE;
    }

    @Override
    public PyObject visitFromImportStatement(FromImportStatement statement) {
        String moduleName = statement.getModuleName();
        
        try {
            PyModule module = moduleLoader.importModule(moduleName);
            
            if (statement.isImportAll()) {
                // from module import * - import all public attributes
                moduleLoader.importAllFromModule(module, environment);
            } else {
                // from module import item1, item2 as alias
                for (FromImportStatement.ImportClause importClause : statement.getImports()) {
                    String itemName = importClause.getItemName();
                    String effectiveName = importClause.getEffectiveName();
                    
                    PyObject item = moduleLoader.importFromModule(module, itemName);
                    if (item == null) {
                        throw new RuntimeException("ImportError: cannot import name '" + itemName + "' from '" + moduleName + "'");
                    }
                    environment.define(effectiveName, item);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("ImportError: No module named '" + moduleName + "'");
        }
        
        return PyNone.INSTANCE;
    }

    @Override
    public PyObject visitTryExceptStatement(TryExceptStatement statement) {
        PyObject result = PyNone.INSTANCE;
        PyFunction.YieldException yieldCaught = null;
        boolean exceptionCaught = false;
        
        try {
            // 执行try块
            for (ASTNode stmt : statement.getTryBody()) {
                try {
                    result = stmt.accept(this);
                } catch (PyFunction.YieldException e) {
                    // 如果在分支中遇到yield，抛出异常以便外部处理
                    PyFunction.YieldingClause clause = new PyFunction.YieldingClause(
                            statement,
                            statement.getTryBody(),
                            false,
                            null
                    );
                    e.addFrom(clause);
                    throw e;
                }
            }
        } catch (PyExceptionWrapper pyExceptionWrapper) {
            // Python异常处理
            PyException pyException = pyExceptionWrapper.getPyException();
            exceptionCaught = handlePyException(statement, pyException);
            
            if (!exceptionCaught) {
                // 没有匹配的except子句，重新抛出异常
                throw pyExceptionWrapper;
            }
        } catch (RuntimeException runtimeException) {
            if (runtimeException instanceof PyFunction.YieldException y) {
                yieldCaught = y;
            } else {
                if (runtimeException instanceof PyFunction.ReturnException r) {
                    return r.getValue(); // 如果是return语句，直接返回值
                }
                // Java运行时异常转换为Python异常
                PyException pyException = convertRuntimeExceptionToPyException(runtimeException);
                try {
                    exceptionCaught = handlePyException(statement, pyException);
                } catch (PyFunction.YieldException yieldException) {
                    yieldCaught = yieldException;
                }

                if (!exceptionCaught) {
                    // 没有匹配的except子句，重新抛出原始异常
                    throw runtimeException;
                }
            }
        } finally {
            if (yieldCaught != null) {
                throw yieldCaught;
            }
            // 执行finally块
            if (!statement.getFinallyBody().isEmpty()) {
                for (ASTNode stmt : statement.getFinallyBody()) {
                    try {
                        stmt.accept(this);
                    } catch (PyFunction.YieldException e) {
                        // 如果在分支中遇到yield，抛出异常以便外部处理
                        PyFunction.YieldingClause clause = new PyFunction.YieldingClause(
                                statement,
                                statement.getFinallyBody(),
                                false,
                                null
                        );
                        e.addFrom(clause);
                        throw e;
                    }
                }
            }
        }
        
        return result;
    }
    
    public boolean handlePyException(TryExceptStatement statement, PyException pyException) {
        for (TryExceptStatement.ExceptClause exceptClause : statement.getExceptClauses()) {
            // 检查异常类型是否匹配
            if (exceptClause.getExceptionType() == null || 
                exceptClause.getExceptionType().equals(pyException.getExceptionType())) {
                
                // 如果有变量名，将异常绑定到变量
                if (exceptClause.getVariable() != null) {
                    environment.define(exceptClause.getVariable(), pyException);
                }
                
                // 执行except块
                for (ASTNode stmt : exceptClause.getBody()) {
                    try {
                        stmt.accept(this);
                    } catch (PyFunction.YieldException e) {
                        // 如果在分支中遇到yield，抛出异常以便外部处理
                        PyFunction.YieldingClause clause = new PyFunction.YieldingClause(
                                statement,
                                exceptClause.getBody(),
                                false,
                                pyException
                        );
                        e.addFrom(clause);
                        throw e;
                    }
                }
                
                return true; // 异常已被处理
            }
        }
        return false; // 没有匹配的except子句
    }
    
    public PyException convertRuntimeExceptionToPyException(RuntimeException runtimeException) {
        String message = runtimeException.getMessage();
        if (message == null) {
            message = runtimeException.getClass().getSimpleName();
        }
          // 根据异常类型和消息内容推断Python异常类型
        if (message.contains("division by zero") || message.contains("modulo by zero")) {
            return PyException.zeroDivisionError(message);
        } else if (message.contains("invalid literal for int()")) {
            return PyException.valueError(message);
        } else if (message.contains("not defined") || message.contains("name") || 
                   message.contains("undefined")) {
            return PyException.nameError(message);
        } else if (message.contains("type") || message.contains("operand")) {
            return PyException.typeError(message);
        } else if (message.contains("index") || message.contains("out of range")) {
            return PyException.indexError(message);
        } else if (message.contains("key")) {
            return PyException.keyError(message);
        } else if (message.contains("value")) {
            return PyException.valueError(message);
        } else if (message.contains("attribute")) {
            return PyException.attributeError(message);
        } else {
            return PyException.runtimeError(message);
        }
    }
    
    // Python异常包装器，用于区分Python异常和Java异常
    public static class PyExceptionWrapper extends RuntimeException {
        private final PyException pyException;
        
        public PyExceptionWrapper(PyException pyException) {
            super(pyException.toString());
            this.pyException = pyException;
        }
        
        public PyException getPyException() {
            return pyException;
        }

        @Override
        public String getMessage() {
            return pyException.toString();
        }
    }

    @Override
    public PyObject visitBinaryExpression(BinaryExpression expression) {
        PyObject left = expression.getLeft().accept(this);
        
        // 短路求值
        if (expression.getOperator() == BinaryExpression.Operator.AND) {
            if (!left.isTruthy()) return left;
            return expression.getRight().accept(this);
        }
        
        if (expression.getOperator() == BinaryExpression.Operator.OR) {
            if (left.isTruthy()) return left;
            return expression.getRight().accept(this);
        }
        
        PyObject right = expression.getRight().accept(this);
        
        // 使用运算符重载系统
        return switch (expression.getOperator()) {
            case PLUS -> callMagicMethod(left, "__add__", right, () -> add(left, right));
            case MINUS -> callMagicMethod(left, "__sub__", right, () -> subtract(left, right));
            case MULTIPLY -> callMagicMethod(left, "__mul__", right, () -> multiply(left, right));
            case DIVIDE -> callMagicMethod(left, "__truediv__", right, () -> divide(left, right));
            case MODULO -> callMagicMethod(left, "__mod__", right, () -> modulo(left, right));
            case POWER -> callMagicMethod(left, "__pow__", right, () -> power(left, right));
            case EQUAL -> callMagicMethod(left, "__eq__", right, () -> PyBool.valueOf(left.equals(right)));
            case NOT_EQUAL -> callMagicMethod(left, "__ne__", right, () -> PyBool.valueOf(!left.equals(right)));
            case LESS -> callMagicMethod(left, "__lt__", right, () -> PyBool.valueOf(compare(left, right) < 0));
            case LESS_EQUAL -> callMagicMethod(left, "__le__", right, () -> PyBool.valueOf(compare(left, right) <= 0));
            case GREATER -> callMagicMethod(left, "__gt__", right, () -> PyBool.valueOf(compare(left, right) > 0));
            case GREATER_EQUAL ->
                    callMagicMethod(left, "__ge__", right, () -> PyBool.valueOf(compare(left, right) >= 0));
            case IN -> PyBool.valueOf(isIn(left, right));
            case IS -> PyBool.valueOf(left == right);
            case FLOOR_DIVIDE -> callMagicMethod(left, "__floordiv__", right, () -> floorDivide(left, right));
            case BITWISE_AND -> callMagicMethod(left, "__and__", right, () -> bitwiseAnd(left, right));
            case BITWISE_OR -> callMagicMethod(left, "__or__", right, () -> bitwiseOr(left, right));
            case BITWISE_XOR -> callMagicMethod(left, "__xor__", right, () -> bitwiseXor(left, right));
            case LEFT_SHIFT -> callMagicMethod(left, "__lshift__", right, () -> leftShift(left, right));
            case RIGHT_SHIFT -> callMagicMethod(left, "__rshift__", right, () -> rightShift(left, right));
            default -> throw new RuntimeException("Unknown binary operator: " + expression.getOperator());
        };
    }

    @Override
    public PyObject visitUnaryExpression(UnaryExpression expression) {
        PyObject operand = expression.getOperand().accept(this);

        return switch (expression.getOperator()) {
            case MINUS -> callMagicMethod(operand, "__neg__", null, () -> {
                if (operand instanceof PyInt) {
                    return new PyInt(-((PyInt) operand).getValue());
                } else if (operand instanceof PyFloat) {
                    return new PyFloat(-((PyFloat) operand).getValue());
                } else {
                    throw new PyExceptionWrapper(PyException.typeError("bad operand type for unary -: '" + operand.getTypeName() + "'"));
                }
            });
            case NOT -> PyBool.valueOf(!operand.isTruthy());
            default -> throw new RuntimeException("Unknown unary operator: " + expression.getOperator());
        };
    }
    
    /**
     * 调用对象的魔术方法，如果不存在则回退到默认实现
     */
    private PyObject callMagicMethod(PyObject obj, String methodName, PyObject arg, java.util.function.Supplier<PyObject> fallback) {
        try {
            PyObject method = obj.getAttribute(methodName);
            List<PyObject> args = new ArrayList<>();
            if (arg != null) {
                args.add(arg);
            }
            return method.call(args, this);
        } catch (RuntimeException e) {
            // 如果魔术方法不存在或调用失败，使用回退实现
            if (e.getMessage().contains("has no attribute")) {
                return fallback.get();
            }
            throw e;
        }
    }

    @Override
    public PyObject visitConditionalExpression(ConditionalExpression expression) {
        // Evaluate the condition first
        PyObject condition = expression.getCondition().accept(this);
        
        // Python's truthiness evaluation: return the true expression if condition is truthy, 
        // otherwise return the false expression
        if (condition.isTruthy()) {
            return expression.getTrueExpression().accept(this);
        } else {
            if (expression.getFalseExpression() == null) {
                // If there's no false expression, return None
                return PyNone.INSTANCE;
            }
            return expression.getFalseExpression().accept(this);
        }
    }

    @Override
    public PyObject visitCallExpression(CallExpression expression) {
        PyObject function = expression.getFunction().accept(this);
        
        // 处理位置参数，包括*args展开
        List<PyObject> positionalArguments = new ArrayList<>();
        for (ASTNode arg : expression.getPositionalArguments()) {
            if (arg instanceof StarredExpression) {
                // Handle *args unpacking
                StarredExpression starred = (StarredExpression) arg;
                PyObject starredValue = starred.getExpression().accept(this);
                
                // Unpack the starred argument
                if (starredValue instanceof PyList) {
                    PyList list = (PyList) starredValue;
                    positionalArguments.addAll(list.getElements());
                } else if (starredValue instanceof PyTuple) {
                    PyTuple tuple = (PyTuple) starredValue;
                    positionalArguments.addAll(tuple.getElements());
                } else {
                    // Try to iterate over the object
                    throw new RuntimeException("Cannot unpack non-sequence object in function call");
                }
            } else {
                positionalArguments.add(arg.accept(this));
            }
        }
        
        // 处理关键字参数，包括**kwargs展开
        Map<String, PyObject> keywordArguments = new HashMap<>();
        if (expression.hasKeywordArguments()) {
            for (Map.Entry<String, ASTNode> entry : expression.getKeywordArguments().entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("**")) {
                    // Handle **kwargs unpacking
                    String kwargName = key.substring(2);
                    PyObject kwargsValue = entry.getValue().accept(this);
                        if (kwargsValue instanceof PyDict) {
                            PyDict dict = (PyDict) kwargsValue;
                            // Add all key-value pairs from the dict to keyword arguments
                            for (Map.Entry<PyObject, PyObject> dictEntry : dict.getEntries().entrySet()) {
                                // Convert PyObject key to String for keyword arguments
                                String keyStr = dictEntry.getKey().toString();
                                keywordArguments.put(keyStr, dictEntry.getValue());
                            }
                    } else {
                        throw new RuntimeException("Cannot unpack non-dict object as keyword arguments");
                    }
                } else {
                    keywordArguments.put(key, entry.getValue().accept(this));
                }
            }
        }
        
        // 调用支持关键字参数的call方法
        if (!keywordArguments.isEmpty()) {
            return function.call(positionalArguments, keywordArguments, this);
        } else {
            return function.call(positionalArguments, this);
        }
    }
    
    @Override
    public PyObject visitAttributeExpression(AttributeExpression expression) {
        PyObject object = expression.getObject().accept(this);
        return object.getAttribute(expression.getAttribute());
    }

    @Override
    public PyObject visitIndexExpression(IndexExpression expression) {
        PyObject object = expression.getObject().accept(this);
        PyObject index = expression.getIndex().accept(this);
        
        // Check if this is a slice operation
        if (expression.isSlice()) {
            // Handle slice operation
            SliceExpression slice = (SliceExpression) expression.getIndex();
            PyObject start = slice.getStart() != null ? slice.getStart().accept(this) : PyNone.INSTANCE;
            PyObject stop = slice.getStop() != null ? slice.getStop().accept(this) : PyNone.INSTANCE;
            PyObject step = slice.getStep() != null ? slice.getStep().accept(this) : PyNone.INSTANCE;
            
            // Call __getslice__ or __getitem__ with slice object
            return object.getSlice(start, stop, step);
        } else {
            // Regular index operation
            return object.getItem(index);
        }
    }

    @Override
    public PyObject visitSliceExpression(SliceExpression expression) {
        // This method should not be called directly since slices are always part of IndexExpression
        // But we implement it for completeness
        PyObject start = expression.getStart() != null ? expression.getStart().accept(this) : PyNone.INSTANCE;
        PyObject stop = expression.getStop() != null ? expression.getStop().accept(this) : PyNone.INSTANCE;
        PyObject step = expression.getStep() != null ? expression.getStep().accept(this) : PyNone.INSTANCE;
        
        // Return a slice object (we could create a PySlice class for this)
        return new PySlice(start, stop, step);
    }

    @Override
    public PyObject visitIdentifier(Identifier identifier) {
        String name = identifier.getName();
        return environment.get(name, true);
    }

    public static PyObject dealWithPropertyGet(PyObject input) {
        if (!(input instanceof PyProperty property)) {
            return input;
        }
        return property.call(new ArrayList<>());
    }

    public static PyObject dealWithPropertySet(PyObject key, PyObject value) {
        if (!(key instanceof PyProperty property)) {
            return value;
        }
        ArrayList<PyObject> args = new ArrayList<>();
        args.add(value);
        property.call(args);
        return value;
    }

    @Override
    public PyObject visitLiteral(Literal literal) {
        Object value = literal.getValue();
        
        if (value == null) {
            return PyNone.INSTANCE;
        } else if (value instanceof Boolean) {
            return PyBool.valueOf((Boolean) value);
        } else if (value instanceof Long) {
            return new PyInt((Long) value);
        } else if (value instanceof Double) {
            return new PyFloat((Double) value);
        } else if (value instanceof String) {
            return new PyString((String) value);
        } else {
            throw new RuntimeException("Unknown literal type: " + value.getClass());
        }
    }

    @Override
    public PyObject visitFStringLiteral(FStringLiteral fStringLiteral) {
        String rawValue = fStringLiteral.getValue();
        
        // Process the F-string by evaluating embedded expressions
        String processedValue = FStringProcessor.processFString(rawValue, this, this.environment);
        
        return new PyString(processedValue);
    }
    
    @Override
    public PyObject visitListLiteral(ListLiteral listLiteral) {
        List<PyObject> elements = new ArrayList<>();
        for (ASTNode element : listLiteral.getElements()) {
            elements.add(element.accept(this));
        }
        return new PyList(elements);
    }

    @Override
    public PyObject visitDictLiteral(DictLiteral dictLiteral) {
        Map<PyObject, PyObject> entries = new HashMap<>();
        for (Map.Entry<ASTNode, ASTNode> entry : dictLiteral.getEntries().entrySet()) {
            PyObject key = entry.getKey().accept(this);
            PyObject value = entry.getValue().accept(this);
            entries.put(key, value);
        }
        return new PyDict(entries);
    }
    
    @Override
    public PyObject visitSetLiteral(SetLiteral setLiteral) {
        Set<PyObject> elements = new HashSet<>();
        for (ASTNode element : setLiteral.getElements()) {
            elements.add(element.accept(this));
        }
        return new PySet(elements);
    }
    
    @Override
    public PyObject visitTupleLiteral(TupleLiteral tupleLiteral) {
        List<PyObject> elements = new ArrayList<>();
        for (ASTNode element : tupleLiteral.getElements()) {
            elements.add(element.accept(this));
        }
        return new PyTuple(elements);
    }
    
    // 辅助方法
    
    private PyObject add(PyObject left, PyObject right) {
        if (left instanceof PyInt && right instanceof PyInt) {
            return new PyInt(((PyInt) left).getValue() + ((PyInt) right).getValue());
        } else if (left instanceof PyFloat && right instanceof PyFloat) {
            return new PyFloat(((PyFloat) left).getValue() + ((PyFloat) right).getValue());
        } else if (left instanceof PyInt && right instanceof PyFloat) {
            return new PyFloat(((PyInt) left).getValue() + ((PyFloat) right).getValue());
        } else if (left instanceof PyFloat && right instanceof PyInt) {
            return new PyFloat(((PyFloat) left).getValue() + ((PyInt) right).getValue());
        } else if (left instanceof PyString && right instanceof PyString) {
            return new PyString(((PyString) left).getValue() + ((PyString) right).getValue());
        } else if (left instanceof PyList && right instanceof PyList) {
            List<PyObject> combined = new ArrayList<>(((PyList) left).getElements());
            combined.addAll(((PyList) right).getElements());
            return new PyList(combined);
        } else {
            throw new PyExceptionWrapper(PyException.typeError("unsupported operand type(s) for +: '" + 
                left.getTypeName() + "' and '" + right.getTypeName() + "'"));
        }
    }
    
    private PyObject subtract(PyObject left, PyObject right) {
        if (left instanceof PyInt && right instanceof PyInt) {
            return new PyInt(((PyInt) left).getValue() - ((PyInt) right).getValue());
        } else if (left instanceof PyFloat && right instanceof PyFloat) {
            return new PyFloat(((PyFloat) left).getValue() - ((PyFloat) right).getValue());
        } else if (left instanceof PyInt && right instanceof PyFloat) {
            return new PyFloat(((PyInt) left).getValue() - ((PyFloat) right).getValue());
        } else if (left instanceof PyFloat && right instanceof PyInt) {
            return new PyFloat(((PyFloat) left).getValue() - ((PyInt) right).getValue());        } else {
            throw new PyExceptionWrapper(PyException.typeError("unsupported operand type(s) for -: '" + 
                left.getTypeName() + "' and '" + right.getTypeName() + "'"));
        }
    }
    
    private PyObject multiply(PyObject left, PyObject right) {
        if (left instanceof PyInt && right instanceof PyInt) {
            return new PyInt(((PyInt) left).getValue() * ((PyInt) right).getValue());
        } else if (left instanceof PyFloat && right instanceof PyFloat) {
            return new PyFloat(((PyFloat) left).getValue() * ((PyFloat) right).getValue());
        } else if (left instanceof PyInt && right instanceof PyFloat) {
            return new PyFloat(((PyInt) left).getValue() * ((PyFloat) right).getValue());
        } else if (left instanceof PyFloat && right instanceof PyInt) {
            return new PyFloat(((PyFloat) left).getValue() * ((PyInt) right).getValue());
        } else if (left instanceof PyString && right instanceof PyInt) {
            String str = ((PyString) left).getValue();
            int times = (int) ((PyInt) right).getValue();
            return new PyString(str.repeat(Math.max(0, times)));
        } else if (left instanceof PyInt && right instanceof PyString) {
            String str = ((PyString) right).getValue();
            int times = (int) ((PyInt) left).getValue();
            return new PyString(str.repeat(Math.max(0, times)));        } else {
            throw new PyExceptionWrapper(PyException.typeError("unsupported operand type(s) for *: '" + 
                left.getTypeName() + "' and '" + right.getTypeName() + "'"));
        }
    }
    
    private PyObject divide(PyObject left, PyObject right) {
        if (left instanceof PyInt && right instanceof PyInt) {
            long rightValue = ((PyInt) right).getValue();            if (rightValue == 0) {
                throw new PyExceptionWrapper(PyException.zeroDivisionError("division by zero"));
            }
            return new PyFloat(((PyInt) left).getValue() / (double) rightValue);        } else if (left instanceof PyFloat && right instanceof PyFloat) {
            double rightValue = ((PyFloat) right).getValue();
            if (rightValue == 0.0) {
                throw new PyExceptionWrapper(PyException.zeroDivisionError("float division by zero"));
            }
            return new PyFloat(((PyFloat) left).getValue() / rightValue);
        } else if (left instanceof PyInt && right instanceof PyFloat) {
            double rightValue = ((PyFloat) right).getValue();
            if (rightValue == 0.0) {
                throw new PyExceptionWrapper(PyException.zeroDivisionError("float division by zero"));
            }
            return new PyFloat(((PyInt) left).getValue() / rightValue);
        } else if (left instanceof PyFloat && right instanceof PyInt) {
            long rightValue = ((PyInt) right).getValue();
            if (rightValue == 0) {
                throw new PyExceptionWrapper(PyException.zeroDivisionError("float division by zero"));
            }
            return new PyFloat(((PyFloat) left).getValue() / rightValue);
        } else {
            throw new PyExceptionWrapper(PyException.typeError("unsupported operand type(s) for /: '" + 
                left.getTypeName() + "' and '" + right.getTypeName() + "'"));
        }
    }
    
    private PyObject modulo(PyObject left, PyObject right) {
        if (left instanceof PyInt && right instanceof PyInt) {
            long rightValue = ((PyInt) right).getValue();            if (rightValue == 0) {
                throw new PyExceptionWrapper(PyException.zeroDivisionError("integer division or modulo by zero"));
            }
            return new PyInt(((PyInt) left).getValue() % rightValue);
        } else if (left instanceof PyFloat && right instanceof PyFloat) {
            double rightValue = ((PyFloat) right).getValue();            if (rightValue == 0.0) {
                throw new PyExceptionWrapper(PyException.zeroDivisionError("float modulo"));
            }
            return new PyFloat(((PyFloat) left).getValue() % rightValue);        } else {
            throw new PyExceptionWrapper(PyException.typeError("unsupported operand type(s) for %: '" + 
                left.getTypeName() + "' and '" + right.getTypeName() + "'"));
        }
    }

    private PyObject power(PyObject left, PyObject right) {
        if (left instanceof PyInt && right instanceof PyInt) {
            long base = ((PyInt) left).getValue();
            long exp = ((PyInt) right).getValue();
            return new PyInt((long) Math.pow(base, exp));
        } else if (left instanceof PyFloat || right instanceof PyFloat) {
            double base = left instanceof PyFloat ? ((PyFloat) left).getValue() : ((PyInt) left).getValue();
            double exp = right instanceof PyFloat ? ((PyFloat) right).getValue() : ((PyInt) right).getValue();
            return new PyFloat(Math.pow(base, exp));
        } else {
            throw new PyExceptionWrapper(PyException.typeError("unsupported operand type(s) for **: '" + 
                left.getTypeName() + "' and '" + right.getTypeName() + "'"));
        }
    }
    
    private PyObject floorDivide(PyObject left, PyObject right) {
        if (left instanceof PyInt && right instanceof PyInt) {
            long rightValue = ((PyInt) right).getValue();
            if (rightValue == 0) {
                throw new PyExceptionWrapper(PyException.zeroDivisionError("integer division or modulo by zero"));
            }
            return new PyInt(Math.floorDiv(((PyInt) left).getValue(), rightValue));
        } else if (left instanceof PyFloat && right instanceof PyFloat) {
            double rightValue = ((PyFloat) right).getValue();
            if (rightValue == 0.0) {
                throw new PyExceptionWrapper(PyException.zeroDivisionError("float floor division by zero"));
            }
            return new PyFloat(Math.floor(((PyFloat) left).getValue() / rightValue));
        } else if (left instanceof PyInt && right instanceof PyFloat) {
            double rightValue = ((PyFloat) right).getValue();
            if (rightValue == 0.0) {
                throw new PyExceptionWrapper(PyException.zeroDivisionError("float floor division by zero"));
            }
            return new PyFloat(Math.floor(((PyInt) left).getValue() / rightValue));
        } else if (left instanceof PyFloat && right instanceof PyInt) {
            long rightValue = ((PyInt) right).getValue();
            if (rightValue == 0) {
                throw new PyExceptionWrapper(PyException.zeroDivisionError("float floor division by zero"));
            }
            return new PyFloat(Math.floor(((PyFloat) left).getValue() / rightValue));
        } else {
            throw new PyExceptionWrapper(PyException.typeError("unsupported operand type(s) for //: '" + 
                left.getTypeName() + "' and '" + right.getTypeName() + "'"));
        }
    }
    
    private PyObject bitwiseAnd(PyObject left, PyObject right) {
        if (left instanceof PyInt && right instanceof PyInt) {
            return new PyInt(((PyInt) left).getValue() & ((PyInt) right).getValue());
        } else {
            throw new PyExceptionWrapper(PyException.typeError("unsupported operand type(s) for &: '" + 
                left.getTypeName() + "' and '" + right.getTypeName() + "'"));
        }
    }
    
    private PyObject bitwiseOr(PyObject left, PyObject right) {
        if (left instanceof PyInt && right instanceof PyInt) {
            return new PyInt(((PyInt) left).getValue() | ((PyInt) right).getValue());
        } else {
            throw new PyExceptionWrapper(PyException.typeError("unsupported operand type(s) for |: '" + 
                left.getTypeName() + "' and '" + right.getTypeName() + "'"));
        }
    }
    
    private PyObject bitwiseXor(PyObject left, PyObject right) {
        if (left instanceof PyInt && right instanceof PyInt) {
            return new PyInt(((PyInt) left).getValue() ^ ((PyInt) right).getValue());
        } else {
            throw new PyExceptionWrapper(PyException.typeError("unsupported operand type(s) for ^: '" + 
                left.getTypeName() + "' and '" + right.getTypeName() + "'"));
        }
    }
    
    private PyObject leftShift(PyObject left, PyObject right) {
        if (left instanceof PyInt && right instanceof PyInt) {
            long shiftCount = ((PyInt) right).getValue();
            if (shiftCount < 0) {
                throw new PyExceptionWrapper(PyException.valueError("negative shift count"));
            }            if (shiftCount >= 64) {
                throw new PyExceptionWrapper(PyException.valueError("shift count too large"));
            }
            return new PyInt(((PyInt) left).getValue() << shiftCount);
        } else {
            throw new PyExceptionWrapper(PyException.typeError("unsupported operand type(s) for <<: '" + 
                left.getTypeName() + "' and '" + right.getTypeName() + "'"));
        }
    }
    
    private PyObject rightShift(PyObject left, PyObject right) {
        if (left instanceof PyInt && right instanceof PyInt) {
            long shiftCount = ((PyInt) right).getValue();
            if (shiftCount < 0) {
                throw new PyExceptionWrapper(PyException.valueError("negative shift count"));
            }
            if (shiftCount >= 64) {
                long leftValue = ((PyInt) left).getValue();
                return new PyInt(leftValue < 0 ? -1 : 0);
            }
            return new PyInt(((PyInt) left).getValue() >> shiftCount);
        } else {
            throw new PyExceptionWrapper(PyException.typeError("unsupported operand type(s) for >>: '" + 
                left.getTypeName() + "' and '" + right.getTypeName() + "'"));
        }
    }
    
    private int compare(PyObject left, PyObject right) {
        if (left instanceof PyInt && right instanceof PyInt) {
            return Long.compare(((PyInt) left).getValue(), ((PyInt) right).getValue());
        } else if (left instanceof PyFloat && right instanceof PyFloat) {
            return Double.compare(((PyFloat) left).getValue(), ((PyFloat) right).getValue());
        } else if (left instanceof PyInt && right instanceof PyFloat) {
            return Double.compare(((PyInt) left).getValue(), ((PyFloat) right).getValue());
        } else if (left instanceof PyFloat && right instanceof PyInt) {
            return Double.compare(((PyFloat) left).getValue(), ((PyInt) right).getValue());
        } else if (left instanceof PyString && right instanceof PyString) {
            return ((PyString) left).getValue().compareTo(((PyString) right).getValue());
        } else {
            throw new RuntimeException("unorderable types: " + left.getTypeName() + " and " + right.getTypeName());
        }
    }
    
    private boolean isIn(PyObject item, PyObject container) {
        if (container instanceof PyString && item instanceof PyString) {
            return ((PyString) container).getValue().contains(((PyString) item).getValue());
        } else if (container instanceof PyList) {
            for (PyObject element : ((PyList) container).getElements()) {
                if (element.equals(item)) {
                    return true;
                }
            }
            return false;
        } else if (container instanceof PyDict) {
            return ((PyDict) container).getEntries().containsKey(item);
        } else {
            throw new RuntimeException("argument of type '" + container.getTypeName() + "' is not iterable");        }
    }
    
    @Override
    public PyObject visitLambdaExpression(LambdaExpression lambdaExpression) {
        return new PyLambda(lambdaExpression.getParameters(), lambdaExpression.getBody(), environment);
    }

    @Override
    public PyObject visitSuperExpression(SuperExpression superExpression) {
        // Get the current class and instance from the environment context
        PyClass currentClass = environment.getCurrentClass();
        PyInstance currentInstance = environment.getCurrentInstance();
        
        if (currentClass == null || currentInstance == null) {
            throw new RuntimeException("super() can only be used inside a method");
        }
        
        // Create a super object that knows how to resolve methods from parent classes
        return new PySuper(currentClass, currentInstance, superExpression.getClassName());
    }

    @Override
    public PyObject visitStarredExpression(StarredExpression starredExpression) {
        // For starred expressions (*args), we need to evaluate the expression
        // and mark it as a starred argument for function calls
        PyObject value = starredExpression.getExpression().accept(this);
        
        // For now, return the value directly - the CallExpression visitor
        // should handle the unpacking logic
        return value;
    }

    @Override
    public PyObject visitListComprehension(ListComprehension listComprehension) {
        List<PyObject> result = new ArrayList<>();
        
        // Save current environment
        Environment previous = this.environment;
        
        try {
            // Execute comprehension
            executeComprehension(listComprehension.getElement(), listComprehension.getClauses(), 0, result);
        } finally {
            this.environment = previous;
        }
        
        return new PyList(result);
    }
    
    @Override
    public PyObject visitGeneratorExpression(GeneratorExpression generatorExpression) {
        return new PyGenerator(
            generatorExpression.getElement(),
            generatorExpression.getClauses(),
            environment,
            this
        );
    }
    
    private void executeComprehension(ASTNode element, List<ListComprehension.ComprehensionClause> clauses, 
                                     int clauseIndex, List<PyObject> result) {
        if (clauseIndex >= clauses.size()) {
            // Base case: evaluate element and add to result
            PyObject value = element.accept(this);
            result.add(value);
            return;
        }
        
        ListComprehension.ComprehensionClause clause = clauses.get(clauseIndex);
        PyObject iterable = clause.getIterable().accept(this);
        
        // Create new environment for this iteration
        Environment iterationEnv = new Environment(this.environment);
        Environment previous = this.environment;
        this.environment = iterationEnv;
        try {
            Iterator<PyObject> iterator;
            if (iterable instanceof PyString pyStr) {
                String value = pyStr.getValue();
                List<PyObject> strs = new ArrayList<>(value.length());
                for (char c : value.toCharArray()) {
                    strs.add(new PyString(String.valueOf(c)));
                }
                iterator = strs.iterator();
            } else {
                PyObject iterMethod = iterable.getAttribute("__iter__");
                PyObject iterResult = iterMethod.call(Collections.emptyList(), this);
                iterator = iterResult.iterator();
            }
            while (iterator.hasNext()) {
                PyObject iterElement = iterator.next();
                iterationEnv.define(clause.getVariable(), iterElement);
                if (clause.getCondition() != null) {
                    PyObject condition = clause.getCondition().accept(this);
                    if (!condition.isTruthy()) {
                        continue; // Skip this iteration if condition is not met
                    }
                }
                executeComprehension(element, clauses, clauseIndex + 1, result);
            }
        } catch (Exception e) {
            throw new PyExceptionWrapper(PyException.typeError("'" + iterable.getTypeName() + "' object is not iterable"));
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public PyObject visitDecorator(Decorator decorator) {
        // Decorators are handled in a special way, they modify the function or class definition
        return visitDecorator(decorator, false);
    }

    public PyObject visitDecorator(Decorator decorator, boolean inClass) {
        // First, evaluate the target function or class
        PyObject target = decorator.getTarget().accept(this);
        // Then, evaluate the decorator expression

        PyObject decorated;
        PyObject decoratorFunc;
        PyString setterName = null;
        // Apply the decorator to the target by calling the decorator with the target as argument
        List<PyObject> args = new ArrayList<>();
        args.add(target);

        ASTNode decoratorExpression = decorator.getExpression();
        if (decoratorExpression instanceof Identifier identifier &&
                identifier.getName().endsWith("setter")) {
            String name = identifier.getName().substring(0,
                    identifier.getName().length() - 7);
            setterName = new PyString(name);
            Identifier identifier1 = new Identifier("setter",
                    identifier.getLine(), identifier.getColumn());
            decoratorFunc = identifier1.accept(this);
            args.add(setterName);
        } else {
            decoratorFunc = decorator.getExpression().accept(this);
        }

        // The result of applying a decorator is the decorated function/class - use context-aware call
        decorated = decoratorFunc.call(args, this);


        // Update the environment binding if the target is a named function or class
        if (decorator.getTarget() instanceof FunctionDef) {
            String name = ((FunctionDef) decorator.getTarget()).getName();
            environment.define(name, decorated);
        } else if (decorator.getTarget() instanceof ClassDef) {
            String name = ((ClassDef) decorator.getTarget()).getName();
            environment.define(name, decorated);
        } else if (decorator.getTarget() instanceof Decorator) {
            // For nested decorators, the name binding will be handled by the outermost decorator
        }
        
        return decorated;
    }

    @Override
    public PyObject visitWithStatement(WithStatement statement) {
        // Evaluate the context expression
        PyObject contextManager = statement.getContextExpression().accept(this);
        
        // Call __enter__ method
        PyObject contextValue = contextManager.contextEnter();
        
        // If there's a target variable, assign the context value to it
        if (statement.getTargetVariable() != null) {
            environment.define(statement.getTargetVariable(), contextValue);
        }
        
        PyObject result = PyNone.INSTANCE;
        Throwable exception = null;
        boolean yieldCaught = false;

        try {
            // Execute the body
            for (ASTNode stmt : statement.getBody()) {
                result = stmt.accept(this);
            }
        } catch (Throwable e) {
            if (e instanceof PyFunction.YieldException y) {
                PyFunction.YieldingClause clause = new PyFunction.YieldingClause(
                        statement,
                        statement.getBody(),
                        false,
                        contextValue
                );
                y.addFrom(clause);
                yieldCaught = true;
                throw y;
            }
            if (!(e instanceof PyFunction.ReturnException)) {
                exception = e;
            }
            throw e;
        } finally {
            if (!yieldCaught) {
                // Always call __exit__, even if an exception occurred
                exit(contextManager, exception);
            }
        }
        
        return result;
    }

    public void exit(PyObject contextManager, Throwable exception) {
        try {
            PyObject excType = exception != null ? new PyString(exception.getClass().getSimpleName()) : PyNone.INSTANCE;
            PyObject excValue = exception != null ? new PyString(exception.getMessage()) : PyNone.INSTANCE;
            PyObject excTraceback = PyNone.INSTANCE; // Simplified for now

            contextManager.contextExit(excType, excValue, excTraceback);
        } catch (Exception exitException) {
            // If __exit__ raises an exception, it replaces the original exception
            if (exception == null) {
                throw new RuntimeException(exitException);
            }
            // If there was already an exception, the exit exception is ignored
        }
    }
    
    // Match-case statement and pattern visitor methods
    
    @Override
    public PyObject visitMatchStatement(MatchStatement statement) {
        // Evaluate the subject expression
        PyObject subject = statement.getSubject().accept(this);
        
        // Try each case clause until one matches
        for (MatchStatement.CaseClause caseClause : statement.getCases()) {
            // Create a new environment for pattern variable bindings
            Environment caseEnvironment = new Environment(this.environment);
            Environment previous = this.environment;
            
            try {
                this.environment = caseEnvironment;
                
                // Try to match the pattern against the subject
                if (matchPattern(caseClause.getPattern(), subject)) {
                    // Check guard condition if present
                    if (caseClause.getGuard() != null) {
                        PyObject guardResult = caseClause.getGuard().accept(this);
                        if (!guardResult.isTruthy()) {
                            continue; // Guard failed, try next case
                        }
                    }
                    
                    // Pattern matched and guard passed, execute the case body
                    PyObject result = PyNone.INSTANCE;
                    for (ASTNode stmt : caseClause.getBody()) {
                        try {
                            result = stmt.accept(this);
                        } catch (PyFunction.YieldException e) {
                            // 如果在分支中遇到yield，抛出异常以便外部处理
                            PyFunction.YieldingClause clause = new PyFunction.YieldingClause(
                                    statement,
                                    caseClause.getBody(),
                                    false,
                                    null
                            );
                            e.addFrom(clause);
                            throw e;
                        }
                    }
                    
                    // Merge any captured variables back to the original environment
                    mergePatternVariables(previous, caseEnvironment);
                    
                    return result;
                }
            } finally {
                this.environment = previous;
            }
        }
        if (statement.hasDefaultCase()) {
            Environment caseEnvironment = new Environment(this.environment);
            Environment previous = this.environment;
            try {
                this.environment = caseEnvironment;
                PyObject result = PyNone.INSTANCE;
                for (ASTNode stmt : statement.getDefaultBody()) {
                    try {
                        result = stmt.accept(this);
                    } catch (PyFunction.YieldException e) {
                        // 如果在分支中遇到yield，抛出异常以便外部处理
                        PyFunction.YieldingClause clause = new PyFunction.YieldingClause(
                                statement,
                                statement.getDefaultBody(),
                                false,
                                null
                        );
                        e.addFrom(clause);
                        throw e;
                    }
                }
                mergePatternVariables(previous, caseEnvironment);
                return result;
            } finally {
                this.environment = previous;
            }
        }
        
        // No case matched - this is a runtime error in Python
        throw new RuntimeException("No matching case in match statement");
    }
    
    /**
     * Match a pattern against a subject value
     */
    private boolean matchPattern(ASTNode pattern, PyObject subject) {
        if (pattern instanceof WildcardPattern) {
            return matchWildcardPattern((WildcardPattern) pattern, subject);
        } else if (pattern instanceof CapturePattern) {
            return matchCapturePattern((CapturePattern) pattern, subject);
        } else if (pattern instanceof LiteralPattern) {
            return matchLiteralPattern((LiteralPattern) pattern, subject);
        } else if (pattern instanceof SequencePattern) {
            return matchSequencePattern((SequencePattern) pattern, subject);
        } else if (pattern instanceof OrPattern) {
            return matchOrPattern((OrPattern) pattern, subject);
        } else {
            throw new RuntimeException("Unknown pattern type: " + pattern.getClass().getSimpleName());
        }
    }
      /**
     * Merge pattern variables from case environment to parent environment
     */
    private void mergePatternVariables(Environment parent, Environment caseEnv) {
        // Get all variables defined in case environment that weren't in parent
        Map<String, PyObject> caseVars = caseEnv.getValues();
        Map<String, PyObject> parentVars = parent.getValues();
        
        for (Map.Entry<String, PyObject> entry : caseVars.entrySet()) {
            String name = entry.getKey();
            PyObject value = entry.getValue();
            
            // Only merge new variables (captured by patterns)
            if (!parentVars.containsKey(name)) {
                parent.define(name, value);
            }
        }
    }
    
    private boolean matchWildcardPattern(WildcardPattern pattern, PyObject subject) {
        // Wildcard pattern always matches
        return true;
    }
    
    private boolean matchCapturePattern(CapturePattern pattern, PyObject subject) {
        // Capture pattern always matches and binds the subject to the variable
        environment.define(pattern.getName(), subject);
        return true;
    }


    private boolean matchLiteralPattern(LiteralPattern pattern, PyObject subject) {
        // Evaluate the literal value and compare with subject
        PyObject literalValue = pattern.getValue().accept(this);
        // Use Python equality semantics instead of Java equals()
        PyObject result = callMagicMethod(literalValue, "__eq__", subject, () -> PyBool.valueOf(literalValue.equals(subject)));
        return ((PyBool) result).getValue();
    }
    
    private boolean matchSequencePattern(SequencePattern pattern, PyObject subject) {
        List<ASTNode> patterns = pattern.getPatterns();
        
        // Check if subject is a sequence type
        if (pattern.isTuple() && !(subject instanceof PyTuple)) {
            return false;
        } else if (!pattern.isTuple() && !(subject instanceof PyList)) {
            return false;
        }
        
        // Get sequence elements
        List<PyObject> elements;
        if (subject instanceof PyTuple) {
            elements = ((PyTuple) subject).getElements();
        } else if (subject instanceof PyList) {
            elements = ((PyList) subject).getElements();
        } else {
            return false;
        }
        
        // Check if length matches
        if (patterns.size() != elements.size()) {
            return false;
        }
        
        // Match each pattern with corresponding element
        for (int i = 0; i < patterns.size(); i++) {
            if (!matchPattern(patterns.get(i), elements.get(i))) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean matchOrPattern(OrPattern pattern, PyObject subject) {
        // Try left pattern first
        Environment leftEnv = new Environment(this.environment);
        Environment previous = this.environment;
        
        try {
            this.environment = leftEnv;
            if (matchPattern(pattern.getLeft(), subject)) {
                // Left pattern matched, merge variables and return true
                mergePatternVariables(previous, leftEnv);
                return true;
            }
        } finally {
            this.environment = previous;
        }
        
        // Try right pattern
        Environment rightEnv = new Environment(this.environment);
        try {
            this.environment = rightEnv;
            if (matchPattern(pattern.getRight(), subject)) {
                // Right pattern matched, merge variables and return true
                mergePatternVariables(previous, rightEnv);
                return true;
            }
        } finally {
            this.environment = previous;
        }
        
        // Neither pattern matched
        return false;
    }
    
    @Override
    public PyObject visitWildcardPattern(WildcardPattern pattern) {
        // This method should not be called directly during matching
        // Pattern matching is handled by matchPattern() method
        throw new RuntimeException("Wildcard pattern should not be visited directly");
    }
    
    @Override
    public PyObject visitCapturePattern(CapturePattern pattern) {
        // This method should not be called directly during matching
        // Pattern matching is handled by matchPattern() method
        throw new RuntimeException("Capture pattern should not be visited directly");
    }
    
    @Override
    public PyObject visitLiteralPattern(LiteralPattern pattern) {
        // This method should not be called directly during matching
        // Pattern matching is handled by matchPattern() method
        throw new RuntimeException("Literal pattern should not be visited directly");
    }
    
    @Override
    public PyObject visitSequencePattern(SequencePattern pattern) {
        // This method should not be called directly during matching
        // Pattern matching is handled by matchPattern() method
        throw new RuntimeException("Sequence pattern should not be visited directly");
    }
    
    @Override
    public PyObject visitOrPattern(OrPattern pattern) {
        // This method should not be called directly during matching
        // Pattern matching is handled by matchPattern() method
        throw new RuntimeException("Or pattern should not be visited directly");
    }
    
    // 控制流异常
    public static class BreakException extends RuntimeException {}

    public static class ContinueException extends RuntimeException {}
}

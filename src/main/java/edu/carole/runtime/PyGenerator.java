package edu.carole.runtime;

import edu.carole.ast.ASTNode;
import edu.carole.ast.expressions.ListComprehension.ComprehensionClause;
import edu.carole.ast.statements.ForStatement;
import edu.carole.ast.statements.TryExceptStatement;
import edu.carole.ast.statements.WithStatement;
import edu.carole.interpreter.Environment;
import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.file_context.PyTextFileContext;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * Python生成器对象
 */
public class PyGenerator extends PyObject implements Iterable<PyObject> {
    private final ASTNode element;
    private final List<ComprehensionClause> clauses;
    private final Environment closure;
    private final Interpreter interpreter;
    private boolean exhausted = false;
    private GeneratorIterator currentIterator = null;
    private boolean isFunctionGenerator = false;
    
    public PyGenerator(ASTNode element, List<ComprehensionClause> clauses, Environment closure, Interpreter interpreter) {
        this.element = element;
        this.clauses = clauses;
        this.closure = closure;
        this.interpreter = interpreter;
    }

    public PyGenerator(PyFunction.FunctionParameterPacket yieldException) {
        interpreter = yieldException.interpreter();
        clauses = new ArrayList<>();
        closure = yieldException.environment();
        element = null;
        isFunctionGenerator = true;
        FunctionGeneratorIterator generatorIterator = new FunctionGeneratorIterator(yieldException.func());
        generatorIterator.setYieldingPoint(yieldException.lastYield());
        currentIterator = generatorIterator;
    }

    public boolean isFunctionGenerator() {
        return isFunctionGenerator;
    }

    @Override
    public Iterator<PyObject> iterator() {
        // For for-loops, return a new iterator
        if (currentIterator == null) {
            return new GeneratorIterator();
        }
        return currentIterator;
    }
    
    /**
     * Get the current iterator for next() function calls
     */
    public Iterator<PyObject> getCurrentIterator() {
        if (currentIterator == null) {
            currentIterator = new GeneratorIterator();
        }
        return currentIterator;
    }
    
    public boolean isExhausted() {
        return exhausted;
    }
    
    @Override
    public String toString() {
        return "<generator object>";
    }
    
    @Override
    public String getTypeName() {
        return "generator";
    }
      @Override
    public boolean isTruthy() {
        return !exhausted;
    }
    
    @Override
    public PyObject getAttribute(String name) {
        switch (name) {
            case "__iter__":
                return new PyBuiltinFunction("__iter__", (args, kwargs) -> {
                    if (args.size() != 0) {
                        throw new RuntimeException("__iter__() takes no arguments (" + args.size() + " given)");
                    }
                    return this; // Generators return themselves for __iter__
                });
                
            case "__next__":
                return new PyBuiltinFunction("__next__", (args, kwargs) -> {
                    if (args.size() != 0) {
                        throw new RuntimeException("__next__() takes no arguments (" + args.size() + " given)");
                    }
                    Iterator<PyObject> iter = getCurrentIterator();
                    if (iter.hasNext()) {
                        return iter.next();
                    } else {
                        exhausted = true;
                        throw new RuntimeException("StopIteration");
                    }
                });
                
            default:
                return super.getAttribute(name);
        }
    }
    
    class GeneratorIterator implements Iterator<PyObject> {
        private List<PyObject> values = null;
        private int index = 0;
        
        @Override
        public boolean hasNext() {
            if (values == null) {
                generateValues();
            }
            return index < values.size();
        }
        
        @Override
        public PyObject next() {
            if (values == null) {
                generateValues();
            }
            if (index >= values.size()) {
                exhausted = true;
                throw new RuntimeException("StopIteration");
            }
            return values.get(index++);
        }
        
        void generateValues() {
            values = new ArrayList<>();
            generateRecursive(0, new Environment(closure));
        }

        private void generateRecursive(int clauseIndex, Environment env) {
            if (clauseIndex >= clauses.size()) {
                // 生成元素
                Environment previous = interpreter.getEnvironment();
                try {
                    interpreter.setEnvironment(env);
                    PyObject result = element.accept(interpreter);
                    values.add(result);
                } finally {
                    interpreter.setEnvironment(previous);
                }
                return;
            }
            
            ComprehensionClause clause = clauses.get(clauseIndex);
            Environment previous = interpreter.getEnvironment();
            try {
                interpreter.setEnvironment(env);
                PyObject iterable = clause.getIterable().accept(interpreter);
                
                // Support all iterable objects
                Iterator<PyObject> iterator;
                try {
                    iterator = iterable.iterator();
                } catch (Exception e) {
                    throw new RuntimeException("'" + iterable.getTypeName() + "' object is not iterable");
                }
                
                while (iterator.hasNext()) {
                    PyObject item = iterator.next();
                    Environment newEnv = new Environment(env);
                    newEnv.define(clause.getVariable(), item);
                    
                    // 检查条件
                    if (clause.getCondition() != null) {
                        interpreter.setEnvironment(newEnv);
                        PyObject condition = clause.getCondition().accept(interpreter);
                        if (!condition.isTruthy()) {
                            continue;
                        }
                    }
                    
                    generateRecursive(clauseIndex + 1, newEnv);
                }
            } finally {
                interpreter.setEnvironment(previous);
            }
        }
    }

    class FunctionGeneratorIterator extends GeneratorIterator {
        private final PyFunction function;
        private PyFunction.YieldException yieldingPoint = null;
        private PyObject cachedValue = null;
        private final List<ASTNode> funcBody;

        public FunctionGeneratorIterator(PyFunction function) {
            this.function = function;
            funcBody = function.getBody();
        }

        public void setYieldingPoint(PyFunction.YieldException yieldingPoint) {
            this.yieldingPoint = yieldingPoint;
            yieldingPoint.addFrom(
                    new PyFunction.YieldingClause(
                            null,
                            funcBody,
                            false,
                            null
                    )
            );
            cachedValue = yieldingPoint.getValue();
        }

        public PyFunction getFunction() {
            return function;
        }

        public PyObject getCachedValue() {
            return cachedValue;
        }

        @Override
        public boolean hasNext() {
            if (yieldingPoint == null) {
                return cachedValue != null;
            }
            return true;
        }

        @Override
        void generateValues() {

        }

        @Override
        public PyObject next() {
            if (!hasNext()) {
                throw new RuntimeException("StopIteration");
            }
            if (yieldingPoint == null) {
                if (cachedValue != null) {
                    exhausted = true;
                    PyObject retValue = cachedValue;
                    cachedValue = null; // 重置缓存值
                    return retValue; // 返回缓存的值
                }
            }
            try {
                return goToNextYield();
            } catch (PyFunction.YieldException e) {
                PyObject retValue = cachedValue;
                if (e.getValue() != null) {
                    cachedValue = e.getValue();
                }
                yieldingPoint = e;
                return retValue;
            } catch (PyFunction.ReturnException e) {
                PyObject retValue = cachedValue;
                yieldingPoint = null;
                exhausted = true;
                if (e.getValue() != null) {
                    cachedValue = e.getValue();
                } else {
                    cachedValue = null; // 如果没有返回值，重置缓存值
                }
                return retValue;
            } catch (RuntimeException e) {
                if (e.getMessage().equals("StopIteration")) {
                    if (cachedValue == null) {
                        throw new RuntimeException("StopIteration");
                    }
                    exhausted = true;
                    yieldingPoint = null;
                    PyObject retValue = cachedValue;
                    cachedValue = null; // 重置缓存值
                    return retValue; // 返回缓存的值
                }
                throw e; // 其他异常直接抛出
            }
        }

        private PyObject goToNextYield() {
            if (!hasNext()) {
                throw new RuntimeException("StopIteration");
            }
            List<PyFunction.YieldingClause> clauses = yieldingPoint.getFrom();
            if (clauses.size() < 2) {
                throw new RuntimeException("Invalid yielding point, not enough clauses");
            }
            Environment previous = interpreter.getEnvironment();
            interpreter.setEnvironment(closure);
            try {
                for (int i = 1; i < clauses.size(); i++) {
                    PyFunction.YieldingClause lower = clauses.get(i - 1);
                    PyFunction.YieldingClause upper = clauses.get(i);
                    try {
                        runTree(upper, lower, closure);
                    } catch (PyFunction.YieldException e) {
                        // 如果遇到YieldException，说明需要返回值
                        for (int j = i; j < clauses.size(); j++) {
                            e.addFrom(clauses.get(j));
                        }
                        throw e;
                    } catch (PyFunction.ReturnException e) {
                        // 如果遇到ReturnException，说明函数执行完毕
                        exhausted = true;
                        yieldingPoint = null;
                        throw e;
                    }
                }
                return cachedValue;
            } finally {
                interpreter.setEnvironment(previous);
            }
        }

        private void runTree(PyFunction.YieldingClause upper,
                             PyFunction.YieldingClause lower,
                             Environment environment) {
            ASTNode statement = lower.self();
            List<ASTNode> body = upper.body();
            int beginIndex = body.indexOf(statement) + (lower.isCirculate() ? 0 : 1);
            if (beginIndex >= body.size()) {
                if (body == funcBody) {
                    // 如果是函数体，说明已经执行完毕
                    exhausted = true;
                    yieldingPoint = null;
                    dealWithWithExit(lower, null);
                    throw new PyFunction.ReturnException(null);
                }
                try {
                    dealWithWithExit(upper, null);
                    dealWithTryCatchFinally(upper);
                } catch (PyFunction.YieldException finallyYield) {
                    ASTNode upperNode = upper.self();
                    upper.setBody(((TryExceptStatement) upperNode).getFinallyBody());
                    throw finallyYield;
                }
                // 如果已经到达最后一个语句，直接返回
                return;
            }
            if (upper.self() instanceof WithStatement with) {
                if (with.getTargetVariable() != null && upper.iterableCache() != null) {
                    environment.define(with.getTargetVariable(), upper.iterableCache());
                }
            }
            for (int i = beginIndex; i < body.size(); i++) {
                ASTNode node = body.get(i);
                if (i == beginIndex && lower.iterableCache() != null) {
                    if (lower.isCirculate() && node instanceof ForStatement forStat) {
                        interpreter.visitForStatement(forStat, lower.iterableCache());
                    } else {
                        node.accept(interpreter);
                    }
                } else if (upper.self() instanceof TryExceptStatement tryExcept &&
                        body == tryExcept.getTryBody()) {
                    try {
                        node.accept(interpreter);
                    } catch (RuntimeException e) {
                        dealWithTryCatchExcept(upper, e);
                    }
                } else if (upper.self() instanceof WithStatement) {
                    try {
                        node.accept(interpreter);
                    } catch (Throwable e) {
                        if (e instanceof PyFunction.YieldException y) {
                            throw y;
                        }
                        dealWithWithExit(upper, e);
                        throw e;
                    }
                } else {
                    node.accept(interpreter);
                }
                if (body == funcBody && i >= body.size() - 1) {
                    // 函数体已经执行完成
                    yieldingPoint = null;
                    exhausted = true;
                    throw new PyFunction.ReturnException(cachedValue);
                }
            }
            try {
                dealWithTryCatchFinally(upper);
            } catch (PyFunction.YieldException finallyYield) {
                ASTNode upperNode = upper.self();
                upper.setBody(((TryExceptStatement) upperNode).getFinallyBody());
                throw finallyYield;
            }
        }

        private void dealWithWithExit(PyFunction.YieldingClause clause, Throwable exception) {
            if (!(clause.self() instanceof WithStatement)) return;
            PyObject obj = clause.iterableCache();
            if (!(obj instanceof PyTextFileContext context)) return;
            if (!context.isOpen()) return;
            interpreter.exit(obj, exception);
        }

        private void dealWithTryCatchFinally(PyFunction.YieldingClause upper) {
            if (!(upper.self() instanceof TryExceptStatement tryExcept)) return;
            List<ASTNode> finallyBody = tryExcept.getFinallyBody();
            if (upper.body() == finallyBody) {
                // 如果finally块是当前执行的块，直接返回
                return;
            }
            if (!(finallyBody != null && !finallyBody.isEmpty())) return;
            for (ASTNode finallyNode : finallyBody) {
                finallyNode.accept(interpreter);
            }
        }

        private void dealWithTryCatchExcept(PyFunction.YieldingClause upper, RuntimeException e) {
            if (!(upper.self() instanceof TryExceptStatement tryExcept)) return;
            if (e instanceof PyFunction.YieldException ||
                    e instanceof PyFunction.ReturnException)
                throw e;
            PyException except;
            if (!(e instanceof Interpreter.PyExceptionWrapper wrapper)) {
                except = interpreter.convertRuntimeExceptionToPyException(e);
            } else {
                except = wrapper.getPyException();
            }
            try {
                interpreter.handlePyException(tryExcept, except);
            } catch (PyFunction.YieldException yield) {
                yieldingPoint.getFrom().remove(upper);
                throw yield;
            }
        }
    }
}

package edu.carole.runtime;

import edu.carole.ast.ASTNode;
import edu.carole.ast.expressions.ListComprehension.ComprehensionClause;
import edu.carole.interpreter.Environment;
import edu.carole.interpreter.Interpreter;
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
    
    public PyGenerator(ASTNode element, List<ComprehensionClause> clauses, Environment closure, Interpreter interpreter) {
        this.element = element;
        this.clauses = clauses;
        this.closure = closure;
        this.interpreter = interpreter;
    }
    
    @Override
    public Iterator<PyObject> iterator() {
        // For for-loops, return a new iterator
        return new GeneratorIterator();
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
                return new PyBuiltinFunction("__iter__", args -> {
                    if (args.size() != 0) {
                        throw new RuntimeException("__iter__() takes no arguments (" + args.size() + " given)");
                    }
                    return this; // Generators return themselves for __iter__
                });
                
            case "__next__":
                return new PyBuiltinFunction("__next__", args -> {
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
    
    private class GeneratorIterator implements Iterator<PyObject> {
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
        
        private void generateValues() {
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
}

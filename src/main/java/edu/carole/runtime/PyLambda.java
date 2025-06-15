package edu.carole.runtime;

import edu.carole.ast.ASTNode;
import edu.carole.interpreter.Environment;
import edu.carole.interpreter.Interpreter;
import java.util.List;

/**
 * Python Lambda对象
 */
public class PyLambda extends PyObject {
    private final List<String> parameters;
    private final ASTNode body;
    private final Environment closure;
    
    public PyLambda(List<String> parameters, ASTNode body, Environment closure) {
        this.parameters = parameters;
        this.body = body;
        this.closure = closure;
    }
    
    public List<String> getParameters() {
        return parameters;
    }
    
    public ASTNode getBody() {
        return body;
    }
    
    public Environment getClosure() {
        return closure;
    }
    
    @Override
    public String toString() {
        return "<lambda>";
    }
    
    @Override
    public String getTypeName() {
        return "function";
    }
    
    @Override
    public boolean isTruthy() {
        return true;
    }

    @Override
    public PyObject call(List<PyObject> arguments, Interpreter interpreter) {
        if (arguments.size() != parameters.size()) {
            throw new RuntimeException("lambda takes " + parameters.size() +
                    " positional arguments but " + arguments.size() + " were given");
        }

        // 创建新的lambda作用域
        Environment environment = new Environment(interpreter, closure);

        // 绑定参数
        for (int i = 0; i < parameters.size(); i++) {
            environment.define(parameters.get(i), arguments.get(i));
        }

        // 执行lambda体 (单个表达式)
        return interpreter.execute(body, environment);
    }
}

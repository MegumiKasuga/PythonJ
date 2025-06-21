package edu.carole.runtime.clazz.builtins;

import edu.carole.ast.expressions.Literal;
import edu.carole.interpreter.BuiltinFunctions;
import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.PyObject;
import edu.carole.runtime.clazz.BuiltinClass;
import edu.carole.runtime.clazz.PyClass;
import edu.carole.runtime.instance.BuiltinInstance;

import javax.lang.model.type.NullType;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PyNone extends BuiltinClass<NullType> {
    public PyNone(String name, Map<String, PyObject> methods) {
        super(name, methods, NullType.class);
    }

    public PyNone(String name, Map<String, PyObject> methods, List<PyClass> baseClasses) {
        super(name, methods, baseClasses, NullType.class);
    }

    public PyNone(String name, String modulePath, Map<String, PyObject> methods) {
        super(name, modulePath, methods, NullType.class);
    }

    public PyNone(String name, String modulePath, Map<String, PyObject> methods, List<PyClass> baseClasses) {
        super(name, modulePath, methods, baseClasses, NullType.class);
    }

    @Override
    public void initMethods(Map<String, PyObject> methods, Map<String, PyObject> classAttr) {
        registerNArgs(methods, "__hash__", 0, (args, kwargs, inter) ->
                inter.getInteger(args.get(0).hashCode()));
    }

    @Override
    public BuiltinClass<NullType> getPyClass(Interpreter interpreter) {
        return interpreter.getMemoryModel().getNONE_TYPE();
    }

    @Override
    public boolean isValidLiteral(Literal literal) {
        return literal.getValue() == null;
    }

    @Override
    public int hashCode(BuiltinInstance<NullType> instance) {
        return Objects.hashCode(null);
    }
}

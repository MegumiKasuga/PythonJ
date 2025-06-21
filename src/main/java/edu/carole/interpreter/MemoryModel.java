package edu.carole.interpreter;

import edu.carole.runtime.PyObject;
import edu.carole.runtime.clazz.BuiltinClass;
import edu.carole.runtime.clazz.builtins.*;
import edu.carole.runtime.instance.BuiltinInstance;
import lombok.Getter;

import javax.lang.model.type.NullType;
import java.util.HashMap;
import java.util.Map;

public class MemoryModel {

    @Getter
    private final Interpreter interpreter;
    private final Map<Long, BuiltinInstance<Long>> integers;
    private final Map<String, BuiltinClass> builtinClasses;

    @Getter
    private BuiltinClass<NullType> NONE_TYPE;

    @Getter
    private BuiltinClass<Long> INTEGER;

    @Getter
    private BuiltinClass<Double> FLOAT;

    @Getter
    private BuiltinClass<Boolean> BOOL;

    @Getter
    private BuiltinClass<String> STR;

    private BuiltinInstance<Boolean> BOOL_FALSE, BOOL_TRUE;
    private BuiltinInstance<NullType> NONE;

    public MemoryModel(Interpreter interpreter) {
        this.interpreter = interpreter;
        integers = new HashMap<>();
        builtinClasses = new HashMap<>();
    }

    private void initBuiltinClass() {
        // 布尔值
        BOOL = new PyBool("bool", new HashMap<>());
        builtinClasses.put("bool", BOOL);
        BOOL_FALSE = BOOL.fromValue(interpreter, false);
        BOOL_TRUE = BOOL.fromValue(interpreter, true);

        // None
        NONE_TYPE = new PyNone("NoneType", new HashMap<>());
        builtinClasses.put("NoneType", NONE_TYPE);
        NONE = NONE_TYPE.fromValue(interpreter, null);

        INTEGER = new PyInt("int", new HashMap<>());
        FLOAT = new PyFloat("float", new HashMap<>());
        STR = new PyString("str", new HashMap<>());
    }

    public boolean isNone(PyObject value) {
        return value == NONE;
    }

    public boolean isInt(PyObject value) {
        return INTEGER.is(value);
    }

    public boolean isFloat(PyObject value) {
        return FLOAT.is(value);
    }

    public boolean isBool(PyObject value) {
        return BOOL.is(value);
    }

    public boolean isStr(PyObject value) {
        return STR.is(value);
    }

    public BuiltinInstance<String> createString(String str) {
        return STR.fromValue(interpreter, str);
    }

    public BuiltinInstance<Double> getFloat(Double value) {
        return FLOAT.fromValue(interpreter, value);
    }

    public BuiltinInstance<NullType> none() {
        return NONE;
    }

    public BuiltinInstance<Boolean> boolTrue() {
        return BOOL_TRUE;
    }

    public BuiltinInstance<Boolean> boolFalse() {
        return BOOL_FALSE;
    }

    public BuiltinInstance<Boolean> boolValue(boolean value) {
        return value ? boolTrue() : boolFalse();
    }

    public BuiltinClass reigsterBuiltinClass(BuiltinClass clazz) {
        return builtinClasses.put(clazz.getName(), clazz);
    }

    public BuiltinClass getBuiltinClazz(String name) {
        return builtinClasses.getOrDefault(name, null);
    }

    public boolean hasClazz(String name) {
        return builtinClasses.containsKey(name);
    }

    public BuiltinInstance<Long> getInteger(Long integer) {
        if (integers.containsKey(integer)) return integers.get(integer);
        // TODO: 完成IntegerClass之后完成这个
        BuiltinInstance<Long> result = INTEGER.fromValue(interpreter, integer);
        integers.put(integer, result);
        return result;
    }
}

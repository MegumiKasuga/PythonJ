package edu.carole.interpreter;

import edu.carole.runtime.PyObject;
import edu.carole.runtime.clazz.BuiltinClass;
import edu.carole.runtime.clazz.builtins.*;
import edu.carole.runtime.instance.BuiltinInstance;
import lombok.Getter;

import javax.lang.model.type.NullType;
import java.util.*;

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

    @Getter
    private BuiltinClass<ArrayList> LIST;

    @Getter
    private BuiltinClass<PyObject[]> TUPLE;

    @Getter
    private BuiltinClass<HashSet> SET;

    @Getter
    private BuiltinClass<HashMap> DICT;

    private BuiltinInstance<Boolean> BOOL_FALSE, BOOL_TRUE;
    private BuiltinInstance<NullType> NONE;

    public MemoryModel(Interpreter interpreter) {
        this.interpreter = interpreter;
        integers = new HashMap<>();
        builtinClasses = new HashMap<>();
        initBuiltinClass();
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
        builtinClasses.put("int", INTEGER);
        
        FLOAT = new PyFloat("float", new HashMap<>());
        builtinClasses.put("float", FLOAT);
        
        STR = new PyString("str", new HashMap<>());
        builtinClasses.put("str", STR);
        
        LIST = new PyList("list", new HashMap<>());
        builtinClasses.put("list", LIST);

        TUPLE = new PyTuple("tuple", new HashMap<>());
        builtinClasses.put("tuple", TUPLE);

        SET = new PySet("set", new HashMap<>());
        builtinClasses.put("set", SET);

        DICT = new PyDict("dict", new HashMap<>());
        builtinClasses.put("dict", DICT);
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

    public boolean isList(PyObject value) {return LIST.is(value);}

    public boolean isTuple(PyObject value) {return TUPLE.is(value);}

    public boolean isSet(PyObject value) {return SET.is(value);}

    public boolean isDict(PyObject value) {return DICT.is(value);}

    public BuiltinInstance<String> createString(String str) {
        return STR.fromValue(interpreter, str);
    }

    public BuiltinInstance<Double> getFloat(Double value) {
        return FLOAT.fromValue(interpreter, value);
    }

    public BuiltinInstance<ArrayList> createList(ArrayList<PyObject> value) {
        return LIST.fromValue(interpreter, value);
    }

    public BuiltinInstance<PyObject[]> createTuple(PyObject[] value) {
        return TUPLE.fromValue(interpreter, value);
    }

    public BuiltinInstance<HashSet> createSet(HashSet<PyObject> value) {
        return SET.fromValue(interpreter, value);
    }

    public BuiltinInstance<HashMap> createDict(HashMap<PyObject, PyObject> value) {
        return DICT.fromValue(interpreter, value);
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

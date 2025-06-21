package edu.carole.runtime.instance;

import edu.carole.runtime.clazz.BuiltinClass;
import edu.carole.runtime.clazz.PyClass;
import lombok.Getter;
import lombok.Setter;

public class BuiltinInstance<T> extends PyInstance {

    @Setter
    @Getter
    private T value;

    @Getter
    private final Class<T> valueClass;

    public BuiltinInstance(BuiltinClass<T> pyClass, Class<T> clazz) {
        super(pyClass);
        this.valueClass = clazz;
    }

    public boolean is(BuiltinClass clazz) {
        return valueClass.equals(clazz.getValueClass());
    }

    public int hashCode() {
        BuiltinClass<T> clazz = (BuiltinClass<T>) getPyClass();
        return clazz.hashCode(this);
    }
}

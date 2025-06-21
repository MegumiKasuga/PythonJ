package edu.carole.runtime.clazz.helper;

import edu.carole.interpreter.BuiltinFunctions;
import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.PyObject;
import edu.carole.runtime.clazz.BuiltinClass;
import edu.carole.runtime.instance.BuiltinInstance;
import lombok.Getter;

public class Slice {

    private final Interpreter interpreter;

    @Getter
    private final PyObject start, end, step;
    public Slice(Interpreter interpreter, PyObject start, PyObject end, PyObject step) {
        this.interpreter = interpreter;
        this.start = start;
        this.end = end;
        this.step = step;
    }

    public long[] indices(long length) {
        long startIdx = 0;
        long stopIdx = length;
        long stepVal = 1;

        if (!interpreter.isNone(step)) {
            if (interpreter.isInt(step)) {
                stepVal = ((BuiltinInstance<Integer>) step).getValue();
                if (stepVal == 0) {
                    throw BuiltinClass.badValue(interpreter, "slice step cannot be zero");
                }
            } else {
                throw BuiltinClass.badType(interpreter, "slice step must be an integer");
            }
        }

        if (!interpreter.isNone(start)) {
            if (interpreter.isInt(start)) {
                startIdx = ((BuiltinInstance<Integer>) start).getValue();
                if (startIdx < 0) {
                    startIdx += length;
                }
                startIdx = Math.max(0, Math.min(startIdx, length));
            }  else {
                throw BuiltinClass.badType(interpreter, "slice start must be an integer");
            }
        } else {
            startIdx = stepVal > 0 ? 0 : length - 1;
        }

        if (!interpreter.isNone(end)) {
            if (interpreter.isInt(end)) {
                stopIdx = ((BuiltinInstance<Integer>) end).getValue();
                if (stopIdx < 0) {
                    stopIdx += length;
                }
                stopIdx = Math.max(-1, Math.min(stopIdx, length));
            } else {
                throw BuiltinClass.badType(interpreter, "slice stop must be an integer");
            }
        } else {
            stopIdx = stepVal > 0 ? length : -1;
        }
        return new long[]{startIdx, stopIdx, stepVal};
    }
}

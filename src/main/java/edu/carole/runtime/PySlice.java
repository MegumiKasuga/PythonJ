package edu.carole.runtime;

import java.util.*;

/**
 * Python slice object implementation
 */
public class PySlice extends PyObject {
    private final PyObject start;
    private final PyObject stop;
    private final PyObject step;
    
    public PySlice(PyObject start, PyObject stop, PyObject step) {
        this.start = start;
        this.stop = stop;
        this.step = step;
    }
    
    public PyObject getStart() { return start; }
    public PyObject getStop() { return stop; }
    public PyObject getStep() { return step; }
    
    @Override
    public String getTypeName() {
        return "slice";
    }
    
    @Override
    public String toString() {
        return "slice(" + start + ", " + stop + ", " + step + ")";
    }

    @Override
    public boolean isTruthy() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PySlice)) return false;
        PySlice other = (PySlice) obj;
        return Objects.equals(start, other.start) && 
               Objects.equals(stop, other.stop) && 
               Objects.equals(step, other.step);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(start, stop, step);
    }
    
    /**
     * Convert slice indices to actual indices for a sequence of given length
     * Returns [actualStart, actualStop, actualStep]
     */
    public int[] indices(int length) {
        int startIdx = 0;
        int stopIdx = length;
        int stepVal = 1;
        
        // Process step
        if (step != PyNone.INSTANCE) {
            if (step instanceof PyInt) {
                stepVal = (int) ((PyInt) step).getValue();
                if (stepVal == 0) {
                    throw new RuntimeException("slice step cannot be zero");
                }
            } else {
                throw new RuntimeException("slice step must be an integer");
            }
        }
        
        // Process start
        if (start != PyNone.INSTANCE) {
            if (start instanceof PyInt) {
                startIdx = (int) ((PyInt) start).getValue();
                if (startIdx < 0) {
                    startIdx += length;
                }
                startIdx = Math.max(0, Math.min(startIdx, length));
            } else {
                throw new RuntimeException("slice start must be an integer");
            }
        } else {
            // Default start based on step direction
            startIdx = stepVal > 0 ? 0 : length - 1;
        }
        
        // Process stop
        if (stop != PyNone.INSTANCE) {
            if (stop instanceof PyInt) {
                stopIdx = (int) ((PyInt) stop).getValue();
                if (stopIdx < 0) {
                    stopIdx += length;
                }
                stopIdx = Math.max(-1, Math.min(stopIdx, length));
            } else {
                throw new RuntimeException("slice stop must be an integer");
            }
        } else {
            // Default stop based on step direction
            stopIdx = stepVal > 0 ? length : -1;
        }
        
        return new int[]{startIdx, stopIdx, stepVal};
    }
}

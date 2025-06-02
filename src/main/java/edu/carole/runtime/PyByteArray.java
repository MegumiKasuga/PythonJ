package edu.carole.runtime;

import edu.carole.runtime.base.PyObjectWithMethods;
import edu.carole.runtime.registry.MethodBuilder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

/**
 * Python bytearray object implementation using method registration system
 */
public class PyByteArray extends PyObjectWithMethods {
    private byte[] value;
    
    public PyByteArray(byte[] value) {
        super();
        this.value = value;
    }
    
    public byte[] getValue() {
        return value;
    }
    
    @Override
    public String getTypeName() {
        return "bytearray";
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("bytearray(b'");
        for (byte b : value) {
            if (b >= 32 && b < 127) {
                // ASCII printable characters
                result.append((char)b);
            } else {
                // Convert to hexadecimal representation
                result.append(String.format("\\x%02x", b));
            }
        }
        result.append("')");
        return result.toString();
    }
    
    @Override
    public boolean isTruthy() {
        return value.length > 0;
    }
      @Override
    public PyObject len() {
        return new PyInt(value.length);
    }

    @Override
    public PyObject getItem(PyObject key) {
        if (key instanceof PyInt) {
            int index = (int)((PyInt)key).getValue();
            if (index < 0) {
                index += value.length;
            }
            if (index < 0 || index >= value.length) {
                throw new RuntimeException("bytearray index out of range");
            }
            return new PyInt(value[index] & 0xff);  // Convert to unsigned byte (0-255)
        } else {
            throw new RuntimeException("bytearray indices must be integers");
        }
    }
      @Override
    public void setItem(PyObject key, PyObject value) {
        if (key instanceof PyInt) {
            int index = (int)((PyInt)key).getValue();
            if (index < 0) {
                index += this.value.length;
            }
            if (index < 0 || index >= this.value.length) {
                throw new RuntimeException("bytearray index out of range");
            }
              if (value instanceof PyInt) {
                int byteValue = (int)((PyInt)value).getValue();
                if (byteValue < 0 || byteValue > 255) {
                    throw new RuntimeException("byte must be in range(0, 256)");
                }
                this.value[index] = (byte)byteValue;
            } else {
                throw new RuntimeException("can't assign " + value.getTypeName() + " to bytearray item");
            }
        } else {
            throw new RuntimeException("bytearray indices must be integers");
        }
    }
    
    @Override
    public Iterator<PyObject> iterator() {
        return new Iterator<PyObject>() {
            private int index = 0;
            
            @Override
            public boolean hasNext() {
                return index < value.length;
            }
            
            @Override
            public PyObject next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return new PyInt(value[index++] & 0xff);  // Convert to unsigned byte (0-255)
            }
        };
    }
    
    @Override
    protected void registerMethods() {
        // Iterator method
        methodRegistry.registerMethod("__iter__", MethodBuilder.noArgs(() ->
            new PyIterator(iterator(), "bytearray_iterator")
        ));
        
        // Append method
        methodRegistry.registerMethod("append", MethodBuilder.oneArg(this::append));
        
        // Extend method  
        methodRegistry.registerMethod("extend", MethodBuilder.oneArg(this::extend));
          // Decode method
        methodRegistry.registerMethod("decode", MethodBuilder.varArgs(this::decodeVarArgs));
        
        // Magic methods
        methodRegistry.registerMethod("__add__", MethodBuilder.oneArg(this::add));
        methodRegistry.registerMethod("__eq__", MethodBuilder.oneArg(this::eq));
        methodRegistry.registerMethod("__ne__", MethodBuilder.oneArg(this::ne));
        methodRegistry.registerMethod("__len__", MethodBuilder.noArgs(() ->new PyInt(this.value.length)));        methodRegistry.registerMethod("__getitem__", MethodBuilder.oneArg(this::getItem));
        methodRegistry.registerMethod("__getslice__", MethodBuilder.varArgs(args ->
                this.__getslice__(args.get(0), args.size() > 1 ? args.get(1) : PyNone.INSTANCE, args.size() > 2 ? args.get(2) : new PyInt(1))));
        methodRegistry.registerMethod("__setitem__", MethodBuilder.twoArgs(args -> {
            this.setItem(args[0], args[1]);
            return PyNone.INSTANCE;
        }));
        methodRegistry.registerMethod("__setslice__", MethodBuilder.varArgs(args ->
                this.__setslice__(args.get(0), args.size() > 1 ? args.get(1) : PyNone.INSTANCE, args.size() > 2 ? args.get(2) : new PyInt(1), args.get(args.size() - 1))));
        methodRegistry.registerMethod("__contains__", MethodBuilder.oneArg(this::contains));
        
        // Mutating methods
        methodRegistry.registerMethod("clear", MethodBuilder.noArgs(this::clear));
        methodRegistry.registerMethod("copy", MethodBuilder.noArgs(this::copy));
        methodRegistry.registerMethod("count", MethodBuilder.oneArg(this::count));
        methodRegistry.registerMethod("index", MethodBuilder.varArgs(this::indexVarArgs));
        methodRegistry.registerMethod("insert", MethodBuilder.twoArgs(args -> insert(args[0], args[1])));
        methodRegistry.registerMethod("pop", MethodBuilder.varArgs(this::popVarArgs));
        methodRegistry.registerMethod("remove", MethodBuilder.oneArg(this::remove));
        methodRegistry.registerMethod("reverse", MethodBuilder.noArgs(this::reverse));
        
        // Missing methods to complete the implementation
        methodRegistry.registerMethod("__delitem__", MethodBuilder.oneArg(this::__delitem__));
        methodRegistry.registerMethod("__mul__", MethodBuilder.oneArg(this::__mul__));
        methodRegistry.registerMethod("__repr__", MethodBuilder.noArgs(this::__repr__));
        methodRegistry.registerMethod("__str__", MethodBuilder.noArgs(this::__str__));
        methodRegistry.registerMethod("endswith", MethodBuilder.varArgs(this::endswith));
        methodRegistry.registerMethod("find", MethodBuilder.varArgs(this::find));
        methodRegistry.registerMethod("join", MethodBuilder.oneArg(this::join));
        methodRegistry.registerMethod("lstrip", MethodBuilder.varArgs(this::lstrip));
        methodRegistry.registerMethod("replace", MethodBuilder.varArgs(this::replace));
        methodRegistry.registerMethod("rfind", MethodBuilder.varArgs(this::rfind));
        methodRegistry.registerMethod("rindex", MethodBuilder.varArgs(this::rindex));
        methodRegistry.registerMethod("rsplit", MethodBuilder.varArgs(this::rsplit));
        methodRegistry.registerMethod("rstrip", MethodBuilder.varArgs(this::rstrip));
        methodRegistry.registerMethod("split", MethodBuilder.varArgs(this::split));
        methodRegistry.registerMethod("startswith", MethodBuilder.varArgs(this::startswith));
        methodRegistry.registerMethod("strip", MethodBuilder.varArgs(this::strip));
    }
    
    // Method implementations
    
    // Adapter methods for varArgs compatibility
    private PyObject decodeVarArgs(List<PyObject> args) {
        return decode(args.toArray(new PyObject[0]));
    }
    
    private PyObject indexVarArgs(List<PyObject> args) {
        return index(args.toArray(new PyObject[0]));
    }
    
    private PyObject popVarArgs(List<PyObject> args) {
        return pop(args.toArray(new PyObject[0]));
    }

    private PyObject append(PyObject item) {
        if (!(item instanceof PyInt)) {
            throw new RuntimeException("an integer is required");
        }
        
        int byteValue = (int)((PyInt)item).getValue();
        if (byteValue < 0 || byteValue > 255) {
            throw new RuntimeException("byte must be in range(0, 256)");
        }
        
        byte[] newValue = Arrays.copyOf(value, value.length + 1);
        newValue[value.length] = (byte)byteValue;
        value = newValue;
        
        return PyNone.INSTANCE;
    }
    
    private PyObject extend(PyObject iterable) {
        List<Byte> bytesToAdd = new ArrayList<>();
        
        try {
            Iterator<PyObject> iterator = iterable.iterator();
            while (iterator.hasNext()) {
                PyObject item = iterator.next();                if (!(item instanceof PyInt)) {
                    throw new RuntimeException("an integer is required");
                }
                
                int byteValue = (int)((PyInt)item).getValue();
                if (byteValue < 0 || byteValue > 255) {
                    throw new RuntimeException("byte must be in range(0, 256)");
                }
                
                bytesToAdd.add((byte)byteValue);
            }
        } catch (Exception e) {
            throw new RuntimeException("'" + iterable.getTypeName() + "' object is not iterable");
        }
        
        byte[] newValue = Arrays.copyOf(value, value.length + bytesToAdd.size());
        for (int i = 0; i < bytesToAdd.size(); i++) {
            newValue[value.length + i] = bytesToAdd.get(i);
        }
        value = newValue;
        
        return PyNone.INSTANCE;
    }
    
    private PyObject decode(PyObject[] args) {
        if (args.length > 1) {
            throw new RuntimeException("decode() takes at most 1 argument (" + args.length + " given)");
        }
        
        String encoding = "utf-8";  // Default encoding
        if (args.length == 1) {            if (args[0] instanceof PyString) {
                encoding = ((PyString)args[0]).getValue();
            } else {
                throw new RuntimeException("decode() argument 1 must be str, not " + args[0].getTypeName());
            }
        }
        
        try {
            return new PyString(new String(value, encoding));
        } catch (Exception e) {
            throw new RuntimeException("'bytearray' object cannot be decoded: " + e.getMessage());
        }
    }
    
    private PyObject add(PyObject other) {
        byte[] otherBytes;
        
        if (other instanceof PyBytes) {
            otherBytes = ((PyBytes)other).getValue();
        } else if (other instanceof PyByteArray) {
            otherBytes = ((PyByteArray)other).getValue();
        } else {
            throw new RuntimeException("can't concat bytearray to " + other.getTypeName());
        }
        
        byte[] result = new byte[value.length + otherBytes.length];
        System.arraycopy(value, 0, result, 0, value.length);
        System.arraycopy(otherBytes, 0, result, value.length, otherBytes.length);
        
        return new PyByteArray(result);
    }
    
    private PyObject eq(PyObject other) {
        byte[] otherBytes;
        
        if (other instanceof PyBytes) {
            otherBytes = ((PyBytes)other).getValue();
        } else if (other instanceof PyByteArray) {
            otherBytes = ((PyByteArray)other).getValue();
        } else {
            return PyBool.FALSE;
        }
        
        return PyBool.valueOf(Arrays.equals(value, otherBytes));
    }
    
    private PyObject ne(PyObject other) {
        PyObject eqResult = eq(other);
        return PyBool.valueOf(!((PyBool) eqResult).getValue());
    }
    
    private PyObject contains(PyObject item) {
        if (item instanceof PyInt) {
            int byteValue = (int) ((PyInt) item).getValue();
            if (byteValue < 0 || byteValue > 255) {
                return PyBool.FALSE;
            }
            for (byte b : value) {
                if ((b & 0xff) == byteValue) {
                    return PyBool.TRUE;
                }
            }
            return PyBool.FALSE;
        } else if (item instanceof PyBytes) {
            byte[] needle = ((PyBytes) item).getValue();
            return PyBool.valueOf(indexOf(needle, this.value) >= 0);
        } else if (item instanceof PyByteArray) {
            byte[] needle = ((PyByteArray) item).getValue();
            return PyBool.valueOf(indexOf(needle, this.value) >= 0);
        } else {
            throw new RuntimeException("argument should be int or bytes-like object, not '" + item.getTypeName() + "'");
        }
    }
    
    private PyObject clear() {
        this.value = new byte[0];
        return PyNone.INSTANCE;
    }
    
    private PyObject copy() {
        return new PyByteArray(Arrays.copyOf(this.value, this.value.length));
    }
    
    private PyObject count(PyObject item) {
        if (item instanceof PyInt) {
            int byteValue = (int) ((PyInt) item).getValue();
            if (byteValue < 0 || byteValue > 255) {
                return new PyInt(0);
            }
            int count = 0;
            for (byte b : value) {
                if ((b & 0xff) == byteValue) {
                    count++;
                }
            }
            return new PyInt(count);
        } else if (item instanceof PyBytes) {
            byte[] needle = ((PyBytes) item).getValue();
            return new PyInt(bytesToLong(needle, false));
        } else if (item instanceof PyByteArray) {
            byte[] needle = ((PyByteArray) item).getValue();
            return new PyInt(bytesToLong(needle, false));
        } else {
            throw new RuntimeException("argument should be int or bytes-like object, not '" + item.getTypeName() + "'");
        }
    }

    public static long bytesToLong(byte[] bytes, boolean littleEndian) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        return buffer.getLong();
    }
    
    private PyObject index(PyObject[] args) {
        if (args.length < 1 || args.length > 3) {
            throw new RuntimeException("index() takes from 1 to 3 arguments but " + args.length + " were given");
        }
        
        PyObject item = args[0];
        int start = 0;
        int end = value.length;
        
        if (args.length >= 2 && args[1] instanceof PyInt) {
            start = (int) ((PyInt) args[1]).getValue();
            if (start < 0) start += value.length;
            start = Math.max(0, start);
        }
        
        if (args.length == 3 && args[2] instanceof PyInt) {
            end = (int) ((PyInt) args[2]).getValue();
            if (end < 0) end += value.length;
            end = Math.min(value.length, end);
        }
        
        if (item instanceof PyInt) {
            int byteValue = (int) ((PyInt) item).getValue();
            if (byteValue < 0 || byteValue > 255) {
                throw new RuntimeException("ValueError: " + byteValue + " is not in bytearray");
            }
            for (int i = start; i < end; i++) {
                if ((value[i] & 0xff) == byteValue) {
                    return new PyInt(i);
                }
            }
            throw new RuntimeException("ValueError: " + byteValue + " is not in bytearray");
        } else if (item instanceof PyBytes) {
            byte[] needle = ((PyBytes) item).getValue();
            int index = indexOf(needle, start, end);
            if (index == -1) {
                throw new RuntimeException("ValueError: subsection not found");
            }
            return new PyInt(index);
        } else if (item instanceof PyByteArray) {
            byte[] needle = ((PyByteArray) item).getValue();
            int index = indexOf(needle, start, end);
            if (index == -1) {
                throw new RuntimeException("ValueError: subsection not found");
            }
            return new PyInt(index);
        } else {
            throw new RuntimeException("argument should be int or bytes-like object, not '" + item.getTypeName() + "'");
        }
    }
    
    private PyObject insert(PyObject indexObj, PyObject valueObj) {
        if (!(indexObj instanceof PyInt)) {
            throw new RuntimeException("an integer is required");
        }
        if (!(valueObj instanceof PyInt)) {
            throw new RuntimeException("an integer is required");
        }
        
        int index = (int) ((PyInt) indexObj).getValue();
        int byteValue = (int) ((PyInt) valueObj).getValue();
        
        if (byteValue < 0 || byteValue > 255) {
            throw new RuntimeException("byte must be in range(0, 256)");
        }
        
        if (index < 0) index += value.length;
        if (index < 0) index = 0;
        if (index > value.length) index = value.length;
        
        byte[] newValue = new byte[value.length + 1];
        System.arraycopy(value, 0, newValue, 0, index);
        newValue[index] = (byte) byteValue;
        System.arraycopy(value, index, newValue, index + 1, value.length - index);
        value = newValue;
        
        return PyNone.INSTANCE;
    }
    
    private PyObject pop(PyObject[] args) {
        if (args.length > 1) {
            throw new RuntimeException("pop() takes at most 1 argument (" + args.length + " given)");
        }
        
        if (value.length == 0) {
            throw new RuntimeException("pop from empty bytearray");
        }
        
        int index = value.length - 1; // Default to last element
        if (args.length == 1) {
            if (!(args[0] instanceof PyInt)) {
                throw new RuntimeException("an integer is required");
            }
            index = (int) ((PyInt) args[0]).getValue();
            if (index < 0) index += value.length;
            if (index < 0 || index >= value.length) {
                throw new RuntimeException("pop index out of range");
            }
        }
        
        int result = value[index] & 0xff;
        byte[] newValue = new byte[value.length - 1];
        System.arraycopy(value, 0, newValue, 0, index);
        System.arraycopy(value, index + 1, newValue, index, value.length - index - 1);
        value = newValue;
        
        return new PyInt(result);
    }
    
    private PyObject remove(PyObject item) {
        if (!(item instanceof PyInt)) {
            throw new RuntimeException("an integer is required");
        }
        
        int byteValue = (int) ((PyInt) item).getValue();
        if (byteValue < 0 || byteValue > 255) {
            throw new RuntimeException("ValueError: " + byteValue + " is not in bytearray");
        }
        
        for (int i = 0; i < value.length; i++) {
            if ((value[i] & 0xff) == byteValue) {
                byte[] newValue = new byte[value.length - 1];
                System.arraycopy(value, 0, newValue, 0, i);
                System.arraycopy(value, i + 1, newValue, i, value.length - i - 1);
                value = newValue;
                return PyNone.INSTANCE;
            }
        }
        
        throw new RuntimeException("ValueError: " + byteValue + " is not in bytearray");
    }
    
    private PyObject reverse() {
        for (int i = 0; i < value.length / 2; i++) {
            byte temp = value[i];
            value[i] = value[value.length - 1 - i];
            value[value.length - 1 - i] = temp;
        }
        
        return PyNone.INSTANCE;
    }
    
    @Override
    public PyObject getSlice(PyObject start, PyObject stop, PyObject step) {
        PySlice slice = new PySlice(start, stop, step);
        int[] indices = slice.indices(value.length);
        int startIdx = indices[0];
        int stopIdx = indices[1];
        int stepVal = indices[2];
        
        // Calculate the size of the result
        int size = 0;
        if (stepVal > 0) {
            for (int i = startIdx; i < stopIdx; i += stepVal) {
                size++;
            }
        } else {
            for (int i = startIdx; i > stopIdx; i += stepVal) {
                size++;
            }
        }
        
        byte[] result = new byte[size];
        int index = 0;
        
        if (stepVal > 0) {
            for (int i = startIdx; i < stopIdx; i += stepVal) {
                result[index++] = value[i];
            }
        } else {
            for (int i = startIdx; i > stopIdx; i += stepVal) {
                result[index++] = value[i];
            }
        }
        
        return new PyByteArray(result);
    }

    @Override
    public void setSlice(PyObject start, PyObject stop, PyObject step, PyObject value) {
        PySlice slice = new PySlice(start, stop, step);
        int[] indices = slice.indices(this.value.length);
        int startIdx = indices[0];
        int stopIdx = indices[1];
        int stepVal = indices[2];
        
        // Convert value to byte array
        byte[] newBytes;
        if (value instanceof PyByteArray) {
            newBytes = ((PyByteArray) value).getValue();
        } else if (value instanceof PyBytes) {
            newBytes = ((PyBytes) value).getValue();
        } else {
            throw new RuntimeException("can only assign bytes-like objects to bytearray slices");
        }
        
        // Extended slice assignment (step != 1) must be same length
        if (stepVal != 1) {
            List<Integer> sliceIndices = new ArrayList<>();
            
            if (stepVal > 0) {
                for (int i = startIdx; i < stopIdx; i += stepVal) {
                    sliceIndices.add(i);
                }
            } else {
                for (int i = startIdx; i > stopIdx; i += stepVal) {
                    sliceIndices.add(i);
                }
            }
            
            if (sliceIndices.size() != newBytes.length) {
                throw new RuntimeException("bytes length not equal to slice length");
            }
            
            for (int i = 0; i < sliceIndices.size(); i++) {
                this.value[sliceIndices.get(i)] = newBytes[i];
            }
        } else {
            // Simple slice assignment (step == 1) can change length
            // Remove the old slice elements
            byte[] before = Arrays.copyOfRange(this.value, 0, startIdx);
            byte[] after = Arrays.copyOfRange(this.value, stopIdx, this.value.length);
            
            // Create new array with new content
            byte[] result = new byte[before.length + newBytes.length + after.length];
            System.arraycopy(before, 0, result, 0, before.length);
            System.arraycopy(newBytes, 0, result, before.length, newBytes.length);
            System.arraycopy(after, 0, result, before.length + newBytes.length, after.length);
            
            this.value = result;
        }
    }

    // ...existing iterator method...
    
    // Missing method implementations
    private PyObject __delitem__(PyObject key) {
        if (key instanceof PyInt) {
            int index = (int)((PyInt)key).getValue();
            if (index < 0) {
                index += value.length;
            }
            if (index < 0 || index >= value.length) {
                throw new RuntimeException("bytearray index out of range");
            }
            
            byte[] newValue = new byte[value.length - 1];
            System.arraycopy(value, 0, newValue, 0, index);
            System.arraycopy(value, index + 1, newValue, index, value.length - index - 1);
            value = newValue;
            
            return PyNone.INSTANCE;
        } else {
            throw new RuntimeException("bytearray indices must be integers");
        }
    }
    
    private PyObject __mul__(PyObject other) {
        if (other instanceof PyInt) {
            long times = ((PyInt) other).getValue();
            if (times <= 0) {
                return new PyByteArray(new byte[0]);
            }
            
            byte[] result = new byte[(int)(value.length * times)];
            for (int i = 0; i < times; i++) {
                System.arraycopy(value, 0, result, i * value.length, value.length);
            }
            return new PyByteArray(result);
        }
        throw new RuntimeException("can't multiply sequence by non-int of type '" + other.getTypeName() + "'");
    }
    
    private PyObject __repr__() {
        return new PyString(toString());
    }
    
    private PyObject __str__() {
        return new PyString(toString());
    }
    
    private PyObject endswith(List<PyObject> args) {
        if (args.isEmpty() || args.size() > 3) {
            throw new RuntimeException("endswith() takes from 1 to 3 positional arguments but " + args.size() + " were given");
        }
        
        byte[] suffix = getBytes(args.get(0));
        int start = 0;
        int end = value.length;
        
        if (args.size() >= 2 && args.get(1) != PyNone.INSTANCE) {
            start = (int) ((PyInt) args.get(1)).getValue();
            if (start < 0) start += value.length;
            start = Math.max(0, start);
        }
        
        if (args.size() == 3 && args.get(2) != PyNone.INSTANCE) {
            end = (int) ((PyInt) args.get(2)).getValue();
            if (end < 0) end += value.length;
            end = Math.min(value.length, end);
        }
        
        if (end - start < suffix.length) {
            return PyBool.FALSE;
        }
        
        for (int i = 0; i < suffix.length; i++) {
            if (value[end - suffix.length + i] != suffix[i]) {
                return PyBool.FALSE;
            }
        }
        
        return PyBool.TRUE;
    }
    
    private PyObject find(List<PyObject> args) {
        if (args.isEmpty() || args.size() > 3) {
            throw new RuntimeException("find() takes from 1 to 3 positional arguments but " + args.size() + " were given");
        }
        
        byte[] sub = getBytes(args.get(0));
        int start = 0;
        int end = value.length;
        
        if (args.size() >= 2 && args.get(1) != PyNone.INSTANCE) {
            start = (int) ((PyInt) args.get(1)).getValue();
            if (start < 0) start += value.length;
            start = Math.max(0, start);
        }
        
        if (args.size() == 3 && args.get(2) != PyNone.INSTANCE) {
            end = (int) ((PyInt) args.get(2)).getValue();
            if (end < 0) end += value.length;
            end = Math.min(value.length, end);
        }
        
        return new PyInt(indexOf(sub, start, end));
    }

    public int indexOf(byte[] needle, int start, int end) {
        for (int i = start; i <= end - needle.length; i++) {
            boolean found = true;
            for (int j = 0; j < needle.length; j++) {
                if (value[i + j] != needle[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i;
            }
        }
        return -1; // Not found
    }

    public int indexOf(byte[] needle, byte[] haystack) {
        return indexOf(needle, 0, haystack.length);
    }
    
    private PyObject join(PyObject iterable) {
        List<byte[]> parts = new ArrayList<>();
        int totalLength = 0;
        
        Iterator<PyObject> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            PyObject item = iterator.next();
            byte[] bytes = getBytes(item);
            parts.add(bytes);
            totalLength += bytes.length;
        }
        
        if (parts.isEmpty()) {
            return new PyByteArray(new byte[0]);
        }
        
        totalLength += (parts.size() - 1) * value.length; // Add separators
        byte[] result = new byte[totalLength];
        int pos = 0;
        
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                System.arraycopy(value, 0, result, pos, value.length);
                pos += value.length;
            }
            System.arraycopy(parts.get(i), 0, result, pos, parts.get(i).length);
            pos += parts.get(i).length;
        }
        
        return new PyByteArray(result);
    }
    
    private PyObject lstrip(List<PyObject> args) {
        byte[] chars = null;
        if (!args.isEmpty() && args.get(0) != PyNone.INSTANCE) {
            chars = getBytes(args.get(0));
        }
        
        int start = 0;
        if (chars == null) {
            // Strip whitespace
            while (start < value.length && isWhitespace(value[start])) {
                start++;
            }
        } else {
            // Strip specified characters
            while (start < value.length && contains(chars, value[start])) {
                start++;
            }
        }
        
        byte[] result = new byte[value.length - start];
        System.arraycopy(value, start, result, 0, result.length);
        return new PyByteArray(result);
    }
    
    private PyObject replace(List<PyObject> args) {
        if (args.size() < 2 || args.size() > 3) {
            throw new RuntimeException("replace() takes 2 or 3 arguments (" + args.size() + " given)");
        }
        
        byte[] old = getBytes(args.get(0));
        byte[] replacement = getBytes(args.get(1));
        int count = -1;
        
        if (args.size() == 3) {
            count = (int) ((PyInt) args.get(2)).getValue();
        }
        
        List<Byte> result = new ArrayList<>();
        int i = 0;
        int replacements = 0;
        
        while (i < value.length && (count == -1 || replacements < count)) {
            if (i <= value.length - old.length) {
                boolean match = true;
                for (int j = 0; j < old.length; j++) {
                    if (value[i + j] != old[j]) {
                        match = false;
                        break;
                    }
                }
                
                if (match) {
                    for (byte b : replacement) {
                        result.add(b);
                    }
                    i += old.length;
                    replacements++;
                    continue;
                }
            }
            
            result.add(value[i]);
            i++;
        }
        
        // Add remaining bytes
        while (i < value.length) {
            result.add(value[i]);
            i++;
        }
        
        byte[] resultArray = new byte[result.size()];
        for (int j = 0; j < result.size(); j++) {
            resultArray[j] = result.get(j);
        }
        
        return new PyByteArray(resultArray);
    }
    
    private PyObject rfind(List<PyObject> args) {
        if (args.isEmpty() || args.size() > 3) {
            throw new RuntimeException("rfind() takes from 1 to 3 positional arguments but " + args.size() + " were given");
        }
        
        byte[] sub = getBytes(args.get(0));
        int start = 0;
        int end = value.length;
        
        if (args.size() >= 2 && args.get(1) != PyNone.INSTANCE) {
            start = (int) ((PyInt) args.get(1)).getValue();
            if (start < 0) start += value.length;
            start = Math.max(0, start);
        }
        
        if (args.size() == 3 && args.get(2) != PyNone.INSTANCE) {
            end = (int) ((PyInt) args.get(2)).getValue();
            if (end < 0) end += value.length;
            end = Math.min(value.length, end);
        }
        
        // Search backwards
        for (int i = end - sub.length; i >= start; i--) {
            boolean found = true;
            for (int j = 0; j < sub.length; j++) {
                if (value[i + j] != sub[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return new PyInt(i);
            }
        }
        
        return new PyInt(-1);
    }
    
    private PyObject rindex(List<PyObject> args) {
        PyObject result = rfind(args);
        int idx = (int) ((PyInt) result).getValue();
        if (idx == -1) {
            throw new RuntimeException("subsection not found");
        }
        return result;
    }
    
    // Placeholder implementations for rsplit, rstrip, split, startswith, strip
    private PyObject rsplit(List<PyObject> args) {
        // Simple implementation - could be enhanced
        return split(args);
    }
    
    private PyObject rstrip(List<PyObject> args) {
        byte[] chars = null;
        if (!args.isEmpty() && args.get(0) != PyNone.INSTANCE) {
            chars = getBytes(args.get(0));
        }
        
        int end = value.length;
        if (chars == null) {
            // Strip whitespace
            while (end > 0 && isWhitespace(value[end - 1])) {
                end--;
            }
        } else {
            // Strip specified characters
            while (end > 0 && contains(chars, value[end - 1])) {
                end--;
            }
        }
        
        byte[] result = new byte[end];
        System.arraycopy(value, 0, result, 0, end);
        return new PyByteArray(result);
    }
      private PyObject split(List<PyObject> args) {
        // Simple implementation returning a list with the original bytearray
        List<PyObject> result = new ArrayList<>();
        result.add(new PyByteArray(Arrays.copyOf(value, value.length)));
        return new PyList(result);
    }
    
    private PyObject startswith(List<PyObject> args) {
        if (args.isEmpty() || args.size() > 3) {
            throw new RuntimeException("startswith() takes from 1 to 3 positional arguments but " + args.size() + " were given");
        }
        
        byte[] prefix = getBytes(args.get(0));
        int start = 0;
        int end = value.length;
        
        if (args.size() >= 2 && args.get(1) != PyNone.INSTANCE) {
            start = (int) ((PyInt) args.get(1)).getValue();
            if (start < 0) start += value.length;
            start = Math.max(0, start);
        }
        
        if (args.size() == 3 && args.get(2) != PyNone.INSTANCE) {
            end = (int) ((PyInt) args.get(2)).getValue();
            if (end < 0) end += value.length;
            end = Math.min(value.length, end);
        }
        
        if (end - start < prefix.length) {
            return PyBool.FALSE;
        }
        
        for (int i = 0; i < prefix.length; i++) {
            if (value[start + i] != prefix[i]) {
                return PyBool.FALSE;
            }
        }
        
        return PyBool.TRUE;
    }
    
    private PyObject strip(List<PyObject> args) {
        byte[] chars = null;
        if (!args.isEmpty() && args.get(0) != PyNone.INSTANCE) {
            chars = getBytes(args.get(0));
        }
        
        int start = 0;
        int end = value.length;
        
        if (chars == null) {
            // Strip whitespace
            while (start < value.length && isWhitespace(value[start])) {
                start++;
            }
            while (end > start && isWhitespace(value[end - 1])) {
                end--;
            }
        } else {
            // Strip specified characters
            while (start < value.length && contains(chars, value[start])) {
                start++;
            }
            while (end > start && contains(chars, value[end - 1])) {
                end--;
            }
        }
        
        byte[] result = new byte[end - start];
        System.arraycopy(value, start, result, 0, result.length);
        return new PyByteArray(result);
    }
    
    // Helper methods
    private byte[] getBytes(PyObject obj) {
        if (obj instanceof PyByteArray) {
            return ((PyByteArray) obj).value;
        } else if (obj instanceof PyBytes) {
            return ((PyBytes) obj).getValue();
        } else if (obj instanceof PyInt) {
            int val = (int) ((PyInt) obj).getValue();
            if (val < 0 || val > 255) {
                throw new RuntimeException("byte must be in range(0, 256)");
            }
            return new byte[]{(byte) val};
        } else {
            throw new RuntimeException("argument should be bytes, bytearray or int");
        }
    }
    
    private boolean isWhitespace(byte b) {
        return b == ' ' || b == '\t' || b == '\n' || b == '\r' || b == '\f';
    }
    
    private boolean contains(byte[] array, byte value) {
        for (byte b : array) {
            if (b == value) {
                return true;
            }
        }
        return false;
    }

    private PyObject __getslice__(PyObject start, PyObject stop, PyObject step) {
        return this.getSlice(start, stop, step);
    }
    
    private PyObject __setslice__(PyObject start, PyObject stop, PyObject step, PyObject value) {
        this.setSlice(start, stop, step, value);
        return PyNone.INSTANCE;
    }
}
    

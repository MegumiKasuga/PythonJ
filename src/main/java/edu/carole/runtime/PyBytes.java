package edu.carole.runtime;

import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.base.PyObjectWithMethods;
import edu.carole.runtime.registry.MethodBuilder;
import java.util.*;

/**
 * Python bytes对象的实现 - 使用方法注册系统重构
 */
public class PyBytes extends PyObjectWithMethods {
    private final byte[] value;
    
    public PyBytes(byte[] value) {
        super();
        this.value = value;
    }
    
    public byte[] getValue() {
        return value;
    }
    
    /**
     * Get bytes as a List of PyObject integers (0-255)
     * Used by PyIntNew.from_bytes() method
     */
    public java.util.List<PyObject> getItems() {
        java.util.List<PyObject> items = new java.util.ArrayList<>();
        for (byte b : value) {
            items.add(new PyInt(b & 0xff));  // Convert to unsigned byte (0-255)
        }
        return items;
    }
    
    @Override
    public String getTypeName() {
        return "bytes";
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("b'");
        for (byte b : value) {
            if (b >= 32 && b < 127) {
                // ASCII可打印字符
                result.append((char)b);
            } else {
                // 转换为十六进制表示
                result.append(String.format("\\x%02x", b));
            }
        }
        result.append("'");
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
                throw new RuntimeException("bytes index out of range");
            }
            return new PyInt(value[index] & 0xff);  // Convert to unsigned byte (0-255)
        } else {
            throw new RuntimeException("bytes indices must be integers");
        }
    }
    
    @Override
    public Iterator<PyObject> iterator(Interpreter interpreter) {
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
        // 注册迭代器方法
        methodRegistry.registerMethod("__iter__", MethodBuilder.noArgs(inter ->
            new PyIterator(iterator(inter), "bytes_iterator")));
          // 注册解码方法
        methodRegistry.registerMethod("decode", MethodBuilder.varArgs(args -> {
            String encoding = "utf-8";  // 默认编码
            if (args.size() == 1) {
                if (args.get(0) instanceof PyString) {
                    encoding = ((PyString)args.get(0)).getValue();
                } else {
                    throw new RuntimeException("decode() argument 1 must be str, not " + args.get(0).getTypeName());
                }
            } else if (args.size() > 1) {
                throw new RuntimeException("decode() takes at most 1 argument (" + args.size() + " given)");
            }
            
            try {
                return new PyString(new String(value, encoding));
            } catch (Exception e) {
                throw new RuntimeException("'bytes' object cannot be decoded: " + e.getMessage());
            }
        }));
        
        // 注册添加方法
        methodRegistry.registerMethod("__add__", MethodBuilder.oneArg(other -> {
            if (!(other instanceof PyBytes)) {
                throw new RuntimeException("can't concat bytes to " + other.getTypeName());
            }
            
            byte[] otherBytes = ((PyBytes)other).getValue();
            byte[] result = new byte[value.length + otherBytes.length];
            System.arraycopy(value, 0, result, 0, value.length);
            System.arraycopy(otherBytes, 0, result, value.length, otherBytes.length);
            
            return new PyBytes(result);
        }));
        
        // 注册相等比较方法
        methodRegistry.registerMethod("__eq__", MethodBuilder.oneArg(other -> {
            if (!(other instanceof PyBytes)) {
                return PyBool.FALSE;
            }
            return PyBool.valueOf(Arrays.equals(value, ((PyBytes)other).getValue()));
        }));
          // 注册不相等比较方法
        methodRegistry.registerMethod("__ne__", MethodBuilder.oneArg((other, inter) -> {
            PyObject eqResult = methodRegistry.getMethod("__eq__").call(List.of(other), inter);
            return PyBool.valueOf(!((PyBool) eqResult).getValue());
        }));
        
        // 注册其他比较方法
        methodRegistry.registerMethod("__lt__", MethodBuilder.oneArg(this::__lt__));
        methodRegistry.registerMethod("__le__", MethodBuilder.oneArg(this::__le__));
        methodRegistry.registerMethod("__gt__", MethodBuilder.oneArg(this::__gt__));
        methodRegistry.registerMethod("__ge__", MethodBuilder.oneArg(this::__ge__));
          // 注册长度方法
        methodRegistry.registerMethod("__len__", MethodBuilder.noArgs(() ->
            new PyInt(this.value.length)));
          // 注册获取项方法
        methodRegistry.registerMethod("__getitem__", MethodBuilder.oneArg(this::getItem));
        methodRegistry.registerMethod("__getslice__", MethodBuilder.varArgs(args ->
                this.__getslice__(args.get(0), args.size() > 1 ? args.get(1) : PyNone.INSTANCE, args.size() > 2 ? args.get(2) : new PyInt(1))));
          // 注册包含检查方法
        methodRegistry.registerMethod("__contains__", MethodBuilder.oneArg(item -> {
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
                return PyBool.valueOf(indexOf(needle) >= 0);
            } else {
                throw new RuntimeException("argument should be int or bytes-like object, not '" + item.getTypeName() + "'");
            }
        }));
          // 注册乘法方法
        methodRegistry.registerMethod("__mul__", MethodBuilder.oneArg(other -> {
            if (other instanceof PyInt) {
                long times = ((PyInt) other).getValue();
                if (times <= 0) {
                    return new PyBytes(new byte[0]);
                }
                byte[] result = new byte[(int)(value.length * times)];
                for (int i = 0; i < times; i++) {
                    System.arraycopy(value, 0, result, i * value.length, value.length);
                }
                return new PyBytes(result);
            } else {
                throw new RuntimeException("can't multiply sequence by non-int of type '" + other.getTypeName() + "'");
            }
        }));
        
        // 注册查找方法
        methodRegistry.registerMethod("find", MethodBuilder.varArgs(args -> {
            if (args.isEmpty() || args.size() > 3) {
                throw new RuntimeException("find() takes from 1 to 3 arguments but " + args.size() + " were given");
            }
            
            if (!(args.get(0) instanceof PyBytes)) {
                throw new RuntimeException("a bytes-like object is required, not '" + args.get(0).getTypeName() + "'");
            }
            
            byte[] needle = ((PyBytes) args.get(0)).getValue();
            int start = 0;
            int end = value.length;
              if (args.size() >= 2 && args.get(1) instanceof PyInt) {
                start = (int) ((PyInt) args.get(1)).getValue();
                if (start < 0) start += value.length;
                start = Math.max(0, start);
            }
            
            if (args.size() == 3 && args.get(2) instanceof PyInt) {
                end = (int) ((PyInt) args.get(2)).getValue();
                if (end < 0) end += value.length;
                end = Math.min(value.length, end);
            }
            
            int index = indexOf(needle, start, end);
            return new PyInt(index);
        }));
        
        // 注册计数方法
        methodRegistry.registerMethod("count", MethodBuilder.oneArg(arg -> {
            if (!(arg instanceof PyBytes)) {
                throw new RuntimeException("a bytes-like object is required, not '" + arg.getTypeName() + "'");
            }
              byte[] needle = ((PyBytes) arg).getValue();
            if (needle.length == 0) {
                return new PyInt(value.length + 1);
            }
            
            int count = 0;
            int start = 0;
            while (start <= value.length - needle.length) {
                int index = indexOf(needle, start, value.length);
                if (index == -1) break;
                count++;
                start = index + needle.length;
            }
            
            return new PyInt(count);
        }));
        
        // 注册以...开始方法
        methodRegistry.registerMethod("startswith", MethodBuilder.oneArg(arg -> {
            if (!(arg instanceof PyBytes)) {
                throw new RuntimeException("startswith first arg must be bytes, not " + arg.getTypeName());
            }
            
            byte[] prefix = ((PyBytes) arg).getValue();
            if (prefix.length > value.length) {
                return PyBool.FALSE;
            }
            
            for (int i = 0; i < prefix.length; i++) {
                if (value[i] != prefix[i]) {
                    return PyBool.FALSE;
                }
            }
            
            return PyBool.TRUE;
        }));
        
        // 注册以...结束方法
        methodRegistry.registerMethod("endswith", MethodBuilder.oneArg(arg -> {
            if (!(arg instanceof PyBytes)) {
                throw new RuntimeException("endswith first arg must be bytes, not " + arg.getTypeName());
            }
            
            byte[] suffix = ((PyBytes) arg).getValue();
            if (suffix.length > value.length) {
                return PyBool.FALSE;
            }
            
            int startPos = value.length - suffix.length;
            for (int i = 0; i < suffix.length; i++) {
                if (value[startPos + i] != suffix[i]) {
                    return PyBool.FALSE;
                }
            }
            
            return PyBool.TRUE;
        }));
          // 注册表示方法
        methodRegistry.registerMethod("__repr__", MethodBuilder.noArgs(() ->
            new PyString(this.toString())));
            
        methodRegistry.registerMethod("__str__", MethodBuilder.noArgs(() ->
            new PyString(this.toString())));
        
        // 注册哈希方法
        methodRegistry.registerMethod("__hash__", MethodBuilder.noArgs(() ->
            new PyInt(Arrays.hashCode(value))));
        
        // 注册布尔值方法
        methodRegistry.registerMethod("__bool__", MethodBuilder.noArgs(() ->
            PyBool.valueOf(value.length > 0)));
            
        // 注册复制方法（bytes是不可变的，所以返回自身）
        methodRegistry.registerMethod("copy", MethodBuilder.noArgs(() -> this));
        
        // 注册右乘法方法
        methodRegistry.registerMethod("__rmul__", MethodBuilder.oneArg((other, inter) -> {
            // 右乘法与左乘法相同
            return methodRegistry.getMethod("__mul__").call(List.of(other), inter);
        }));
        
        // 注册替换方法
        methodRegistry.registerMethod("replace", MethodBuilder.varArgs(args -> {
            if (args.size() < 2 || args.size() > 3) {
                throw new RuntimeException("replace expected 2 to 3 arguments, got " + args.size());
            }
            
            if (!(args.get(0) instanceof PyBytes) || !(args.get(1) instanceof PyBytes)) {
                throw new RuntimeException("replace() arguments must be bytes");
            }
            
            byte[] oldBytes = ((PyBytes) args.get(0)).getValue();
            byte[] newBytes = ((PyBytes) args.get(1)).getValue();
            int maxReplace = -1; // 默认全部替换
              if (args.size() == 3) {
                if (!(args.get(2) instanceof PyInt)) {
                    throw new RuntimeException("'int' object cannot be interpreted as an integer");
                }
                maxReplace = (int) ((PyInt) args.get(2)).getValue();
                if (maxReplace < 0) {
                    return this; // 不替换
                }
            }
            
            if (oldBytes.length == 0) {
                // 如果旧字节数组为空，在每个位置插入新字节数组
                List<Byte> result = new ArrayList<>();
                int insertCount = 0;
                for (int i = 0; i <= value.length && (maxReplace == -1 || insertCount < maxReplace); i++) {
                    for (byte b : newBytes) {
                        result.add(b);
                    }
                    insertCount++;
                    if (i < value.length) {
                        result.add(value[i]);
                    }
                }
                // 如果还有剩余的原始字节，添加它们
                for (int i = insertCount - 1; i < value.length; i++) {
                    result.add(value[i]);
                }
                
                byte[] resultArray = new byte[result.size()];
                for (int i = 0; i < result.size(); i++) {
                    resultArray[i] = result.get(i);
                }
                return new PyBytes(resultArray);
            }
            
            List<Byte> result = new ArrayList<>();
            int pos = 0;
            int replaceCount = 0;
            
            while (pos < value.length && (maxReplace == -1 || replaceCount < maxReplace)) {
                int foundIndex = indexOf(oldBytes, pos, value.length);
                if (foundIndex == -1) {
                    break;
                }
                
                // 添加查找位置之前的字节
                for (int i = pos; i < foundIndex; i++) {
                    result.add(value[i]);
                }
                
                // 添加替换字节
                for (byte b : newBytes) {
                    result.add(b);
                }
                
                pos = foundIndex + oldBytes.length;
                replaceCount++;
            }
            
            // 添加剩余的字节
            for (int i = pos; i < value.length; i++) {
                result.add(value[i]);
            }
            
            byte[] resultArray = new byte[result.size()];
            for (int i = 0; i < result.size(); i++) {
                resultArray[i] = result.get(i);
            }
            return new PyBytes(resultArray);
        }));
        
        // 注册分割方法
        methodRegistry.registerMethod("split", MethodBuilder.varArgs(args -> {
            if (args.size() > 2) {
                throw new RuntimeException("split expected at most 2 arguments, got " + args.size());
            }
            
            byte[] sep = null;
            int maxSplit = -1;
            
            if (!args.isEmpty() && args.get(0) != PyNone.INSTANCE) {
                if (!(args.get(0) instanceof PyBytes)) {
                    throw new RuntimeException("sep must be bytes or None, not " + args.get(0).getTypeName());
                }
                sep = ((PyBytes) args.get(0)).getValue();
            }
              if (args.size() == 2) {
                if (!(args.get(1) instanceof PyInt)) {
                    throw new RuntimeException("maxsplit must be an integer");
                }
                maxSplit = (int) ((PyInt) args.get(1)).getValue();
            }
            
            List<PyObject> result = new ArrayList<>();
            
            if (sep == null) {
                // 按空白分割（实际上字节没有空白的概念，这里简单处理）
                if (value.length == 0) {
                    return new PyList(result);
                }
                result.add(this);
                return new PyList(result);
            }
            
            if (sep.length == 0) {
                throw new RuntimeException("empty separator");
            }
            
            int start = 0;
            int splitCount = 0;
            
            while (start < value.length && (maxSplit == -1 || splitCount < maxSplit)) {
                int foundIndex = indexOf(sep, start, value.length);
                if (foundIndex == -1) {
                    break;
                }
                
                // 添加分隔符之前的部分
                byte[] part = new byte[foundIndex - start];
                System.arraycopy(value, start, part, 0, foundIndex - start);
                result.add(new PyBytes(part));
                
                start = foundIndex + sep.length;
                splitCount++;
            }
              // 添加剩余部分
            byte[] remaining = new byte[value.length - start];
            System.arraycopy(value, start, remaining, 0, value.length - start);
            result.add(new PyBytes(remaining));
            
            return new PyList(result);
        }));
        
        // 注册连接方法
        methodRegistry.registerMethod("join", MethodBuilder.oneArg(arg -> {
            if (!(arg instanceof PyList list)) {
                throw new RuntimeException("can only join an iterable");
            }

            List<PyObject> items = list.getElements();
            
            if (items.isEmpty()) {
                return new PyBytes(new byte[0]);
            }
            
            // 检查所有项都是bytes类型
            for (PyObject item : items) {
                if (!(item instanceof PyBytes)) {
                    throw new RuntimeException("sequence item: expected bytes, " + item.getTypeName() + " found");
                }
            }
            
            // 计算总长度
            int totalLength = 0;
            for (int i = 0; i < items.size(); i++) {
                if (i > 0) {
                    totalLength += value.length; // 分隔符长度
                }
                totalLength += ((PyBytes) items.get(i)).getValue().length;
            }
            
            // 构建结果
            byte[] result = new byte[totalLength];
            int pos = 0;
            
            for (int i = 0; i < items.size(); i++) {
                if (i > 0) {
                    // 添加分隔符
                    System.arraycopy(value, 0, result, pos, value.length);
                    pos += value.length;
                }
                
                // 添加项目
                byte[] itemBytes = ((PyBytes) items.get(i)).getValue();
                System.arraycopy(itemBytes, 0, result, pos, itemBytes.length);
                pos += itemBytes.length;
            }
              return new PyBytes(result);
        }));
        
        // 注册字符串处理方法
        methodRegistry.registerMethod("capitalize", MethodBuilder.noArgs(this::capitalize));
        methodRegistry.registerMethod("center", MethodBuilder.varArgs(this::center));
        methodRegistry.registerMethod("fromhex", MethodBuilder.oneArg(this::fromhex));
        methodRegistry.registerMethod("hex", MethodBuilder.noArgs(this::hex));
        methodRegistry.registerMethod("index", MethodBuilder.varArgs(this::index));
        methodRegistry.registerMethod("ljust", MethodBuilder.varArgs(this::ljust));
        methodRegistry.registerMethod("lower", MethodBuilder.noArgs(this::lower));
        methodRegistry.registerMethod("lstrip", MethodBuilder.varArgs(this::lstrip));
        methodRegistry.registerMethod("rfind", MethodBuilder.varArgs(this::rfind));
        methodRegistry.registerMethod("rindex", MethodBuilder.varArgs(this::rindex));
        methodRegistry.registerMethod("rjust", MethodBuilder.varArgs(this::rjust));
        methodRegistry.registerMethod("rsplit", MethodBuilder.varArgs(this::rsplit));
        methodRegistry.registerMethod("rstrip", MethodBuilder.varArgs(this::rstrip));
        methodRegistry.registerMethod("strip", MethodBuilder.varArgs(this::strip));
        methodRegistry.registerMethod("title", MethodBuilder.noArgs(this::title));
        methodRegistry.registerMethod("upper", MethodBuilder.noArgs(this::upper));
        methodRegistry.registerMethod("zfill", MethodBuilder.oneArg(this::zfill));
    }
    
    private PyObject __getslice__(PyObject start, PyObject stop, PyObject step) {
        return this.getSlice(start, stop, step);
    }
    
    // 比较方法实现
    private PyObject __lt__(PyObject other) {
        if (other instanceof PyBytes) {
            byte[] otherBytes = ((PyBytes) other).getValue();
            return PyBool.valueOf(compareBytes(value, otherBytes) < 0);
        }
        throw new RuntimeException("'<' not supported between instances of 'bytes' and '" + other.getTypeName() + "'");
    }
    
    private PyObject __le__(PyObject other) {
        if (other instanceof PyBytes) {
            byte[] otherBytes = ((PyBytes) other).getValue();
            return PyBool.valueOf(compareBytes(value, otherBytes) <= 0);
        }
        throw new RuntimeException("'<=' not supported between instances of 'bytes' and '" + other.getTypeName() + "'");
    }
    
    private PyObject __gt__(PyObject other) {
        if (other instanceof PyBytes) {
            byte[] otherBytes = ((PyBytes) other).getValue();
            return PyBool.valueOf(compareBytes(value, otherBytes) > 0);
        }
        throw new RuntimeException("'>' not supported between instances of 'bytes' and '" + other.getTypeName() + "'");
    }
    
    private PyObject __ge__(PyObject other) {
        if (other instanceof PyBytes) {
            byte[] otherBytes = ((PyBytes) other).getValue();
            return PyBool.valueOf(compareBytes(value, otherBytes) >= 0);
        }
        throw new RuntimeException("'>=' not supported between instances of 'bytes' and '" + other.getTypeName() + "'");
    }
    
    // 字符串方法实现
    private PyObject capitalize() {
        if (value.length == 0) {
            return this;
        }
        
        byte[] result = new byte[value.length];
        System.arraycopy(value, 0, result, 0, value.length);
        
        // 首字母大写
        if (result[0] >= 'a' && result[0] <= 'z') {
            result[0] = (byte)(result[0] - 32);
        }
        
        // 其余字母小写
        for (int i = 1; i < result.length; i++) {
            if (result[i] >= 'A' && result[i] <= 'Z') {
                result[i] = (byte)(result[i] + 32);
            }
        }
        
        return new PyBytes(result);
    }
    
    private PyObject center(List<PyObject> args) {
        if (args.isEmpty()) {
            throw new RuntimeException("center() takes at least 1 argument (0 given)");
        }
          if (!(args.get(0) instanceof PyInt)) {
            throw new RuntimeException("center() argument 1 must be integer");
        }
        
        int width = (int)((PyInt)args.get(0)).getValue();
        byte fillByte = ' ';
        
        if (args.size() > 1) {
            if (!(args.get(1) instanceof PyBytes) || ((PyBytes)args.get(1)).getValue().length != 1) {
                throw new RuntimeException("center() argument 2 must be a byte string of length 1");
            }
            fillByte = ((PyBytes)args.get(1)).getValue()[0];
        }
        
        if (width <= value.length) {
            return this;
        }
        
        int leftPadding = (width - value.length) / 2;
        int rightPadding = width - value.length - leftPadding;
        
        byte[] result = new byte[width];
        Arrays.fill(result, 0, leftPadding, fillByte);
        System.arraycopy(value, 0, result, leftPadding, value.length);
        Arrays.fill(result, leftPadding + value.length, width, fillByte);
        
        return new PyBytes(result);
    }
    
    private PyObject hex() {
        StringBuilder sb = new StringBuilder();
        for (byte b : value) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return new PyString(sb.toString());
    }
    
    private PyObject fromhex(PyObject arg) {
        if (!(arg instanceof PyString)) {
            throw new RuntimeException("fromhex() argument must be a string");
        }
        
        String hexString = ((PyString)arg).getValue().replaceAll("\\s+", "");
        
        if (hexString.length() % 2 != 0) {
            throw new RuntimeException("fromhex() argument length must be a multiple of 2");
        }
        
        byte[] result = new byte[hexString.length() / 2];
        
        for (int i = 0; i < hexString.length(); i += 2) {
            try {
                result[i/2] = (byte)Integer.parseInt(hexString.substring(i, i+2), 16);
            } catch (NumberFormatException e) {
                throw new RuntimeException("non-hexadecimal number found in fromhex() arg");
            }
        }
        
        return new PyBytes(result);
    }
    
    private PyObject index(List<PyObject> args, Interpreter inter) {
        // 类似于find，但没找到时抛出异常
        if (args.isEmpty()) {
            throw new RuntimeException("index() takes at least 1 argument (0 given)");
        }
          PyObject findResult = methodRegistry.getMethod("find").call(args, inter);
        if (findResult instanceof PyInt && ((PyInt)findResult).getValue() == -1) {
            throw new RuntimeException("substring not found");
        }
        
        return findResult;
    }
    
    // Helper methods
    private int indexOf(byte[] needle) {
        return indexOf(needle, 0, value.length);
    }
      private int indexOf(byte[] needle, int start, int end) {
        if (needle.length == 0) {
            return start;
        }
        
        if (start < 0) start = 0;
        if (end > value.length) end = value.length;
        
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
        return -1;
    }
    
    // 比较两个字节数组
    private int compareBytes(byte[] bytes1, byte[] bytes2) {
        int minLength = Math.min(bytes1.length, bytes2.length);
        for (int i = 0; i < minLength; i++) {
            int byte1 = bytes1[i] & 0xff;
            int byte2 = bytes2[i] & 0xff;
            if (byte1 != byte2) {
                return byte1 - byte2;
            }
        }
        return bytes1.length - bytes2.length;
    }
    
    private PyObject ljust(List<PyObject> args) {
        if (args.isEmpty()) {
            throw new RuntimeException("ljust() takes at least 1 argument (0 given)");
        }
          if (!(args.get(0) instanceof PyInt)) {
            throw new RuntimeException("ljust() argument 1 must be integer");
        }
        
        int width = (int)((PyInt)args.get(0)).getValue();
        byte fillByte = ' ';
        
        if (args.size() > 1) {
            if (!(args.get(1) instanceof PyBytes) || ((PyBytes)args.get(1)).getValue().length != 1) {
                throw new RuntimeException("ljust() argument 2 must be a byte string of length 1");
            }
            fillByte = ((PyBytes)args.get(1)).getValue()[0];
        }
        
        if (width <= value.length) {
            return this;
        }
        
        byte[] result = new byte[width];
        System.arraycopy(value, 0, result, 0, value.length);
        Arrays.fill(result, value.length, width, fillByte);
        
        return new PyBytes(result);
    }
    
    private PyObject lower() {
        byte[] result = new byte[value.length];
        
        for (int i = 0; i < value.length; i++) {
            if (value[i] >= 'A' && value[i] <= 'Z') {
                result[i] = (byte)(value[i] + 32);
            } else {
                result[i] = value[i];
            }
        }
        
        return new PyBytes(result);
    }
    
    private PyObject lstrip(List<PyObject> args) {
        byte[] chars = null;
        
        if (!args.isEmpty() && args.get(0) != PyNone.INSTANCE) {
            if (!(args.get(0) instanceof PyBytes)) {
                throw new RuntimeException("lstrip() argument must be bytes");
            }
            chars = ((PyBytes)args.get(0)).getValue();
        }
        
        int start = 0;
        while (start < value.length) {
            boolean shouldStrip = false;
            
            if (chars == null) {
                // 默认去除空白
                shouldStrip = (value[start] <= ' ');
            } else {
                for (byte c : chars) {
                    if (value[start] == c) {
                        shouldStrip = true;
                        break;
                    }
                }
            }
            
            if (!shouldStrip) {
                break;
            }
            
            start++;
        }
        
        if (start == 0) {
            return this;
        }
        
        byte[] result = new byte[value.length - start];
        System.arraycopy(value, start, result, 0, value.length - start);
        
        return new PyBytes(result);
    }
    
    private PyObject rfind(List<PyObject> args) {
        if (args.isEmpty()) {
            throw new RuntimeException("rfind() takes at least 1 argument (0 given)");
        }
        
        if (!(args.get(0) instanceof PyBytes)) {
            throw new RuntimeException("rfind() argument must be bytes");
        }
        
        byte[] subBytes = ((PyBytes)args.get(0)).getValue();
        int start = 0;
        int end = value.length;
        
        if (args.size() > 1 && args.get(1) != PyNone.INSTANCE) {
            if (!(args.get(1) instanceof PyInt)) {
                throw new RuntimeException("rfind() start argument must be integer");
            }
            start = (int)((PyInt)args.get(1)).getValue();
        }
        
        if (args.size() > 2 && args.get(2) != PyNone.INSTANCE) {
            if (!(args.get(2) instanceof PyInt)) {
                throw new RuntimeException("rfind() end argument must be integer");
            }
            end = (int)((PyInt)args.get(2)).getValue();
        }
        
        // 调整索引
        if (start < 0) start = Math.max(0, value.length + start);
        if (end < 0) end = Math.max(0, value.length + end);
        if (end > value.length) end = value.length;
        
        // 特殊情况
        if (subBytes.length == 0) {
            return new PyInt(end);
        }
        
        if (start >= end || subBytes.length > end - start) {
            return new PyInt(-1);
        }
        
        // 从后往前搜索
        for (int i = end - subBytes.length; i >= start; i--) {
            boolean found = true;
            for (int j = 0; j < subBytes.length; j++) {
                if (value[i + j] != subBytes[j]) {
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
        // 类似于rfind，但没找到时抛出异常
        if (args.isEmpty()) {
            throw new RuntimeException("rindex() takes at least 1 argument (0 given)");
        }
        
        PyObject findResult = rfind(args);
        if (findResult instanceof PyInt && ((PyInt)findResult).getValue() == -1) {
            throw new RuntimeException("substring not found");
        }
        
        return findResult;
    }
    
    private PyObject rjust(List<PyObject> args) {
        if (args.isEmpty()) {
            throw new RuntimeException("rjust() takes at least 1 argument (0 given)");
        }
        
        if (!(args.get(0) instanceof PyInt)) {
            throw new RuntimeException("rjust() argument 1 must be integer");
        }
        
        int width = (int)((PyInt)args.get(0)).getValue();
        byte fillByte = ' ';
        
        if (args.size() > 1) {
            if (!(args.get(1) instanceof PyBytes) || ((PyBytes)args.get(1)).getValue().length != 1) {
                throw new RuntimeException("rjust() argument 2 must be a byte string of length 1");
            }
            fillByte = ((PyBytes)args.get(1)).getValue()[0];
        }
        
        if (width <= value.length) {
            return this;
        }
        
        int padding = width - value.length;
        byte[] result = new byte[width];
        Arrays.fill(result, 0, padding, fillByte);
        System.arraycopy(value, 0, result, padding, value.length);
        
        return new PyBytes(result);
    }
    
    private PyObject rsplit(List<PyObject> args) {
        if (args.size() > 2) {
            throw new RuntimeException("rsplit expected at most 2 arguments, got " + args.size());
        }
        
        byte[] sep = null;
        int maxSplit = -1;
        
        if (!args.isEmpty() && args.get(0) != PyNone.INSTANCE) {
            if (!(args.get(0) instanceof PyBytes)) {
                throw new RuntimeException("sep must be bytes or None, not " + args.get(0).getTypeName());
            }
            sep = ((PyBytes) args.get(0)).getValue();
        }
        
        if (args.size() == 2) {
            if (!(args.get(1) instanceof PyInt)) {
                throw new RuntimeException("maxsplit must be an integer");
            }
            maxSplit = (int) ((PyInt) args.get(1)).getValue();
        }
        
        List<PyObject> result = new ArrayList<>();
        
        if (sep == null) {            // 按空白分割
            if (value.length == 0) {
                return new PyList(result);
            }
            result.add(this);
            return new PyList(result);
        }
        
        if (sep.length == 0) {
            throw new RuntimeException("empty separator");
        }
        
        // 找到所有分割点
        List<Integer> splitPoints = new ArrayList<>();
        int pos = 0;
        
        while (pos <= value.length - sep.length) {
            int foundIndex = indexOf(sep, pos, value.length);
            if (foundIndex == -1) {
                break;
            }
            splitPoints.add(foundIndex);
            pos = foundIndex + sep.length;
        }
        
        // 从后往前应用maxSplit限制
        if (maxSplit >= 0 && splitPoints.size() > maxSplit) {
            splitPoints = splitPoints.subList(splitPoints.size() - maxSplit, splitPoints.size());
        }
        
        // 创建结果
        if (splitPoints.isEmpty()) {
            result.add(this);
        } else {
            int start = 0;
            
            for (int point : splitPoints) {
                byte[] part = new byte[point - start];
                System.arraycopy(value, start, part, 0, point - start);
                result.add(new PyBytes(part));
                start = point + sep.length;
            }
            
            // 添加最后一部分
            byte[] lastPart = new byte[value.length - start];
            System.arraycopy(value, start, lastPart, 0, value.length - start);            result.add(new PyBytes(lastPart));
        }
        
        return new PyList(result);
    }
    
    private PyObject rstrip(List<PyObject> args) {
        byte[] chars = null;
        
        if (!args.isEmpty() && args.get(0) != PyNone.INSTANCE) {
            if (!(args.get(0) instanceof PyBytes)) {
                throw new RuntimeException("rstrip() argument must be bytes");
            }
            chars = ((PyBytes)args.get(0)).getValue();
        }
        
        int end = value.length;
        while (end > 0) {
            boolean shouldStrip = false;
            
            if (chars == null) {
                // 默认去除空白
                shouldStrip = (value[end - 1] <= ' ');
            } else {
                for (byte c : chars) {
                    if (value[end - 1] == c) {
                        shouldStrip = true;
                        break;
                    }
                }
            }
            
            if (!shouldStrip) {
                break;
            }
            
            end--;
        }
        
        if (end == value.length) {
            return this;
        }
        
        byte[] result = new byte[end];
        System.arraycopy(value, 0, result, 0, end);
        
        return new PyBytes(result);
    }
    
    private PyObject strip(List<PyObject> args) {
        PyObject lstripped = lstrip(args);
        if (lstripped instanceof PyBytes) {
            return ((PyBytes)lstripped).rstrip(args);
        }
        return lstripped;
    }
    
    private PyObject title() {
        if (value.length == 0) {
            return this;
        }
        
        byte[] result = new byte[value.length];
        boolean prevWasLetter = false;
        
        for (int i = 0; i < value.length; i++) {
            byte b = value[i];
            boolean isLetter = (b >= 'a' && b <= 'z') || (b >= 'A' && b <= 'Z');
            
            if (isLetter) {
                if (!prevWasLetter) {
                    // 首字母大写
                    if (b >= 'a' && b <= 'z') {
                        result[i] = (byte)(b - 32);
                    } else {
                        result[i] = b;
                    }
                } else {
                    // 非首字母小写
                    if (b >= 'A' && b <= 'Z') {
                        result[i] = (byte)(b + 32);
                    } else {
                        result[i] = b;
                    }
                }
            } else {
                result[i] = b;
            }
            
            prevWasLetter = isLetter;
        }
        
        return new PyBytes(result);
    }
    
    private PyObject upper() {
        byte[] result = new byte[value.length];
        
        for (int i = 0; i < value.length; i++) {
            if (value[i] >= 'a' && value[i] <= 'z') {
                result[i] = (byte)(value[i] - 32);
            } else {
                result[i] = value[i];
            }
        }
        
        return new PyBytes(result);
    }
    
    private PyObject zfill(PyObject arg) {
        if (!(arg instanceof PyInt)) {
            throw new RuntimeException("zfill() argument must be integer");
        }
        
        int width = (int)((PyInt)arg).getValue();
        
        if (width <= value.length) {
            return this;
        }
        
        byte[] result = new byte[width];
        Arrays.fill(result, 0, width - value.length, (byte)'0');
        System.arraycopy(value, 0, result, width - value.length, value.length);
        
        return new PyBytes(result);
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
        
        return new PyBytes(result);
    }
}

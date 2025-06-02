package edu.carole.runtime;

import edu.carole.runtime.base.PyObjectWithMethods;
import edu.carole.runtime.registry.MethodBuilder;
import java.util.*;

/**
 * Python字符串类型 - 使用方法注册系统重构
 */
public class PyString extends PyObjectWithMethods {
    private final String value;
    
    public PyString(String value) {
        super();
        this.value = value;
    }
    
    public String getValue() { return value; }
    
    @Override
    public String getTypeName() { return "str"; }
    
    @Override
    public String toString() { return value; }
    
    @Override
    public boolean isTruthy() { return !value.isEmpty(); }
    
    @Override    public PyObject len() {
        return new PyInt(value.length());
    }
      @Override
    public PyObject getItem(PyObject key) {
        if (!(key instanceof PyInt)) {
            throw new RuntimeException("string indices must be integers");
        }
        int index = (int) ((PyInt) key).getValue();
        if (index < 0) index += value.length();
        if (index < 0 || index >= value.length()) {
            throw new RuntimeException("string index out of range");
        }
        return new PyString(String.valueOf(value.charAt(index)));
    }
    
    @Override
    public PyObject getSlice(PyObject start, PyObject stop, PyObject step) {
        PySlice slice = new PySlice(start, stop, step);
        int[] indices = slice.indices(value.length());
        int startIdx = indices[0];
        int stopIdx = indices[1];
        int stepVal = indices[2];
        
        StringBuilder result = new StringBuilder();
        
        if (stepVal > 0) {
            for (int i = startIdx; i < stopIdx; i += stepVal) {
                result.append(value.charAt(i));
            }
        } else {
            for (int i = startIdx; i > stopIdx; i += stepVal) {
                result.append(value.charAt(i));
            }
        }
        
        return new PyString(result.toString());
    }
    
    @Override
    public Iterator<PyObject> iterator() {
        return new Iterator<PyObject>() {
            private int index = 0;
            
            @Override
            public boolean hasNext() {
                return index < value.length();
            }
            
            @Override
            public PyObject next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return new PyString(String.valueOf(value.charAt(index++)));
            }
        };
    }
    
    @Override
    protected void registerMethods() {
        // 基本方法
        methodRegistry.registerMethod("__len__", MethodBuilder.noArgs(() -> new PyInt(value.length())));
        methodRegistry.registerMethod("__str__", MethodBuilder.noArgs(() -> new PyString(toString())));
        methodRegistry.registerMethod("__repr__", MethodBuilder.noArgs(() -> new PyString("'" + value + "'")));
        methodRegistry.registerMethod("__getitem__", MethodBuilder.oneArg(this::getItem));
        methodRegistry.registerMethod("__getslice__", MethodBuilder.varArgs(args ->
                this.__getslice__(args.get(0), args.size() > 1 ? args.get(1) : PyNone.INSTANCE, args.size() > 2 ? args.get(2) : new PyInt(1))));
        methodRegistry.registerMethod("__iter__", MethodBuilder.noArgs(() -> new PyIterator(iterator(), "str")));
        
        // 比较方法
        methodRegistry.registerMethod("__eq__", MethodBuilder.oneArg(this::__eq__));
        methodRegistry.registerMethod("__ne__", MethodBuilder.oneArg(this::__ne__));
        methodRegistry.registerMethod("__lt__", MethodBuilder.oneArg(this::__lt__));
        methodRegistry.registerMethod("__le__", MethodBuilder.oneArg(this::__le__));
        methodRegistry.registerMethod("__gt__", MethodBuilder.oneArg(this::__gt__));
        methodRegistry.registerMethod("__ge__", MethodBuilder.oneArg(this::__ge__));
        
        // 算术方法
        methodRegistry.registerMethod("__add__", MethodBuilder.oneArg(this::__add__));
        methodRegistry.registerMethod("__mul__", MethodBuilder.oneArg(this::__mul__));
        methodRegistry.registerMethod("__contains__", MethodBuilder.oneArg(this::__contains__));
        
        // 字符串方法
        methodRegistry.registerMethod("capitalize", MethodBuilder.noArgs(this::capitalize));
        methodRegistry.registerMethod("casefold", MethodBuilder.noArgs(this::casefold));
        methodRegistry.registerMethod("upper", MethodBuilder.noArgs(this::upper));
        methodRegistry.registerMethod("lower", MethodBuilder.noArgs(this::lower));
        methodRegistry.registerMethod("title", MethodBuilder.noArgs(this::title));
        methodRegistry.registerMethod("swapcase", MethodBuilder.noArgs(this::swapcase));
        methodRegistry.registerMethod("strip", MethodBuilder.varArgs(this::strip));
        methodRegistry.registerMethod("lstrip", MethodBuilder.varArgs(this::lstrip));
        methodRegistry.registerMethod("rstrip", MethodBuilder.varArgs(this::rstrip));
        
        // 判断方法
        methodRegistry.registerMethod("isalnum", MethodBuilder.noArgs(this::isalnum));
        methodRegistry.registerMethod("isalpha", MethodBuilder.noArgs(this::isalpha));
        methodRegistry.registerMethod("isdigit", MethodBuilder.noArgs(this::isdigit));
        methodRegistry.registerMethod("isdecimal", MethodBuilder.noArgs(this::isdecimal));
        methodRegistry.registerMethod("isnumeric", MethodBuilder.noArgs(this::isnumeric));
        methodRegistry.registerMethod("islower", MethodBuilder.noArgs(this::islower));
        methodRegistry.registerMethod("isupper", MethodBuilder.noArgs(this::isupper));
        methodRegistry.registerMethod("isspace", MethodBuilder.noArgs(this::isspace));
        methodRegistry.registerMethod("istitle", MethodBuilder.noArgs(this::istitle));
        
        // 搜索和计数方法
        methodRegistry.registerMethod("find", MethodBuilder.rangeArgs(1, 3, this::find));
        methodRegistry.registerMethod("rfind", MethodBuilder.rangeArgs(1, 3, this::rfind));
        methodRegistry.registerMethod("index", MethodBuilder.rangeArgs(1, 3, this::index));
        methodRegistry.registerMethod("rindex", MethodBuilder.rangeArgs(1, 3, this::rindex));
        methodRegistry.registerMethod("count", MethodBuilder.rangeArgs(1, 3, this::count));
        methodRegistry.registerMethod("startswith", MethodBuilder.rangeArgs(1, 3, this::startswith));
        methodRegistry.registerMethod("endswith", MethodBuilder.rangeArgs(1, 3, this::endswith));
        
        // 格式化和对齐方法
        methodRegistry.registerMethod("center", MethodBuilder.rangeArgs(1, 2, this::center));
        methodRegistry.registerMethod("ljust", MethodBuilder.rangeArgs(1, 2, this::ljust));
        methodRegistry.registerMethod("rjust", MethodBuilder.rangeArgs(1, 2, this::rjust));
        methodRegistry.registerMethod("zfill", MethodBuilder.oneArg(this::zfill));
        
        // 分割和连接方法
        methodRegistry.registerMethod("split", MethodBuilder.varArgs(this::split));
        methodRegistry.registerMethod("rsplit", MethodBuilder.varArgs(this::rsplit));
        methodRegistry.registerMethod("splitlines", MethodBuilder.varArgs(this::splitlines));
        methodRegistry.registerMethod("join", MethodBuilder.oneArg(this::join));
          // 替换方法
        methodRegistry.registerMethod("replace", MethodBuilder.rangeArgs(2, 3, this::replace));
        
        // 编码和格式化方法
        methodRegistry.registerMethod("encode", MethodBuilder.varArgs(this::encode));
        methodRegistry.registerMethod("format", MethodBuilder.varArgs(this::format));
    }
    
    // Slice method implementation
    private PyObject __getslice__(PyObject start, PyObject stop, PyObject step) {
        return this.getSlice(start, stop, step);
    }
    
    // 比较方法实现
    private PyObject __eq__(PyObject other) {
        if (other instanceof PyString) {
            return PyBool.valueOf(value.equals(((PyString) other).value));
        }
        return PyBool.FALSE;
    }
    
    private PyObject __ne__(PyObject other) {
        PyBool eqResult = (PyBool) __eq__(other);
        return PyBool.valueOf(!eqResult.getValue());
    }
    
    private PyObject __lt__(PyObject other) {
        if (other instanceof PyString) {
            return PyBool.valueOf(value.compareTo(((PyString) other).value) < 0);
        }
        throw new RuntimeException("'<' not supported between instances of 'str' and '" + other.getTypeName() + "'");
    }
    
    private PyObject __le__(PyObject other) {
        if (other instanceof PyString) {
            return PyBool.valueOf(value.compareTo(((PyString) other).value) <= 0);
        }
        throw new RuntimeException("'<=' not supported between instances of 'str' and '" + other.getTypeName() + "'");
    }
    
    private PyObject __gt__(PyObject other) {
        if (other instanceof PyString) {
            return PyBool.valueOf(value.compareTo(((PyString) other).value) > 0);
        }
        throw new RuntimeException("'>' not supported between instances of 'str' and '" + other.getTypeName() + "'");
    }
    
    private PyObject __ge__(PyObject other) {
        if (other instanceof PyString) {
            return PyBool.valueOf(value.compareTo(((PyString) other).value) >= 0);
        }
        throw new RuntimeException("'>=' not supported between instances of 'str' and '" + other.getTypeName() + "'");
    }
    
    // 算术方法实现
    private PyObject __add__(PyObject other) {
        if (other instanceof PyString) {
            return new PyString(value + ((PyString) other).value);
        }
        throw new RuntimeException("can only concatenate str (not \"" + other.getTypeName() + "\") to str");
    }
      private PyObject __mul__(PyObject other) {
        if (other instanceof PyInt) {
            long times = ((PyInt) other).getValue();
            if (times <= 0) {
                return new PyString("");
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < times; i++) {
                sb.append(value);
            }
            return new PyString(sb.toString());
        }
        throw new RuntimeException("can't multiply sequence by non-int of type '" + other.getTypeName() + "'");
    }
    
    private PyObject __contains__(PyObject item) {
        if (item instanceof PyString) {
            return PyBool.valueOf(value.contains(((PyString) item).value));
        }
        return PyBool.FALSE;
    }
    
    // 字符串方法实现
    private PyObject capitalize() {
        if (value.isEmpty()) {
            return new PyString("");
        }
        return new PyString(Character.toUpperCase(value.charAt(0)) + value.substring(1).toLowerCase());
    }
    
    private PyObject casefold() {
        return new PyString(value.toLowerCase());
    }
    
    private PyObject upper() {
        return new PyString(value.toUpperCase());
    }
    
    private PyObject lower() {
        return new PyString(value.toLowerCase());
    }
    
    private PyObject title() {
        if (value.isEmpty()) {
            return new PyString("");
        }
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = true;
        
        for (char c : value.toCharArray()) {
            if (Character.isLetter(c)) {
                if (capitalizeNext) {
                    sb.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            } else {
                sb.append(c);
                capitalizeNext = true;
            }
        }
        
        return new PyString(sb.toString());
    }
    
    private PyObject swapcase() {
        StringBuilder sb = new StringBuilder();
        for (char c : value.toCharArray()) {
            if (Character.isLowerCase(c)) {
                sb.append(Character.toUpperCase(c));
            } else if (Character.isUpperCase(c)) {
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return new PyString(sb.toString());
    }
    
    // 去除空白方法
    private PyObject strip(List<PyObject> args) {
        if (args.isEmpty()) {
            return new PyString(value.trim());
        } else {
            PyObject chars = args.get(0);
            if (chars == PyNone.INSTANCE) {
                return new PyString(value.trim());
            }
            if (!(chars instanceof PyString)) {
                throw new RuntimeException("strip arg must be None or str");
            }
            String stripChars = ((PyString) chars).value;
            String result = value;
            
            // 去除开头
            while (!result.isEmpty() && stripChars.indexOf(result.charAt(0)) != -1) {
                result = result.substring(1);
            }
            
            // 去除结尾
            while (!result.isEmpty() && stripChars.indexOf(result.charAt(result.length() - 1)) != -1) {
                result = result.substring(0, result.length() - 1);
            }
            
            return new PyString(result);
        }
    }
    
    private PyObject lstrip(List<PyObject> args) {
        if (args.isEmpty()) {
            return new PyString(value.replaceAll("^\\s+", ""));
        } else {
            PyObject chars = args.get(0);
            if (chars == PyNone.INSTANCE) {
                return new PyString(value.replaceAll("^\\s+", ""));
            }
            if (!(chars instanceof PyString)) {
                throw new RuntimeException("lstrip arg must be None or str");
            }
            String stripChars = ((PyString) chars).value;
            String result = value;
            
            while (!result.isEmpty() && stripChars.indexOf(result.charAt(0)) != -1) {
                result = result.substring(1);
            }
            
            return new PyString(result);
        }
    }
    
    private PyObject rstrip(List<PyObject> args) {
        if (args.isEmpty()) {
            return new PyString(value.replaceAll("\\s+$", ""));
        } else {
            PyObject chars = args.get(0);
            if (chars == PyNone.INSTANCE) {
                return new PyString(value.replaceAll("\\s+$", ""));
            }
            if (!(chars instanceof PyString)) {
                throw new RuntimeException("rstrip arg must be None or str");
            }
            String stripChars = ((PyString) chars).value;
            String result = value;
            
            while (!result.isEmpty() && stripChars.indexOf(result.charAt(result.length() - 1)) != -1) {
                result = result.substring(0, result.length() - 1);
            }
            
            return new PyString(result);
        }
    }
    
    // 判断方法实现
    private PyObject isalnum() {
        if (value.isEmpty()) return PyBool.FALSE;
        return PyBool.valueOf(value.chars().allMatch(Character::isLetterOrDigit));
    }
    
    private PyObject isalpha() {
        if (value.isEmpty()) return PyBool.FALSE;
        return PyBool.valueOf(value.chars().allMatch(Character::isLetter));
    }
    
    private PyObject isdigit() {
        if (value.isEmpty()) return PyBool.FALSE;
        return PyBool.valueOf(value.chars().allMatch(Character::isDigit));
    }
    
    private PyObject isdecimal() {
        if (value.isEmpty()) return PyBool.FALSE;
        return PyBool.valueOf(value.chars().allMatch(c -> Character.getNumericValue(c) >= 0 && Character.getNumericValue(c) <= 9));
    }
    
    private PyObject isnumeric() {
        if (value.isEmpty()) return PyBool.FALSE;
        return PyBool.valueOf(value.chars().allMatch(c -> Character.getNumericValue(c) >= 0));
    }
    
    private PyObject islower() {
        if (value.isEmpty()) return PyBool.FALSE;
        boolean hasLower = false;
        for (char c : value.toCharArray()) {
            if (Character.isUpperCase(c)) return PyBool.FALSE;
            if (Character.isLowerCase(c)) hasLower = true;
        }
        return PyBool.valueOf(hasLower);
    }
    
    private PyObject isupper() {
        if (value.isEmpty()) return PyBool.FALSE;
        boolean hasUpper = false;
        for (char c : value.toCharArray()) {
            if (Character.isLowerCase(c)) return PyBool.FALSE;
            if (Character.isUpperCase(c)) hasUpper = true;
        }
        return PyBool.valueOf(hasUpper);
    }
    
    private PyObject isspace() {
        if (value.isEmpty()) return PyBool.FALSE;
        return PyBool.valueOf(value.chars().allMatch(Character::isWhitespace));
    }
    
    private PyObject istitle() {
        if (value.isEmpty()) return PyBool.FALSE;
        boolean previousWasNonLetter = true;
        boolean foundLetter = false;
        
        for (char c : value.toCharArray()) {
            if (Character.isLetter(c)) {
                foundLetter = true;
                if (previousWasNonLetter) {
                    if (!Character.isUpperCase(c)) return PyBool.FALSE;
                    previousWasNonLetter = false;
                } else {
                    if (!Character.isLowerCase(c)) return PyBool.FALSE;
                }
            } else {
                previousWasNonLetter = true;
            }
        }
        
        return PyBool.valueOf(foundLetter);
    }
    
    // 搜索方法实现
    private PyObject find(List<PyObject> args) {
        if (args.isEmpty() || args.size() > 3) {
            throw new RuntimeException("find() takes from 1 to 3 positional arguments but " + args.size() + " were given");
        }
          String sub = MethodBuilder.requireType(args.get(0), PyString.class, "substring").value;
        int start = 0;
        int end = value.length();
        
        if (args.size() >= 2 && args.get(1) != PyNone.INSTANCE) {
            start = (int) MethodBuilder.requireType(args.get(1), PyInt.class, "start").getValue();
            if (start < 0) start += value.length();
            start = Math.max(0, start);
        }
        
        if (args.size() == 3 && args.get(2) != PyNone.INSTANCE) {
            end = (int) MethodBuilder.requireType(args.get(2), PyInt.class, "end").getValue();
            if (end < 0) end += value.length();
            end = Math.min(value.length(), end);
        }
        
        String searchIn = value.substring(start, Math.min(end, value.length()));
        int index = searchIn.indexOf(sub);
        return new PyInt(index == -1 ? -1 : index + start);
    }
    
    private PyObject rfind(List<PyObject> args) {
        if (args.isEmpty() || args.size() > 3) {
            throw new RuntimeException("rfind() takes from 1 to 3 positional arguments but " + args.size() + " were given");
        }
          String sub = MethodBuilder.requireType(args.get(0), PyString.class, "substring").value;
        int start = 0;
        int end = value.length();
        
        if (args.size() >= 2 && args.get(1) != PyNone.INSTANCE) {
            start = (int) MethodBuilder.requireType(args.get(1), PyInt.class, "start").getValue();
            if (start < 0) start += value.length();
            start = Math.max(0, start);
        }
        
        if (args.size() == 3 && args.get(2) != PyNone.INSTANCE) {
            end = (int) MethodBuilder.requireType(args.get(2), PyInt.class, "end").getValue();
            if (end < 0) end += value.length();
            end = Math.min(value.length(), end);
        }
        
        String searchIn = value.substring(start, Math.min(end, value.length()));
        int index = searchIn.lastIndexOf(sub);
        return new PyInt(index == -1 ? -1 : index + start);
    }
      private PyObject index(List<PyObject> args) {
        PyObject result = find(args);
        int idx = (int) ((PyInt) result).getValue();
        if (idx == -1) {
            throw new RuntimeException("substring not found");
        }
        return result;
    }
    
    private PyObject rindex(List<PyObject> args) {
        PyObject result = rfind(args);
        int idx = (int) ((PyInt) result).getValue();
        if (idx == -1) {
            throw new RuntimeException("substring not found");
        }
        return result;
    }
    
    private PyObject count(List<PyObject> args) {
        if (args.isEmpty() || args.size() > 3) {
            throw new RuntimeException("count() takes from 1 to 3 positional arguments but " + args.size() + " were given");
        }
          String sub = MethodBuilder.requireType(args.get(0), PyString.class, "substring").value;
        if (sub.isEmpty()) {
            return new PyInt(value.length() + 1);
        }
        
        int start = 0;
        int end = value.length();
        
        if (args.size() >= 2 && args.get(1) != PyNone.INSTANCE) {
            start = (int) MethodBuilder.requireType(args.get(1), PyInt.class, "start").getValue();
            if (start < 0) start += value.length();
            start = Math.max(0, start);
        }
        
        if (args.size() == 3 && args.get(2) != PyNone.INSTANCE) {
            end = (int) MethodBuilder.requireType(args.get(2), PyInt.class, "end").getValue();
            if (end < 0) end += value.length();
            end = Math.min(value.length(), end);
        }
        
        String searchIn = value.substring(start, Math.min(end, value.length()));
        int count = 0;
        int pos = 0;
        while ((pos = searchIn.indexOf(sub, pos)) != -1) {
            count++;
            pos += sub.length();
        }
        
        return new PyInt(count);
    }
    
    private PyObject startswith(List<PyObject> args) {
        if (args.isEmpty() || args.size() > 3) {
            throw new RuntimeException("startswith() takes from 1 to 3 positional arguments but " + args.size() + " were given");
        }
          String prefix = MethodBuilder.requireType(args.get(0), PyString.class, "prefix").value;
        int start = 0;
        int end = value.length();
        
        if (args.size() >= 2 && args.get(1) != PyNone.INSTANCE) {
            start = (int) MethodBuilder.requireType(args.get(1), PyInt.class, "start").getValue();
            if (start < 0) start += value.length();
            start = Math.max(0, start);
        }
        
        if (args.size() == 3 && args.get(2) != PyNone.INSTANCE) {
            end = (int) MethodBuilder.requireType(args.get(2), PyInt.class, "end").getValue();
            if (end < 0) end += value.length();
            end = Math.min(value.length(), end);
        }
        
        String checkIn = value.substring(start, Math.min(end, value.length()));
        return PyBool.valueOf(checkIn.startsWith(prefix));
    }
    
    private PyObject endswith(List<PyObject> args) {
        if (args.isEmpty() || args.size() > 3) {
            throw new RuntimeException("endswith() takes from 1 to 3 positional arguments but " + args.size() + " were given");
        }
          String suffix = MethodBuilder.requireType(args.get(0), PyString.class, "suffix").value;
        int start = 0;
        int end = value.length();
        
        if (args.size() >= 2 && args.get(1) != PyNone.INSTANCE) {
            start = (int) MethodBuilder.requireType(args.get(1), PyInt.class, "start").getValue();
            if (start < 0) start += value.length();
            start = Math.max(0, start);
        }
        
        if (args.size() == 3 && args.get(2) != PyNone.INSTANCE) {
            end = (int) MethodBuilder.requireType(args.get(2), PyInt.class, "end").getValue();
            if (end < 0) end += value.length();
            end = Math.min(value.length(), end);
        }
        
        String checkIn = value.substring(start, Math.min(end, value.length()));
        return PyBool.valueOf(checkIn.endsWith(suffix));    }
    
    // 格式化方法实现
    private PyObject center(List<PyObject> args) {
        int width = (int) MethodBuilder.requireType(args.get(0), PyInt.class, "width").getValue();
        String fillChar = " ";
        
        if (args.size() == 2) {
            String fill = MethodBuilder.requireType(args.get(1), PyString.class, "fillchar").value;
            if (fill.length() != 1) {
                throw new RuntimeException("The fill character must be exactly one character long");
            }
            fillChar = fill;
        }
        
        if (width <= value.length()) {
            return new PyString(value);
        }
        
        int padding = width - value.length();
        int leftPad = padding / 2;
        int rightPad = padding - leftPad;
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leftPad; i++) sb.append(fillChar);
        sb.append(value);
        for (int i = 0; i < rightPad; i++) sb.append(fillChar);
        
        return new PyString(sb.toString());
    }
      private PyObject ljust(List<PyObject> args) {
        int width = (int) MethodBuilder.requireType(args.get(0), PyInt.class, "width").getValue();
        String fillChar = " ";
        
        if (args.size() == 2) {
            String fill = MethodBuilder.requireType(args.get(1), PyString.class, "fillchar").value;
            if (fill.length() != 1) {
                throw new RuntimeException("The fill character must be exactly one character long");
            }
            fillChar = fill;
        }
        
        if (width <= value.length()) {
            return new PyString(value);
        }
        
        StringBuilder sb = new StringBuilder(value);
        while (sb.length() < width) {
            sb.append(fillChar);
        }
        
        return new PyString(sb.toString());
    }
      private PyObject rjust(List<PyObject> args) {
        int width = (int) MethodBuilder.requireType(args.get(0), PyInt.class, "width").getValue();
        String fillChar = " ";
        
        if (args.size() == 2) {
            String fill = MethodBuilder.requireType(args.get(1), PyString.class, "fillchar").value;
            if (fill.length() != 1) {
                throw new RuntimeException("The fill character must be exactly one character long");
            }
            fillChar = fill;
        }
        
        if (width <= value.length()) {
            return new PyString(value);
        }
        
        StringBuilder sb = new StringBuilder();
        while (sb.length() < width - value.length()) {
            sb.append(fillChar);
        }
        sb.append(value);
        
        return new PyString(sb.toString());
    }
      private PyObject zfill(PyObject widthObj) {
        int width = (int) MethodBuilder.requireType(widthObj, PyInt.class, "width").getValue();
        
        if (width <= value.length()) {
            return new PyString(value);
        }
        
        StringBuilder sb = new StringBuilder();
        String val = value;
        
        // 处理符号
        if (val.startsWith("+") || val.startsWith("-")) {
            sb.append(val.charAt(0));
            val = val.substring(1);
        }
        
        // 添加零填充
        while (sb.length() + val.length() < width) {
            sb.append("0");
        }
        
        sb.append(val);
        return new PyString(sb.toString());
    }
    
    // 分割方法实现
    private PyObject split(List<PyObject> args) {
        String sep = null;
        int maxsplit = -1;
        
        if (!args.isEmpty()) {
            PyObject sepObj = args.get(0);
            if (sepObj != PyNone.INSTANCE) {
                sep = MethodBuilder.requireType(sepObj, PyString.class, "sep").value;
            }
        }
          if (args.size() >= 2) {
            maxsplit = (int) MethodBuilder.requireType(args.get(1), PyInt.class, "maxsplit").getValue();
        }
        
        List<PyObject> result = new ArrayList<>();
        
        if (sep == null) {
            // 默认分割（按空白字符）
            String[] parts = value.trim().split("\\s+");
            if (maxsplit == -1) {
                for (String part : parts) {
                    if (!part.isEmpty()) {
                        result.add(new PyString(part));
                    }
                }
            } else {
                int count = 0;
                for (int i = 0; i < parts.length && count < maxsplit; i++) {
                    if (!parts[i].isEmpty()) {
                        result.add(new PyString(parts[i]));
                        count++;
                    }
                }
                if (count < parts.length) {
                    StringBuilder remaining = new StringBuilder();
                    for (int i = count; i < parts.length; i++) {
                        if (i > count) remaining.append(" ");
                        remaining.append(parts[i]);
                    }
                    result.add(new PyString(remaining.toString()));
                }
            }
        } else {
            // 按指定分隔符分割
            if (sep.isEmpty()) {
                throw new RuntimeException("empty separator");
            }
            
            String[] parts;
            if (maxsplit == -1) {
                parts = value.split(java.util.regex.Pattern.quote(sep), -1);
            } else {
                parts = value.split(java.util.regex.Pattern.quote(sep), maxsplit + 1);
            }
              for (String part : parts) {
                result.add(new PyString(part));
            }
        }
        
        return new PyList(result);
    }
    
    private PyObject rsplit(List<PyObject> args) {
        // 简化实现，基本与split相同但从右开始
        return split(args);  // 实际实现会更复杂
    }
    
    private PyObject splitlines(List<PyObject> args) {
        boolean keepends = false;
        if (!args.isEmpty()) {
            keepends = MethodBuilder.requireType(args.get(0), PyBool.class, "keepends").getValue();
        }
        
        List<PyObject> result = new ArrayList<>();
        String[] lines = value.split("\\r?\\n", -1);
        
        for (int i = 0; i < lines.length - 1; i++) {
            if (keepends) {
                result.add(new PyString(lines[i] + "\n"));
            } else {
                result.add(new PyString(lines[i]));
            }
        }
          // 最后一行（如果不为空）
        if (!lines[lines.length - 1].isEmpty()) {
            result.add(new PyString(lines[lines.length - 1]));
        }
        
        return new PyList(result);
    }
    
    private PyObject join(PyObject iterable) {
        StringBuilder sb = new StringBuilder();
        Iterator<PyObject> iterator = iterable.iterator();
        
        boolean first = true;
        while (iterator.hasNext()) {
            if (!first) {
                sb.append(value);
            }
            PyObject item = iterator.next();
            if (!(item instanceof PyString)) {
                throw new RuntimeException("sequence item: expected str instance, " + item.getTypeName() + " found");
            }
            sb.append(((PyString) item).value);
            first = false;
        }
        
        return new PyString(sb.toString());
    }
    
    // 替换方法实现
    private PyObject replace(List<PyObject> args) {
        String old = MethodBuilder.requireType(args.get(0), PyString.class, "old").value;
        String newStr = MethodBuilder.requireType(args.get(1), PyString.class, "new").value;
        int count = -1;
          if (args.size() == 3) {
            count = (int) MethodBuilder.requireType(args.get(2), PyInt.class, "count").getValue();
        }
        
        if (old.isEmpty()) {
            // 特殊情况：替换空字符串
            if (count == -1 || count > value.length() + 1) {
                count = value.length() + 1;
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(count, value.length() + 1); i++) {
                sb.append(newStr);
                if (i < value.length()) {
                    sb.append(value.charAt(i));
                }
            }
            if (count <= value.length()) {
                sb.append(value.substring(count));
            }
            return new PyString(sb.toString());
        }
        
        String result = value;
        if (count == -1) {
            result = result.replace(old, newStr);
        } else {
            for (int i = 0; i < count; i++) {
                int index = result.indexOf(old);
                if (index == -1) break;
                result = result.substring(0, index) + newStr + result.substring(index + old.length());
            }        }
        
        return new PyString(result);
    }
    
    // 编码方法
    private PyObject encode(List<PyObject> args) {
        String encoding = "utf-8";
        String errors = "strict";
        
        if (!args.isEmpty()) {
            encoding = MethodBuilder.requireType(args.get(0), PyString.class, "encoding").value;
        }
        if (args.size() >= 2) {
            errors = MethodBuilder.requireType(args.get(1), PyString.class, "errors").value;
        }
          try {
            byte[] bytes = value.getBytes(encoding);
            return new PyBytes(bytes);
        } catch (Exception e) {
            if ("strict".equals(errors)) {
                throw new RuntimeException("'" + encoding + "' codec can't encode characters");
            } else if ("ignore".equals(errors)) {
                return new PyBytes(new byte[0]);
            } else {
                // 简化的替换处理
                return new PyBytes(new byte[]{63}); // '?' character
            }
        }
    }
    
    // 格式化方法 (简化实现)
    private PyObject format(List<PyObject> args) {
        String result = value;
        
        // 简化的格式化：仅支持位置参数替换 {}
        for (int i = 0; i < args.size(); i++) {
            String placeholder = "{}";
            if (result.contains(placeholder)) {
                String replacement = args.get(i).toString();
                result = result.replaceFirst("\\{\\}", replacement);
            }
        }
        
        return new PyString(result);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PyString pyString = (PyString) obj;
        return Objects.equals(value, pyString.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}

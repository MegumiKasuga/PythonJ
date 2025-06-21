package edu.carole.runtime.clazz.builtins;

import edu.carole.ast.expressions.Literal;
import edu.carole.interpreter.BuiltinFunctions;
import edu.carole.interpreter.Interpreter;
import edu.carole.runtime.PyObject;
import edu.carole.runtime.clazz.BuiltinClass;
import edu.carole.runtime.clazz.PyClass;
import edu.carole.runtime.clazz.helper.Slice;
import edu.carole.runtime.instance.BuiltinInstance;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PyString extends BuiltinClass<String> {
    public PyString(String name, Map<String, PyObject> methods) {
        super(name, methods, String.class);
    }

    public PyString(String name, Map<String, PyObject> methods, List<PyClass> baseClasses) {
        super(name, methods, baseClasses, String.class);
    }

    public PyString(String name, String modulePath, Map<String, PyObject> methods) {
        super(name, modulePath, methods, String.class);
    }

    public PyString(String name, String modulePath, Map<String, PyObject> methods, List<PyClass> baseClasses) {
        super(name, modulePath, methods, baseClasses, String.class);
    }

    @Override
    public void initMethods(Map<String, PyObject> methods, Map<String, PyObject> classAttr) {
        // 基础
        registerNArgs(methods, "__len__", 0, (args, kwargs, inter) -> {
            return len(inter, args.get(0));
        });
        registerNArgs(methods, "__str__", 0, (args, kwargs, inter) -> {
            return args.get(0);
        });
        registerNArgs(methods, "__repr__", 0, (args, kwargs, inter) -> {
            String value = getValue(inter, args.get(0));
            return inter.createString("'" + value + "'");
        });
        registerMethod(methods, "__getslice__", (args, kwargs, inter) -> {
            return getSlice(inter, args, kwargs);
        });
        registerNArgs(methods, "__getitem__", 1, (args, kwargs, inter) -> {
            return getItem(inter, args.get(0), args.get(1));
        });


        // 比较
        registerNArgs(methods, "__eq__", 1, (args, kwargs, inter) -> {
            return eq(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__ne__", 1, (args, kwargs, inter) -> {
            return PyBool.reverse(inter, eq(inter, args.get(0), args.get(1)));
        });
        registerNArgs(methods, "__lt__", 1, (args, kwargs, inter) -> {
            return compare(inter, args.get(0), args.get(1), r -> r < 0);
        });
        registerNArgs(methods, "__le__", 1, (args, kwargs, inter) -> {
            return compare(inter, args.get(0), args.get(1), r -> r <= 0);
        });
        registerNArgs(methods, "__gt__", 1, (args, kwargs, inter) -> {
            return compare(inter, args.get(0), args.get(1), r -> r > 0);
        });
        registerNArgs(methods, "__ge__", 1, (args, kwargs, inter) -> {
            return compare(inter, args.get(0), args.get(1), r -> r >= 0);
        });


        // 算术
        registerNArgs(methods, "__add__", 1, (args, kwargs, inter) -> {
            return add(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__mul__", 1, (args, kwargs, inter) -> {
            return mul(inter, args.get(0), args.get(1));
        });
        registerNArgs(methods, "__contains__", 1, (args, kwargs, inter) -> {
            return contains(inter, args.get(0), args.get(1));
        });
        // TODO: 待PyTuple完成后，完成__mod__方法实现'%'运算符

        // 字符串处理
        registerNArgs(methods, "capitalize", 0, (args, kwargs, inter) -> {
            return capitalize(inter, args.get(0));
        });
        registerNArgs(methods, "casefold", 0, (args, kwargs, inter) -> {
            return caseFold(inter, args.get(0));
        });
        registerNArgs(methods, "upper", 0, (args, kwargs, inter) -> {
            return upper(inter, args.get(0));
        });
        registerNArgs(methods, "lower", 0, (args, kwargs, inter) -> {
            return lower(inter, args.get(0));
        });
        registerNArgs(methods, "title", 0, (args, kwargs, inter) -> {
            return title(inter, args.get(0));
        });
        registerNArgs(methods, "swapcase", 0, (args, kwargs, inter) -> {
            return swapCase(inter, args.get(0));
        });
        registerMethod(methods, "strip", (args, kwargs, inter) -> {
            return strip(inter, args);
        });
        registerMethod(methods, "lstrip", (args, kwargs, inter) -> {
            return sideStrip(inter, "lstrip", args, true);
        });
        registerMethod(methods, "rstrip", (args, kwargs, inter) -> {
            return sideStrip(inter, "rstrip", args, false);
        });


        // 字符串判断
        registerNArgs(methods, "isalnum", 0, (args, kwargs, inter) -> {
            return isAlphaNum(inter, args.get(0));
        });
        registerNArgs(methods, "isalpha", 0, (args, kwargs, inter) -> {
            return isAlpha(inter, args.get(0));
        });
        registerNArgs(methods, "isdigit", 0, (args, kwargs, inter) -> {
            return isDigit(inter, args.get(0));
        });
        registerNArgs(methods, "isdecimal", 0, (args, kwargs, inter) -> {
            return isDecimal(inter, args.get(0));
        });
        registerNArgs(methods, "isnumeric", 0, (args, kwargs, inter) -> {
            return isNumeric(inter, args.get(0));
        });
        registerNArgs(methods, "islower", 0, (args, kwargs, inter) -> {
            return isLower(inter, args.get(0));
        });
        registerNArgs(methods, "isupper", 0, (args, kwargs, inter) -> {
            return isUpper(inter, args.get(0));
        });
        registerNArgs(methods, "isspace", 0, (args, kwargs, inter) -> {
            return isSpace(inter, args.get(0));
        });
        registerNArgs(methods, "istitle", 0, (args, kwargs, inter) -> {
            return isTitle(inter, args.get(0));
        });


        // 搜索和计数
        registerMethod(methods, "find", (args, kwargs, inter) -> {
            return find(inter, "find", true, args, kwargs);
        });
        registerMethod(methods, "rfind", (args, kwargs, inter) -> {
            return find(inter, "rfind", false, args, kwargs);
        });
        registerMethod(methods, "index", (args, kwargs, inter) -> {
            return index(inter, "index", true, args, kwargs);
        });
        registerMethod(methods, "rindex", (args, kwargs, inter) -> {
            return index(inter, "rindex", false, args, kwargs);
        });
        registerMethod(methods, "count", (args, kwargs, inter) -> {
            return count(inter, args, kwargs);
        });
        registerMethod(methods, "startswith", (args, kwargs, inter) -> {
            return startsOrEndsWith(inter, "startswith", true, args, kwargs);
        });
        registerMethod(methods, "endswith", (args, kwargs, inter) -> {
            return startsOrEndsWith(inter, "endswith", false, args, kwargs);
        });


        // 格式化和对齐
        registerMethod(methods, "center", (args, kwargs, inter) -> {
            return center(inter, args, kwargs);
        });
        registerMethod(methods, "ljust", (args, kwargs, inter) -> {
            return just(inter, "ljust", true, args, kwargs);
        });
        registerMethod(methods, "rjust", (args, kwargs, inter) -> {
            return just(inter, "rjust", false, args, kwargs);
        });
        registerNArgs(methods, "zfill", 1, (args, kwargs, inter) -> {
            return zeroFill(inter, args.get(0), args.get(1));
        });


        // 分割和连接
        registerMethod(methods, "split", (args, kwargs, inter) -> {
            return split(inter, "split", true, args, kwargs);
        });
        registerMethod(methods, "rsplit", (args, kwargs, inter) -> {
            return split(inter, "rsplit", false, args, kwargs);
        });
        registerMethod(methods, "splitlines", (args, kwargs, inter) -> {
            return splitLines(inter, args, kwargs);
        });
        registerNArgs(methods, "join", 1, (args, kwargs, inter) -> {
            return join(inter, args.get(0), args.get(1));
        });


        // 替换
        registerMethod(methods, "replace", (args, kwargs, inter) -> {
            return replace(inter, args, kwargs);
        });

        // 编码和格式化
        // TODO: 待完成PyBytes之后，完成encode和format方法
    }

    private BuiltinInstance<String> replace(Interpreter interpreter, List<PyObject> args, Map<String, PyObject> kwargs) {
        if (args.isEmpty() || !(args.get(0) instanceof BuiltinInstance ins)) {
            throw BuiltinFunctions.typeError(interpreter, "'replace' is not an static method");
        } else if (!ins.is(this)) {
            throw BuiltinFunctions.typeError(interpreter, "'replace' is not an static method");
        }
        boolean hasKwarg = kwargs != null;
        int argCount = args.size() + (hasKwarg ? kwargs.size() : 0);
        if (argCount < 3 || argCount > 4) {
            throw BuiltinFunctions.atRangeNArgs(interpreter, "replace", 2, 3, args.size() - 1);
        }
        BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
        PyObject self = args.get(0);
        PyObject old = interpreter.none();
        PyObject neo = interpreter.none();
        PyObject count = interpreter.none();
        int size = -1;
        if (args.size() == 1 && hasKwarg) {
            if (kwargs.containsKey("old")) {
                old = kwargs.get("old");
            } else {
                throw BuiltinFunctions.keyError(interpreter, "'replace' must have argument 'old'");
            }
            if (kwargs.containsKey("replacement")) {
                neo = kwargs.get("replacement");
            } else {
                throw BuiltinFunctions.keyError(interpreter, "'replace' must have argument 'replacement'");
            }
            if (kwargs.containsKey("count")) {
                count = kwargs.get("count");
            }
        } else if (args.size() == 2) {
            old = args.get(1);
            if (hasKwarg) {
                if (kwargs.containsKey("replacement")) {
                    neo = kwargs.get("replacement");
                } else {
                    throw BuiltinFunctions.keyError(interpreter, "'replace' must have argument 'replacement'");
                }
                if (kwargs.containsKey("count")) {
                    count = kwargs.get("count");
                }
            }
        } else if (args.size() == 3) {
            old = args.get(1);
            neo = args.get(2);
            if (hasKwarg && kwargs.containsKey("count")) {
                count = kwargs.get("count");
                if (!INTEGER.is(count)) {
                    throw badType(interpreter, "replace", "int", count.getTypeName());
                }
            }
        } else {
            old = args.get(1);
            neo = args.get(2);
            count = args.get(3);
        }
        if (!is(old)) {
            throw badType(interpreter, "replace", "str", neo.getTypeName());
        }
        if (!is(neo)) {
            throw badType(interpreter, "replace", "str", neo.getTypeName());
        }
        if (!interpreter.isNone(count)) {
            if (!INTEGER.is(count)) {
                throw badType(interpreter, "replace", "int", count.getTypeName());
            }
            size = INTEGER.getValue(interpreter, count).intValue();
        }
        String value = getValue(interpreter, self);
        String replaced = getValue(interpreter, old);
        String pattern = getValue(interpreter, neo);

        // 替换空串
        if (replaced.isEmpty()) {
            if (size == -1 || size > value.length() + 1) {
                size = value.length() + 1;
            }
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < Math.min(size, value.length() + 1); i++) {
                builder.append(pattern);
                if (i < value.length()) {
                    builder.append(value.charAt(i));
                }
            }
            if (size <= value.length()) {
                builder.append(value.substring(size));
            }
            return interpreter.createString(builder.toString());
        } else {
            String result = value;
            if (size == -1) {
                result = result.replace(replaced, pattern);
            } else {
                for (int i = 0; i < size; i++) {
                    int index = result.indexOf(replaced);
                    if (index == -1) break;
                    result = result.substring(0, index) + pattern + result.substring(index + replaced.length());
                }
            }
            return interpreter.createString(result);
        }
    }

    private BuiltinInstance<String> join(Interpreter interpreter, PyObject self, PyObject iterable) {
        String value = getValue(interpreter, self);
        Iterator<PyObject> iterator = iterable.iterator(interpreter);
        StringBuilder builder = new StringBuilder();

        boolean first = true;
        while (iterator.hasNext()) {
            if (!first) {
                builder.append(value);
            }
            PyObject item = iterator.next();
            if (!is(item)) {
                throw badType(interpreter,
                        "sequence item: expected str instance, " +
                                item.getTypeName() + " found");
            }
            builder.append(getValue(interpreter, item));
            first = false;
        }

        return interpreter.createString(builder.toString());
    }

    private BuiltinInstance<String> splitLines(Interpreter interpreter, List<PyObject> args, Map<String, PyObject> kwargs) {
        if (args.isEmpty() || !(args.get(0) instanceof BuiltinInstance ins)) {
            throw BuiltinFunctions.typeError(interpreter, "'splitlines' is not an static method");
        } else if (!ins.is(this)) {
            throw BuiltinFunctions.typeError(interpreter, "'splitlines' is not an static method");
        }
        boolean hasKwarg = kwargs != null;
        int argCount = args.size() + (hasKwarg ? kwargs.size() : 0);
        if (argCount > 2) {
            throw BuiltinFunctions.atMostNArgs(interpreter, "splitlines", 1, args.size() - 1);
        }
        PyObject keepEndVal = interpreter.boolFalse();
        if (args.size() == 1 && hasKwarg) {
            if (kwargs.containsKey("keepends")) {
                keepEndVal = kwargs.get("keepends");
            }
        } else if (args.size() == 2) {
            keepEndVal = args.get(1);
        }
        BuiltinClass<Boolean> BOOLEAN = interpreter.getMemoryModel().getBOOL();
        boolean keepEnd = BOOLEAN.getValue(interpreter, keepEndVal);
        PyObject self = args.get(0);
        String value = getValue(interpreter, self);
        List<BuiltinInstance<String>> result = new ArrayList<>();
        String[] lines = value.split("\\r?\\n", -1);

        for (String line : lines) {
            result.add(interpreter.createString(line + (keepEnd ? "\n" : "")));
        }

        // TODO: 待PyList完成后，补完该方法
        return interpreter.createString("");
    }

    private List<PyObject> getSplitArgs(Interpreter interpreter, String name,
                                        List<PyObject> args, Map<String, PyObject> kwargs) {
        if (args.isEmpty() || !(args.get(0) instanceof BuiltinInstance ins)) {
            throw BuiltinFunctions.typeError(interpreter, "'" + name + "' is not an static method");
        } else if (!ins.is(this)) {
            throw BuiltinFunctions.typeError(interpreter, "'" + name + "' is not an static method");
        }
        boolean hasKwarg = kwargs != null;
        int argCount = args.size() + (hasKwarg ? kwargs.size() : 0);
        if (argCount > 3) {
            throw BuiltinFunctions.atMostNArgs(interpreter, name, 2, args.size() - 1);
        }
        PyObject self = args.get(0);
        PyObject sep = interpreter.none();
        PyObject maxSepCount = interpreter.none();
        if (args.size() == 1 && hasKwarg) {
            if (kwargs.containsKey("str")) {
                sep = kwargs.get("str");
            }
            if (kwargs.containsKey("num")) {
                maxSepCount = kwargs.get("num");
            }
        } else if (args.size() == 2) {
            sep = args.get(1);
            if (hasKwarg && kwargs.containsKey("num")) {
                maxSepCount = kwargs.get("num");
            }
        } else {
            sep = args.get(1);
            maxSepCount = args.get(2);
        }
        return List.of(self, sep, maxSepCount);
    }

    private BuiltinInstance<String> split(Interpreter interpreter, String name, boolean left,
                                          List<PyObject> args, Map<String, PyObject> kwargs) {
        List<PyObject> splitArgs = getSplitArgs(interpreter, name, args, kwargs);
        PyObject self = splitArgs.get(0);
        PyObject sep = splitArgs.get(1);
        PyObject maxSplit = splitArgs.get(2);
        String value = getValue(interpreter, self);
        String separator = null;
        int limit = -1;
        if (!interpreter.isNone(sep)) {
            if (is(sep)) {
                separator = getValue(interpreter, sep);
            } else {
                throw badType(interpreter, name, "str", sep.getTypeName());
            }
        }
        BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
        if (!interpreter.isNone(maxSplit)) {
            if (INTEGER.is(maxSplit)) {
                limit = INTEGER.getValue(interpreter, maxSplit).intValue();
            } else {
                throw badType(interpreter, name, "int", maxSplit.getTypeName());
            }
        }

        Pattern pattern = Pattern.compile(separator == null ? "\\s+" : separator);
        Matcher matcher = pattern.matcher(value);
        List<Integer> matcherResult = new ArrayList<>();
        List<Integer> groupLength = new ArrayList<>();
        while (matcher.find()) {
            matcherResult.add(left ? matcher.start() : matcher.end());
            groupLength.add(matcher.group().length());
        }
        int pos, index, matcherSize = matcherResult.size(),
                lastIndex = left ? 0 : value.length(),
                iteratorLimit = (limit <= 0 ? matcherSize : Math.min(matcherSize, limit));
        List<BuiltinInstance<String>> result = new ArrayList<>(iteratorLimit + 1);
        String cache;
        for (int i = 0; i < iteratorLimit; i++) {
            index = left ? i : matcherSize - i - 1;
            pos = matcherResult.get(index);
            cache = left ? value.substring(lastIndex, pos) :
                    value.substring(pos, lastIndex);
            lastIndex = pos + groupLength.get(index) * (left ? 1 : -1);
            if (limit < 0 && cache.isEmpty()) {
                continue;
            }
            result.add(interpreter.createString(cache));
        }
        if (left ? lastIndex < value.length() : lastIndex > 0) {
            result.add(interpreter.createString(
                left ? value.substring(lastIndex) : value.substring(0, lastIndex)
            ));
        }

        // TODO: 待PyTuple完成，补完该方法
        return interpreter.createString("");
    }

    private BuiltinInstance<String> zeroFill(Interpreter interpreter, PyObject self, PyObject others) {
        String value = getValue(interpreter, self);
        BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
        int width = INTEGER.getValue(interpreter, others).intValue();
        if (value.length() >= width) {
            return (BuiltinInstance<String>) self;
        }
        StringBuilder builder = new StringBuilder();
        String val = value;
        if (val.startsWith("+") || val.startsWith("-")) {
            builder.append(val.charAt(0));
            val = val.substring(1);
        }

        while (builder.length() + val.length() < width) {
            builder.append("0");
        }

        builder.append(val);
        return interpreter.createString(builder.toString());
    }

    private List<PyObject> need2Args(Interpreter interpreter, String name,
                                     List<PyObject> args, Map<String, PyObject> kwargs) {
        if (args.isEmpty() || !(args.get(0) instanceof BuiltinInstance ins)) {
            throw BuiltinFunctions.typeError(interpreter, "'" + name + "' is not an static method");
        } else if (!ins.is(this)) {
            throw BuiltinFunctions.typeError(interpreter, "'" + name + "' is not an static method");
        }
        boolean hasKwarg = kwargs != null;
        int argCount = args.size() + (hasKwarg ? kwargs.size() : 0);
        if (argCount < 2 || argCount > 3) {
            throw BuiltinFunctions.atRangeNArgs(interpreter, "center", 2, 3, args.size() - 1);
        }
        PyObject self = args.get(0);
        PyObject fillC = interpreter.none();
        PyObject pyWidth = interpreter.none();
        if (args.size() == 1) {
            if (hasKwarg) {
                if (kwargs.containsKey("width")) {
                    pyWidth = kwargs.get("width");
                } else {
                    throw BuiltinFunctions.keyError(interpreter, "'center' must has param 'width'");
                }
                if (kwargs.containsKey("fillchar")) {
                    fillC = kwargs.get("fillchar");
                }
            }
        } else if (args.size() == 2) {
            pyWidth = args.get(1);
            if (hasKwarg && kwargs.containsKey("fillchar")) {
                fillC = kwargs.get("fillchar");
            }
        } else {
            fillC = args.get(2);
        }
        return List.of(self, pyWidth, fillC);
    }

    private String getFillChar(Interpreter interpreter, String name, PyObject fillC) {
        String fillChars = " ";
        if (!is(fillC) && !interpreter.isNone(fillC)) {
            throw BuiltinFunctions.typeError(interpreter, "center", "str", fillC.getTypeName());
        }
        if (!interpreter.isNone(fillC)) {
            fillChars = getValue(interpreter, fillC);
            if (fillChars.isEmpty()) {
                fillChars = " ";
            }
        }
        return fillChars;
    }

    private BuiltinInstance<String> just(Interpreter interpreter, String name, boolean left,
                                         List<PyObject> args, Map<String, PyObject> kwargs) {
        BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
        List<PyObject> needArgs = need2Args(interpreter, name, args, kwargs);
        String value = getValue(interpreter, needArgs.get(0));
        int width = INTEGER.getValue(interpreter, needArgs.get(1)).intValue();
        String fillChars = getFillChar(interpreter, name, needArgs.get(2));

        if (width <= value.length()) {
            return ((BuiltinInstance<String>) needArgs.get(0));
        }

        String fc = fillChars.repeat((int) Math.ceil(((float) width -
                (float) value.length()) / (float) fillChars.length()));
        return interpreter.createString(left ? value + fc : fc + value);
    }

    private BuiltinInstance<String> center(Interpreter interpreter, List<PyObject> args,
                                           Map<String, PyObject> kwargs) {
        BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
        List<PyObject> needArgs = need2Args(interpreter, "center", args, kwargs);
        PyObject self = needArgs.get(0);
        String value = getValue(interpreter, self);
        PyObject pyWidth = needArgs.get(1);
        PyObject fillC = needArgs.get(2);
        int width = INTEGER.getValue(interpreter, pyWidth).intValue();
        String fillChars = getFillChar(interpreter, "center", fillC);

        if (width <= value.length()) {
            return ((BuiltinInstance<String>) args.get(0));
        }

        int padding = width - value.length();
        int leftPad = padding / 2;
        int rightPad = padding - leftPad;

        String sb = fillChars.repeat(leftPad) + value +
                fillChars.repeat(Math.max(0, rightPad));
        return interpreter.createString(sb);

    }

    private BuiltinInstance<Boolean> startsOrEndsWith(Interpreter interpreter, String name, boolean left, List<PyObject> args, Map<String, PyObject> kwargs) {
        List<PyObject> needArgs = need3Args(interpreter, name, args, kwargs);
        PyObject self = needArgs.get(0);
        PyObject pattern = needArgs.get(1);
        PyObject start = needArgs.get(2);
        PyObject end = needArgs.get(3);
        String value = subString(interpreter, name, self, start, end);
        String patternVal = getValue(interpreter, pattern);
        return interpreter.boolValue(left ? value.startsWith(patternVal) : value.endsWith(patternVal));
    }

    private BuiltinInstance<Boolean> isAlphaNum(Interpreter interpreter, PyObject self) {
        String value = getValue(interpreter, self);
        return interpreter.boolValue(value.chars().allMatch(Character::isLetterOrDigit));
    }

    private BuiltinInstance<Boolean> isAlpha(Interpreter interpreter, PyObject self) {
        String value = getValue(interpreter, self);
        return interpreter.boolValue(value.chars().allMatch(Character::isLetter));
    }

    private BuiltinInstance<Boolean> isDigit(Interpreter interpreter, PyObject self) {
        String value = getValue(interpreter, self);
        return interpreter.boolValue(value.chars().allMatch(Character::isDigit));
    }

    private BuiltinInstance<Boolean> isDecimal(Interpreter interpreter, PyObject self) {
        String value = getValue(interpreter, self);
        return interpreter.boolValue(value.chars().allMatch(c -> {
            int num = Character.getNumericValue(c);
            return num >= 0 && num <= 9;
        }));
    }

    private BuiltinInstance<Boolean> isNumeric(Interpreter interpreter, PyObject self) {
        String value = getValue(interpreter, self);
        return interpreter.boolValue(value.chars().allMatch(c -> Character.getNumericValue(c) >= 0));
    }

    private BuiltinInstance<Boolean> isLower(Interpreter interpreter, PyObject self) {
        String value = getValue(interpreter, self);
        if (value.isBlank()) return interpreter.boolFalse();
        boolean hasLower = false;
        for (char c : value.toCharArray()) {
            if (Character.isUpperCase(c)) return interpreter.boolFalse();
            if (Character.isLowerCase(c)) hasLower = true;
        }
        return interpreter.boolValue(hasLower);
    }

    private BuiltinInstance<Boolean> isUpper(Interpreter interpreter, PyObject self) {
        String value = getValue(interpreter, self);
        if (value.isBlank()) return interpreter.boolFalse();
        boolean hasUpper = false;
        for (char c : value.toCharArray()) {
            if (Character.isLowerCase(c)) return interpreter.boolFalse();
            if (Character.isUpperCase(c)) hasUpper = true;
        }
        return interpreter.boolValue(hasUpper);
    }

    private BuiltinInstance<Boolean> isSpace(Interpreter interpreter, PyObject self) {
        String value = getValue(interpreter, self);
        if (value.isEmpty()) return interpreter.boolFalse();
        return interpreter.boolValue(value.isBlank());
    }

    private BuiltinInstance<Boolean> isTitle(Interpreter interpreter, PyObject self) {
        String value = getValue(interpreter, self);
        if (value.isBlank()) return interpreter.boolFalse();
        boolean previousWasNonLetter = true;
        boolean foundLetter = false;

        for (char c : value.toCharArray()) {
            if (Character.isLetter(c)) {
                foundLetter = true;
                if (previousWasNonLetter) {
                    if (!Character.isUpperCase(c)) return interpreter.boolFalse();
                    previousWasNonLetter = false;
                } else {
                    if (!Character.isLowerCase(c)) return interpreter.boolFalse();
                }
            } else {
                previousWasNonLetter = true;
            }
        }

        return interpreter.boolValue(foundLetter);
    }

    private BuiltinInstance<Long> index(Interpreter interpreter, String name, boolean left,
                                        List<PyObject> args, Map<String, PyObject> kwargs) {
        BuiltinInstance<Long> result = find(interpreter, name, left, args, kwargs);
        if (result.getValue() < 0) {
            throw BuiltinFunctions.error(interpreter, "IndexError", "pattern not found");
        }
        return result;
    }

    private BuiltinInstance<Long> count(Interpreter interpreter,
                                        List<PyObject> args, Map<String, PyObject> kwargs) {
        List<PyObject> needArgs = need3Args(interpreter, "count", args, kwargs);
        PyObject self = needArgs.get(0);
        String pattern = getValue(interpreter, needArgs.get(1));
        String value = subString(interpreter, "count", self, needArgs.get(2), needArgs.get(3));
        if (pattern.isEmpty()) {
            return interpreter.getInteger(0);
        }
        int count = 0, pos = 0, patternLen = pattern.length();
        while ((pos = value.indexOf(pattern, pos)) > -1) {
            count ++;
            pos += patternLen;
        }
        return interpreter.getInteger(count);
    }

    private List<PyObject> need3Args(Interpreter interpreter, String name,
                                     List<PyObject> args, Map<String, PyObject> kwargs) {
        if (args.isEmpty() || !(args.get(0) instanceof BuiltinInstance ins)) {
            throw BuiltinFunctions.typeError(interpreter, "'" + name + "' is not an static method");
        } else if (!ins.is(this)) {
            throw BuiltinFunctions.typeError(interpreter, "'" + name + "' is not an static method");
        }
        boolean hasKwarg = kwargs != null;
        int argCount = args.size() + (hasKwarg ? kwargs.size() : 0);
        if (argCount > 4 || argCount < 2) {
            throw BuiltinFunctions.atRangeNArgs(interpreter, name, 1, 3, argCount - 1);
        }
        PyObject self = args.get(0);
        PyObject pattern = args.get(1);
        PyObject start = interpreter.none();
        PyObject end = interpreter.none();
        if (args.size() == 2) {
            if (hasKwarg) {
                if (kwargs.containsKey("start")) {
                    start = kwargs.get("start");
                } else if (kwargs.containsKey("end")) {
                    end = kwargs.get("end");
                }
            }
            return List.of(self, pattern, start, end);
        } else if (args.size() == 3) {
            start = args.get(2);
            if (hasKwarg) {
                if (kwargs.containsKey("end")) {
                    end = kwargs.get("end");
                }
            }
            return List.of(self, pattern, start, end);
        } else {
            start = args.get(2);
            end = args.get(3);
            return List.of(self, pattern, self, end);
        }
    }


    private BuiltinInstance<Long> find(Interpreter interpreter, String name,
                                       boolean left, List<PyObject> args,
                                       Map<String, PyObject> kwargs) {
        List<PyObject> need3Args = need3Args(interpreter, name, args, kwargs);
        PyObject self = need3Args.get(0);
        PyObject pattern = need3Args.get(1);
        PyObject start = need3Args.get(2);
        PyObject end = need3Args.get(3);
        return find(interpreter, name, left, self, pattern, start, end);
    }

    private BuiltinInstance<Long> find(Interpreter interpreter, String name, boolean left,
                                       PyObject self, PyObject pattern, PyObject start,
                                       PyObject end) {
        String patternStr = getValue(interpreter, pattern);
        String value = subString(interpreter, name, self, start, end);
        return interpreter.getInteger(left ? value.indexOf(patternStr) : value.lastIndexOf(patternStr));
    }

    private String subString(Interpreter interpreter, String name,
                             PyObject self, PyObject start, PyObject end) {
        String value = getValue(interpreter, self);
        int len = value.length();

        int startVal = 0, endVal = len;
        BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
        if (interpreter.isInt(start)) {
            startVal = INTEGER.getValue(interpreter, start).intValue();
        } else if (!interpreter.isNone(start)) {
            throw BuiltinFunctions.typeError(interpreter, name, "int", start.getTypeName());
        }
        if (interpreter.isInt(end)) {
            endVal = INTEGER.getValue(interpreter, end).intValue();
        } else if (!interpreter.isNone(end)) {
            throw BuiltinFunctions.typeError(interpreter, name, "int", end.getTypeName());
        }
        if (startVal < 0) startVal += len;
        if (endVal < 0) endVal += len;
        if (startVal < 0 || endVal < 0 || endVal > len || startVal > endVal) {
            throw badValue(interpreter, name + " value out of range");
        }
        return value.substring(startVal, endVal);
    }



    private BuiltinInstance<String> swapCase(Interpreter interpreter, PyObject self) {
        String value = getValue(interpreter, self);
        StringBuilder builder = new StringBuilder();
        for (char c : value.toCharArray()) {
            if (Character.isLowerCase(c)) {
                builder.append(Character.toUpperCase(c));
            } else if (Character.isUpperCase(c)) {
                builder.append(Character.toLowerCase(c));
            } else {
                builder.append(c);
            }
        }
        return interpreter.createString(builder.toString());
    }

    private BuiltinInstance<String> capitalize(Interpreter interpreter, PyObject self) {
        String value = getValue(interpreter, self);
        if (value.isBlank()) {
            return (BuiltinInstance<String>) self;
        }
        return interpreter.createString(Character.toUpperCase(value.charAt(0)) +
                value.substring(1).toLowerCase());
    }

    private BuiltinInstance<String> caseFold(Interpreter interpreter, PyObject self) {
        String value = getValue(interpreter, self);
        if (value.isBlank()) {
            return (BuiltinInstance<String>) self;
        }
        return interpreter.createString(value.toLowerCase());
    }

    private BuiltinInstance<String> upper(Interpreter interpreter, PyObject self) {
        String value = getValue(interpreter, self);
        if (value.isBlank()) {
            return (BuiltinInstance<String>) self;
        }
        return interpreter.createString(value.toUpperCase());
    }

    private BuiltinInstance<String> lower(Interpreter interpreter, PyObject self) {
        String value = getValue(interpreter, self);
        if (value.isBlank()) {
            return (BuiltinInstance<String>) self;
        }
        return interpreter.createString(value.toLowerCase());
    }

    private BuiltinInstance<String> title(Interpreter interpreter, PyObject self) {
        String value = getValue(interpreter, self);
        if (value.isBlank()) {
            return (BuiltinInstance<String>) self;
        }
        StringBuilder builder = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : value.toCharArray()) {
            if (Character.isLetter(c)) {
                if (capitalizeNext) {
                    builder.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    builder.append(Character.toLowerCase(c));
                }
            } else {
                builder.append(c);
                capitalizeNext = true;
            }
        }
        return interpreter.createString(builder.toString());
    }

    private BuiltinInstance<String> sideStrip(Interpreter interpreter, String name, List<PyObject> args, boolean isLeft) {
        if (args.isEmpty() || !(args.get(0) instanceof BuiltinInstance ins)) {
            throw BuiltinFunctions.typeError(interpreter, "'" + name + "' is not an static method");
        } else if (!ins.is(this)) {
            throw BuiltinFunctions.typeError(interpreter, "'" + name + "' is not an static method");
        }
        PyObject self = args.get(0);
        String value = getValue(interpreter, self);
        if (value.isEmpty()) {
            return ((BuiltinInstance<String>) self);
        }
        if (args.size() == 1) {
            return interpreter.createString(
                    value.replaceAll(isLeft ? "^\\s+" : "\\s+$", "")
            );
        } else if (args.size() == 2) {
            String stripChars = null;
            PyObject arg = args.get(1);
            if (is(arg)) {
                stripChars = getValue(interpreter, arg);
            } else if (interpreter.isNone(arg)) {
                // do nothing, just pass
            } else {
                throw BuiltinFunctions.typeError(interpreter, name, "str", arg.getTypeName());
            }
            if (stripChars != null && !stripChars.isEmpty()) {
                String result = value;
                while (result.length() >= stripChars.length() &&
                        isLeft ? result.startsWith(stripChars) : result.endsWith(stripChars)) {
                    result = isLeft ? result.substring(stripChars.length()) :
                            result.substring(0, result.length() - stripChars.length());
                }
                return interpreter.createString(result);
            } else {
                return interpreter.createString(
                        value.replaceAll(isLeft ? "^\\s+" : "\\s+$", "")
                );
            }
        } else {
            throw BuiltinFunctions.atMostNArgs(interpreter, name, 1, args.size() - 1);
        }
    }

    private BuiltinInstance<String> strip(Interpreter interpreter, List<PyObject> args) {
        if (args.isEmpty() || !(args.get(0) instanceof BuiltinInstance ins)) {
            throw BuiltinFunctions.typeError(interpreter, "'strip' is not an static method");
        } else if (!ins.is(this)) {
            throw BuiltinFunctions.typeError(interpreter, "'strip' is not an static method");
        }
        PyObject self = args.get(0);
        String value = getValue(interpreter, self);
        if (value.isEmpty()) {
            return ((BuiltinInstance<String>) self);
        }
        if (args.size() == 1) {
            if (value.isBlank()) {
                return interpreter.createString("");
            } else {
                return interpreter.createString(value.trim());
            }
        } else if (args.size() == 2) {
            PyObject additionalArg = args.get(1);
            String stripChars = null;
            if (is(additionalArg)) {
                stripChars = getValue(interpreter, additionalArg);
            } else if (interpreter.isNone(additionalArg)) {
                // do nothing, just skip;
            } else {
                throw BuiltinFunctions.typeError(interpreter, "strip",
                        "str", additionalArg.getTypeName());
            }
            if (stripChars != null && !stripChars.isEmpty()) {
                String result = value;
                while (result.length() >= stripChars.length() &&
                        result.startsWith(stripChars)) {
                    result = result.substring(stripChars.length());
                }
                while (result.length() >= stripChars.length() && result.endsWith(stripChars)) {
                    result = result.substring(0, result.length() - stripChars.length());
                }
                return interpreter.createString(result);
            } else {
                return interpreter.createString(value.trim());
            }
        } else {
            throw BuiltinFunctions.atMostNArgs(interpreter, "strip", 1, args.size() - 1);
        }
    }



    public BuiltinInstance<Boolean> contains(Interpreter interpreter, PyObject self, PyObject others) {
        if (!is(others)) {
            return interpreter.boolFalse();
        }
        String value = getValue(interpreter, self);
        String pattern = getValue(interpreter, others);
        return interpreter.boolValue(value.contains(pattern));
    }

    public BuiltinInstance<String> mul(Interpreter interpreter, PyObject self, PyObject others) {
        BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
        String value = getValue(interpreter, self);
        if (!interpreter.isInt(others)) {
            throw BuiltinFunctions.typeError(interpreter, "__mul__", "str", others.getTypeName());
        }
        long otherValue = INTEGER.getValue(interpreter, others);
        return interpreter.createString(value.repeat((int) otherValue));
    }

    public BuiltinInstance<String> add(Interpreter interpreter, PyObject self, PyObject others) {
        String value = getValue(interpreter, self);
        if (!is(others)) {
            throw BuiltinFunctions.typeError(interpreter, "__add__", "str", others.getTypeName());
        }
        String otherValue = getValue(interpreter, others);
        return interpreter.createString(value + otherValue);
    }

    public BuiltinInstance<Boolean> eq(Interpreter interpreter, PyObject self, PyObject other) {
        if (!is(other)) return interpreter.boolFalse();
        String b1 = getValue(interpreter, self);
        String b2 = getValue(interpreter, other);
        return interpreter.boolValue(b1.equals(b2));
    }

    public BuiltinInstance<Boolean> compare(Interpreter interpreter, PyObject self,
                                            PyObject others, Function<Integer, Boolean> op) {
        if (!is(others)) {
            throw BuiltinFunctions.typeError(interpreter, "'<' not supported between " +
                    "instances of 'str' and '" + others.getTypeName() + "'");
        }
        String b1 = getValue(interpreter, self);
        String b2 = getValue(interpreter, others);
        return interpreter.boolValue(op.apply(b1.compareTo(b2)));
    }

    public BuiltinInstance<Long> len(Interpreter interpreter, PyObject self) {
        String value = getValue(interpreter, self);
        return interpreter.getInteger(value.length());
    }

    public BuiltinInstance<String> getItem(Interpreter interpreter, PyObject self, PyObject index) {
        if (!interpreter.isInt(index)) {
            throw BuiltinFunctions.typeError(interpreter, "__getitem__", "int", index.getTypeName());
        }
        BuiltinClass<Long> INTEGER = interpreter.getMemoryModel().getINTEGER();
        String value = getValue(interpreter, self);
        long indexValue = INTEGER.getValue(interpreter, index);
        if (indexValue < 0) indexValue += value.length();
        if (indexValue < 0 || indexValue >= value.length()) {
            throw BuiltinFunctions.valueError(interpreter, "string index out of range");
        }
        return interpreter.createString(String.valueOf(value.charAt((int) indexValue)));
    }

    public BuiltinInstance<String> getSlice(Interpreter interpreter, List<PyObject> args,
                                            Map<String, PyObject> kwargs) {
        if (args.size() == 0 || !(args.get(0) instanceof BuiltinInstance ins)) {
            throw BuiltinFunctions.typeError(interpreter, "'__getslice__' is not an static method");
        } else if (!ins.is(this)) {
            throw BuiltinFunctions.typeError(interpreter, "'__getslice__' is not an static method");
        }
        boolean hasKwarg = kwargs != null;
        int argSize = args.size() + (hasKwarg ? kwargs.size() : 0);
        if (argSize > 4) {
            throw BuiltinFunctions.atMostNArgs(interpreter, "__getslice__", 3, argSize - 1);
        }
        PyObject start = interpreter.none();
        PyObject stop = interpreter.none();
        PyObject step = interpreter.getInteger(1);

        boolean onlyPositionArg = argSize == args.size();
        if (args.size() == 1 && hasKwarg) {
            if (kwargs.containsKey("start")) {
                start = kwargs.get("start");
            }
            if (kwargs.containsKey("stop")) {
                stop = kwargs.get("stop");
            }
            if (kwargs.containsKey("step")) {
                step = kwargs.get("step");
            }
        } else if (args.size() == 2) {
            if (onlyPositionArg) {
                start = args.get(1);
            } else if (hasKwarg) {
                if (kwargs.containsKey("step")) {
                    step = kwargs.get("step");
                }
                if (kwargs.containsKey("stop")) {
                    stop = kwargs.get("stop");
                }
            }
        } else if (args.size() == 3) {
            if (onlyPositionArg) {
                start = args.get(1);
                stop = args.get(2);
            } else if (hasKwarg) {
                if (kwargs.containsKey("step")) {
                    step = kwargs.get("step");
                }
            }
        } else if (args.size() == 4) {
            start = args.get(1);
            stop = args.get(2);
            step = args.get(3);
        }
        return getSlice(interpreter, ins, start, stop, step);
    }

    public BuiltinInstance<String> getSlice(Interpreter interpreter, PyObject self, PyObject start,
                                            PyObject end, PyObject step) {
        String value = getValue(interpreter, self);
        Slice slice = new Slice(interpreter, start, end, step);
        long[] indices = slice.indices(value.length());

        long startIdx = indices[0];
        long stopIdx = indices[1];
        long stepVal = indices[2];
        StringBuilder builder = new StringBuilder();
        for (long i = startIdx; stepVal > 0 ? (i < stopIdx) : (i > stopIdx); i += stepVal) {
            builder.append(value.charAt((int) i));
        }
        return interpreter.createString(builder.toString());
    }

    @Override
    public BuiltinClass<String> getPyClass(Interpreter interpreter) {
        return interpreter.getMemoryModel().getSTR();
    }

    @Override
    public boolean isValidLiteral(Literal literal) {
        return literal.getValue() instanceof String;
    }

    @Override
    public int hashCode(BuiltinInstance<String> instance) {
        return instance.getValue().hashCode();
    }
}

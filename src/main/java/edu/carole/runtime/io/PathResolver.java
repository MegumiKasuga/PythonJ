package edu.carole.runtime.io;

/**
 * 路径解析工具 - 处理跨平台路径和协议解析
 * 解决Windows下路径中包含冒号的问题
 */
public class PathResolver {
    
    // Windows兼容的协议分隔符
    private static final String WINDOWS_SCHEME_DELIMITER = "___";
    private static final String STANDARD_SCHEME_DELIMITER = "://";
    
    /**
     * 解析标识符中的协议
     * @param identifier 资源标识符
     * @return 解析结果
     */
    public static ParseResult parseIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return new ParseResult(null, identifier);
        }
        
        // 首先尝试标准协议格式 (scheme://)
        String scheme = tryParseStandardScheme(identifier);
        if (scheme != null) {
            String path = identifier.substring(scheme.length() + STANDARD_SCHEME_DELIMITER.length());
            return new ParseResult(scheme, path);
        }
        
        // 然后尝试Windows兼容格式 (scheme___)
        scheme = tryParseWindowsScheme(identifier);
        if (scheme != null) {
            String path = identifier.substring(scheme.length() + WINDOWS_SCHEME_DELIMITER.length());
            return new ParseResult(scheme, path);
        }
        
        // 如果都不匹配，返回原始标识符
        return new ParseResult(null, identifier);
    }
    
    /**
     * 尝试解析标准协议格式
     */
    private static String tryParseStandardScheme(String identifier) {
        int schemeEndIndex = identifier.indexOf(STANDARD_SCHEME_DELIMITER);
        if (schemeEndIndex > 0) {
            String potentialScheme = identifier.substring(0, schemeEndIndex);
            if (isValidScheme(potentialScheme)) {
                return potentialScheme;
            }
        }
        return null;
    }
    
    /**
     * 尝试解析Windows兼容协议格式
     */
    private static String tryParseWindowsScheme(String identifier) {
        int schemeEndIndex = identifier.indexOf(WINDOWS_SCHEME_DELIMITER);
        if (schemeEndIndex > 0) {
            String potentialScheme = identifier.substring(0, schemeEndIndex);
            if (isValidScheme(potentialScheme)) {
                return potentialScheme;
            }
        }
        return null;
    }
    
    /**
     * 检查是否为有效的协议名称
     */
    private static boolean isValidScheme(String scheme) {
        return scheme != null && 
               scheme.length() > 0 && 
               scheme.matches("^[a-zA-Z][a-zA-Z0-9+.-]*$");
    }
    
    /**
     * 创建Windows兼容的标识符
     * @param scheme 协议名称
     * @param path 路径
     * @return Windows兼容的标识符
     */
    public static String createWindowsCompatibleIdentifier(String scheme, String path) {
        if (scheme == null || scheme.trim().isEmpty()) {
            return path;
        }
        return scheme + WINDOWS_SCHEME_DELIMITER + path;
    }
    
    /**
     * 创建标准标识符
     * @param scheme 协议名称
     * @param path 路径
     * @return 标准标识符
     */
    public static String createStandardIdentifier(String scheme, String path) {
        if (scheme == null || scheme.trim().isEmpty()) {
            return path;
        }
        return scheme + STANDARD_SCHEME_DELIMITER + path;
    }
    
    /**
     * 检查是否为Windows系统
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
    
    /**
     * 解析结果类
     */
    public static class ParseResult {
        private final String scheme;
        private final String path;
        
        public ParseResult(String scheme, String path) {
            this.scheme = scheme;
            this.path = path;
        }
        
        public String getScheme() {
            return scheme;
        }
        
        public String getPath() {
            return path;
        }
        
        public boolean hasScheme() {
            return scheme != null && !scheme.trim().isEmpty();
        }
        
        @Override
        public String toString() {
            return "ParseResult{scheme='" + scheme + "', path='" + path + "'}";
        }
    }
}

package edu.carole.runtime.io;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * I/O管理器 - 管理Python解释器的文件I/O操作
 * 提供可插拔的I/O提供者机制，允许Java代码自定义I/O行为
 */
public class IOManager {
    
    // 默认I/O提供者
    private IOProvider defaultProvider;
    
    // 协议特定的I/O提供者映射 (scheme -> provider)
    private final Map<String, IOProvider> providers = new ConcurrentHashMap<>();
    
    // 单例实例
    private static volatile IOManager instance;
    
    // 控制台I/O提供者（如果已注册）
    private ConsoleIOProvider consoleProvider;
    
    /**
     * 私有构造函数
     */
    private IOManager() {
        // 设置默认的文件系统提供者
        this.defaultProvider = new FileIOProvider();
        this.providers.put("file", this.defaultProvider);
    }
    
    /**
     * 获取单例实例
     * @return IOManager实例
     */
    public static IOManager getInstance() {
        if (instance == null) {
            synchronized (IOManager.class) {
                if (instance == null) {
                    instance = new IOManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 设置默认I/O提供者
     * @param provider 新的默认提供者
     */
    public void setDefaultProvider(IOProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
        this.defaultProvider = provider;
    }
    
    /**
     * 获取默认I/O提供者
     * @return 默认提供者
     */
    public IOProvider getDefaultProvider() {
        return defaultProvider;
    }
    
    /**
     * 获取控制台I/O提供者
     * @return 控制台提供者，如果未注册则返回null
     */
    public ConsoleIOProvider getConsoleProvider() {
        return consoleProvider;
    }
    
    /**
     * 设置控制台输入流
     * @param inputStream 新的输入流
     */
    public void setConsoleInputStream(InputStream inputStream) {
        if (consoleProvider != null) {
            consoleProvider.setInputStream(inputStream);
        }
    }
    
    /**
     * 设置控制台输出流
     * @param outputStream 新的输出流
     */
    public void setConsoleOutputStream(PrintStream outputStream) {
        if (consoleProvider != null) {
            consoleProvider.setOutputStream(outputStream);
        }
    }
    
    /**
     * 获取当前控制台输入流
     * @return 控制台输入流，如果控制台提供者未注册则返回System.in
     */
    public InputStream getConsoleInputStream() {
        return consoleProvider != null ? consoleProvider.getInputStream() : System.in;
    }
      /**
     * 获取当前控制台输出流
     * @return 控制台输出流，如果控制台提供者未注册则返回System.out
     */
    public PrintStream getConsoleOutputStream() {
        return consoleProvider != null ? consoleProvider.getOutputStream() : System.out;
    }
    
    /**
     * 从控制台输入流读取一行
     * 这个方法确保正确处理不同类型的输入流
     * @return 读取的行，如果到达流末尾则返回null
     * @throws IOException 如果读取失败
     */
    public String readConsoleLine() throws IOException {
        InputStream inputStream = getConsoleInputStream();
        
        // 对于ByteArrayInputStream，我们需要手动读取到换行符
        if (inputStream instanceof ByteArrayInputStream) {
            StringBuilder line = new StringBuilder();
            int ch;
            while ((ch = inputStream.read()) != -1) {
                if (ch == '\n') {
                    break;
                }
                if (ch != '\r') { // 忽略回车符
                    line.append((char) ch);
                }
            }
            return ch == -1 && line.length() == 0 ? null : line.toString();
        } else {
            // 对于其他输入流，使用BufferedReader
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            return reader.readLine();
        }
    }
    
    /**
     * 注册协议特定的I/O提供者
     * @param scheme 协议名称（如 "file", "memory", "http"等）
     * @param provider I/O提供者
     */
    public void registerProvider(String scheme, IOProvider provider) {
        if (scheme == null || scheme.trim().isEmpty()) {
            throw new IllegalArgumentException("Scheme cannot be null or empty");
        }
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
        
        // 特殊处理控制台提供者
        if ("console".equals(scheme.toLowerCase()) && provider instanceof ConsoleIOProvider) {
            this.consoleProvider = (ConsoleIOProvider) provider;
        }
        
        providers.put(scheme.toLowerCase(), provider);
    }
    
    /**
     * 移除协议特定的I/O提供者
     * @param scheme 协议名称
     * @return 被移除的提供者，如果不存在则返回null
     */
    public IOProvider unregisterProvider(String scheme) {
        if (scheme == null) {
            return null;
        }
        return providers.remove(scheme.toLowerCase());
    }
      /**
     * 获取指定标识符的I/O提供者
     * @param identifier 资源标识符
     * @return 合适的I/O提供者
     */
    public IOProvider getProvider(String identifier) {
        if (identifier == null) {
            return defaultProvider;
        }
        
        // 使用PathResolver解析协议
        PathResolver.ParseResult result = PathResolver.parseIdentifier(identifier);
        if (result.hasScheme()) {
            IOProvider provider = providers.get(result.getScheme().toLowerCase());
            if (provider != null) {
                return provider;
            }
        }
        
        return defaultProvider;
    }
      /**
     * 创建输入流
     * @param identifier 资源标识符
     * @param mode 打开模式
     * @return 输入流
     * @throws IOException 如果创建失败
     */
    public InputStream createInputStream(String identifier, String mode) throws IOException {
        IOProvider provider = getProvider(identifier);
        
        // 解析路径，传递实际路径给提供者
        PathResolver.ParseResult result = PathResolver.parseIdentifier(identifier);
        String actualPath = result.hasScheme() ? result.getPath() : identifier;
        
        return provider.createInputStream(actualPath, mode);
    }
      /**
     * 创建输出流
     * @param identifier 资源标识符
     * @param mode 打开模式
     * @return 输出流
     * @throws IOException 如果创建失败
     */
    public OutputStream createOutputStream(String identifier, String mode) throws IOException {
        IOProvider provider = getProvider(identifier);
        
        // 解析路径，传递实际路径给提供者
        PathResolver.ParseResult result = PathResolver.parseIdentifier(identifier);
        String actualPath = result.hasScheme() ? result.getPath() : identifier;
        boolean append = mode != null && mode.startsWith("a");
        
        return provider.createOutputStream(actualPath, mode, append);
    }
      /**
     * 检查资源是否存在
     * @param identifier 资源标识符
     * @return 是否存在
     */
    public boolean exists(String identifier) {
        IOProvider provider = getProvider(identifier);
        
        // 解析路径，传递实际路径给提供者
        PathResolver.ParseResult result = PathResolver.parseIdentifier(identifier);
        String actualPath = result.hasScheme() ? result.getPath() : identifier;
        
        return provider.exists(actualPath);
    }
    
    /**
     * 获取所有注册的提供者
     * @return 提供者映射的副本
     */
    public Map<String, IOProvider> getAllProviders() {
        return new ConcurrentHashMap<>(providers);
    }
      /**
     * 重置IOManager到默认状态（主要用于测试）
     */
    public void reset() {
        providers.clear();
        this.defaultProvider = new FileIOProvider();
        this.providers.put("file", this.defaultProvider);
    }
}

package edu.carole.runtime.io;

import java.io.*;

/**
 * IOManager配置类 - 提供便捷的I/O管理器配置方法
 * 允许Java代码轻松配置和管理Python解释器的I/O行为
 */
public class IOManagerConfig {
    
    private final IOManager ioManager;
    
    /**
     * 构造函数
     * @param ioManager 要配置的IOManager实例
     */
    public IOManagerConfig(IOManager ioManager) {
        this.ioManager = ioManager;
    }
    
    /**
     * 获取默认配置的IOManagerConfig
     * @return 默认配置的实例
     */
    public static IOManagerConfig getDefault() {
        return new IOManagerConfig(IOManager.getInstance());
    }
    
    /**
     * 启用内存文件系统支持
     * 注册内存I/O提供者，允许在内存中创建虚拟文件
     * 使用方式：open("memory:///myfile.txt", "w")
     * @return 当前配置实例（支持链式调用）
     */
    public IOManagerConfig enableMemoryFileSystem() {
        ioManager.registerProvider("memory", new MemoryIOProvider());
        return this;
    }
    
    /**
     * 启用网络资源访问支持
     * 注册网络I/O提供者，允许直接读取HTTP/HTTPS资源
     * 使用方式：open("http://example.com/data.txt", "r")
     * @return 当前配置实例（支持链式调用）
     */
    public IOManagerConfig enableNetworkAccess() {
        ioManager.registerProvider("http", new NetworkIOProvider());
        ioManager.registerProvider("https", new NetworkIOProvider());
        return this;
    }
    
    /**
     * 启用控制台I/O支持
     * 注册控制台I/O提供者，允许自定义标准输入输出流
     * @return 当前配置实例（支持链式调用）
     */
    public IOManagerConfig enableConsoleIO() {
        ioManager.registerProvider("console", new ConsoleIOProvider());
        return this;
    }
    
    /**
     * 设置自定义的默认I/O提供者
     * @param provider 新的默认提供者
     * @return 当前配置实例（支持链式调用）
     */
    public IOManagerConfig setDefaultProvider(IOProvider provider) {
        ioManager.setDefaultProvider(provider);
        return this;
    }
    
    /**
     * 注册自定义协议的I/O提供者
     * @param scheme 协议名称（如 "ftp", "custom"等）
     * @param provider I/O提供者实现
     * @return 当前配置实例（支持链式调用）
     */
    public IOManagerConfig registerCustomProvider(String scheme, IOProvider provider) {
        ioManager.registerProvider(scheme, provider);
        return this;
    }
    
    /**
     * 移除指定协议的I/O提供者
     * @param scheme 协议名称
     * @return 当前配置实例（支持链式调用）
     */
    public IOManagerConfig removeProvider(String scheme) {
        ioManager.unregisterProvider(scheme);
        return this;
    }
    
    /**
     * 设置控制台输入流
     * @param inputStream 新的输入流
     * @return 当前配置实例（支持链式调用）
     */
    public IOManagerConfig setConsoleInputStream(InputStream inputStream) {
        ioManager.setConsoleInputStream(inputStream);
        return this;
    }
    
    /**
     * 设置控制台输出流
     * @param outputStream 新的输出流
     * @return 当前配置实例（支持链式调用）
     */
    public IOManagerConfig setConsoleOutputStream(PrintStream outputStream) {
        ioManager.setConsoleOutputStream(outputStream);
        return this;
    }
    
    /**
     * 获取内存I/O提供者（如果已注册）
     * @return 内存I/O提供者，如果未注册则返回null
     */
    public MemoryIOProvider getMemoryProvider() {
        IOProvider provider = ioManager.getAllProviders().get("memory");
        return provider instanceof MemoryIOProvider ? (MemoryIOProvider) provider : null;
    }
    
    /**
     * 获取控制台I/O提供者（如果已注册）
     * @return 控制台I/O提供者，如果未注册则返回null
     */
    public ConsoleIOProvider getConsoleProvider() {
        return ioManager.getConsoleProvider();
    }
    
    /**
     * 重置IOManager到默认状态
     * @return 当前配置实例（支持链式调用）
     */
    public IOManagerConfig reset() {
        ioManager.reset();
        return this;
    }
    
    /**
     * 获取底层的IOManager实例
     * @return IOManager实例
     */
    public IOManager getIOManager() {
        return ioManager;
    }
    
    /**
     * 创建一个完整配置的IOManager（启用所有内置提供者）
     * @return 完整配置的IOManagerConfig实例
     */
    public static IOManagerConfig createFullConfiguration() {
        return getDefault()
                .enableMemoryFileSystem()
                .enableNetworkAccess()
                .enableConsoleIO();
    }
}

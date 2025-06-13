package edu.carole.runtime.io;

import edu.carole.runtime.file_context.PyFileContext;

import java.io.*;
import java.nio.file.*;

/**
 * 文件系统I/O提供者 - 默认的文件系统访问实现
 * 提供对本地文件系统的标准访问
 */
public class FileIOProvider implements IOProvider {
    
    @Override
    public InputStream createInputStream(String identifier, String mode) throws IOException {
        if (!supportsMode(mode)) {
            throw new IOException("Unsupported mode '" + mode + "' for reading");
        }
        
        Path path = Paths.get(identifier);
        
        // 如果文件不存在且是读取模式，创建空文件
        if (!Files.exists(path) && mode.startsWith("r")) {
            Files.createDirectories(path.getParent() != null ? path.getParent() : Paths.get(""));
            Files.createFile(path);
        }
        
        return Files.newInputStream(path, StandardOpenOption.READ);
    }
    
    @Override
    public OutputStream createOutputStream(String identifier, String mode, boolean append) throws IOException {
        if (!supportsMode(mode)) {
            throw new IOException("Unsupported mode '" + mode + "' for writing");
        }
        
        Path path = Paths.get(identifier);
        
        // 确保父目录存在
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        
        // 根据模式设置打开选项
        StandardOpenOption[] options;
        if (append) {
            options = new StandardOpenOption[]{
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND
            };
        } else {
            options = new StandardOpenOption[]{
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
            };
        }
        
        return Files.newOutputStream(path, options);
    }
    
    @Override
    public boolean exists(String identifier) {
        try {
            Path path = Paths.get(identifier);
            return Files.exists(path);
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String getProviderName() {
        return "FileSystem";
    }
    
    @Override
    public boolean supportsMode(String mode) {
        return PyFileContext.supportsMode(mode);
    }
}

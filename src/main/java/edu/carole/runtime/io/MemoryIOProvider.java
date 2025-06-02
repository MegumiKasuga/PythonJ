package edu.carole.runtime.io;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存I/O提供者 - 在内存中模拟文件系统
 * 允许创建虚拟文件，数据存储在内存中
 * 适用于测试、临时数据处理等场景
 */
public class MemoryIOProvider implements IOProvider {
    
    // 内存中的文件存储 (filename -> content)
    private final Map<String, byte[]> memoryFiles = new ConcurrentHashMap<>();
    
    @Override
    public InputStream createInputStream(String identifier, String mode) throws IOException {
        if (!supportsMode(mode)) {
            throw new IOException("Unsupported mode '" + mode + "' for reading");
        }
        
        byte[] content = memoryFiles.get(identifier);
        if (content == null) {
            // 如果文件不存在，创建空文件
            content = new byte[0];
            memoryFiles.put(identifier, content);
        }
        
        return new ByteArrayInputStream(content);
    }
    
    @Override
    public OutputStream createOutputStream(String identifier, String mode, boolean append) throws IOException {
        if (!supportsMode(mode)) {
            throw new IOException("Unsupported mode '" + mode + "' for writing");
        }
        
        return new MemoryOutputStream(identifier, append);
    }
    
    @Override
    public boolean exists(String identifier) {
        return memoryFiles.containsKey(identifier);
    }
    
    @Override
    public String getProviderName() {
        return "Memory";
    }
    
    @Override
    public boolean supportsMode(String mode) {
        if (mode == null) {
            return false;
        }
        
        // 支持的模式：r, w, a
        return mode.matches("^[rwa][bt]?\\+?$");
    }
    
    /**
     * 获取内存文件的内容
     * @param identifier 文件标识符
     * @return 文件内容，如果不存在则返回null
     */
    public byte[] getFileContent(String identifier) {
        byte[] content = memoryFiles.get(identifier);
        return content != null ? content.clone() : null;
    }
    
    /**
     * 设置内存文件的内容
     * @param identifier 文件标识符
     * @param content 文件内容
     */
    public void setFileContent(String identifier, byte[] content) {
        if (content == null) {
            memoryFiles.remove(identifier);
        } else {
            memoryFiles.put(identifier, content.clone());
        }
    }
    
    /**
     * 删除内存文件
     * @param identifier 文件标识符
     * @return 是否成功删除
     */
    public boolean deleteFile(String identifier) {
        return memoryFiles.remove(identifier) != null;
    }
    
    /**
     * 清空所有内存文件
     */
    public void clear() {
        memoryFiles.clear();
    }
    
    /**
     * 获取所有内存文件的标识符
     * @return 文件标识符集合
     */
    public java.util.Set<String> getAllFileNames() {
        return new java.util.HashSet<>(memoryFiles.keySet());
    }
    
    /**
     * 内存输出流实现
     */
    private class MemoryOutputStream extends OutputStream {
        private final String identifier;
        private final boolean append;
        private final ByteArrayOutputStream buffer;
        
        public MemoryOutputStream(String identifier, boolean append) {
            this.identifier = identifier;
            this.append = append;
            this.buffer = new ByteArrayOutputStream();
            
            // 如果是追加模式，先加载现有内容
            if (append) {
                byte[] existing = memoryFiles.get(identifier);
                if (existing != null) {
                    try {
                        buffer.write(existing);
                    } catch (IOException e) {
                        // ByteArrayOutputStream不会真正抛出IOException
                    }
                }
            }
        }
        
        @Override
        public void write(int b) throws IOException {
            buffer.write(b);
        }
        
        @Override
        public void write(byte[] b) throws IOException {
            buffer.write(b);
        }
        
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            buffer.write(b, off, len);
        }
        
        @Override
        public void flush() throws IOException {
            // 将缓冲区内容写入内存文件
            memoryFiles.put(identifier, buffer.toByteArray());
        }
        
        @Override
        public void close() throws IOException {
            flush();
        }
    }
}

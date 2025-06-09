package edu.carole.runtime.io;

import java.io.*;

/**
 * 控制台I/O提供者 - 管理标准输入输出流
 * 提供对System.in和System.out的可配置访问，允许Java代码自定义控制台I/O行为
 */
public class ConsoleIOProvider implements IOProvider {
    
    // 可配置的输入流，默认为System.in
    private InputStream inputStream = System.in;
    
    // 可配置的输出流，默认为System.out
    private PrintStream outputStream = System.out;

    private PrintStream errStream = System.err; // 错误输出流，默认为System.err
    
    /**
     * 设置输入流
     * @param inputStream 新的输入流
     */
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream != null ? inputStream : System.in;
    }
    
    /**
     * 设置输出流
     * @param outputStream 新的输出流
     */
    public void setOutputStream(PrintStream outputStream) {
        this.outputStream = outputStream != null ? outputStream : System.out;
    }

    public void setErrStream(PrintStream errStream) {
        this.errStream = errStream != null ? errStream : System.err;
    }
    
    /**
     * 获取当前输入流
     * @return 当前输入流
     */
    public InputStream getInputStream() {
        return inputStream;
    }
    
    /**
     * 获取当前输出流
     * @return 当前输出流
     */
    public PrintStream getOutputStream() {
        return outputStream;
    }

    public PrintStream getErrStream() {
        return errStream;
    }
    
    @Override
    public InputStream createInputStream(String identifier, String mode) throws IOException {
        // 对于控制台输入，忽略identifier和mode，直接返回配置的输入流
        return inputStream;
    }
    
    @Override
    public OutputStream createOutputStream(String identifier, String mode, boolean append) throws IOException {
        // 对于控制台输出，忽略identifier、mode和append，直接返回配置的输出流
        return outputStream;
    }
    
    @Override
    public boolean exists(String identifier) {
        // 控制台总是可用的
        return true;
    }
    
    @Override
    public String getProviderName() {
        return "Console";
    }
    
    @Override
    public boolean supportsMode(String mode) {
        // 控制台支持基本的读写模式
        return mode != null && (mode.contains("r") || mode.contains("w"));
    }
    
    /**
     * 重置到默认的System.in和System.out
     */
    public void resetToDefaults() {
        this.inputStream = System.in;
        this.outputStream = System.out;
    }
}

package edu.carole.runtime.io;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * 网络I/O提供者 - 支持HTTP/HTTPS协议的文件读取
 * 允许Python代码直接读取网络资源
 * 示例：open("http://example.com/data.txt", "r")
 */
public class NetworkIOProvider implements IOProvider {
    
    @Override
    public InputStream createInputStream(String identifier, String mode) throws IOException {
        if (!supportsMode(mode)) {
            throw new IOException("Unsupported mode '" + mode + "' for network reading");
        }
        
        URL url = new URL(identifier);
        URLConnection connection = url.openConnection();
        
        // 设置合理的超时时间
        connection.setConnectTimeout(5000); // 5秒连接超时
        connection.setReadTimeout(10000);   // 10秒读取超时
        
        return connection.getInputStream();
    }
    
    @Override
    public OutputStream createOutputStream(String identifier, String mode, boolean append) throws IOException {
        // 网络I/O提供者通常不支持写入（只读）
        throw new IOException("Network provider does not support writing to: " + identifier);
    }
    
    @Override
    public boolean exists(String identifier) {
        try {
            URL url = new URL(identifier);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            
            // 尝试获取内容长度或响应码来判断资源是否存在
            if (connection instanceof java.net.HttpURLConnection) {
                java.net.HttpURLConnection httpConn = (java.net.HttpURLConnection) connection;
                httpConn.setRequestMethod("HEAD"); // 只获取头部信息
                int responseCode = httpConn.getResponseCode();
                return responseCode >= 200 && responseCode < 400;
            } else {
                // 对于非HTTP连接，尝试获取输入流
                try (InputStream is = connection.getInputStream()) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String getProviderName() {
        return "Network";
    }
    
    @Override
    public boolean supportsMode(String mode) {
        if (mode == null) {
            return false;
        }
        
        // 只支持读取模式
        return mode.startsWith("r");
    }
}

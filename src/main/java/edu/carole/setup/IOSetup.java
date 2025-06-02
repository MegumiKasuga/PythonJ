package edu.carole.setup;

import edu.carole.runtime.io.IOManagerConfig;

/**
 * IOManager初始化设置
 * 在Python解释器运行前配置I/O管理器
 */
public class IOSetup {
    
    public static void setupIOManager() {
        // 启用所有内置I/O提供者
        IOManagerConfig.createFullConfiguration();
        System.out.println("IOManager configured with all providers");
    }
    
    public static void main(String[] args) {
        setupIOManager();
        
        // 然后运行Python脚本
        if (args.length > 0) {
            try {
                edu.carole.ProjectEntry.main(args);
            } catch (Exception e) {
                System.err.println("Error running Python script: " + e.getMessage());
            }
        }
    }
}

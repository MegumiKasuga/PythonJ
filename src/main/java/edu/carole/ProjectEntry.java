package edu.carole;

import edu.carole.ast.statements.Program;
import edu.carole.interpreter.Interpreter;
import edu.carole.lexer.Lexer;
import edu.carole.lexer.Token;
import edu.carole.parser.Parser;
import edu.carole.runtime.io.IOManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * Python解释器主入口
 */
public class ProjectEntry {
    public static void main(String[] args) {
        // System.out.println("\u0007");
        if (args.length == 0) {
            // 交互式模式 (REPL)
            runRepl();
        } else if (args.length == 1) {
            // 执行文件
            runFile(args[0]);
        } else {
            System.err.println("Usage: java ProjectEntry [script]");
            System.exit(64);
        }
    }
    
    /**
     * 交互式模式
     */
    private static void runRepl() {
        Scanner scanner = new Scanner(System.in);
        Interpreter interpreter = new Interpreter(IOManager.getInstance());
        
        System.out.println("Python 3.x Interpreter (Custom Implementation)");
        System.out.println("Type 'exit()' to quit");
        
        while (true) {
            System.out.print(">>> ");
            String line = scanner.nextLine();
            
            if (line.trim().equals("exit()")) {
                break;
            }
            
            if (line.trim().isEmpty()) {
                continue;
            }
            
            try {
                run("console", line, interpreter);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        
        scanner.close();
    }
    
    /**
     * 执行Python文件
     */
    private static void runFile(String path) {
        try {
            String source = Files.readString(Paths.get(path));
            Interpreter interpreter = new Interpreter(IOManager.getInstance());
            run(path, source, interpreter);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(74);
        } catch (Exception e) {
            System.err.println("Runtime Error: " + e.getMessage());
            System.err.println("Exception type: " + e.getClass().getSimpleName());
            System.err.println("Stack trace:");
            e.printStackTrace();
            System.exit(70);
        }
    }
      /**
     * 执行Python代码
     */
    private static void run(String fileName, String source, Interpreter interpreter) {
        try {
            // 词法分析
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.tokenize();
            
            // 语法分析
            Parser parser = new Parser(fileName, tokens);
            Program program = parser.parse();
            
            // 执行
            interpreter.interpret(program);
            
        } catch (Exception e) {
            System.err.println("Runtime Error: " + e.getMessage());
            System.err.println("Exception type: " + e.getClass().getSimpleName());
            System.err.println("Stack trace:");
            e.printStackTrace();
        }
    }
}




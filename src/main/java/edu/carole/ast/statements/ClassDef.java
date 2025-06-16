package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;
import lombok.Getter;

import java.util.List;
import java.util.ArrayList;

/**
 * 类定义
 */
public class ClassDef extends ASTNode {
    private final String name;
    private final List<ASTNode> body;
    private final List<String> baseClasses; // 父类名列表

    private final int line, column;

    @Getter
    private final String file;
    
    public ClassDef(String file, String name, List<ASTNode> body, int line, int column) {
        this.name = name;
        this.body = body;
        this.baseClasses = new ArrayList<>(); // 无父类
        this.line = line;
        this.column = column;
        this.file = file;
    }
    
    public ClassDef(String file, String name, List<String> baseClasses, List<ASTNode> body, int line, int column) {
        this.name = name;
        this.body = body;
        this.baseClasses = new ArrayList<>(baseClasses);
        this.line = line;
        this.column = column;
        this.file = file;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getColumn() {
        return column;
    }

    public String getName() { return name; }
    public List<ASTNode> getBody() { return body; }
    public List<String> getBaseClasses() { return baseClasses; }
    public boolean hasBaseClasses() { return !baseClasses.isEmpty(); }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitClassDef(this);
    }
}

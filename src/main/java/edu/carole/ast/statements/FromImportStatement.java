package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

import java.util.List;

/**
 * Represents a from import statement: from module import item1, item2 as alias
 */
public class FromImportStatement extends ASTNode {
    private final String moduleName;
    private final List<ImportClause> imports;
    private final boolean importAll; // for "from module import *"
    private final int line, column;
    
    public FromImportStatement(String moduleName, List<ImportClause> imports, int line, int column) {
        this.moduleName = moduleName;
        this.imports = imports;
        this.importAll = false;
        this.line = line;
        this.column = column;
    }
    
    public FromImportStatement(String moduleName, boolean importAll, int line, int column) {
        this.moduleName = moduleName;
        this.imports = null;
        this.importAll = importAll;
        this.line = line;
        this.column = column;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getColumn() {
        return column;
    }

    public String getModuleName() {
        return moduleName;
    }
    
    public List<ImportClause> getImports() {
        return imports;
    }
    
    public boolean isImportAll() {
        return importAll;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitFromImportStatement(this);
    }
    
    /**
     * Represents a single import clause in from statement: item_name or item_name as alias
     */
    public static class ImportClause {
        private final String itemName;
        private final String alias;
        
        public ImportClause(String itemName, String alias) {
            this.itemName = itemName;
            this.alias = alias;
        }
        
        public ImportClause(String itemName) {
            this(itemName, null);
        }
        
        public String getItemName() {
            return itemName;
        }
        
        public String getAlias() {
            return alias;
        }
        
        public String getEffectiveName() {
            return alias != null ? alias : itemName;
        }
        
        @Override
        public String toString() {
            if (alias != null) {
                return itemName + " as " + alias;
            }
            return itemName;
        }
    }
    
    @Override
    public String toString() {
        if (importAll) {
            return "FromImportStatement{from " + moduleName + " import *}";
        }
        return "FromImportStatement{from " + moduleName + " import " + imports + "}";
    }
}

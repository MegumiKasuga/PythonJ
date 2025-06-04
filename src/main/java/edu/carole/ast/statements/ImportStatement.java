package edu.carole.ast.statements;

import edu.carole.ast.ASTNode;
import edu.carole.ast.ast.ASTVisitor;

import java.util.List;

/**
 * Represents an import statement: import module1, module2 as alias
 */
public class ImportStatement extends ASTNode {
    private final List<ImportClause> imports;
    
    public ImportStatement(List<ImportClause> imports) {
        this.imports = imports;
    }
    
    public List<ImportClause> getImports() {
        return imports;
    }
    
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitImportStatement(this);
    }
    
    /**
     * Represents a single import clause: module_name or module_name as alias
     */
    public static class ImportClause {
        private final String moduleName;
        private final String alias;
        
        public ImportClause(String moduleName, String alias) {
            this.moduleName = moduleName;
            this.alias = alias;
        }
        
        public ImportClause(String moduleName) {
            this(moduleName, null);
        }
        
        public String getModuleName() {
            return moduleName;
        }
        
        public String getAlias() {
            return alias;
        }
        
        public String getEffectiveName() {
            return alias != null ? alias : moduleName;
        }
        
        @Override
        public String toString() {
            if (alias != null) {
                return moduleName + " as " + alias;
            }
            return moduleName;
        }
    }
    
    @Override
    public String toString() {
        return "ImportStatement{imports=" + imports + "}";
    }
}

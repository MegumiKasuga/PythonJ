package edu.carole.runtime;

import edu.carole.ast.ASTNode;
import edu.carole.ast.expressions.Literal;
import edu.carole.ast.expressions.Identifier;
import edu.carole.lexer.Lexer;
import edu.carole.lexer.Token;
import edu.carole.parser.Parser;
import edu.carole.interpreter.Interpreter;
import edu.carole.interpreter.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * F-string processor for formatted string literals
 */
public class FStringProcessor {
    private static final Pattern F_STRING_PATTERN = Pattern.compile("\\{([^}]+)\\}");    /**
     * Process an f-string by evaluating embedded expressions
     */
    public static String processFString(String fString, Interpreter interpreter, Environment environment) {
        StringBuilder result = new StringBuilder();
        Matcher matcher = F_STRING_PATTERN.matcher(fString);
        int lastEnd = 0;
        
        while (matcher.find()) {
            // Add the literal text before the expression
            result.append(fString.substring(lastEnd, matcher.start()));
            
            // Extract and evaluate the expression
            String expression = matcher.group(1).trim();
            try {
                Object value = evaluateExpression(expression, interpreter, environment);
                result.append(formatValue(value));
            } catch (Exception e) {
                // If evaluation fails, keep the original expression
                result.append("{").append(expression).append("}");
            }
            
            lastEnd = matcher.end();
        }
        
        // Add any remaining literal text
        result.append(fString.substring(lastEnd));
        
        return result.toString();
    }    /**
     * Evaluate an expression string within an f-string
     */
    private static Object evaluateExpression(String expression, Interpreter interpreter, Environment environment) {
        try {
            // Parse the expression
            Lexer lexer = new Lexer(expression);
            List<Token> tokens = lexer.tokenize();
            
            if (tokens.isEmpty()) {
                return "";
            }
            
            // Use the full parser for all expressions (keep EOF token for proper parsing)
            Parser parser = new Parser(tokens);
            ASTNode expressionNode = parser.parseExpression();
            
            // Evaluate the expression in the current environment
            return interpreter.evaluateExpression(expressionNode, environment);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate f-string expression: " + expression, e);
        }
    }
      /**
     * Parse a token value based on its type
     */
    private static Object parseTokenValue(Token token) {
        switch (token.getType()) {
            case NUMBER:
                String value = token.getValue();
                if (value.contains(".")) {
                    return Double.parseDouble(value);
                } else {
                    return Long.parseLong(value);
                }
            case STRING:
                return token.getValue();
            case TRUE:
                return true;
            case FALSE:
                return false;
            case NONE:
                return null;
            default:
                return token.getValue();
        }
    }
    
    /**
     * Format a value for string interpolation
     */
    private static String formatValue(Object value) {
        if (value == null) {
            return "None";
        } else if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? "True" : "False";
        } else {
            return value.toString();
        }
    }
}

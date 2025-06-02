package edu.carole.parser;

import edu.carole.ast.*;
import edu.carole.ast.statements.*;
import edu.carole.ast.expressions.*;
import edu.carole.ast.expressions.ListComprehension.ComprehensionClause;
import edu.carole.lexer.Token;

import java.util.*;

/**
 * Python语法分析器
 * 将token流转换为抽象语法树
 */
public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }
    
    public Program parse() {
        List<ASTNode> statements = new ArrayList<>();
        
        while (!isAtEnd()) {
            // 跳过换行符
            if (check(Token.Type.NEWLINE)) {
                advance();
                continue;
            }
            
            ASTNode statement = statement();
            if (statement != null) {
                statements.add(statement);
            }
        }
        
        return new Program(statements);
    }    private ASTNode statement() {
        try {
            // Check for decorators (must come before function and class definitions)
            if (match(Token.Type.AT)) return decoratorStatement();
            
            if (match(Token.Type.IF)) return ifStatement();
            if (match(Token.Type.WHILE)) return whileStatement();
            if (match(Token.Type.FOR)) return forStatement();
            if (match(Token.Type.DEF)) return functionDef();
            if (match(Token.Type.CLASS)) return classDef();
            if (match(Token.Type.RETURN)) return returnStatement();
            if (match(Token.Type.BREAK)) return new BreakStatement();            if (match(Token.Type.CONTINUE)) return new ContinueStatement();
            if (match(Token.Type.PASS)) return new PassStatement();
            if (match(Token.Type.TRY)) return tryExceptStatement();
            if (match(Token.Type.WITH)) return withStatement();
            
            return expressionStatement();
        } catch (Exception e) {
            // 错误恢复：跳到下一行
            synchronize();
            return null;
        }
    }
    
    private ASTNode ifStatement() {
        ASTNode condition = expression();
        consume(Token.Type.COLON, "Expected ':' after if condition");
        consume(Token.Type.NEWLINE, "Expected newline after ':'");
        consume(Token.Type.INDENT, "Expected indentation after if statement");
        
        List<ASTNode> thenBranch = block();
        List<ASTNode> elseBranch = new ArrayList<>();
        
        if (match(Token.Type.ELSE)) {
            consume(Token.Type.COLON, "Expected ':' after else");
            consume(Token.Type.NEWLINE, "Expected newline after ':'");
            consume(Token.Type.INDENT, "Expected indentation after else statement");
            elseBranch = block();
        }
        
        return new IfStatement(condition, thenBranch, elseBranch);
    }
    
    private ASTNode whileStatement() {
        ASTNode condition = expression();
        consume(Token.Type.COLON, "Expected ':' after while condition");
        consume(Token.Type.NEWLINE, "Expected newline after ':'");
        consume(Token.Type.INDENT, "Expected indentation after while statement");
        
        List<ASTNode> body = block();
        return new WhileStatement(condition, body);
    }
    
    private ASTNode forStatement() {
        Token variable = consume(Token.Type.IDENTIFIER, "Expected variable name in for loop");
        consume(Token.Type.IN, "Expected 'in' in for loop");
        ASTNode iterable = expression();
        consume(Token.Type.COLON, "Expected ':' after for statement");
        consume(Token.Type.NEWLINE, "Expected newline after ':'");
        consume(Token.Type.INDENT, "Expected indentation after for statement");
        
        List<ASTNode> body = block();
        return new ForStatement(variable.getValue(), iterable, body);
    }
      private ASTNode functionDef() {
        Token name = consume(Token.Type.IDENTIFIER, "Expected function name");
        consume(Token.Type.LEFT_PAREN, "Expected '(' after function name");
        
        List<String> parameters = new ArrayList<>();
        String varargsParam = null;
        
        if (!check(Token.Type.RIGHT_PAREN)) {
            do {
                // Check for *args syntax
                if (match(Token.Type.MULTIPLY)) {
                    Token varargsToken = consume(Token.Type.IDENTIFIER, "Expected parameter name after '*'");
                    varargsParam = varargsToken.getValue();
                    break; // *args must be last parameter for now
                } else {
                    Token param = consume(Token.Type.IDENTIFIER, "Expected parameter name");
                    parameters.add(param.getValue());
                }
            } while (match(Token.Type.COMMA));
        }
        
        consume(Token.Type.RIGHT_PAREN, "Expected ')' after parameters");
        consume(Token.Type.COLON, "Expected ':' after function signature");
        consume(Token.Type.NEWLINE, "Expected newline after ':'");
        consume(Token.Type.INDENT, "Expected indentation after function definition");
        
        List<ASTNode> body = block();
        return new FunctionDef(name.getValue(), parameters, body, varargsParam);
    }
      private ASTNode classDef() {
        Token name = consume(Token.Type.IDENTIFIER, "Expected class name");
        
        List<String> baseClasses = new ArrayList<>();
        
        // 检查是否有继承语法: class Child(Parent):
        if (match(Token.Type.LEFT_PAREN)) {
            if (!check(Token.Type.RIGHT_PAREN)) {
                do {
                    Token baseClass = consume(Token.Type.IDENTIFIER, "Expected base class name");
                    baseClasses.add(baseClass.getValue());
                } while (match(Token.Type.COMMA));
            }
            consume(Token.Type.RIGHT_PAREN, "Expected ')' after base classes");
        }
        
        consume(Token.Type.COLON, "Expected ':' after class name");
        consume(Token.Type.NEWLINE, "Expected newline after ':'");
        consume(Token.Type.INDENT, "Expected indentation after class definition");
        
        List<ASTNode> body = block();
        return new ClassDef(name.getValue(), baseClasses, body);
    }
      private ASTNode returnStatement() {
        ASTNode value = null;
        if (!check(Token.Type.NEWLINE) && !isAtEnd()) {
            value = tupleExpression(); // Use tupleExpression to support comma-separated returns
        }
        return new ReturnStatement(value);
    }      private List<ASTNode> block() {
        List<ASTNode> statements = new ArrayList<>();
        
        while (!check(Token.Type.DEDENT) && !isAtEnd()) {
            if (check(Token.Type.NEWLINE)) {
                advance();
                continue;
            }
            
            ASTNode statement = statement();
            if (statement != null) {
                statements.add(statement);
            }
        }
        
        if (check(Token.Type.DEDENT)) {
            advance();
        }
        
        return statements;
    }    private ASTNode expressionStatement() {
        ASTNode expr = assignment();
          // If assignment() returned a statement (like AssignmentStatement), don't wrap it
        if (expr instanceof AssignmentStatement || 
            expr instanceof CompoundAssignmentStatement ||
            expr instanceof AttributeAssignmentStatement || 
            expr instanceof IndexAssignmentStatement ||
            expr instanceof TupleUnpackingAssignment) {
            return expr;
        }
        
        return new ExpressionStatement(expr);
    }private ASTNode assignment() {
        ASTNode expr = tupleExpression();
        
        if (match(Token.Type.ASSIGN)) {
            ASTNode value = assignment();
              if (expr instanceof Identifier) {
                return new AssignmentStatement(((Identifier) expr).getName(), value);
            } else if (expr instanceof AttributeExpression) {
                AttributeExpression attrExpr = (AttributeExpression) expr;
                return new AttributeAssignmentStatement(attrExpr.getObject(), attrExpr.getAttribute(), value);
            } else if (expr instanceof IndexExpression) {
                IndexExpression indexExpr = (IndexExpression) expr;
                return new IndexAssignmentStatement(indexExpr.getObject(), indexExpr.getIndex(), value);
            } else if (expr instanceof TupleLiteral) {
                // Handle tuple unpacking assignment: a, b, c = expression
                TupleLiteral tuple = (TupleLiteral) expr;
                List<String> targets = new ArrayList<>();
                for (ASTNode element : tuple.getElements()) {
                    if (!(element instanceof Identifier)) {
                        throw new RuntimeException("Invalid tuple unpacking target");
                    }
                    targets.add(((Identifier) element).getName());
                }
                return new TupleUnpackingAssignment(targets, value);
            }
            throw new RuntimeException("Invalid assignment target");        } else if (match(Token.Type.PLUS_ASSIGN, Token.Type.MINUS_ASSIGN, 
                          Token.Type.MULTIPLY_ASSIGN, Token.Type.DIVIDE_ASSIGN,
                          Token.Type.MODULO_ASSIGN, Token.Type.POWER_ASSIGN, 
                          Token.Type.FLOOR_DIVIDE_ASSIGN, Token.Type.AND_ASSIGN,
                          Token.Type.OR_ASSIGN, Token.Type.XOR_ASSIGN,
                          Token.Type.LEFT_SHIFT_ASSIGN, Token.Type.RIGHT_SHIFT_ASSIGN)) {
            // Handle compound assignments
            Token.Type operatorType = previous().getType();
            ASTNode value = assignment();
            
            if (expr instanceof Identifier) {
                CompoundAssignmentStatement.Operator operator;
                switch (operatorType) {
                    case PLUS_ASSIGN:
                        operator = CompoundAssignmentStatement.Operator.PLUS_ASSIGN;
                        break;
                    case MINUS_ASSIGN:
                        operator = CompoundAssignmentStatement.Operator.MINUS_ASSIGN;
                        break;
                    case MULTIPLY_ASSIGN:
                        operator = CompoundAssignmentStatement.Operator.MULTIPLY_ASSIGN;
                        break;
                    case DIVIDE_ASSIGN:
                        operator = CompoundAssignmentStatement.Operator.DIVIDE_ASSIGN;
                        break;
                    case MODULO_ASSIGN:
                        operator = CompoundAssignmentStatement.Operator.MODULO_ASSIGN;
                        break;
                    case POWER_ASSIGN:
                        operator = CompoundAssignmentStatement.Operator.POWER_ASSIGN;
                        break;
                    case FLOOR_DIVIDE_ASSIGN:
                        operator = CompoundAssignmentStatement.Operator.FLOOR_DIVIDE_ASSIGN;
                        break;
                    case AND_ASSIGN:
                        operator = CompoundAssignmentStatement.Operator.AND_ASSIGN;
                        break;
                    case OR_ASSIGN:
                        operator = CompoundAssignmentStatement.Operator.OR_ASSIGN;
                        break;
                    case XOR_ASSIGN:
                        operator = CompoundAssignmentStatement.Operator.XOR_ASSIGN;
                        break;
                    case LEFT_SHIFT_ASSIGN:
                        operator = CompoundAssignmentStatement.Operator.LEFT_SHIFT_ASSIGN;
                        break;
                    case RIGHT_SHIFT_ASSIGN:
                        operator = CompoundAssignmentStatement.Operator.RIGHT_SHIFT_ASSIGN;
                        break;
                    default:
                        throw new RuntimeException("Unknown compound assignment operator: " + operatorType);
                }
                return new CompoundAssignmentStatement(((Identifier) expr).getName(), operator, value);
            } else {
                throw new RuntimeException("Invalid compound assignment target - must be identifier");
            }
        }
        
        return expr;
    }
      private ASTNode tupleExpression() {
        ASTNode expr = or();
        
        // Check if this is a tuple (comma-separated expressions)
        if (check(Token.Type.COMMA)) {
            List<ASTNode> elements = new ArrayList<>();
            elements.add(expr);
            
            while (match(Token.Type.COMMA)) {
                // Handle trailing comma case (empty after comma)
                if (check(Token.Type.ASSIGN) || check(Token.Type.NEWLINE) || check(Token.Type.RIGHT_PAREN) || isAtEnd()) {
                    break;
                }
                elements.add(or());
            }
            
            // Create a tuple if we have more than one element OR if there's a trailing comma
            // The trailing comma case is important for single-element tuple unpacking: "a, = ..."
            return new TupleLiteral(elements);
        }
        
        return expr;
    }
    
    private ASTNode or() {
        ASTNode expr = and();
        
        while (match(Token.Type.OR)) {
            ASTNode right = and();
            expr = new BinaryExpression(expr, BinaryExpression.Operator.OR, right);
        }
        
        return expr;
    }
      private ASTNode and() {
        ASTNode expr = bitwiseOr();
        
        while (match(Token.Type.AND)) {
            ASTNode right = bitwiseOr();
            expr = new BinaryExpression(expr, BinaryExpression.Operator.AND, right);
        }
        
        return expr;
    }
    
    private ASTNode bitwiseOr() {
        ASTNode expr = bitwiseXor();
        
        while (match(Token.Type.BITWISE_OR)) {
            ASTNode right = bitwiseXor();
            expr = new BinaryExpression(expr, BinaryExpression.Operator.BITWISE_OR, right);
        }
        
        return expr;
    }
    
    private ASTNode bitwiseXor() {
        ASTNode expr = bitwiseAnd();
        
        while (match(Token.Type.BITWISE_XOR)) {
            ASTNode right = bitwiseAnd();
            expr = new BinaryExpression(expr, BinaryExpression.Operator.BITWISE_XOR, right);
        }
        
        return expr;
    }
    
    private ASTNode bitwiseAnd() {
        ASTNode expr = shift();
        
        while (match(Token.Type.BITWISE_AND)) {
            ASTNode right = shift();
            expr = new BinaryExpression(expr, BinaryExpression.Operator.BITWISE_AND, right);
        }
        
        return expr;
    }
    
    private ASTNode shift() {
        ASTNode expr = equality();
        
        while (match(Token.Type.LEFT_SHIFT, Token.Type.RIGHT_SHIFT)) {
            Token operator = previous();
            ASTNode right = equality();
            BinaryExpression.Operator op = operator.getType() == Token.Type.LEFT_SHIFT ? 
                BinaryExpression.Operator.LEFT_SHIFT : BinaryExpression.Operator.RIGHT_SHIFT;
            expr = new BinaryExpression(expr, op, right);
        }
        
        return expr;
    }
      private ASTNode equality() {
        ASTNode expr = comparison();
        
        while (match(Token.Type.EQUAL, Token.Type.NOT_EQUAL)) {
            Token operator = previous();
            ASTNode right = comparison();
            BinaryExpression.Operator op = operator.getType() == Token.Type.EQUAL ? 
                BinaryExpression.Operator.EQUAL : BinaryExpression.Operator.NOT_EQUAL;
            expr = new BinaryExpression(expr, op, right);
        }
        
        return expr;
    }
    
    private ASTNode comparison() {
        ASTNode expr = term();
        
        while (match(Token.Type.GREATER, Token.Type.GREATER_EQUAL, Token.Type.LESS, Token.Type.LESS_EQUAL, Token.Type.IN, Token.Type.IS)) {
            Token operator = previous();
            ASTNode right = term();
            BinaryExpression.Operator op;
            switch (operator.getType()) {
                case GREATER: op = BinaryExpression.Operator.GREATER; break;
                case GREATER_EQUAL: op = BinaryExpression.Operator.GREATER_EQUAL; break;
                case LESS: op = BinaryExpression.Operator.LESS; break;
                case LESS_EQUAL: op = BinaryExpression.Operator.LESS_EQUAL; break;
                case IN: op = BinaryExpression.Operator.IN; break;
                case IS: op = BinaryExpression.Operator.IS; break;
                default: throw new RuntimeException("Unknown comparison operator");
            }
            expr = new BinaryExpression(expr, op, right);
        }
        
        return expr;
    }
    
    private ASTNode term() {
        ASTNode expr = factor();
        
        while (match(Token.Type.MINUS, Token.Type.PLUS)) {
            Token operator = previous();
            ASTNode right = factor();
            BinaryExpression.Operator op = operator.getType() == Token.Type.MINUS ? 
                BinaryExpression.Operator.MINUS : BinaryExpression.Operator.PLUS;
            expr = new BinaryExpression(expr, op, right);
        }
        
        return expr;
    }
      private ASTNode factor() {
        ASTNode expr = power();
        
        while (match(Token.Type.DIVIDE, Token.Type.MULTIPLY, Token.Type.MODULO, Token.Type.FLOOR_DIVIDE)) {
            Token operator = previous();
            ASTNode right = power();
            BinaryExpression.Operator op;
            switch (operator.getType()) {
                case DIVIDE: op = BinaryExpression.Operator.DIVIDE; break;
                case MULTIPLY: op = BinaryExpression.Operator.MULTIPLY; break;
                case MODULO: op = BinaryExpression.Operator.MODULO; break;
                case FLOOR_DIVIDE: op = BinaryExpression.Operator.FLOOR_DIVIDE; break;
                default: throw new RuntimeException("Unknown factor operator");
            }
            expr = new BinaryExpression(expr, op, right);
        }
        
        return expr;
    }
    
    private ASTNode power() {
        ASTNode expr = unary();
        
        if (match(Token.Type.POWER)) {
            ASTNode right = power(); // 右结合
            expr = new BinaryExpression(expr, BinaryExpression.Operator.POWER, right);
        }
        
        return expr;
    }
    
    private ASTNode unary() {
        if (match(Token.Type.NOT)) {
            ASTNode expr = unary();
            return new UnaryExpression(UnaryExpression.Operator.NOT, expr);
        }
        
        if (match(Token.Type.MINUS)) {
            ASTNode expr = unary();
            return new UnaryExpression(UnaryExpression.Operator.MINUS, expr);
        }
        
        return call();
    }
    
    private ASTNode call() {
        ASTNode expr = primary();
        
        while (true) {
            if (match(Token.Type.LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(Token.Type.DOT)) {
                Token name = consume(Token.Type.IDENTIFIER, "Expected property name after '.'");
                expr = new AttributeExpression(expr, name.getValue());            } else if (match(Token.Type.LEFT_BRACKET)) {
                ASTNode indexOrSlice = parseIndexOrSlice();
                consume(Token.Type.RIGHT_BRACKET, "Expected ']' after index");
                expr = new IndexExpression(expr, indexOrSlice);
            } else {
                break;
            }
        }
        
        return expr;
    }
      private ASTNode finishCall(ASTNode callee) {
        List<ASTNode> arguments = new ArrayList<>();
        
        if (!check(Token.Type.RIGHT_PAREN)) {
            do {
                // Use or() instead of expression() to avoid tuple parsing
                arguments.add(or());
            } while (match(Token.Type.COMMA));
        }
        
        consume(Token.Type.RIGHT_PAREN, "Expected ')' after arguments");
        return new CallExpression(callee, arguments);
    }
      private ASTNode primary() {
        if (match(Token.Type.TRUE)) return new Literal(true);
        if (match(Token.Type.FALSE)) return new Literal(false);
        if (match(Token.Type.NONE)) return new Literal(null);
          // Lambda expression
        if (match(Token.Type.LAMBDA)) {
            return lambdaExpression();
        }
        
        // Super expression
        if (match(Token.Type.SUPER)) {
            return superExpression();
        }
        
        if (match(Token.Type.NUMBER)) {
            String value = previous().getValue();
            if (value.contains(".")) {
                return new Literal(Double.parseDouble(value));
            } else {
                return new Literal(Long.parseLong(value));
            }
        }        if (match(Token.Type.STRING, Token.Type.RAW_STRING, Token.Type.TRIPLE_STRING, Token.Type.TRIPLE_RAW_STRING)) {
            return new Literal(previous().getValue());
        }
        
        if (match(Token.Type.F_STRING, Token.Type.TRIPLE_F_STRING)) {
            Token token = previous();
            boolean isTriple = token.getType() == Token.Type.TRIPLE_F_STRING;
            return new FStringLiteral(token.getValue(), false, isTriple);
        }
        
        if (match(Token.Type.IDENTIFIER)) {
            return new Identifier(previous().getValue());
        }        if (match(Token.Type.LEFT_PAREN)) {
            // Handle both grouped expressions, tuple literals, and generator expressions
            if (check(Token.Type.RIGHT_PAREN)) {
                // Empty tuple: ()
                consume(Token.Type.RIGHT_PAREN, "Expected ')' after '('");
                return new TupleLiteral(new ArrayList<>());
            }
            
            // Use or() instead of expression() to avoid tuple parsing interference
            ASTNode first = or();
            
            // Check for generator expression: (x for x in range(10))
            if (match(Token.Type.FOR)) {
                ASTNode result = parseGeneratorExpression(first);
                consume(Token.Type.RIGHT_PAREN, "Expected ')' after generator expression");
                return result;
            }
            
            if (match(Token.Type.COMMA)) {
                // This is a tuple: (a, b, c) or (a,) for single element
                List<ASTNode> elements = new ArrayList<>();
                elements.add(first);
                
                // Handle trailing comma case and additional elements
                if (!check(Token.Type.RIGHT_PAREN)) {
                    do {
                        elements.add(or());
                    } while (match(Token.Type.COMMA) && !check(Token.Type.RIGHT_PAREN));
                }
                
                consume(Token.Type.RIGHT_PAREN, "Expected ')' after tuple elements");
                return new TupleLiteral(elements);
            } else {
                // This is a grouped expression: (a)
                consume(Token.Type.RIGHT_PAREN, "Expected ')' after expression");
                return first;
            }
        }
          if (match(Token.Type.LEFT_BRACKET)) {
            if (check(Token.Type.RIGHT_BRACKET)) {
                // Empty list
                consume(Token.Type.RIGHT_BRACKET, "Expected ']' after '['");
                return new ListLiteral(new ArrayList<>());
            }
            
            ASTNode result = parseListOrComprehension();
            consume(Token.Type.RIGHT_BRACKET, "Expected ']' after list elements");
            return result;
        }        if (match(Token.Type.LEFT_BRACE)) {
            // Handle empty braces as empty dictionary
            if (check(Token.Type.RIGHT_BRACE)) {
                consume(Token.Type.RIGHT_BRACE, "Expected '}' after empty braces");
                return new DictLiteral(new HashMap<>());
            }
            
            // Parse first element to determine if it's a set or dict
            ASTNode firstElement = or();
            
            // Check if it's a dictionary (has colon after first element)
            if (match(Token.Type.COLON)) {
                // It's a dictionary
                Map<ASTNode, ASTNode> entries = new HashMap<>();
                ASTNode value = or();
                entries.put(firstElement, value);
                
                // Parse remaining key-value pairs
                while (match(Token.Type.COMMA)) {
                    if (check(Token.Type.RIGHT_BRACE)) break; // trailing comma
                    ASTNode key = or();
                    consume(Token.Type.COLON, "Expected ':' after dictionary key");
                    value = or();
                    entries.put(key, value);
                }
                
                consume(Token.Type.RIGHT_BRACE, "Expected '}' after dictionary entries");
                return new DictLiteral(entries);
            } else {
                // It's a set
                List<ASTNode> elements = new ArrayList<>();
                elements.add(firstElement);
                
                // Parse remaining elements
                while (match(Token.Type.COMMA)) {
                    if (check(Token.Type.RIGHT_BRACE)) break; // trailing comma
                    elements.add(or());
                }
                
                consume(Token.Type.RIGHT_BRACE, "Expected '}' after set elements");
                return new SetLiteral(elements);
            }
        }
        
        throw new RuntimeException("Expected expression at " + peek().getLine());
    }
    
    private ASTNode expression() {
        return assignment();
    }    private boolean match(Token.Type... types) {
        for (Token.Type type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }
    
    private boolean check(Token.Type type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }
    
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }
    
    private boolean isAtEnd() {
        return peek().getType() == Token.Type.EOF;
    }
    
    private Token peek() {
        return tokens.get(current);
    }
    
    private Token previous() {
        return tokens.get(current - 1);
    }
    
    private Token consume(Token.Type type, String message) {
        if (check(type)) return advance();
        throw new RuntimeException(message + " at line " + peek().getLine());
    }
    
    private void synchronize() {
        advance();
        
        while (!isAtEnd()) {
            if (previous().getType() == Token.Type.NEWLINE) return;
            
            switch (peek().getType()) {
                case CLASS:
                case DEF:
                case IF:
                case WHILE:
                case FOR:
                case RETURN:
                    return;
            }
            
            advance();
        }
    }

    private ASTNode tryExceptStatement() {
        // try:
        consume(Token.Type.COLON, "Expected ':' after try");
        consume(Token.Type.NEWLINE, "Expected newline after ':'");
        consume(Token.Type.INDENT, "Expected indentation after try statement");
        
        List<ASTNode> tryBody = block();
        List<TryExceptStatement.ExceptClause> exceptClauses = new ArrayList<>();
        
        // 解析except子句
        while (match(Token.Type.EXCEPT)) {
            String exceptionType = null;
            String variable = null;
            
            // except ExceptionType:
            // except ExceptionType as var:
            // except:
            if (!check(Token.Type.COLON)) {
                Token exceptionToken = consume(Token.Type.IDENTIFIER, "Expected exception type");
                exceptionType = exceptionToken.getValue();
                
                // as variable
                if (match(Token.Type.AS)) {
                    Token varToken = consume(Token.Type.IDENTIFIER, "Expected variable name after 'as'");
                    variable = varToken.getValue();
                }
            }
            
            consume(Token.Type.COLON, "Expected ':' after except clause");
            consume(Token.Type.NEWLINE, "Expected newline after ':'");
            consume(Token.Type.INDENT, "Expected indentation after except statement");
            
            List<ASTNode> exceptBody = block();
            exceptClauses.add(new TryExceptStatement.ExceptClause(exceptionType, variable, exceptBody));
        }
        
        // 可选的finally块
        List<ASTNode> finallyBody = new ArrayList<>();
        if (match(Token.Type.FINALLY)) {
            consume(Token.Type.COLON, "Expected ':' after finally");
            consume(Token.Type.NEWLINE, "Expected newline after ':'");
            consume(Token.Type.INDENT, "Expected indentation after finally statement");
            finallyBody = block();
        }
        
        if (exceptClauses.isEmpty() && finallyBody.isEmpty()) {
            throw new RuntimeException("Expected at least one except or finally clause");
        }
          return new TryExceptStatement(tryBody, exceptClauses, finallyBody);
    }
    
    private ASTNode withStatement() {
        // with context_expression as target_variable:
        ASTNode contextExpression = expression();
        String targetVariable = null;
        
        // Optional 'as' variable
        if (match(Token.Type.AS)) {
            Token varToken = consume(Token.Type.IDENTIFIER, "Expected variable name after 'as'");
            targetVariable = varToken.getValue();
        }
        
        consume(Token.Type.COLON, "Expected ':' after with statement");
        consume(Token.Type.NEWLINE, "Expected newline after ':'");
        consume(Token.Type.INDENT, "Expected indentation after with statement");
        
        List<ASTNode> body = block();
        return new WithStatement(contextExpression, targetVariable, body);
    }
    
    private ASTNode lambdaExpression() {
        List<String> parameters = new ArrayList<>();
        
        // Parse parameters (optional)
        if (!check(Token.Type.COLON)) {
            do {
                Token param = consume(Token.Type.IDENTIFIER, "Expected parameter name");
                parameters.add(param.getValue());
            } while (match(Token.Type.COMMA));
        }
          consume(Token.Type.COLON, "Expected ':' after lambda parameters");
        ASTNode body = or(); // Use or() to avoid tuple parsing issues
        
        return new LambdaExpression(parameters, body);
    }
    
    private ASTNode superExpression() {
        consume(Token.Type.LEFT_PAREN, "Expected '(' after super");
        
        // Check if it's super() or super(ClassName)
        if (check(Token.Type.RIGHT_PAREN)) {
            // super() - automatic parent class resolution
            consume(Token.Type.RIGHT_PAREN, "Expected ')' after super");
            return new SuperExpression();
        } else {
            // super(ClassName) - specific parent class
            Token className = consume(Token.Type.IDENTIFIER, "Expected class name in super()");
            consume(Token.Type.RIGHT_PAREN, "Expected ')' after class name");
            return new SuperExpression(className.getValue());
        }
    }
      private ASTNode parseListOrComprehension() {
        // Parse first element
        ASTNode firstElement = or();
        
        // Check for comprehension
        if (match(Token.Type.FOR)) {
            return parseListComprehension(firstElement);
        }
        
        // Regular list
        List<ASTNode> elements = new ArrayList<>();
        elements.add(firstElement);
        
        while (match(Token.Type.COMMA)) {
            if (check(Token.Type.RIGHT_BRACKET)) break; // Trailing comma
            elements.add(or());
        }
        
        return new ListLiteral(elements);
    }
    
    private ASTNode parseListComprehension(ASTNode element) {
        List<ListComprehension.ComprehensionClause> clauses = new ArrayList<>();
        
        // Parse first for clause
        Token variable = consume(Token.Type.IDENTIFIER, "Expected variable name in comprehension");
        consume(Token.Type.IN, "Expected 'in' in comprehension");
        ASTNode iterable = or();
        
        ASTNode condition = null;
        if (match(Token.Type.IF)) {
            condition = or();
        }
        
        clauses.add(new ListComprehension.ComprehensionClause(variable.getValue(), iterable, condition));
        
        // Parse additional for clauses (nested comprehensions)
        while (match(Token.Type.FOR)) {
            variable = consume(Token.Type.IDENTIFIER, "Expected variable name in comprehension");
            consume(Token.Type.IN, "Expected 'in' in comprehension");
            iterable = or();
            
            condition = null;
            if (match(Token.Type.IF)) {
                condition = or();
            }
            
            clauses.add(new ListComprehension.ComprehensionClause(variable.getValue(), iterable, condition));
        }
        
        return new ListComprehension(element, clauses);
    }
    
    private ASTNode parseGeneratorExpression(ASTNode element) {
        List<ListComprehension.ComprehensionClause> clauses = new ArrayList<>();
        
        // Parse first for clause
        Token variable = consume(Token.Type.IDENTIFIER, "Expected variable name in generator");
        consume(Token.Type.IN, "Expected 'in' in generator");
        ASTNode iterable = or();
        
        ASTNode condition = null;
        if (match(Token.Type.IF)) {
            condition = or();
        }
        
        clauses.add(new ListComprehension.ComprehensionClause(variable.getValue(), iterable, condition));
        
        // Parse additional for clauses
        while (match(Token.Type.FOR)) {
            variable = consume(Token.Type.IDENTIFIER, "Expected variable name in generator");
            consume(Token.Type.IN, "Expected 'in' in generator");
            iterable = or();
            
            condition = null;
            if (match(Token.Type.IF)) {
                condition = or();
            }
            
            clauses.add(new ListComprehension.ComprehensionClause(variable.getValue(), iterable, condition));
        }
        
        return new GeneratorExpression(element, clauses);
    }    private ASTNode decoratorStatement() {
        // Parse the decorator expression (can be a simple identifier or a call)
        ASTNode expression = expression();
        
        consume(Token.Type.NEWLINE, "Expected newline after decorator");
        
        // The next statement after the decorator must be either a function or class definition
        ASTNode decorated;
        
        if (check(Token.Type.AT)) {
            // This is a stacked decorator, parse the next one in the stack
            decorated = decoratorStatement();
        } else if (check(Token.Type.DEF)) {
            advance();  // Consume DEF token
            decorated = functionDef();
        } else if (check(Token.Type.CLASS)) {
            advance();  // Consume CLASS token
            decorated = classDef();
        } else {
            throw new RuntimeException("Decorator must be followed by a function or class definition");
        }
        
        // Create a decorator node
        return new Decorator(expression, decorated);
    }    /**
     * Parse a single expression (public method for F-string evaluation)
     */
    public ASTNode parseExpression() {
        return expression();
    }
    
    /**
     * Parse index or slice expression: [expr], [start:], [:stop], [start:stop], [start:stop:step]
     */
    private ASTNode parseIndexOrSlice() {
        ASTNode start = null;
        ASTNode stop = null;
        ASTNode step = null;
        
        // Check if it starts with a colon (e.g., [:stop])
        if (check(Token.Type.COLON)) {
            // This is a slice starting with :
            advance(); // consume :
            
            if (!check(Token.Type.RIGHT_BRACKET) && !check(Token.Type.COLON)) {
                stop = expression();
            }
            
            // Check for step part
            if (match(Token.Type.COLON)) {
                if (!check(Token.Type.RIGHT_BRACKET)) {
                    step = expression();
                }
            }
            
            return new SliceExpression(start, stop, step);
        }
        
        // Parse first expression
        start = expression();
        
        // Check if this is a slice
        if (match(Token.Type.COLON)) {
            // This is a slice: [start:...]
            
            // Parse stop if present
            if (!check(Token.Type.RIGHT_BRACKET) && !check(Token.Type.COLON)) {
                stop = expression();
            }
            
            // Check for step part
            if (match(Token.Type.COLON)) {
                if (!check(Token.Type.RIGHT_BRACKET)) {
                    step = expression();
                }
            }
            
            return new SliceExpression(start, stop, step);
        }
        
        // This is a simple index
        return start;
    }
}

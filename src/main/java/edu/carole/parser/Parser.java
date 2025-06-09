package edu.carole.parser;

import edu.carole.ast.*;
import edu.carole.ast.statements.*;
import edu.carole.ast.expressions.*;
import edu.carole.ast.expressions.ListComprehension.ComprehensionClause;
import edu.carole.lexer.Token;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        scanAndRemoveUselessDedents();
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
    }

    private void scanAndRemoveUselessDedents() {
        int index = 0;
        boolean matching = false;
        int indentDepth = 0, startIndex = -1;
        while (index < tokens.size()) {
            Token token = tokens.get(index);
            if (token.getType() == Token.Type.DEDENT) {
                indentDepth += token.getType() == Token.Type.INDENT ? 1 : -1;
                startIndex = index;
                index++;
                matching = true;
            } else if (token.getType() == Token.Type.NEWLINE) {
                index++;
            } else if (matching && token.getType() == Token.Type.INDENT) {
                indentDepth++;
                if (indentDepth >= 0) {
                    for (int i = startIndex; i < index; i++) {
                        tokens.remove(i);
                    }
                    index = startIndex; // Reset index to start of dedent block
                    startIndex = -1;
                    indentDepth = 0;
                    matching = false; // Reset matching after indent
                }
            } else {
                matching = false;
                indentDepth = 0;
                startIndex = -1;
                index++;
            }
        }
    }

    private ASTNode statement() {
        try {
            // Check for decorators (must come before function and class definitions)
            if (match(Token.Type.AT)) return decoratorStatement();
            
            if (match(Token.Type.IF)) return ifStatement();
            if (match(Token.Type.WHILE)) return whileStatement();
            if (match(Token.Type.FOR)) return forStatement();
            if (match(Token.Type.DEF)) return functionDef();
            if (match(Token.Type.CLASS)) return classDef();
            if (match(Token.Type.RETURN)) return returnStatement();
            if (match(Token.Type.BREAK)) return new BreakStatement();
            if (match(Token.Type.CONTINUE)) return new ContinueStatement();
            if (match(Token.Type.PASS)) return new PassStatement();
            if (match(Token.Type.TRY)) return tryExceptStatement();
            if (match(Token.Type.WITH)) return withStatement();
            if (match(Token.Type.GLOBAL)) return globalStatement();
            if (match(Token.Type.NONLOCAL)) return nonlocalStatement();
            if (match(Token.Type.IMPORT)) return importStatement();
            if (match(Token.Type.FROM)) return fromImportStatement();
            if (match(Token.Type.MATCH)) return matchStatement();
//            if (checkNext(Token.Type.LEFT_PAREN) && match(Token.Type.IDENTIFIER)) {
//                return call();
//            }
            return expressionStatement();
        } catch (Exception e) {
            // 错误恢复：跳到下一行
            e.printStackTrace();
            synchronize();
            return null;
        }
    }
    
    private ASTNode ifStatement() {
        ASTNode condition = expression(true);
        boolean singleLine = skipLineAndCheckIfSingleLine(
                "if condition", "if statement"
        );
        
        List<ASTNode> thenBranch = block(singleLine);
        List<Map.Entry<ASTNode, List<ASTNode>>> conditionBranches = new ArrayList<>();
        conditionBranches.add(new AbstractMap.SimpleEntry<>(condition, thenBranch));
        if (match(Token.Type.ELIF)) {
            // Handle elif clauses
            while (check(Token.Type.ELIF)) {
                advance();
                ASTNode elifCondition = expression(true);
                singleLine = skipLineAndCheckIfSingleLine(
                        "elif", "elif statement"
                );
                List<ASTNode> elifBranch = block(singleLine);
                conditionBranches.add(new AbstractMap.SimpleEntry<>(elifCondition, elifBranch));
            }
        }

        List<ASTNode> elseBranch = new ArrayList<>();
        if (match(Token.Type.ELSE)) {
            consume(Token.Type.COLON, "Expected ':' after else");
            singleLine = skipLineAndCheckIfSingleLine(
                    "else", "else statement"
            );
            elseBranch = block(singleLine);
        }
        
        return new IfStatement(conditionBranches, elseBranch);
    }

    private ASTNode whileStatement() {
        ASTNode condition = expression(true);
        boolean singleLine = skipLineAndCheckIfSingleLine(
                "while condition", "while statement"
        );

        List<ASTNode> body = block(singleLine);
        List<ASTNode> elseBody = new ArrayList<>();
        
        // Check for else clause
        if (match(Token.Type.ELSE)) {
            singleLine = skipLineAndCheckIfSingleLine(
                    "else", "else statement"
            );
            elseBody = block(singleLine);
        }
        
        return new WhileStatement(condition, body, elseBody);
    }

    private ASTNode forStatement() {
        Token variable = consume(Token.Type.IDENTIFIER, "Expected variable name in for loop");
        consume(Token.Type.IN, "Expected 'in' in for loop");
        ASTNode iterable = expression(true);
        boolean singleLine = skipLineAndCheckIfSingleLine(
                "for statement", "for statement"
        );
        List<ASTNode> body = block(singleLine);
        List<ASTNode> elseBody = new ArrayList<>();
        
        // Check for else clause
        if (match(Token.Type.ELSE)) {
            singleLine = skipLineAndCheckIfSingleLine(
                    "else", "else statement"
            );
            elseBody = block(singleLine);
        }
        return new ForStatement(variable.getValue(), iterable, body, elseBody);
    }

    private ASTNode functionDef() {
        Token name = consume(Token.Type.IDENTIFIER, "Expected function name");
        consume(Token.Type.LEFT_PAREN, "Expected '(' after function name");
        
        List<FunctionParameter> parameters = new ArrayList<>();
        
        if (!check(Token.Type.RIGHT_PAREN)) {
            do {
                FunctionParameter param = parseParameter();
                parameters.add(param);
            } while (match(Token.Type.COMMA));
        }
        
        consume(Token.Type.RIGHT_PAREN, "Expected ')' after parameters");
        
        // 检查返回值类型提示
        ASTNode returnTypeHint = null;
        if (match(Token.Type.ARROW)) { // -> 
            returnTypeHint = expression(false);
        }
        boolean singleLine = skipLineAndCheckIfSingleLine(
                "function signature", "function definition"
        );

        List<ASTNode> body = block(singleLine);
        return new FunctionDef(name.getValue(), parameters, body, returnTypeHint);
    }

    private FunctionParameter parseParameter() {
        FunctionParameter.ParameterType paramType = FunctionParameter.ParameterType.NORMAL;
        
        // 检查 **kwargs
        if (match(Token.Type.POWER)) {
            paramType = FunctionParameter.ParameterType.KWARGS;
        }
        // 检查 *args
        else if (match(Token.Type.MULTIPLY)) {
            paramType = FunctionParameter.ParameterType.VARARGS;
        }
        
        Token paramToken = consume(Token.Type.IDENTIFIER, "Expected parameter name");
        String paramName = paramToken.getValue();
        
        // For special parameter types, modify the parameter name to include the prefix
        // This will help with runtime identification
        if (paramType == FunctionParameter.ParameterType.VARARGS) {
            // Mark it as a varargs parameter with the prefix, but don't change the actual name
            System.out.println("DEBUG: Parsed varargs parameter: *" + paramName);
        } else if (paramType == FunctionParameter.ParameterType.KWARGS) {
            // Mark it as a kwargs parameter with the prefix, but don't change the actual name
            System.out.println("DEBUG: Parsed kwargs parameter: **" + paramName);
        }
        
        // 解析类型提示
        ASTNode typeHint = null;
        if (match(Token.Type.COLON)) {
            typeHint = expression(false);
        }
        
        // 解析默认值（只有普通参数可以有默认值）
        ASTNode defaultValue = null;
        if (paramType == FunctionParameter.ParameterType.NORMAL && match(Token.Type.ASSIGN)) {
            defaultValue = expression(false);
        }
        
        return new FunctionParameter(paramName, paramType, defaultValue, typeHint);
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
        while (check(Token.Type.NEWLINE)) {
            advance(); // Skip any additional newlines
        }
        consume(Token.Type.INDENT, "Expected indentation after class definition");
        
        List<ASTNode> body = block(false);
        return new ClassDef(name.getValue(), baseClasses, body);
    }


    private ASTNode returnStatement() {
        ASTNode value = null;
        if (!check(Token.Type.NEWLINE) && !isAtEnd()) {
            value = tupleExpression(true); // Use tupleExpression to support comma-separated returns
        }
        return new ReturnStatement(value);
    }

    private List<ASTNode> block(boolean singleLine) {
        List<ASTNode> statements = new ArrayList<>();
        
        while (!check(Token.Type.DEDENT) && !isAtEnd()) {
            if (check(Token.Type.NEWLINE)) {
                advance();
                // If single line, return immediately
                if (singleLine) return statements;
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
    }

    private ASTNode expressionStatement() {
        ASTNode expr = assignment(true);
          // If assignment() returned a statement (like AssignmentStatement), don't wrap it
        if (expr instanceof AssignmentStatement || 
            expr instanceof CompoundAssignmentStatement ||
            expr instanceof AttributeAssignmentStatement || 
            expr instanceof IndexAssignmentStatement ||
            expr instanceof TupleUnpackingAssignment) {
            return expr;
        }
        
        return new ExpressionStatement(expr);
    }

    private ASTNode assignment(boolean greedy) {
        ASTNode expr = tupleExpression(greedy);
        
        if (match(Token.Type.ASSIGN)) {
            ASTNode value = assignment(greedy);
              if (expr instanceof Identifier) {
                return new AssignmentStatement(((Identifier) expr).getName(), value);
            } else if (expr instanceof AttributeExpression attrExpr) {
                  return new AttributeAssignmentStatement(attrExpr.getObject(), attrExpr.getAttribute(), value);
            } else if (expr instanceof IndexExpression indexExpr) {
                  return new IndexAssignmentStatement(indexExpr.getObject(), indexExpr.getIndex(), value);
            } else if (expr instanceof TupleLiteral tuple) {
                // Handle tuple unpacking assignment: a, b, c = expression
                  List<String> targets = new ArrayList<>();
                for (ASTNode element : tuple.getElements()) {
                    if (!(element instanceof Identifier)) {
                        throw new RuntimeException("Invalid tuple unpacking target");
                    }
                    targets.add(((Identifier) element).getName());
                }
                return new TupleUnpackingAssignment(targets, value);
            }
            throw new RuntimeException("Invalid assignment target");
        } else if (match(Token.Type.PLUS_ASSIGN, Token.Type.MINUS_ASSIGN,
                          Token.Type.MULTIPLY_ASSIGN, Token.Type.DIVIDE_ASSIGN,
                          Token.Type.MODULO_ASSIGN, Token.Type.POWER_ASSIGN, 
                          Token.Type.FLOOR_DIVIDE_ASSIGN, Token.Type.AND_ASSIGN,
                          Token.Type.OR_ASSIGN, Token.Type.XOR_ASSIGN,
                          Token.Type.LEFT_SHIFT_ASSIGN, Token.Type.RIGHT_SHIFT_ASSIGN)) {
            // Handle compound assignments
            Token.Type operatorType = previous().getType();
            ASTNode value = assignment(greedy);



            if (expr instanceof Identifier || expr instanceof AttributeExpression) {
                CompoundAssignmentStatement.Operator operator = switch (operatorType) {
                    case PLUS_ASSIGN -> CompoundAssignmentStatement.Operator.PLUS_ASSIGN;
                    case MINUS_ASSIGN -> CompoundAssignmentStatement.Operator.MINUS_ASSIGN;
                    case MULTIPLY_ASSIGN -> CompoundAssignmentStatement.Operator.MULTIPLY_ASSIGN;
                    case DIVIDE_ASSIGN -> CompoundAssignmentStatement.Operator.DIVIDE_ASSIGN;
                    case MODULO_ASSIGN -> CompoundAssignmentStatement.Operator.MODULO_ASSIGN;
                    case POWER_ASSIGN -> CompoundAssignmentStatement.Operator.POWER_ASSIGN;
                    case FLOOR_DIVIDE_ASSIGN -> CompoundAssignmentStatement.Operator.FLOOR_DIVIDE_ASSIGN;
                    case AND_ASSIGN -> CompoundAssignmentStatement.Operator.AND_ASSIGN;
                    case OR_ASSIGN -> CompoundAssignmentStatement.Operator.OR_ASSIGN;
                    case XOR_ASSIGN -> CompoundAssignmentStatement.Operator.XOR_ASSIGN;
                    case LEFT_SHIFT_ASSIGN -> CompoundAssignmentStatement.Operator.LEFT_SHIFT_ASSIGN;
                    case RIGHT_SHIFT_ASSIGN -> CompoundAssignmentStatement.Operator.RIGHT_SHIFT_ASSIGN;
                    default -> throw new RuntimeException("Unknown compound assignment operator: " + operatorType);
                };
                if (expr instanceof AttributeExpression) {
                    return new CompoundAssignmentStatement(expr.toString(), operator, value);
                }
                return new CompoundAssignmentStatement(((Identifier) expr).getName(), operator, value);
            } else {
                throw new RuntimeException("Invalid compound assignment target - must be identifier");
            }
        }
        
        return expr;
    }

    private ASTNode tupleExpression(boolean greedy) {
        ASTNode expr = or();
        
        // Check if this is a tuple (comma-separated expressions)
        if (check(Token.Type.COMMA)) {
            List<ASTNode> elements = new ArrayList<>();
            elements.add(expr);

            if (greedy) {
                while (match(Token.Type.COMMA)) {
                    // Handle trailing comma case (empty after comma)
                    if (check(Token.Type.ASSIGN) || check(Token.Type.NEWLINE) || check(Token.Type.RIGHT_PAREN) || isAtEnd()) {
                        break;
                    }
                    elements.add(or());
                }
            }
            
            // Create a tuple if we have more than one element OR if there's a trailing comma
            // The trailing comma case is important for single-element tuple unpacking: "a, = ..."
            return greedy ? new TupleLiteral(elements) : elements.get(0);
        }
        
        return expr;
    }
    
    private ASTNode or() {
        ASTNode expr = conditionalExpression();
            while (match(Token.Type.OR)) {
                ASTNode right = conditionalExpression();
                expr = new BinaryExpression(expr, BinaryExpression.Operator.OR, right);
            }
        return expr;
    }

    /**
     * Parse conditional expressions: expr1 if condition else expr2
     * This has lower precedence than 'or' but higher than lambda
     */
    private ASTNode conditionalExpression() {
        ASTNode expr = and();
        
        // Check for conditional expression (ternary operator)
        if (match(Token.Type.IF)) {
            ASTNode condition = or(); // Parse condition with higher precedence
            ASTNode falseExpression = null;
            if (check(Token.Type.ELSE)) {
                consume(Token.Type.ELSE, "Expected 'else' in conditional expression");
                falseExpression = conditionalExpression(); // Right-associative
            }
            return new ConditionalExpression(expr, condition, falseExpression);
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
            BinaryExpression.Operator op = switch (operator.getType()) {
                case GREATER -> BinaryExpression.Operator.GREATER;
                case GREATER_EQUAL -> BinaryExpression.Operator.GREATER_EQUAL;
                case LESS -> BinaryExpression.Operator.LESS;
                case LESS_EQUAL -> BinaryExpression.Operator.LESS_EQUAL;
                case IN -> BinaryExpression.Operator.IN;
                case IS -> BinaryExpression.Operator.IS;
                default -> throw new RuntimeException("Unknown comparison operator");
            };
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
            BinaryExpression.Operator op = switch (operator.getType()) {
                case DIVIDE -> BinaryExpression.Operator.DIVIDE;
                case MULTIPLY -> BinaryExpression.Operator.MULTIPLY;
                case MODULO -> BinaryExpression.Operator.MODULO;
                case FLOOR_DIVIDE -> BinaryExpression.Operator.FLOOR_DIVIDE;
                default -> throw new RuntimeException("Unknown factor operator");
            };
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
                expr = new AttributeExpression(expr, name.getValue());
            } else if (match(Token.Type.LEFT_BRACKET)) {
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
        List<ASTNode> positionalArguments = new ArrayList<>();
        Map<String, ASTNode> keywordArguments = new HashMap<>();
        boolean hasKeywordArg = false;  // 标记是否已经遇到了关键字参数
        
        if (!check(Token.Type.RIGHT_PAREN)) {
            do {
                // 检查是否是*args
                if (check(Token.Type.MULTIPLY)) {
                    advance(); // consume the '*'
                    ASTNode args = or();
                    if (!(args instanceof Identifier)) {
                        throw error("Expected identifier after '*'");
                    }
                    // Create a special StarredExpression to mark this as *args
                    positionalArguments.add(new StarredExpression(args));
                    continue;
                }

                // 检查是否是**kwargs
                if (check(Token.Type.POWER)) {
                    advance();
                    // consume the '**'
                    ASTNode kwargs = or();
                    if (!(kwargs instanceof Identifier)) {
                        throw error("Expected identifier after '**'");
                    }
                    // Create a special DoubleStarredExpression to mark this as **kwargs
                    String kwargName = ((Identifier) kwargs).getName();
                    keywordArguments.put("**" + kwargName, kwargs);
                    continue;
                }

                // 检查是否为关键字参数 (name=value)
                if (check(Token.Type.IDENTIFIER) && checkNext(Token.Type.ASSIGN)) {
                    hasKeywordArg = true;
                    String name = consume(Token.Type.IDENTIFIER, "Expected parameter name").getValue();
                    
                    // 这里检查是否是赋值符号ASSIGN (=)，而不是比较符号EQUAL(==)
                    consume(Token.Type.ASSIGN, "Expected '=' after parameter name");
                    
                    // 解析参数值
                    ASTNode value = or();
                    keywordArguments.put(name, value);
                } else {
                    // 不允许位置参数出现在关键字参数之后
                    if (hasKeywordArg) {
                        throw error("Positional argument follows keyword argument");
                    }
                    
                    // 位置参数
                    positionalArguments.add(or());
                }
            } while (match(Token.Type.COMMA));
        }
        
        consume(Token.Type.RIGHT_PAREN, "Expected ')' after arguments");
        return new CallExpression(callee, positionalArguments, keywordArguments);
    }

    private ASTNode primary() {
//        if (match(Token.Type.INDENT, Token.Type.NEWLINE)) {
//            advance();
//            return primary();
//        }
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
            Long specialNum = dealWithSpecialNumbers();
            if (specialNum != null) {
                return new Literal(specialNum);
            }
            String value = previous().getValue();
            if (value.contains(".")) {
                return new Literal(Double.parseDouble(value));
            } else {
                return new Literal(Long.parseLong(value));
            }
        }
        if (match(Token.Type.STRING, Token.Type.TRIPLE_STRING)) {
            return new Literal(decodeUnicode(previous().getValue()));
        }
        if (match(Token.Type.RAW_STRING, Token.Type.TRIPLE_RAW_STRING)) {
            return new Literal(previous().getValue());
        }
        
        if (match(Token.Type.F_STRING, Token.Type.TRIPLE_F_STRING)) {
            Token token = previous();
            boolean isTriple = token.getType() == Token.Type.TRIPLE_F_STRING;
            return new FStringLiteral(token.getValue(), false, isTriple);
        }
        
        if (match(Token.Type.IDENTIFIER)) {
            return new Identifier(previous().getValue());
        }

        if (match(Token.Type.LEFT_PAREN)) {
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
        }
        if (match(Token.Type.LEFT_BRACE)) {
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
    
    private ASTNode expression(boolean greedy) {
        return assignment(greedy);
    }

    private boolean match(Token.Type... types) {
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
    
    private boolean checkNext(Token.Type type) {
        if (current + 1 >= tokens.size()) return false;
        return tokens.get(current + 1).getType() == type;
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

    private RuntimeException error(String message) {
        return new RuntimeException(message + " at line " + peek().getLine());
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
        boolean singleLine = skipLineAndCheckIfSingleLine(
                "try", "try statement"
        );
        List<ASTNode> tryBody = block(singleLine);
        List<TryExceptStatement.ExceptClause> exceptClauses = new ArrayList<>();
        
        // 解析except子句
        while (match(Token.Type.EXCEPT)) {
            String exceptionType = null;
            String variable = null;
            singleLine = false;
            
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
            if (match(Token.Type.NEWLINE)) {
                advance(); // Consume the newline after ':'
                consume(Token.Type.INDENT, "Expected indentation after except statement");
            } else singleLine = true;

            List<ASTNode> exceptBody = block(singleLine);
            exceptClauses.add(new TryExceptStatement.ExceptClause(exceptionType, variable, exceptBody));
        }
        
        // 可选的finally块
        List<ASTNode> finallyBody = new ArrayList<>();
        if (match(Token.Type.FINALLY)) {
            singleLine = skipLineAndCheckIfSingleLine(
                    "finally", "finally statement"
            );
            finallyBody = block(singleLine);
        }
        
        if (exceptClauses.isEmpty() && finallyBody.isEmpty()) {
            throw new RuntimeException("Expected at least one except or finally clause");
        }
        return new TryExceptStatement(tryBody, exceptClauses, finallyBody);
    }
    
    private ASTNode withStatement() {
        // with context_expression as target_variable:
        ASTNode contextExpression = expression(true);
        String targetVariable = null;
        
        // Optional 'as' variable
        if (match(Token.Type.AS)) {
            Token varToken = consume(Token.Type.IDENTIFIER, "Expected variable name after 'as'");
            targetVariable = varToken.getValue();
        }
        
        consume(Token.Type.COLON, "Expected ':' after with statement");
        boolean singleLine = skipLineAndCheckIfSingleLine(
                "with statement", "with statement"
        );

        List<ASTNode> body = block(singleLine);
        return new WithStatement(contextExpression, targetVariable, body);
    }
    
    private ASTNode globalStatement() {
        List<String> variables = new ArrayList<>();
        
        // Parse variable names: global x, y, z
        do {
            Token variable = consume(Token.Type.IDENTIFIER, "Expected variable name after 'global'");
            variables.add(variable.getValue());
        } while (match(Token.Type.COMMA));
        
        return new GlobalStatement(variables);
    }
    
    private ASTNode nonlocalStatement() {
        List<String> variables = new ArrayList<>();
        
        // Parse variable names: nonlocal x, y, z
        do {
            Token variable = consume(Token.Type.IDENTIFIER, "Expected variable name after 'nonlocal'");
            variables.add(variable.getValue());
        } while (match(Token.Type.COMMA));
        
        return new NonlocalStatement(variables);
    }
    
    private ASTNode importStatement() {
        List<ImportStatement.ImportClause> imports = new ArrayList<>();
        
        // Parse import clauses: import module1, module2 as alias
        do {
            Token moduleName = consume(Token.Type.IDENTIFIER, "Expected module name after 'import'");
            String alias = null;
            
            // Handle 'as alias'
            if (match(Token.Type.AS)) {
                Token aliasToken = consume(Token.Type.IDENTIFIER, "Expected alias name after 'as'");
                alias = aliasToken.getValue();
            }
            
            imports.add(new ImportStatement.ImportClause(moduleName.getValue(), alias));
        } while (match(Token.Type.COMMA));
        
        return new ImportStatement(imports);
    }
    
    private ASTNode fromImportStatement() {
        // from module import ...
        Token moduleToken = consume(Token.Type.IDENTIFIER, "Expected module name after 'from'");
        String moduleName = moduleToken.getValue();
        
        consume(Token.Type.IMPORT, "Expected 'import' after module name");
        
        // Check for 'import *'
        if (match(Token.Type.MULTIPLY)) {
            return new FromImportStatement(moduleName, true);
        }
        
        // Parse import items: import item1, item2 as alias
        List<FromImportStatement.ImportClause> imports = new ArrayList<>();
        do {
            Token itemName = consume(Token.Type.IDENTIFIER, "Expected item name in from import");
            String alias = null;
            
            // Handle 'as alias'
            if (match(Token.Type.AS)) {
                Token aliasToken = consume(Token.Type.IDENTIFIER, "Expected alias name after 'as'");
                alias = aliasToken.getValue();
            }
            
            imports.add(new FromImportStatement.ImportClause(itemName.getValue(), alias));
        } while (match(Token.Type.COMMA));
          return new FromImportStatement(moduleName, imports);
    }
    
    private ASTNode matchStatement() {
        // match subject:
        ASTNode subject = expression(true);
        consume(Token.Type.COLON, "Expected ':' after match subject");
        consume(Token.Type.NEWLINE, "Expected newline after ':'");
        consume(Token.Type.INDENT, "Expected indentation after match statement");
        
        List<MatchStatement.CaseClause> cases = new ArrayList<>();
        List<ASTNode> defaultBody = null;

        while (match(Token.Type.CASE)) {
            boolean singleLine = false;
            if (tokens.get(current).getValue().equals("_")) {
                // default case: case _:
                advance(); // Consume the '_'
                singleLine = skipLineAndCheckIfSingleLine(
                        "case pattern", "case statement");
                defaultBody = block(singleLine);
                break;
            }
            ASTNode pattern = parsePattern();
            
            // Optional guard (if condition)
            ASTNode guard = null;
            if (match(Token.Type.IF)) {
                guard = expression(true);
            }

            singleLine = skipLineAndCheckIfSingleLine(
                    "case pattern", "case statement");
            List<ASTNode> body = block(singleLine);
            cases.add(new MatchStatement.CaseClause(pattern, guard, body));
        }
        
        // Consume the final dedent for the match block
        if (check(Token.Type.DEDENT)) {
            advance();
        }
        
        if (cases.isEmpty()) {
            throw new RuntimeException("Match statement must have at least one case");
        }
        
        return new MatchStatement(subject, cases, defaultBody);
    }
    
    private ASTNode parsePattern() {
        return parseOrPattern();
    }
    
    private ASTNode parseOrPattern() {
        ASTNode pattern = parseSequencePattern();
        
        while (match(Token.Type.BITWISE_OR)) {
            ASTNode right = parseSequencePattern();
            pattern = new OrPattern(pattern, right);
        }
        
        return pattern;
    }
    
    private ASTNode parseSequencePattern() {
        if (match(Token.Type.LEFT_PAREN)) {
            // Tuple pattern: (a, b, c) or just grouped pattern: (a)
            if (check(Token.Type.RIGHT_PAREN)) {
                // Empty tuple pattern: ()
                consume(Token.Type.RIGHT_PAREN, "Expected ')' after '('");
                return new SequencePattern(new ArrayList<>(), true);
            }
            
            List<ASTNode> patterns = new ArrayList<>();
            patterns.add(parsePattern());
            
            if (match(Token.Type.COMMA)) {
                // This is a tuple pattern
                while (!check(Token.Type.RIGHT_PAREN)) {
                    if (check(Token.Type.RIGHT_PAREN)) break; // trailing comma
                    patterns.add(parsePattern());
                    if (!match(Token.Type.COMMA)) break;
                }
                consume(Token.Type.RIGHT_PAREN, "Expected ')' after tuple pattern");
                return new SequencePattern(patterns, true);
            } else {
                // This is just a grouped pattern
                consume(Token.Type.RIGHT_PAREN, "Expected ')' after pattern");
                return patterns.get(0);
            }
        } else if (match(Token.Type.LEFT_BRACKET)) {
            // List pattern: [a, b, c]
            List<ASTNode> patterns = new ArrayList<>();
            
            if (!check(Token.Type.RIGHT_BRACKET)) {
                patterns.add(parsePattern());
                while (match(Token.Type.COMMA)) {
                    if (check(Token.Type.RIGHT_BRACKET)) break; // trailing comma
                    patterns.add(parsePattern());
                }
            }
            
            consume(Token.Type.RIGHT_BRACKET, "Expected ']' after list pattern");
            return new SequencePattern(patterns, false);
        }
        
        return parseAtomicPattern();
    }

    private ASTNode parseAtomicPattern() {
        // Check for identifier first (wildcard or capture pattern)
        if (check(Token.Type.IDENTIFIER)) {
            Token identifier = peek();
            
            // Wildcard pattern: _
            if (identifier.getValue().equals("_")) {
                advance(); // consume the token
                return new WildcardPattern();
            }
            
            // Capture pattern: variable name
            advance(); // consume the token
            return new CapturePattern(identifier.getValue());
        }
        
        // Literal patterns: numbers, strings, booleans, None
        if (match(Token.Type.NUMBER, Token.Type.STRING, Token.Type.RAW_STRING, 
                  Token.Type.TRIPLE_STRING, Token.Type.TRIPLE_RAW_STRING,
                  Token.Type.TRUE, Token.Type.FALSE, Token.Type.NONE)) {
            Token literal = previous();
            ASTNode value;
            
            switch (literal.getType()) {
                case NUMBER -> {
                    String val = literal.getValue();
                    if (val.contains(".")) {
                        value = new Literal(Double.parseDouble(val));
                    } else {
                        value = new Literal(Long.parseLong(val));
                    }
                }
                case STRING, TRIPLE_STRING -> {
                    String v = decodeUnicode(literal.getValue());
                    value = new Literal(v);
                }
                case RAW_STRING, TRIPLE_RAW_STRING ->
                    value = new Literal(literal.getValue());
                case TRUE -> value = new Literal(true);
                case FALSE -> value = new Literal(false);
                case NONE -> value = new Literal(null);
                default -> throw new RuntimeException("Unexpected literal type in pattern");
            }
            
            return new LiteralPattern(value);
        }
        
        throw new RuntimeException("Expected pattern at line " + peek().getLine());
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
        if (iterable instanceof ConditionalExpression conditionalExpression) {
            condition = conditionalExpression.getCondition();
            iterable = conditionalExpression.getTrueExpression();
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
        if (iterable instanceof ConditionalExpression conditionalExpression) {
            condition = conditionalExpression.getCondition();
            iterable = conditionalExpression.getTrueExpression();
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
    }

    private ASTNode decoratorStatement() {
        // Parse the decorator expression (can be a simple identifier or a call)
        ASTNode expression = expression(true);
        
        consume(Token.Type.NEWLINE, "Expected newline after decorator");
        
        // The next statement after the decorator must be either a function or class definition
        ASTNode decorated;
        
        if (check(Token.Type.AT)) {
            advance();
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
    }

    /**
     * Parse a single expression (public method for F-string evaluation)
     */
    public ASTNode parseExpression() {
        return expression(true);
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
                stop = expression(true);
            }
            
            // Check for step part
            if (match(Token.Type.COLON)) {
                if (!check(Token.Type.RIGHT_BRACKET)) {
                    step = expression(true);
                }
            }
            
            return new SliceExpression(start, stop, step);
        }
        
        // Parse first expression
        start = expression(true);
        
        // Check if this is a slice
        if (match(Token.Type.COLON)) {
            // This is a slice: [start:...]
            
            // Parse stop if present
            if (!check(Token.Type.RIGHT_BRACKET) && !check(Token.Type.COLON)) {
                stop = expression(true);
            }
            
            // Check for step part
            if (match(Token.Type.COLON)) {
                if (!check(Token.Type.RIGHT_BRACKET)) {
                    step = expression(true);
                }
            }
            
            return new SliceExpression(start, stop, step);
        }
        
        // This is a simple index
        return start;
    }

    private boolean skipLineAndCheckIfSingleLine(String statementNameColon,
                                                 String statementNameIndent) {
        consume(Token.Type.COLON, "Expected ':' after" + statementNameColon);
        if (check(Token.Type.NEWLINE)) {
            do {
                advance();
            } while (check(Token.Type.NEWLINE));
            consume(Token.Type.INDENT, "Expected indentation after " + statementNameIndent);
            return false;
        }
        return true;
    }

    private Long dealWithSpecialNumbers() {
        if (!tokens.get(current - 1).getValue().equals("0")) return null;
        Token next = tokens.get(current);
        if (!next.getType().equals(Token.Type.IDENTIFIER)) return null;
        String value = next.getValue();
        if (value.length() < 2) return null;
        String lastValue = value.substring(1);
        char firstChar = value.charAt(0);
        Integer radix = switch (firstChar) {
            case 'o', 'O' -> 8;
            case 'x', 'X' -> 16;
            case 'b', 'B' -> 2;
            default -> null;
        };
        if (radix == null) return null;
        Long result = Long.parseLong(lastValue, radix);
        advance();
        return result;
    }

    public static String decodeUnicode(String str) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            str = str.replace(matcher.group(1), ch + "");
        }
        return str;
    }
}

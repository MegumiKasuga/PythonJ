package edu.carole.ast.ast;

import edu.carole.ast.statements.*;
import edu.carole.ast.expressions.*;

/**
 * AST访问者模式接口
 */
public interface ASTVisitor<T> {
    T visitProgram(Program program);    T visitAssignmentStatement(AssignmentStatement statement);
    T visitCompoundAssignmentStatement(CompoundAssignmentStatement statement);
    T visitAttributeAssignmentStatement(AttributeAssignmentStatement statement);
    T visitIndexAssignmentStatement(IndexAssignmentStatement statement);
    T visitTupleUnpackingAssignment(TupleUnpackingAssignment statement);
    T visitExpressionStatement(ExpressionStatement statement);
    T visitIfStatement(IfStatement statement);
    T visitWhileStatement(WhileStatement statement);
    T visitForStatement(ForStatement statement);
    T visitFunctionDef(FunctionDef function);
    T visitClassDef(ClassDef classDef);
    T visitReturnStatement(ReturnStatement statement);    T visitBreakStatement(BreakStatement statement);
    T visitContinueStatement(ContinueStatement statement);    T visitPassStatement(PassStatement statement);
    T visitTryExceptStatement(TryExceptStatement statement);    T visitWithStatement(WithStatement statement);
    T visitGlobalStatement(GlobalStatement statement);    T visitNonlocalStatement(NonlocalStatement statement);
    T visitImportStatement(ImportStatement statement);
    T visitFromImportStatement(FromImportStatement statement);
    T visitMatchStatement(MatchStatement statement);

    T visitBinaryExpression(BinaryExpression expression);
    T visitUnaryExpression(UnaryExpression expression);
    T visitConditionalExpression(ConditionalExpression expression);
    T visitCallExpression(CallExpression expression);    T visitAttributeExpression(AttributeExpression expression);
    T visitIndexExpression(IndexExpression expression);
    T visitSliceExpression(SliceExpression expression);    T visitIdentifier(Identifier identifier);
    T visitLiteral(Literal literal);
    T visitFStringLiteral(FStringLiteral fStringLiteral);    T visitListLiteral(ListLiteral listLiteral);
    T visitDictLiteral(DictLiteral dictLiteral);
    T visitSetLiteral(SetLiteral setLiteral);
    T visitTupleLiteral(TupleLiteral tupleLiteral);T visitLambdaExpression(LambdaExpression lambdaExpression);
    T visitListComprehension(ListComprehension listComprehension);    T visitGeneratorExpression(GeneratorExpression generatorExpression);    T visitSuperExpression(SuperExpression expression);
    T visitStarredExpression(StarredExpression expression);
    T visitDecorator(Decorator decorator);
    
    // Match-case pattern visitors
    T visitWildcardPattern(WildcardPattern pattern);
    T visitCapturePattern(CapturePattern pattern);
    T visitLiteralPattern(LiteralPattern pattern);
    T visitSequencePattern(SequencePattern pattern);
    T visitOrPattern(OrPattern pattern);
}


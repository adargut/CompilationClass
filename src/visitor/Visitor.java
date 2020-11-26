package visitor;

import ast.*;

public interface Visitor {
    public String visit(Program program);
    public String visit(ClassDecl classDecl);
    public String visit(MainClass mainClass);
    public String visit(MethodDecl methodDecl);
    public String visit(FormalArg formalArg);
    public String visit(VarDecl varDecl);

    public String visit(BlockStatement blockStatement);
    public String visit(IfStatement ifStatement);
    public String visit(WhileStatement whileStatement);
    public String visit(SysoutStatement sysoutStatement);
    public String visit(AssignStatement assignStatement);
    public String visit(AssignArrayStatement assignArrayStatement);

    public String visit(AndExpr e);
    public String visit(LtExpr e);
    public String visit(AddExpr e);
    public String visit(SubtractExpr e);
    public String visit(MultExpr e);
    public String visit(ArrayAccessExpr e);
    public String visit(ArrayLengthExpr e);
    public String visit(MethodCallExpr e);
    public String visit(IntegerLiteralExpr e);
    public String visit(TrueExpr e);
    public String visit(FalseExpr e);
    public String visit(IdentifierExpr e);
    public String visit(ThisExpr e);
    public String visit(NewIntArrayExpr e);
    public String visit(NewObjectExpr e);
    public String visit(NotExpr e);

    public String visit(IntAstType t);
    public String visit(BoolAstType t);
    public String visit(IntArrayAstType t);
    public String visit(RefType t);
}

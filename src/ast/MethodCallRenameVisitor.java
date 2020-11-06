package ast;

import symboltable.Class;
import symboltable.SymbolTable;

import java.util.ArrayList;

public class MethodCallRenameVisitor implements Visitor {
    private final String newName;
    private final String oldName;
    private final SymbolTable symbolTable;
    private final ArrayList<Class> relevantClasses; // todo optimize to set somewhen
    private Class currentClass;

    public MethodCallRenameVisitor(String newName, SymbolTable symbolTable, String oldName, int oldMethodLine) {
        this.newName = newName;
        this.oldName = oldName;
        this.symbolTable = symbolTable;
        this.relevantClasses = symbolTable.getAllRelevantClasses(oldName, oldMethodLine);
    }

    @Override
    public void visit(Program program) {
        // Visit main class
        program.mainClass().accept(this);

        // Visit other classes
        for (var classdecl : program.classDecls()) {
            classdecl.accept(this);
        }
    }

    @Override
    public void visit(ClassDecl classDecl) {
        this.currentClass = this.symbolTable.getClass(classDecl.name());
        for (var methodDecl : classDecl.methoddecls()) {
            methodDecl.accept(this);
        }
        this.currentClass = null;
    }

    @Override
    public void visit(MainClass mainClass) {
        this.currentClass = this.symbolTable.getClass(mainClass.name());
        mainClass.mainStatement().accept(this);
        this.currentClass = null;
    }

    @Override
    public void visit(MethodDecl methodDecl) {

    }

    @Override
    public void visit(FormalArg formalArg) {

    }

    @Override
    public void visit(VarDecl varDecl) {

    }

    @Override
    public void visit(BlockStatement blockStatement) {
        for (var statement : blockStatement.statements()) {
            statement.accept(this);
        }
    }

    @Override
    public void visit(IfStatement ifStatement) {
        ifStatement.cond().accept(this);
        ifStatement.thencase().accept(this);
        ifStatement.elsecase().accept(this);
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        whileStatement.cond().accept(this);
        whileStatement.body().accept(this);
    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {
        sysoutStatement.arg().accept(this);
    }

    @Override
    public void visit(AssignStatement assignStatement) {
        assignStatement.rv().accept(this);
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        assignArrayStatement.index().accept(this);
        assignArrayStatement.rv().accept(this);
    }

    @Override
    public void visit(AndExpr e) {
        e.e1().accept(this);
        e.e2().accept(this);
    }

    @Override
    public void visit(LtExpr e) {
        e.e1().accept(this);
        e.e2().accept(this);
    }

    @Override
    public void visit(AddExpr e) {
        e.e1().accept(this);
        e.e2().accept(this);
    }

    @Override
    public void visit(SubtractExpr e) {
        e.e1().accept(this);
        e.e2().accept(this);
    }

    @Override
    public void visit(MultExpr e) {
        e.e1().accept(this);
        e.e2().accept(this);
    }

    @Override
    public void visit(ArrayAccessExpr e) {
        e.indexExpr().accept(this);
        e.arrayExpr().accept(this); // todo is this needed?
    }

    @Override
    public void visit(ArrayLengthExpr e) {
        e.arrayExpr().accept(this);
    }

    @Override
    public void visit(MethodCallExpr e) {
        // todo change this at the end to change after traversal
        var owner = e.ownerExpr();
        if (!e.methodId().equals(this.oldName)) return;

        // Case this.foo()
        if (e.ownerExpr() instanceof ThisExpr && relevantClasses.contains(currentClass)) {
            e.setMethodId(this.newName);
        }

        // Case x.foo()
        if (owner instanceof IdentifierExpr) {
            String className = ((IdentifierExpr) owner).id();
            var staticType = symbolTable.getClass(className);
            if (staticType != null && relevantClasses.contains(staticType)) e.setMethodId(this.newName);
        }

        // Case New X().foo()
        if (owner instanceof NewObjectExpr) {
            String className = ((NewObjectExpr) owner).classId();
            var staticType = symbolTable.getClass(className);
            if (staticType != null && relevantClasses.contains(staticType)) e.setMethodId(this.newName);
        }
    }

    @Override
    public void visit(IntegerLiteralExpr e) {

    }

    @Override
    public void visit(TrueExpr e) {

    }

    @Override
    public void visit(FalseExpr e) {

    }

    @Override
    public void visit(IdentifierExpr e) {

    }

    @Override
    public void visit(ThisExpr e) {

    }

    @Override
    public void visit(NewIntArrayExpr e) {

    }

    @Override
    public void visit(NewObjectExpr e) {

    }

    @Override
    public void visit(NotExpr e) {

    }

    @Override
    public void visit(IntAstType t) {

    }

    @Override
    public void visit(BoolAstType t) {

    }

    @Override
    public void visit(IntArrayAstType t) {

    }

    @Override
    public void visit(RefType t) {

    }
}

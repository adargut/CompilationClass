package visitor;

import ast.*;
import symboltable.Class;
import symboltable.Method;
import symboltable.SymbolTable;
import symboltable.Variable;

import java.util.ArrayList;

public class MethodRenameVisitor implements Visitor {
    private final String newName;
    private final String oldName;
    private final SymbolTable symbolTable;
    private final ArrayList<Class> relevantClasses; // todo optimize to set somewhen
    private final ArrayList<MethodDecl> relevantMethodDeclarations; // todo make this method not methoddecl
    private Class currentClass;
    private Method currentMethod;

    private void visitBinaryExpr(BinaryExpr e, String infixSymbol) {
        e.e1().accept(this);
        e.e2().accept(this);
    }

    public MethodRenameVisitor(String newName,
                               SymbolTable symbolTable,
                               String oldName,
                               int oldMethodLine) {
        this.newName = newName;
        this.oldName = oldName;
        this.symbolTable = symbolTable;
        this.relevantMethodDeclarations = symbolTable.getAllMethodsDeclarations(oldName, oldMethodLine);
        this.relevantClasses = symbolTable.getAllRelevantClasses(oldName, oldMethodLine);
    }

    @Override
    public String visit(Program program) {
        // Visit main class
        program.mainClass().accept(this);

        // Visit other classes
        for (var classdecl : program.classDecls()) {
            classdecl.accept(this);
        }
        return null;
    }

    @Override
    public String visit(ClassDecl classDecl) {
        this.currentClass = this.symbolTable.getClass(classDecl.name());
        for (var methodDecl : classDecl.methoddecls()) {
            methodDecl.accept(this);
        }
        this.currentClass = null;
        return null;
    }

    @Override
    public String visit(MainClass mainClass) {
        this.currentClass = this.symbolTable.getClass(mainClass.name());
        this.currentMethod = this.currentClass.getMethod("main");
        mainClass.mainStatement().accept(this);
        this.currentMethod = null;
        this.currentClass = null;
        return null;
    }

    @Override
    public String visit(MethodDecl methodDecl) {
        this.currentMethod = this.currentClass.getMethod(methodDecl.name());

        if (relevantMethodDeclarations.contains(methodDecl)) {
            methodDecl.setName(this.newName);
        }
        for (var formal : methodDecl.formals()) {
            formal.accept(this);
        }

        for (var varDecl : methodDecl.vardecls()) {
            varDecl.accept(this);
        }

        for (var stmt : methodDecl.body()) {
            stmt.accept(this);
        }

        methodDecl.ret().accept(this);

        this.currentMethod = null;
        return null;
    }

    @Override
    public String visit(FormalArg formalArg) {

        return null;
    }

    @Override
    public String visit(VarDecl varDecl) {

        return null;
    }

    @Override
    public String visit(BlockStatement blockStatement) {
        for (var statement : blockStatement.statements()) {
            statement.accept(this);
        }
        return null;
    }

    @Override
    public String visit(IfStatement ifStatement) {
        ifStatement.cond().accept(this);
        ifStatement.thencase().accept(this);
        ifStatement.elsecase().accept(this);
        return null;
    }

    @Override
    public String visit(WhileStatement whileStatement) {
        whileStatement.cond().accept(this);
        whileStatement.body().accept(this);
        return null;
    }

    @Override
    public String visit(SysoutStatement sysoutStatement) {
        sysoutStatement.arg().accept(this);
        return null;
    }

    @Override
    public String visit(AssignStatement assignStatement) {
        assignStatement.rv().accept(this);
        return null;
    }

    @Override
    public String visit(AssignArrayStatement assignArrayStatement) {
        assignArrayStatement.index().accept(this);
        assignArrayStatement.rv().accept(this);
        return null;
    }

    @Override
    public String visit(AndExpr e) {
        visitBinaryExpr(e, "&&");
        return null;
    }

    @Override
    public String visit(LtExpr e) {
        visitBinaryExpr(e, "<");
        return null;
    }

    @Override
    public String visit(AddExpr e) {
        visitBinaryExpr(e, "+");
        return null;
    }

    @Override
    public String visit(SubtractExpr e) {
        visitBinaryExpr(e, "-");
        return null;
    }

    @Override
    public String visit(MultExpr e) {
        visitBinaryExpr(e, "*");
        return null;
    }

    @Override
    public String visit(ArrayAccessExpr e) {
        e.indexExpr().accept(this);
        e.arrayExpr().accept(this); // todo is this needed?
        return null;
    }

    @Override
    public String visit(ArrayLengthExpr e) {
        e.arrayExpr().accept(this);
        return null;
    }

    @Override
    public String visit(MethodCallExpr e) {
        // todo change this at the end to change after traversal
        var owner = e.ownerExpr();
        if (!e.methodId().equals(this.oldName)) return null;

        // Case this.foo()
        if (e.ownerExpr() instanceof ThisExpr && relevantClasses.contains(currentClass)) {
            e.setMethodId(this.newName);
        }

        // Case x.foo()
        if (owner instanceof IdentifierExpr) {
            String symbol = ((IdentifierExpr) owner).id();

            Variable variable;

            // Search for symbol upwards in symbol table to get type of the variable
            if (this.currentMethod == null) {
                // Global scope
                variable = this.symbolTable.getVar(this.currentClass, symbol);
            }
            else {
                // Local scope (method scope)
                variable = this.symbolTable.getVar(this.currentMethod, symbol);
            }

            if (variable == null) {
                throw new RuntimeException(String.format("Variable with name %s was not declared in current scope!", symbol));
            }

            var varType = variable.getType();

            if (varType instanceof RefType) {
                String className = ((RefType) varType).id();
                var staticType = symbolTable.getClass(className);
                if (staticType != null && relevantClasses.contains(staticType)) e.setMethodId(this.newName);
            }
        }

        // Case New X().foo()
        if (owner instanceof NewObjectExpr) {
            String className = ((NewObjectExpr) owner).classId();
            var staticType = symbolTable.getClass(className);
            if (staticType != null && relevantClasses.contains(staticType)) e.setMethodId(this.newName);
        }
        return null;
    }

    @Override
    public String visit(IntegerLiteralExpr e) {

        return null;
    }

    @Override
    public String visit(TrueExpr e) {

        return null;
    }

    @Override
    public String visit(FalseExpr e) {

        return null;
    }

    @Override
    public String visit(IdentifierExpr e) {

        return null;
    }

    @Override
    public String visit(ThisExpr e) {

        return null;
    }

    @Override
    public String visit(NewIntArrayExpr e) {
        e.lengthExpr().accept(this);
        return null;
    }

    @Override
    public String visit(NewObjectExpr e) {

        return null;
    }

    @Override
    public String visit(NotExpr e) {
        e.e().accept(this);
        return null;
    }

    @Override
    public String visit(IntAstType t) {

        return null;
    }

    @Override
    public String visit(BoolAstType t) {

        return null;
    }

    @Override
    public String visit(IntArrayAstType t) {

        return null;
    }

    @Override
    public String visit(RefType t) {

        return null;
    }
}

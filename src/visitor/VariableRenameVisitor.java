package visitor;

import ast.*;
import symboltable.Class;
import symboltable.Method;
import symboltable.SymbolTable;
import symboltable.Variable;

public class VariableRenameVisitor implements Visitor {
    private final String newName;
    private final SymbolTable symbolTable;
    private final Variable variableToReplace;
    private Class currentClass;
    private Method currentMethod;

    public VariableRenameVisitor(String newName,
                                 SymbolTable symbolTable,
                                 String oldName,
                                 int oldMethodLine) {
        this.newName = newName;
        this.symbolTable = symbolTable;
        this.variableToReplace = symbolTable.getVarByNameAndLine(oldName, oldMethodLine);

        if (this.variableToReplace == null) {
            throw new RuntimeException(String.format("Variable %S at line %d was not found!", oldName, oldMethodLine));
        }
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

        for (var fieldDecl : classDecl.fields()) {
            fieldDecl.accept(this);
        }
        for (var methodDecl : classDecl.methoddecls()) {
            methodDecl.accept(this);
        }

        this.currentClass = null;
    }

    @Override
    public void visit(MainClass mainClass) {
        this.currentClass = this.symbolTable.getClass(mainClass.name());
        this.currentMethod = this.currentClass.getMethod("main");
        mainClass.mainStatement().accept(this);
        this.currentMethod = null;
        this.currentClass = null;
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        this.currentMethod = this.currentClass.getMethod(methodDecl.name());

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
    }

    @Override
    public void visit(FormalArg formalArg) {
        formalArg.type().accept(this);

        Variable variable;

        // Search for symbol upwards in symbol table to get type of the variable
        if (this.currentMethod == null) {
            // Global scope
            variable = this.symbolTable.getVar(this.currentClass, formalArg.name());
        }
        else {
            // Local scope (method scope)
            variable = this.symbolTable.getVar(this.currentMethod, formalArg.name());
        }

        if (variable == null) {
            throw new RuntimeException(String.format("Variable with name %s was not declared in current scope!", formalArg.name()));
        }

        if (variable.equals(this.variableToReplace)) {
            formalArg.setName(this.newName);
        }


    }

    @Override
    public void visit(VarDecl varDecl) {
        varDecl.type().accept(this);

        Variable variable;

        // Search for symbol upwards in symbol table to get type of the variable
        if (this.currentMethod == null) {
            // Global scope
            variable = this.symbolTable.getVar(this.currentClass, varDecl.name());
        }
        else {
            // Local scope (method scope)
            variable = this.symbolTable.getVar(this.currentMethod, varDecl.name());
        }

        if (variable == null) {
            throw new RuntimeException(String.format("Variable with name %s was not declared in current scope!", varDecl.name()));
        }

        if (variable.equals(this.variableToReplace)) {
            varDecl.setName(this.newName);
        }
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
        Variable variable;

        // Search for symbol upwards in symbol table to get type of the variable
        if (this.currentMethod == null) {
            // Global scope
            variable = this.symbolTable.getVar(this.currentClass, assignStatement.lv());
        }
        else {
            // Local scope (method scope)
            variable = this.symbolTable.getVar(this.currentMethod, assignStatement.lv());
        }

        if (variable == null) {
            throw new RuntimeException(String.format("Variable with name %s was not declared in current scope!", assignStatement.lv()));
        }

        if (variable.equals(this.variableToReplace)) {
            assignStatement.setLv(this.newName);
        }

        assignStatement.rv().accept(this);
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        Variable variable;

        // Search for symbol upwards in symbol table to get type of the variable
        if (this.currentMethod == null) {
            // Global scope
            variable = this.symbolTable.getVar(this.currentClass, assignArrayStatement.lv());
        }
        else {
            // Local scope (method scope)
            variable = this.symbolTable.getVar(this.currentMethod, assignArrayStatement.lv());
        }

        if (variable == null) {
            throw new RuntimeException(String.format("Variable with name %s was not declared in current scope!", assignArrayStatement.lv()));
        }

        if (variable.equals(this.variableToReplace)) {
            assignArrayStatement.setLv(this.newName);
        }

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
        e.arrayExpr().accept(this);
    }

    @Override
    public void visit(ArrayLengthExpr e) {
        e.arrayExpr().accept(this);
    }

    @Override
    public void visit(MethodCallExpr e) {
        e.ownerExpr().accept(this);

        for (Expr arg : e.actuals()) {
            arg.accept(this);
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
        Variable variable;

        // Search for symbol upwards in symbol table to get type of the variable
        if (this.currentMethod == null) {
            // Global scope
            variable = this.symbolTable.getVar(this.currentClass, e.id());
        }
        else {
            // Local scope (method scope)
            variable = this.symbolTable.getVar(this.currentMethod, e.id());
        }

        if (variable == null) {
            throw new RuntimeException(String.format("Variable with name %s was not declared in current scope!", e.id()));
        }

        if (variable.equals(this.variableToReplace)) {
            e.setId(this.newName);
        }
    }

    @Override
    public void visit(ThisExpr e) {

    }

    @Override
    public void visit(NewIntArrayExpr e) {
        e.lengthExpr().accept(this);
    }

    @Override
    public void visit(NewObjectExpr e) {
    }

    @Override
    public void visit(NotExpr e) {
        e.e().accept(this);
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

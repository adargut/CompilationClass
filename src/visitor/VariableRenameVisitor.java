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

    private void visitBinaryExpr(BinaryExpr e, String infixSymbol) {
        e.e1().accept(this);
        e.e2().accept(this);
    }

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

        for (var fieldDecl : classDecl.fields()) {
            fieldDecl.accept(this);
        }
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


        return null;
    }

    @Override
    public String visit(VarDecl varDecl) {
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
        return null;
    }

    @Override
    public String visit(AssignArrayStatement assignArrayStatement) {
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
        e.arrayExpr().accept(this);
        return null;
    }

    @Override
    public String visit(ArrayLengthExpr e) {
        e.arrayExpr().accept(this);
        return null;
    }

    @Override
    public String visit(MethodCallExpr e) {
        e.ownerExpr().accept(this);

        for (Expr arg : e.actuals()) {
            arg.accept(this);
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

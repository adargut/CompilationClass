package visitor;

import ast.*;
import semanticanalysis.SemanticError;
import semanticanalysis.SemanticException;
import symboltable.Method;
import symboltable.SymbolTable;
import symboltable.Variable;
import symboltable.Class;

public class BuildClassHierarchyVisitor implements Visitor {
    private Class currentClass;
    private Method currentMethod;
    private final SymbolTable symbolTable;

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public BuildClassHierarchyVisitor() {
        this.currentMethod = null;
        this.symbolTable = new SymbolTable();
        this.currentClass = null;
    }

    private void visitBinaryExpr(BinaryExpr e, String infixSymbol) {
        e.e1().accept(this);
        e.e2().accept(this);
    }

    @Override
    public String visit(Program program) {
        program.mainClass().accept(this);

        for (ClassDecl classdecl : program.classDecls()) {
            classdecl.accept(this);
        }
        return null;
    }

    @Override
    public String visit(ClassDecl classDecl) {
        // Verify that a class with the name was not declared before
        if (!this.symbolTable.addClass(classDecl.name(), classDecl.superName(), classDecl, false)) {
            // Class name is already in use - SEMANTIC ERROR #3
            throw new SemanticException(
                    String.format("Class %s already declared.", classDecl.name()),
                    SemanticError.NAME_ALREADY_EXISTS
            );
        }

        this.currentClass = this.symbolTable.getClass(classDecl.name());

        for (var fieldDecl : classDecl.fields()) {
            fieldDecl.accept(this);
        }
        for (var methodDecl : classDecl.methoddecls()) {
            methodDecl.accept(this);
        }

        // Backtrack - exit class
        this.currentClass = null;
        return null;
    }

    @Override
    public String visit(MainClass mainClass) {
        if (!this.symbolTable.addClass(mainClass.name(), null, null, true)) {
            // Class name is already in use - SEMANTIC ERROR #3
            throw new SemanticException(
                    String.format("Class %s already declared.", mainClass.name()),
                    SemanticError.NAME_ALREADY_EXISTS
            );
        }

        this.currentClass = this.symbolTable.getClass(mainClass.name());
        this.currentMethod = new Method("main", null, this.symbolTable.classHierarchy.getRoot().getData());
        this.currentClass.addMethod(this.currentMethod);
        this.currentMethod.addParam(new Variable(mainClass.argsName(), new IntArrayAstType(), null, false, false, true));
        mainClass.mainStatement().accept(this);
        this.currentMethod = null;
        this.currentClass = null;
        return null;
    }

    @Override
    public String visit(MethodDecl methodDecl) {
        if (this.currentClass == null) {
            throw new SemanticException(SemanticError.METHOD_OUTSIDE_CLASS);
        }

        // Add method to its class
        this.currentMethod = new Method(methodDecl.name(), methodDecl.lineNumber, this.currentClass, methodDecl);

        if (!this.currentClass.addMethod(this.currentMethod)) {
            // A method with the same name already exists in the current class (overloading) - SEMANTIC ERROR #5
            throw new SemanticException(
                    SemanticError.OVERLOADING_NOT_SUPPORTED,
                    new String[]{methodDecl.name()}
            );
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

        // Backtrack - exit method
        this.currentMethod = null;
        return null;
    }

    @Override
    public String visit(FormalArg formalArg) {
        if (this.currentMethod == null) {
            throw new SemanticException(SemanticError.FORMAL_OUTSIDE_METHOD);
        }

        if (!this.currentMethod.addParam(new Variable(formalArg.name(), formalArg.type(), formalArg.lineNumber, false, false, true))) {
            // Formal with the same name already exists in method declaration - SEMANTIC ERROR #24
            throw new SemanticException(
                    String.format("Formal with symbol %s was already declared.", formalArg.name()),
                    SemanticError.NAME_ALREADY_EXISTS
            );
        }

        formalArg.type().accept(this);
        return null;
    }

    @Override
    public String visit(VarDecl varDecl) {
        if (this.currentMethod != null) {
            // Local scope - inside a method
            if (!this.currentMethod.addVar(new Variable(varDecl.name(), varDecl.type(), varDecl.lineNumber, false, true, false))) {
                // A variable or formal with the same name was already declared  - SEMANTIC ERROR #24
                throw new SemanticException(
                        String.format("Variable or formal with symbol %s was already declared.", varDecl.name()),
                        SemanticError.NAME_ALREADY_EXISTS
                );
            }
        } else {
            // Global scope - inside a class (a field)
            if (!this.currentClass.addVar(new Variable(varDecl.name(), varDecl.type(), varDecl.lineNumber, true, false, false))) {
                // A field with the same name already exists - SEMANTIC ERROR #4
                throw new SemanticException(
                        String.format("Field with symbol %s was already declared.", varDecl.name()),
                        SemanticError.NAME_ALREADY_EXISTS
                );
            }
        }

        varDecl.type().accept(this);
        return null;
    }

    @Override
    public String visit(BlockStatement blockStatement) {
        for (var s : blockStatement.statements()) {
            s.accept(this);
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
        e.arrayExpr().accept(this);
        e.indexExpr().accept(this);
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
        return null;
    }

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

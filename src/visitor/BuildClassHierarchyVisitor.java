package visitor;

import ast.*;
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
        if (!this.symbolTable.addClass(classDecl.name(), classDecl.superName(), classDecl, false)) {
            throw new RuntimeException(
                    String.format("A class with name %s was already declared!",
                            classDecl.name())
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
            throw new RuntimeException(
                    String.format("A class with name %s was already declared!",
                            mainClass.name())
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
            throw new RuntimeException("Methods can't be declared outside of a class!");
        }

        // Add method to its class
        this.currentMethod = new Method(methodDecl.name(), methodDecl.lineNumber, this.currentClass, methodDecl);

        if (!this.currentClass.addMethod(this.currentMethod)) {
            // Duplicate
            throw new RuntimeException(
                    String.format("A method with name %s was already declared (overloading is not supported)!",
                            methodDecl.name())
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
            throw new RuntimeException("Formals can't be declared outside of a method!");
        }

        if (!this.currentMethod.addParam(new Variable(formalArg.name(), formalArg.type(), formalArg.lineNumber, false, false, true))) {
            // A duplicate
            throw new RuntimeException(
                    String.format("Formal with symbol %s was already declared!", formalArg.name())
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
                // A duplicate
                throw new RuntimeException(
                        String.format("Variable with symbol %s was already declared!", varDecl.name())
                );
            }
        } else {
            // Global scope - inside a class (a field)
            if (!this.currentClass.addVar(new Variable(varDecl.name(), varDecl.type(), varDecl.lineNumber, true, false, false))) {
                // A duplicate
                throw new RuntimeException(
                        String.format("Field with symbol %s was already declared!", varDecl.name())
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

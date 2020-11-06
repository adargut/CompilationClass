package ast;

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

    @Override
    public void visit(Program program) {
        program.mainClass().accept(this);

        for (ClassDecl classdecl : program.classDecls()) {
            classdecl.accept(this);
        }
    }

    @Override
    public void visit(ClassDecl classDecl) {
        if (!this.symbolTable.addClass(classDecl.name(), classDecl.superName(), classDecl)) {
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
    }

    @Override
    public void visit(MainClass mainClass) {
        if (!this.symbolTable.addClass(mainClass.name(), null, null)) {
            throw new RuntimeException(
                    String.format("A class with name %s was already declared!",
                            mainClass.name())
            );
        }

        this.currentClass = this.symbolTable.getClass(mainClass.name());
        this.currentMethod = new Method("main", null, this.symbolTable.classHierarchy.getRoot().getData());
        this.currentClass.addMethod(this.currentMethod);
        this.currentMethod.addParam(new Variable(mainClass.argsName(), new IntArrayAstType(), null));
        mainClass.mainStatement().accept(this);
        this.currentMethod = null;
        this.currentClass = null;
    }

    @Override
    public void visit(MethodDecl methodDecl) {
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
    }

    @Override
    public void visit(FormalArg formalArg) {
        if (this.currentMethod == null) {
            throw new RuntimeException("Formals can't be declared outside of a method!");
        }

        if (!this.currentMethod.addParam(new Variable(formalArg.name(), formalArg.type(), formalArg.lineNumber))) {
            // A duplicate
            throw new RuntimeException(
                    String.format("Formal with symbol %s was already declared!", formalArg.name())
            );
        }

        formalArg.type().accept(this);
    }

    @Override
    public void visit(VarDecl varDecl) {
        if (this.currentMethod != null) {
            // Local scope - inside a method
            if (!this.currentMethod.addVar(new Variable(varDecl.name(), varDecl.type(), varDecl.lineNumber))) {
                // A duplicate
                throw new RuntimeException(
                        String.format("Variable with symbol %s was already declared!", varDecl.name())
                );
            }
        } else {
            // Global scope - inside a class (a field)
            if (!this.currentClass.addVar(new Variable(varDecl.name(), varDecl.type(), varDecl.lineNumber))) {
                // A duplicate
                throw new RuntimeException(
                        String.format("Field with symbol %s was already declared!", varDecl.name())
                );
            }
        }

        varDecl.type().accept(this);
    }

    @Override
    public void visit(BlockStatement blockStatement) {
        for (var s : blockStatement.statements()) {
            s.accept(this);
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
        visitBinaryExpr(e, "&&");
    }

    @Override
    public void visit(LtExpr e) {
        visitBinaryExpr(e, "<");
    }

    @Override
    public void visit(AddExpr e) {
        visitBinaryExpr(e, "+");
    }

    @Override
    public void visit(SubtractExpr e) {
        visitBinaryExpr(e, "-");
    }

    @Override
    public void visit(MultExpr e) {
        visitBinaryExpr(e, "*");
    }

    @Override
    public void visit(ArrayAccessExpr e) {
        e.arrayExpr().accept(this);
        e.indexExpr().accept(this);
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
    }

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

    private void visitBinaryExpr(BinaryExpr e, String infixSymbol) {
        e.e1().accept(this);
        e.e2().accept(this);
    }

}

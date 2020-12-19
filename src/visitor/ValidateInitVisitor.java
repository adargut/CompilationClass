package visitor;

import ast.*;
import symboltable.Class;
import symboltable.Method;
import symboltable.SymbolTable;
import utils.InitMap;

public class ValidateInitVisitor implements Visitor {
    private Class currentClass;
    private Method currentMethod;
    private final SymbolTable symbolTable;
    private InitMap currentInitMap;
    private boolean valid;

    public ValidateInitVisitor(SymbolTable symbolTable) {
        this.currentMethod = null;
        this.symbolTable = symbolTable;
        this.currentClass = null;
    }

    public boolean isValid() {
        return valid;
    }

    private void visitBinaryExpr(BinaryExpr e, String infixSymbol) {
        e.e1().accept(this);
        e.e2().accept(this);
    }

    @Override
    public String visit(Program program) {
        valid = true;
        program.mainClass().accept(this);

        for (ClassDecl classdecl : program.classDecls()) {
            classdecl.accept(this);
        }
        return null;
    }

    @Override
    public String visit(MainClass mainClass) {
        this.currentClass = this.symbolTable.getClass(mainClass.name());
        this.currentMethod = this.symbolTable.getMethod("main", null);
        mainClass.mainStatement().accept(this);
        this.currentMethod = null;
        this.currentClass = null;
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

        // Backtrack - exit class
        this.currentClass = null;
        return null;
    }

    @Override
    public String visit(MethodDecl methodDecl) {
        if (this.currentClass == null) {
            throw new RuntimeException("Methods can't be declared outside of a class!");
        }

        this.currentMethod = symbolTable.getMethod(methodDecl.name(), methodDecl.lineNumber);
        currentInitMap = new InitMap();

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
        this.currentInitMap = null;
        return null;
    }

    @Override
    public String visit(FormalArg formalArg) {
        return null;
    }

    @Override
    public String visit(VarDecl varDecl) {
        this.currentInitMap.addVariable(varDecl.name());
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
        InitMap thenMap = new InitMap(currentInitMap); //copy
        InitMap elseMap = new InitMap(currentInitMap); //copy
        currentInitMap = thenMap;
        ifStatement.thencase().accept(this);
        currentInitMap = elseMap;
        ifStatement.elsecase().accept(this);
        currentInitMap = InitMap.merge(thenMap, elseMap);
        return null;
    }

    @Override
    public String visit(WhileStatement whileStatement) {
        whileStatement.cond().accept(this);
        InitMap enterMap = new InitMap(currentInitMap); //copy
        InitMap noEnterMap = new InitMap(currentInitMap); //copy
        currentInitMap = enterMap;
        whileStatement.body().accept(this);
        currentInitMap = InitMap.merge(enterMap, noEnterMap);
        return null;
    }

    @Override
    public String visit(SysoutStatement sysoutStatement) {
        sysoutStatement.arg().accept(this);
        return null;
    }

    @Override
    public String visit(AssignStatement assignStatement) {
        if(currentMethod.getVar(assignStatement.lv()).isLocalVariable()) { //lv is a local variable
            currentInitMap.init(assignStatement.lv());
        }
        assignStatement.rv().accept(this);
        return null;
    }

    @Override
    public String visit(AssignArrayStatement assignArrayStatement) {
        if(currentMethod.getVar(assignArrayStatement.lv()).isLocalVariable()) { //lv is a local variable
            currentInitMap.init(assignArrayStatement.lv());
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
        if(currentMethod.getVar(e.id()).isLocalVariable()) { //e is a local variable
            valid = currentInitMap.isInit(e.id()); // if e is not init here the validation fails
        }
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
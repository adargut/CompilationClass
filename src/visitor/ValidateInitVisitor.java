package visitor;

import ast.*;
import semanticanalysis.SemanticError;
import semanticanalysis.SemanticException;
import symboltable.Class;
import symboltable.Method;
import symboltable.SymbolTable;
import utils.InitMap;

public class ValidateInitVisitor implements Visitor {
    private Class currentClass;
    private Method currentMethod;
    private final SymbolTable symbolTable;
    private InitMap currentInitMap;

    public ValidateInitVisitor(SymbolTable symbolTable) {
        this.currentMethod = null;
        this.symbolTable = symbolTable;
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
    public String visit(MainClass mainClass) {
        this.currentInitMap = new InitMap();
        this.currentClass = this.symbolTable.getClass(mainClass.name());
        this.currentMethod = this.symbolTable.getMethod("main", null);
        mainClass.mainStatement().accept(this);
        this.currentMethod = null;
        this.currentClass = null;
        this.currentInitMap = null;
        return null;
    }

    @Override
    public String visit(ClassDecl classDecl) {
        this.currentClass = this.symbolTable.getClass(classDecl.name());
        this.currentInitMap = new InitMap();

        for (var fieldDecl : classDecl.fields()) {
            fieldDecl.accept(this);
        }

        for (var methodDecl : classDecl.methoddecls()) {
            methodDecl.accept(this);
        }

        // Backtrack - exit class
        this.currentClass = null;
        this.currentInitMap = null;
        return null;
    }

    @Override
    public String visit(MethodDecl methodDecl) {
        this.currentMethod = symbolTable.getMethod(methodDecl.name(), methodDecl.lineNumber);
        InitMap classMap = new InitMap(currentInitMap);
        InitMap methodMap = new InitMap(currentInitMap);
        this.currentInitMap = methodMap;

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
        this.currentInitMap = classMap;
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
        InitMap beforeMap = new InitMap(currentInitMap); //copy
        ifStatement.thencase().accept(this); // construct map of then clause
        InitMap thenMap = new InitMap(currentInitMap);
        currentInitMap = beforeMap; // reset map to state before if
        ifStatement.elsecase().accept(this); // construct map of else clause
        currentInitMap = InitMap.merge(thenMap, currentInitMap);
        return null;
    }

    @Override
    public String visit(WhileStatement whileStatement) {
        whileStatement.cond().accept(this);
        InitMap noEnterMap = new InitMap(currentInitMap); //copy
        whileStatement.body().accept(this); // construct map of inside while
        currentInitMap = InitMap.merge(noEnterMap, currentInitMap);
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
        if(this.symbolTable.getVar(currentMethod, assignStatement.lv()).isLocalVariable()) { //lv is a local variable
            currentInitMap.init(assignStatement.lv());
        }
        return null;
    }

    @Override
    public String visit(AssignArrayStatement assignArrayStatement) {
        var variable = this.symbolTable.getVar(currentMethod, assignArrayStatement.lv());
        if(variable != null && variable.isLocalVariable()) { // lv is a local variable
            if (!currentInitMap.isInit(assignArrayStatement.lv())) {
                // if lv is not init here the validation fails
                // Obj is not definitely initialized - SEMANTIC ERROR #15
                throw new SemanticException(
                        SemanticError.OBJ_NOT_INITIALIZED,
                        new String[] {
                                assignArrayStatement.lv(),
                                this.currentClass != null ? this.currentClass.getName() : "",
                                this.currentMethod != null ? this.currentMethod.getName() : ""
                        }
                );
            }
        }

        assignArrayStatement.index().accept(this);
        assignArrayStatement.rv().accept(this);
        if(this.symbolTable.getVar(currentMethod, assignArrayStatement.lv()).isLocalVariable()) { //lv is a local variable
            currentInitMap.init(assignArrayStatement.lv());
        }
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
        var variable = this.symbolTable.getVar(currentMethod, e.id());
        if(variable != null && variable.isLocalVariable()) { //e is a local variable
            if (!currentInitMap.isInit(e.id())) {
                // if e is not init here the validation fails
                // Obj is not definitely initialized - SEMANTIC ERROR #15
                throw new SemanticException(
                        SemanticError.OBJ_NOT_INITIALIZED,
                        new String[] {
                                e.id(),
                                this.currentClass != null ? this.currentClass.getName() : "",
                                this.currentMethod != null ? this.currentMethod.getName() : ""
                        }
                );
            }
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
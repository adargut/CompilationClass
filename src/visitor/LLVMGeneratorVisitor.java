package visitor;

import ast.*;
import symboltable.Class;
import symboltable.Method;
import symboltable.SymbolTable;
import symboltable.Variable;
import vtables.VTables;

import java.util.Map;

public class LLVMGeneratorVisitor implements Visitor {
    private StringBuilder builder = new StringBuilder();
    private final SymbolTable symbolTable;
    private final VTables vTables;
    private Class currentClass;
    private Method currentMethod;
    private int register;
    private int loopLabel;
    private int ifLabel;
    private int andLabel;
    private int boundsLabel;
    private int arrAllocLabel;

    public String getString() {
        return builder.toString();
    }

    public LLVMGeneratorVisitor(VTables vTables,
                                SymbolTable symbolTable) {
        this.vTables = vTables;
        this.symbolTable = symbolTable;
        this.register = 0;
        this.loopLabel = 0;
        this.ifLabel = 0;
        this.andLabel = 0;
        this.boundsLabel = 0;
        this.arrAllocLabel = 0;
    }

    String get_register() {
        int ret = this.register;
        this.register++;
        return "%_" + ret;
    }

    String getObjectType(Object obj) {
        if (obj instanceof IntAstType) {
            return "i32";
        }

        else if (obj instanceof BoolAstType) {
            return "i1";
        }

        else if (obj instanceof IntArrayAstType) {
            return "i32*";
        }

        else {
            return "i8*";
        }
    }
    void createVTables() {
        boolean isFirst = true;

        for(Map.Entry entry: this.vTables.classesTables.entrySet()) {
            String className = (String) entry.getKey();
            VTables.ClassVTable classVTable = this.vTables.classesTables.get(className);

            if (classVTable.isMainClass) {
                this.builder.append("@." + className + "_vtable = global [0 x i8*] []\n");
                continue;
            }

            int numberOfMethods = classVTable.methodsTable.size();
            this.builder.append("@." + className + "_vtable = global [" + numberOfMethods + " x i8*] [");

            // Return type
            for (Map.Entry methodEntry: classVTable.methodsTable.entrySet()) {
                if (!isFirst) {
                    this.builder.append(", ");
                }

                String methodName = (String) methodEntry.getKey();
                Method method = (Method) methodEntry.getValue();

                this.builder.append("i8* bitcast (" + getObjectType(method.getMethodDecl().returnType()) + " (i8*");

                // Params
                for (Map.Entry methodParam: method.getParams().entrySet()) {
                    String paramName = (String) methodParam.getKey();
                    Variable param = (Variable) methodParam.getValue();

                    this.builder.append(", " + getObjectType(param.getType()));
                }

                this.builder.append(")* @" + className + "." + methodName + " to i8*)");
                isFirst = false;
            }

            this.builder.append("]\n");
        }
    }

    void createHelperMethods() {
        this.builder.append(
                "declare i8* @calloc(i32, i32)\n" +
                "declare i32 @printf(i8*, ...)\n" +
                "declare void @exit(i32)\n" +
                "\n" +
                "@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n" +
                "@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n" +
                "define void @print_int(i32 %i) {\n" +
                "    %_str = bitcast [4 x i8]* @_cint to i8*\n" +
                "    call i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n" +
                "    ret void\n" +
                "}\n" +
                "\n" +
                "define void @throw_oob() {\n" +
                "    %_str = bitcast [15 x i8]* @_cOOB to i8*\n" +
                "    call i32 (i8*, ...) @printf(i8* %_str)\n" +
                "    call void @exit(i32 1)\n" +
                "    ret void\n" +
                "}"
        );
    }

    @Override
    public String visit(Program program) {
        // Create VTables;
        this.createVTables();

        // Declare the helper methods
        this.createHelperMethods();

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

        // We only need to visit the methods, not the fields
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

        this.builder.append("\ndefine i32 @main() {\n");

        mainClass.mainStatement().accept(this);

        this.builder.append("\n\tret i32 0\n}\n");

        this.register = 0;
        this.loopLabel = 0;
        this.ifLabel = 0;
        this.andLabel = 0;
        this.boundsLabel = 0;
        this.arrAllocLabel = 0;

        this.currentMethod = null;
        this.currentClass = null;

        return null;
    }

    @Override
    public String visit(MethodDecl methodDecl) {
        this.currentMethod = this.currentClass.getMethod(methodDecl.name());

        this.builder.append("\ndefine " + getObjectType(methodDecl.returnType()) + "@" + this.currentClass.getName()
                + "." + methodDecl.name() + "(i8* %this, ");

        boolean isFirst = true;
        for (var formal : methodDecl.formals()) {
            if (!isFirst) {
                this.builder.append(", ");
            }
            this.builder.append(getObjectType(formal.type() + " %." + formal.name()));
            isFirst = false;
        }

        this.builder.append(") {\n");

        // Handle formals allocations to stack
        for (var formal : methodDecl.formals()) {
            formal.accept(this);
        }

        for (var varDecl : methodDecl.vardecls()) {
            varDecl.accept(this);
        }

        for (var stmt : methodDecl.body()) {
            stmt.accept(this);
        }

        String retRegister = methodDecl.ret().accept(this);
        this,builder.append("\n\tret " + getObjectType(methodDecl.returnType()) + " " + retRegister + "\n}\n";)
        this.currentMethod = null;
    }

    @Override
    public String visit(FormalArg formalArg) {
        String type = getObjectType(formalArg.type());
        this.builder.append("\t%" + formalArg.name() + " = alloca " +  type + "\n");
        this.builder.append("\tstore " + type + " %." + formalArg.name() + ", " + type + "* %" + formalArg.name() + "\n");
        return null;
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

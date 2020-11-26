package visitor;

import ast.*;
import codegen.Alloca;
import codegen.Declare;
import codegen.utils.JavaTypeToLLVMType;
import codegen.utils.LLVMType;
import symboltable.Class;
import symboltable.Method;
import symboltable.SymbolTable;
import symboltable.Variable;
import codegen.vtable.VTables;

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
    }

    String getRegister() {
        int retVal = this.register;
        this.register++;
        return "%_" + retVal;
    }

    String getIfLabel() {
        int retVal = this.ifLabel;
        this.ifLabel++;
        return "if" + retVal;
    }

    String getLoopLabel() {
        int retVal = this.loopLabel;
        this.loopLabel++;
        return "loop" + retVal;
    }

    String getAndLabel() {
        int retVal = this.andLabel;
        this.andLabel++;
        return "andcond" + retVal;
    }

    @Override
    public String visit(Program program) {
        // Create VTables;
        this.builder.append(this.vTables.generate());

        // Declare the helper methods
        var declare = Declare.getInstance();
        this.builder.append(declare.generate());

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

        this.currentMethod = null;
        this.currentClass = null;

        return null;
    }

    @Override
    public String visit(MethodDecl methodDecl) {
        this.currentMethod = this.currentClass.getMethod(methodDecl.name());

        this.builder.append("\ndefine " + JavaTypeToLLVMType.getLLVMType(methodDecl.returnType()) + "@" + this.currentClass.getName()
                + "." + methodDecl.name() + "(i8* %this");

        for (var formal : methodDecl.formals()) {
            this.builder.append(", ");
            this.builder.append(JavaTypeToLLVMType.getLLVMType(formal.type()) + " %." + formal.name());
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
        this.builder.append("\tret " + JavaTypeToLLVMType.getLLVMType(methodDecl.returnType()) + " " + retRegister + "\n}\n");

        this.register = 0;
        this.loopLabel = 0;
        this.ifLabel = 0;
        this.andLabel = 0;
        this.currentMethod = null;

        return null;
    }

    @Override
    public String visit(FormalArg formalArg) {
        Alloca alloca = Alloca.getInstance();
        String variableSymbol = formalArg.name();
        LLVMType llvmType = JavaTypeToLLVMType.getLLVMType(formalArg.type());
        alloca.setVariable(symbolTable.getVar(this.currentMethod, formalArg.name()));
        this.builder.append(alloca.generate());
        this.builder.append("\tstore " + llvmType + " %." + variableSymbol + ", " + llvmType + "* %" + variableSymbol + "\n");
        return null;
    }

    @Override
    public String visit(VarDecl varDecl) {
        Variable var = symbolTable.getVar(this.currentMethod, varDecl.name());

        if (var.isLocalVariable()) {
            // Method scope
            Alloca alloca = Alloca.getInstance();
            alloca.setVariable(var);
            this.builder.append(alloca.generate());
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
        String ifLabel = getIfLabel();
        String elseLabel = getIfLabel();
        String exitLabel = getIfLabel();

        String retRegister = ifStatement.cond().accept(this);
        this.builder.append("\tbr i1 " + retRegister + ", label %" + ifLabel + ", label %" + elseLabel + "\n");

        this.builder.append(ifLabel + ":\n");
        ifStatement.thencase().accept(this);
        this.builder.append("\n\tbr label %" + exitLabel + "\n");

        this.builder.append(elseLabel + ":\n");
        ifStatement.elsecase().accept(this);
        this.builder.append("\n\tbr label %" + exitLabel + "\n");

        this.builder.append(exitLabel + ":\n");
        return null;
    }

    @Override
    public String visit(WhileStatement whileStatement) {
        String condLabel = getLoopLabel();
        String loopLabel = getLoopLabel();
        String exitLabel = getLoopLabel();

        this.builder.append("\n\tbr label %" + condLabel + "\n");
        this.builder.append(condLabel + ":\n");
        String condResRegister = whileStatement.cond().accept(this);

        this.builder.append("\tbr i1 " + condResRegister + ", label %" + loopLabel + ", label %" + exitLabel + "\n");
        this.builder.append(loopLabel + ":\n");
        whileStatement.body().accept(this);
        this.builder.append("\n\tbr label %" + condLabel + "\n");

        this.builder.append(exitLabel + ":\n");
        return null;
    }

    @Override
    public String visit(SysoutStatement sysoutStatement) {
        String retRegister = sysoutStatement.arg().accept(this);
        this.builder.append("\tcall void (i32) @print_int(i32 " + retRegister + ")\n");
        return null;
    }

    @Override
    public String visit(AssignStatement assignStatement) {
        Variable var;

        if (this.currentMethod != null) {
            // Method scope - assigning a local var or a param
            var = symbolTable.getVar(this.currentMethod, assignStatement.lv());
        }
        else {
            // Class scope - assigning a field
            var = symbolTable.getVar(this.currentClass, assignStatement.lv());

        }

        if (var == null) {
            throw new RuntimeException(String.format("Variable %s was not found!", assignStatement.lv()));
        }

        String rvReg = assignStatement.rv().accept(this);
        LLVMType type = JavaTypeToLLVMType.getLLVMType(var.getType());

        if (var.isParam() || var.isLocalVariable()) {
            this.builder.append(
                    "\tstore " + type + " " + rvReg + ", " + type + "* %" + assignStatement.lv() + "\n"
            );
        }

        else {
            // Var is a field
            String reg1 = getRegister();
            String reg2 = getRegister();
            VTables.ClassVTable classVTable = this.vTables.classesTables.get(this.currentClass.getName());
            this.builder.append("\t" + reg1 + " = getelementptr i8, i8* %this, i32 " + classVTable.getVarOffset(var.getSymbol()) + "\n");
            this.builder.append("\t" + reg2 + " = bitcast i8* " + reg1 + " to " + type + "*" + "\n");
            this.builder.append(
                    "\tstore " + type + " " + rvReg + ", " + type + "* %" + reg2 + "\n"
            );
        }

        return null;
    }

    @Override
    public String visit(AssignArrayStatement assignArrayStatement) {
        // TODO
        assignArrayStatement.index().accept(this);
        assignArrayStatement.rv().accept(this);
        return null;
    }

    @Override
    public String visit(AndExpr e) {
        String label1 = getAndLabel();
        String label2 = getAndLabel();
        String label3 = getAndLabel();
        String label4 = getAndLabel();

        String cond1Reg = e.e1().accept(this);
        this.builder.append("\tbr label %" + label1 + "\n");
        this.builder.append(label1 + ":\n");
        this.builder.append("\tbr i1 " + cond1Reg + ", label %" + label2 + ", label %" + label4 + "\n");

        this.builder.append(label2 + ":\n");
        String cond2Reg = e.e2().accept(this);
        this.builder.append("\tbr label %" + label3 + "\n");

        this.builder.append(label3 + ":\n");
        this.builder.append("\tbr label %" + label4 + "\n");

        String phi = getRegister();
        this.builder.append(label4 + ":\n");
        this.builder.append("\t" + phi + " = phi i1 [ 0, %" + label1 + " ], [ " + cond2Reg + ", %" + label3 + " ]\n");

        return phi;
    }

    @Override
    public String visit(LtExpr e) {
        String reg1 = e.e1().accept(this);
        String reg2 =  e.e2().accept(this);

        String resReg = getRegister();
        this.builder.append("\t" + resReg + " = icmp slt i32 " + reg1 + ", " + reg2 + "\n");
        return resReg;
    }

    @Override
    public String visit(AddExpr e) {
        String reg1 = e.e1().accept(this);
        String reg2 =  e.e2().accept(this);

        String resReg = getRegister();
        this.builder.append("\t" + resReg + " = add i32 " + reg1 + ", " + reg2 + "\n");
        return resReg;
    }

    @Override
    public String visit(SubtractExpr e) {
        String reg1 = e.e1().accept(this);
        String reg2 =  e.e2().accept(this);

        String resReg = getRegister();
        this.builder.append("\t" + resReg + " = sub i32 " + reg1 + ", " + reg2 + "\n");
        return resReg;
    }

    @Override
    public String visit(MultExpr e) {
        String reg1 = e.e1().accept(this);
        String reg2 =  e.e2().accept(this);

        String resReg = getRegister();
        this.builder.append("\t" + resReg + " = mul i32 " + reg1 + ", " + reg2 + "\n");
        return resReg;
    }

    @Override
    public String visit(ArrayAccessExpr e) {
        // TODO
        e.indexExpr().accept(this);
        e.arrayExpr().accept(this);
        return null;
    }

    @Override
    public String visit(ArrayLengthExpr e) {
        // TODO
        e.arrayExpr().accept(this);
        return null;
    }

    @Override
    public String visit(MethodCallExpr e) {
        // TODO
        return null;
    }

    @Override
    public String visit(IntegerLiteralExpr e) {
        return String.valueOf(e.num());
    }

    @Override
    public String visit(TrueExpr e) {
        return "1";
    }

    @Override
    public String visit(FalseExpr e) {
        return "0";
    }

    @Override
    public String visit(IdentifierExpr e) {
        // TODO ?? No idea if this is really correct (even though seems to work on many examples)
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

        LLVMType type = JavaTypeToLLVMType.getLLVMType(variable.getType());

        if (variable.isParam() || variable.isLocalVariable()) {
            String reg = getRegister();
            this.builder.append("\t" + reg + " = load " + type + ", " + type + "* %" + e.id() + "\n");
            return reg;
        }

        else {
            // Var is a field
            String reg1 = getRegister();
            String reg2 = getRegister();
            String reg3 = getRegister();
            VTables.ClassVTable classVTable = this.vTables.classesTables.get(this.currentClass.getName());
            this.builder.append("\t" + reg1 + " = getelementptr i8, i8* %this, i32 " + classVTable.getVarOffset(variable.getSymbol()) + "\n");
            this.builder.append("\t" + reg2 + " = bitcast i8* " + reg1 + " to " + type + "*" + "\n");
            this.builder.append("\t" + reg3 + " = load " + type + ", " + type + "* " + reg2 + "\n");
            return reg3;
        }

    }

    @Override
    public String visit(ThisExpr e) {
        return "%this";
    }

    @Override
    public String visit(NewIntArrayExpr e) {
        return null;
    }

    @Override
    public String visit(NewObjectExpr e) {
        String reg1 = getRegister();
        String reg2 = getRegister();
        String reg3 = getRegister();

        VTables.ClassVTable classVTable = this.vTables.classesTables.get(e.classId());

        this.builder.append("\t" + reg1 + " = call i8* @calloc(i32 1, i32 " + classVTable.getClassSize() +")\n");
        this.builder.append("\t" + reg2 + " = bitcast i8* " + reg1 + " to i8***\n");
        this.builder.append("\t" + reg3 + " = getelementptr [" + classVTable.methodsTable.size() + " x i8*], [" + classVTable.methodsTable.size() + " x i8*]* @." + e.classId() + "_vtable, i32 0, i32 0\n");
        this.builder.append("\tstore i8** " + reg3 + ", i8*** " + reg2 + "\n");
        return reg1;
    }

    @Override
    public String visit(NotExpr e) {
        String reg1 = e.e().accept(this);
        String xorReg = getRegister();
        this.builder.append("\t" + xorReg + " = xor i1 1, " + reg1 + "\n");
        return xorReg;
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

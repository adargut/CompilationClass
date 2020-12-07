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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
    private int arrayAllocLabel;

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
        this.arrayAllocLabel = 0;
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

    String getArrayAllocLabel() {
        int retVal = this.arrayAllocLabel;
        this.arrayAllocLabel++;
        return "arr_alloc" + retVal;
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

        this.builder.append("\tret i32 0\n}\n");

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

        this.builder.append("\ndefine " + JavaTypeToLLVMType.getLLVMType(methodDecl.returnType()) + " @" + this.currentClass.getName()
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

        // todo: Is it possible that this is not a local variable?
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
                    "\tstore " + type + " " + rvReg + ", " + type + "* " + reg2 + "\n"
            );
        }

        return null;
    }

    //lv[index]=rv
    @Override
    public String visit(AssignArrayStatement assignArrayStatement) {
        // TODO
        Variable var;

        if (this.currentMethod != null) {
            // Method scope - assigning a local var or a param
            var = symbolTable.getVar(this.currentMethod, assignArrayStatement.lv());
        } else {
            // Class scope - assigning a field
            var = symbolTable.getVar(this.currentClass, assignArrayStatement.lv());
        }

        if (var == null) {
            throw new RuntimeException(String.format("Variable %s was not found!", assignArrayStatement.lv()));
        }
        String array_reg = "";

        if (var.isParam() || var.isLocalVariable()) {
            //todo check this is correct
            array_reg = getRegister();
            this.builder.append("\t" + array_reg + " = load i32*, i32** %" + assignArrayStatement.lv() + "\n");
        }

        else {
            // Var is a field
            String array_reg_temp1 = getRegister();
            String array_reg_temp2 = getRegister();
            array_reg = getRegister();
            VTables.ClassVTable classVTable = this.vTables.classesTables.get(this.currentClass.getName());
            this.builder.append("\t" + array_reg_temp1 + " = getelementptr i8, i8* %this, i32 " + classVTable.getVarOffset(var.getSymbol()) + "\n");
            this.builder.append("\t" + array_reg_temp2 + " = bitcast i8* " + array_reg_temp1 + " to i32**" + "\n");
            this.builder.append("\t" + array_reg + " = load i32*, i32** " + array_reg_temp2 + "\n");
        }
        String index_reg = assignArrayStatement.index().accept(this);
        String rv_reg = assignArrayStatement.rv().accept(this);
        String cmp_with_zero_reg = getRegister();
        builder.append("\t" + cmp_with_zero_reg + " = icmp slt i32 " + index_reg + ", 0" + "\n");
        String oob_0_bad_label = getArrayAllocLabel();
        String oob_0_good_label = getArrayAllocLabel();
        builder.append("\tbr i1 " + cmp_with_zero_reg + ", label %" + oob_0_bad_label + ", label %" + oob_0_good_label + "\n");
        builder.append(oob_0_bad_label + ":" + "\n");
        builder.append("\tcall void @throw_oob()\n");
        builder.append("\tbr label %" + oob_0_good_label + "\n");
        builder.append(oob_0_good_label + ":" + "\n");
        String oob_max_bad_label = getArrayAllocLabel();
        String oob_max_good_label = getArrayAllocLabel();
        String arr_length_reg = getRegister();
        builder.append("\t" + arr_length_reg + " = getelementptr i32, i32* " + array_reg + ", i32 0\n");
        String loaded_arr_length_reg = getRegister();
        builder.append("\t" + loaded_arr_length_reg + " = load i32, i32* " + arr_length_reg + "\n");
        String cmp_with_max_reg = getRegister();
        builder.append("\t" + cmp_with_max_reg + " = icmp sle i32 " + loaded_arr_length_reg + ", " + index_reg + "\n");
        builder.append("\tbr i1 " + cmp_with_max_reg + ", label %" + oob_max_bad_label + ", label %" + oob_max_good_label + "\n");
        builder.append(oob_max_bad_label + ":" + "\n");
        builder.append("\tcall void @throw_oob()\n");
        builder.append("\tbr label %" + oob_max_good_label + "\n");
        builder.append(oob_max_good_label + ":" + "\n");
        String physical_index_reg = getRegister();
        builder.append("\t" + physical_index_reg + " = add i32 " + index_reg + ", 1\n");
        String ptr_to_arr_element_reg = getRegister();
        builder.append("\t" + ptr_to_arr_element_reg + " = getelementptr i32, i32* " + array_reg + ", i32 " + physical_index_reg + "\n");
        builder.append("\tstore i32 " + rv_reg + ", i32* " + ptr_to_arr_element_reg + "\n");
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

    //(A.a())[B.b()]
    //(arrayExpr)[indexExpr]
    @Override
    public String visit(ArrayAccessExpr e) {
        // TODO maybe need to bitcast
        // TODO maybe the order needs to be switched?
        String arr_ptr_reg = e.arrayExpr().accept(this);
        String reg1 = e.indexExpr().accept(this);
        String cmp_with_zero_reg = getRegister();
        builder.append("\t" + cmp_with_zero_reg + " = icmp slt i32 " + reg1 + ", 0" + "\n");
        String oob_0_bad_label = getArrayAllocLabel();
        String oob_0_good_label = getArrayAllocLabel();
        builder.append("\tbr i1 " + cmp_with_zero_reg + ", label %" + oob_0_bad_label + ", label %" + oob_0_good_label + "\n");
        builder.append(oob_0_bad_label + ":" + "\n");
        builder.append("\tcall void @throw_oob()\n");
        builder.append("\tbr label %" + oob_0_good_label + "\n");
        builder.append(oob_0_good_label + ":" + "\n");
        String oob_max_bad_label = getArrayAllocLabel();
        String oob_max_good_label = getArrayAllocLabel();
        String arr_length_reg = getRegister();
        builder.append("\t" + arr_length_reg + " = getelementptr i32, i32* " + arr_ptr_reg + ", i32 0\n");
        String loaded_arr_length_reg = getRegister();
        builder.append("\t" + loaded_arr_length_reg + " = load i32, i32* " + arr_length_reg + "\n");
        String cmp_with_max_reg = getRegister();
        builder.append("\t" + cmp_with_max_reg + " = icmp sle i32 " + loaded_arr_length_reg + ", " + reg1 + "\n");
        builder.append("\tbr i1 " + cmp_with_max_reg + ", label %" + oob_max_bad_label + ", label %" + oob_max_good_label + "\n");
        builder.append(oob_max_bad_label + ":" + "\n");
        builder.append("\tcall void @throw_oob()\n");
        builder.append("\tbr label %" + oob_max_good_label + "\n");
        builder.append(oob_max_good_label + ":" + "\n");
        String physical_index_reg = getRegister();
        builder.append("\t" + physical_index_reg + " = add i32 " + reg1 + ", 1\n");
        String ptr_to_arr_element_reg = getRegister();
        String retReg = getRegister();
        builder.append("\t" + ptr_to_arr_element_reg + " = getelementptr i32, i32* " + arr_ptr_reg + ", i32 " + physical_index_reg + "\n");
        builder.append("\t" + retReg + " = load i32, i32* " + ptr_to_arr_element_reg + "\n");
        return retReg;
    }

    //arrayExpr.length
    //todo: the pointer to the array points to the array length, check if cast/load is needed
    @Override
    public String visit(ArrayLengthExpr e) {
        return e.arrayExpr().accept(this);
    }

    //fixme
    // e.(ownerExpression).methodId(args : e.actuals)
    @Override
    public String visit(MethodCallExpr e) {
        // Bitcast owner register
        var ownerRegister = e.ownerExpr().accept(this);
        var castVtable = getRegister();
        builder.append("\t" + castVtable + " = bitcast i8* " + ownerRegister + " to i8*** \n");

        // Get actual register to vtable
        var actualVtableReg = getRegister();
        builder.append("\t" + actualVtableReg + " = load i8**, i8*** " + castVtable + "\n");

        // Read into vtable to get function pointer
        var functionPointerRegister = getRegister();
        var lineNumber = e.lineNumber;
        var methodName = e.methodId();

        // TODO: Move this out as a util method (determining the class of an object)
        String className;

        // Case this.foo()
        if (e.ownerExpr() instanceof ThisExpr) {
            className = this.currentClass.getName();
        }

        // Case x.foo()
        else if (e.ownerExpr() instanceof IdentifierExpr) {
            String symbol = ((IdentifierExpr) e.ownerExpr()).id();

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
                className = ((RefType) varType).id();
            }

            else {
                throw new RuntimeException(String.format("Cannot invoke method on variable %s (not an object)!", symbol));
            }
        }

        // Case New X().foo()
        else {
            className = ((NewObjectExpr) e.ownerExpr()).classId();
        }

        var classVTable = this.vTables.classesTables.get(className);
        var offset = classVTable.getMethodOffset(e.methodId());
        builder.append("\t" + functionPointerRegister + " = getelementptr i8*, i8** " + actualVtableReg + ", i32 " + offset + "\n");

        var functionRegister = getRegister();
        builder.append("\t" + functionRegister + " = load i8*, i8** " + functionPointerRegister + "\n");
        // Cast the function pointer from i8* to correct type
        var castFunctionRegister = getRegister();
        var functionSignature = "";

        // Get the actual method from the class vtable
        var method = classVTable.methodsTable.get(methodName);

        if (method == null) {
            // Shouldn't happen, but lets handle anyway
            throw new RuntimeException(String.format("Method %s is not accessible from class %s!", methodName, className));
        }
        var returnType = JavaTypeToLLVMType.getLLVMType(method.getMethodDecl().returnType());
        functionSignature += "i8* " + functionRegister + " to " + returnType + " ";

        var args = Stream.of("(i8*", method.getParams().values().stream()
                .map(arg -> JavaTypeToLLVMType.getLLVMType(arg.getType()).toString())
                .collect(Collectors.joining(", ")))
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(", "));

        args += ")*";

        functionSignature += args;
        builder.append("\t" + castFunctionRegister + " = bitcast " + functionSignature + "\n");

        // Perform the call on the function register
        List<Variable> paramsArray = method.getParamsArray();

        args = Stream.of("(i8* " + ownerRegister, IntStream
                .range(0, e.actuals().size())
                .mapToObj(i -> JavaTypeToLLVMType.getLLVMType(paramsArray.get(i).getType()).toString() + " " + e.actuals().get(i).accept(this))
                .collect(Collectors.joining(", ")))
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(", "));

        args += ")";

        var callRegister = getRegister();
        builder.append("\t" + callRegister + " = call " + returnType + " " + castFunctionRegister + args + "\n");
        return callRegister;
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

    //new int[lengthExpr]
    @Override
    public String visit(NewIntArrayExpr e) {
        String arr_length_reg = e.lengthExpr().accept(this);
        String cmp_with_zero_reg = getRegister();
        builder.append("\t" + cmp_with_zero_reg + " = icmp slt i32 " + arr_length_reg + ", 0" + "\n");
        String arr_alloc_bad = getArrayAllocLabel();
        String arr_alloc_good = getArrayAllocLabel();
        builder.append("\tbr i1 " + cmp_with_zero_reg + ", label %" + arr_alloc_bad + ", label %" + arr_alloc_good + "\n");
        builder.append(arr_alloc_bad + ":" + "\n");
        builder.append("\tcall void @throw_oob()\n");
        builder.append("\tbr label %" + arr_alloc_good + "\n");
        builder.append(arr_alloc_good + ":" + "\n");
        String arr_physical_length_reg = getRegister();
        builder.append("\t" + arr_physical_length_reg + " = add i32 " + arr_length_reg + ", 1\n");
        String ptr_to_array_reg = getRegister();
        builder.append("\t" + ptr_to_array_reg + " = call i8* @calloc(i32 4, i32 " + arr_physical_length_reg + ")\n");
        String ptr_to_array_reg_after_bitcast = getRegister();
        builder.append("\t" + ptr_to_array_reg_after_bitcast + " = bitcast i8* " + ptr_to_array_reg + " to i32*\n");
        builder.append("\tstore i32 " + arr_length_reg + ", i32* " + ptr_to_array_reg_after_bitcast + "\n");
        return ptr_to_array_reg_after_bitcast;
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
        // TODO: In treeVisitor.java example seems like we need to use sub. Check this.
//        String xorReg = getRegister();
//        this.builder.append("\t" + xorReg + " = xor i1 1, " + reg1 + "\n");
//        return xorReg;
        String reg2 = getRegister();
        this.builder.append("\t" + reg2 + " = sub i1 1, " + reg1 + "\n");
        return reg2;
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

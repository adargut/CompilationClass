package visitor;

import ast.*;
import symboltable.Class;
import symboltable.Method;
import symboltable.SymbolTable;
import symboltable.Variable;
import utils.InitMap;
import java.util.stream.IntStream;

public class ValidateTypeVisitor implements Visitor{
    private Class currentClass;
    private Method currentMethod;
    private final SymbolTable symbolTable;
    private boolean valid;

    public ValidateTypeVisitor(SymbolTable symbolTable) {
        this.currentMethod = null;
        this.symbolTable = symbolTable;
        this.currentClass = null;
    }

    public boolean isValid() {
        return valid;
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
        Method overridden_method = symbolTable.getOverridenMethod(currentMethod);
        if (overridden_method != null) {
            // check same number of args
            if (this.currentMethod.getParamsArray().size() != overridden_method.getParamsArray().size())
            {
                valid = false;
                return null;
            }
            // check same static types (exactly) for each argument
            for (int i = 0; i < currentMethod.getParamsArray().size(); i++){
                var param1 = currentMethod.getParamsArray().get(i);
                var param2 = overridden_method.getParamsArray().get(i);
                if (!(param1.getType().getClass().equals(param2.getType().getClass()))){
                    valid = false;
                    return null;
                }
                if (param1.getType() instanceof RefType){
                    if (!((RefType) param1.getType()).id().equals(((RefType) param2.getType()).id())){
                        valid = false;
                        return null;
                    }
                }
            }
            //check return type is subclass of overridden method return type
            if (!symbolTable.isSubtype(currentMethod.getMethodDecl().returnType(),
                    overridden_method.getMethodDecl().returnType())){
                valid = false;
                return null;
            }
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
        return null;
    }

    @Override
    public String visit(VarDecl varDecl) {
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
        if (!ifStatement.cond().accept(this).equals("boolean")){
            valid = false;
        }
        ifStatement.thencase().accept(this);
        ifStatement.elsecase().accept(this);
        return null;
    }

    @Override
    public String visit(WhileStatement whileStatement) {
        if (!whileStatement.cond().accept(this).equals("boolean")){
            valid = false;
        }
        whileStatement.cond().accept(this);
        whileStatement.body().accept(this);
        return null;
    }

    @Override
    public String visit(SysoutStatement sysoutStatement) {
        if (!sysoutStatement.arg().accept(this).equals("int")){
            valid = false;
        }
        return null;
    }

    @Override
    public String visit(AssignStatement assignStatement) {
        AstType lv_type;
        String rv_string;
        AstType rv_type;
        lv_type = currentMethod.getVar(assignStatement.lv()).getType();
        rv_string = assignStatement.rv().accept(this);

        switch (rv_string) {
            case "int":
                rv_type = new IntAstType();
                break;
            case "bool":
                rv_type = new BoolAstType();
                break;
            case "int[]":
                rv_type = new IntArrayAstType();
                break;
            default:
                rv_type = new RefType();
                ((RefType)rv_type).setId(rv_string);
        }

        valid = symbolTable.isSubtype(rv_type, lv_type);
        return null;
    }

    @Override
    public String visit(AssignArrayStatement assignArrayStatement) {
        AstType lv_type = currentMethod.getVar(assignArrayStatement.lv()).getType();
        if (!(lv_type instanceof IntArrayAstType)){
            valid = false;
            return null;
        }
        String index_type = assignArrayStatement.index().accept(this);
        if (!(index_type.equals("int"))){
            valid = false;
            return null;
        }
        String rv_type =assignArrayStatement.rv().accept(this);
        if (!(rv_type.equals("int"))){
            valid = false;
            return null;
        }
        return null;
    }

    @Override
    public String visit(AndExpr e) {
        String e1_type = e.e1().accept(this);
        if (!(e1_type.equals("bool"))){
            valid = false;
            return null;
        }
        String e2_type =e.e2().accept(this);
        if (!(e2_type.equals("bool"))){
            valid = false;
            return null;
        }
        return "bool";
    }

    @Override
    public String visit(LtExpr e) {
        String e1_type = e.e1().accept(this);
        if (!(e1_type.equals("int"))){
            valid = false;
            return null;
        }
        String e2_type =e.e2().accept(this);
        if (!(e2_type.equals("int"))){
            valid = false;
            return null;
        }
        return "bool";
    }

    @Override
    public String visit(AddExpr e) {
        String e1_type = e.e1().accept(this);
        if (!(e1_type.equals("int"))){
            valid = false;
            return null;
        }
        String e2_type =e.e2().accept(this);
        if (!(e2_type.equals("int"))){
            valid = false;
            return null;
        }
        return "int";
    }

    @Override
    public String visit(SubtractExpr e) {
        String e1_type = e.e1().accept(this);
        if (!(e1_type.equals("int"))){
            valid = false;
            return null;
        }
        String e2_type =e.e2().accept(this);
        if (!(e2_type.equals("int"))){
            valid = false;
            return null;
        }
        return "int";
    }

    @Override
    public String visit(MultExpr e) {
        String e1_type = e.e1().accept(this);
        if (!(e1_type.equals("int"))){
            valid = false;
            return null;
        }
        String e2_type =e.e2().accept(this);
        if (!(e2_type.equals("int"))){
            valid = false;
            return null;
        }
        return "int";
    }

    @Override
    public String visit(ArrayAccessExpr e) {
        String array_type =e.arrayExpr().accept(this);
        if (!(array_type.equals("int[]"))){
            valid = false;
            return null;
        }
        String index_type = e.indexExpr().accept(this);
        if (!(index_type.equals("int"))){
            valid = false;
            return null;
        }
        return "int";
    }

    @Override
    public String visit(ArrayLengthExpr e) {
        String array_type =e.arrayExpr().accept(this);
        if (!(array_type.equals("int[]"))){
            valid = false;
            return null;
        }
        return "int";
    }

    @Override
    public String visit(MethodCallExpr e) {
        // 12
        if (!(e.ownerExpr() instanceof NewObjectExpr) &&
        !(e.ownerExpr() instanceof IdentifierExpr) &&
        !(e.ownerExpr() instanceof ThisExpr)){
            valid = false;
            return null;
        }

        //11
        String owner_type = e.ownerExpr().accept(this);
        Class owner_class = symbolTable.getClass(owner_type);
        if(owner_class == null){
            valid = false;
            return null;
        }
        Method method = owner_class.findMethodUpwards(e.methodId());
        if(method == null){
            valid = false;
            return null;
        }

        var actuals = e.actuals();
        var methodArgs = method.getMethodDecl().formals();

        // Actual args for method call have incorrect length
        if (actuals.size() != methodArgs.size()) {
            valid = false;
            return null;
        }

        // TODO check actuals for non-primitive types? this is only for primitives now.. also if non-primitive, can it be a subtype?
        // Check if actuals given to method call match the type in method signature
        boolean invalidActuals =
                IntStream.range(0, actuals.size())
                    .mapToObj(i -> new String[]{actuals.get(i).accept(this), methodArgs.get(i).stringType()})
                    .anyMatch(ele -> !ele[0].equals(ele[1]));

        if (invalidActuals) valid = false;
        return null;
    }

    @Override
    public String visit(IntegerLiteralExpr e) {
        return "int";
    }

    @Override
    public String visit(TrueExpr e) {
        return "bool";
    }

    @Override
    public String visit(FalseExpr e) {
        return "bool";
    }

    @Override
    public String visit(IdentifierExpr e) {
        var varName = e.id();
        AstType varType;
        Variable var = currentClass.getVar(varName);

        // Variable is a class field
        if (var != null) {
            varType = var.getType();
        }
        // Variable is local (method scope)
        else {
            var = currentMethod.getVar(varName);
            varType = var.getType();
        }

        if (varType instanceof IntAstType) return "int";
        if (varType instanceof IntArrayAstType) return "int[]";
        if (varType instanceof BoolAstType) return "bool";

        // Variable doesn't conform to any of the types MiniJava handles, probably should never happen
        var err = "Type for variable " + varName + " could not be inferred or is illegal in MiniJava!";
        throw new RuntimeException(err);
    }

    public String visit(ThisExpr e) {
        return currentClass.getName();
    }

    @Override
    public String visit(NewIntArrayExpr e) {
        if (!e.lengthExpr().accept(this).equals("int")) {
            this.valid = false;
            return null;
        }
        return "int[]";
    }

    @Override
    public String visit(NewObjectExpr e) {
        return e.classId();
    }

    @Override
    public String visit(NotExpr e) {
        if (!e.e().accept(this).equals("bool")){
            this.valid = false;
            return null;
        }
        return "bool";
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

package visitor;

import ast.*;
import semanticanalysis.SemanticError;
import semanticanalysis.SemanticException;
import symboltable.Class;
import symboltable.Method;
import symboltable.SymbolTable;
import symboltable.Variable;
import utils.VarType;

import java.util.stream.IntStream;

public class ValidateTypeVisitor implements Visitor{
    private Class currentClass;
    private Method currentMethod;
    private final SymbolTable symbolTable;

    public ValidateTypeVisitor(SymbolTable symbolTable) {
        this.currentMethod = null;
        this.symbolTable = symbolTable;
        this.currentClass = null;
    }

    private AstType getVarType(String type) {
        if (type.equals(VarType.INT.getType()))
            return new IntAstType();
        else if (type.equals(VarType.BOOL.getType()))
            return new BoolAstType();
        else if (type.equals(VarType.INT_ARRAY.getType()))
            return new IntArrayAstType();
        else {
            RefType refType = new RefType();
            refType.setId(type);
            return refType;
        }
    }

    private void validateMethodOverriding(Method currentMethod, Method overridenMethod) {
        // Validate same number of parameters (formals)
        if (this.currentMethod.getParamsArray().size() != overridenMethod.getParamsArray().size())
        {
            throw new SemanticException(
                    String.format("Invalid overriding (class %s, method %s): unequal number of parameters.",
                            currentMethod.getParentClass().getName(),
                            currentMethod.getName()
                    ),
                    SemanticError.INVALID_OVERRIDING
            );
        }

        // Validate same static types (exactly) for each argument
        for (int i = 0;  i < currentMethod.getParamsArray().size(); i++) {
            var param1 = currentMethod.getParamsArray().get(i);
            var param2 = overridenMethod.getParamsArray().get(i);

            // Validate that params are instances of the same AstType class
            if (!(param1.getType().getClass().equals(param2.getType().getClass()))){
                throw new SemanticException(
                        String.format("Invalid overriding (class %s, method %s): parameter type mismatch. Got %s (%s), expected %s (%s).",
                                currentMethod.getParentClass().getName(),
                                currentMethod.getName(),
                                param1.getSymbol(),
                                param1.getType().getClass().getName(),
                                param2.getSymbol(),
                                param2.getType().getClass().getName()
                        ),
                        SemanticError.INVALID_OVERRIDING
                );
            }

            if (param1.getType() instanceof RefType){
                // Validate that the actual class of the refType of both params are equal
                if (!((RefType) param1.getType()).id().equals(((RefType) param2.getType()).id())){
                    throw new SemanticException(
                            String.format("Invalid overriding (class %s, method %s): parameter type mismatch. Got %s (%s), expected %s (%s).",
                                    currentMethod.getParentClass().getName(),
                                    currentMethod.getName(),
                                    param1.getSymbol(),
                                    ((RefType) param1.getType()).id(),
                                    param2.getSymbol(),
                                    ((RefType) param2.getType()).id()
                            ),
                            SemanticError.INVALID_OVERRIDING
                    );
                }
            }
        }

        // Validate return type is subclass of overridden method return type
        if (!symbolTable.isSubtype(currentMethod.getMethodDecl().returnType(),
                overridenMethod.getMethodDecl().returnType())){
            throw new SemanticException(
                    String.format("Invalid overriding (class %s, method %s): return type mismatch.",
                            currentMethod.getParentClass().getName(),
                            currentMethod.getName()
                    ),
                    SemanticError.INVALID_OVERRIDING
            );
        }

    }

    private boolean validateTypeExists(String type) {
        AstType varType = getVarType(type);

        if (varType instanceof RefType) {
            // Validate that the class of the variable exists in hierarchy
            return this.symbolTable.getClass(((RefType) varType).id()) != null;
        }

        return true;
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
        this.currentMethod = symbolTable.getMethod(methodDecl.name(), methodDecl.lineNumber);
        Method overriddenMethod = symbolTable.getOverridenMethod(currentMethod);

        if (overriddenMethod != null) {
            // Validate overriding
            validateMethodOverriding(currentMethod, overriddenMethod);
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

        // Validate return type is subclass of the declared return type
        AstType actualReturnType = getVarType(methodDecl.ret().accept(this));
        AstType expectedReturnType = currentMethod.getMethodDecl().returnType();

        if (!symbolTable.isSubtype(actualReturnType, expectedReturnType)) {
            // Return type is not matching declared type - SEMANTIC ERROR #18
            throw new SemanticException(
                    String.format(
                            "Return type mismatch (class: %s, method: %s). Got: %s, expected: %s.",
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            (actualReturnType instanceof RefType) ? ((RefType) actualReturnType).id() : actualReturnType.getClass().getName(),
                            (expectedReturnType instanceof RefType) ? ((RefType) expectedReturnType).id() : expectedReturnType.getClass().getName()
                    ),
                    SemanticError.INVALID_TYPE

            );
        }

        // Backtrack - exit method
        this.currentMethod = null;
        return null;
    }

    @Override
    public String visit(FormalArg formalArg) {
        String formalType = formalArg.type().accept(this);

        if (!validateTypeExists(formalType)) {
            // Class of formal is not declared - SEMANTIC ERROR #8
            throw new SemanticException(
                    String.format("Invalid declaration of formal %s (class: %s, method: %s). Class %s doesn't exist.",
                            formalArg.name(),
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            formalType),
                    SemanticError.OBJ_DOESNT_EXIST
            );
        }

        return null;
    }

    @Override
    public String visit(VarDecl varDecl) {
        String varType = varDecl.type().accept(this);

        if (varType.equals(VarType.REF.getType())) {
            // If var is reference type, its real type is the class
            varType = ((RefType) varDecl.type()).id();
        }

        if (!validateTypeExists(varType)) {
            // Class of var is not declared - SEMANTIC ERROR #8
            throw new SemanticException(
                    String.format("Invalid declaration of var %s (class: %s, method: %s). Class %s doesn't exist.",
                            varDecl.name(),
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            varType),
                    SemanticError.OBJ_DOESNT_EXIST
            );
        }

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
        if (!ifStatement.cond().accept(this).equals(VarType.BOOL.getType())){
            // If condition is not a boolean - SEMANTIC ERROR #17
            throw new SemanticException(
                    SemanticError.IF_COND_NOT_BOOL
            );
        }
        ifStatement.thencase().accept(this);
        ifStatement.elsecase().accept(this);
        return null;
    }

    @Override
    public String visit(WhileStatement whileStatement) {
        if (!whileStatement.cond().accept(this).equals(VarType.BOOL.getType())){
            // While condition is not a boolean - SEMANTIC ERROR #17
            throw new SemanticException(
                    SemanticError.WHILE_COND_NOT_BOOL
            );
        }
        whileStatement.cond().accept(this);
        whileStatement.body().accept(this);
        return null;
    }

    @Override
    public String visit(SysoutStatement sysoutStatement) {
        String argType = sysoutStatement.arg().accept(this);

        if (!argType.equals(VarType.INT.getType())){
            // Argument to print function is not an int - SEMANTIC ERROR #20
            throw new SemanticException(
                    String.format("Type mismatch in print argument (class: %s, method: %s). Type of arg: %s, expected: int.",
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            argType),
                    SemanticError.INVALID_TYPE
            );
        }
        return null;
    }

    @Override
    public String visit(AssignStatement assignStatement) {
        AstType lv_type;
        AstType rv_type;
        Variable lv = symbolTable.getVar(currentMethod, assignStatement.lv());

        if (lv == null) {
            // Variable is not found in current scope - SEMANTIC ERROR #12
            throw new SemanticException(
                    String.format("Var %s doesn't exist in current scope (class: %s, method: %s).",
                            assignStatement.lv(),
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : ""
                    ),
                    SemanticError.OBJ_DOESNT_EXIST
            );
        }

        lv_type = lv.getType();
        rv_type = getVarType(assignStatement.rv().accept(this));

        if (!symbolTable.isSubtype(rv_type, lv_type)) {
            // Type mismatch in assignment - SEMANTIC ERROR #16
            throw new SemanticException(
                    String.format("Assignment of var %s is invalid (class: %s, method %s). LV type: %s, RV type: %s.",
                            lv.getSymbol(),
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            (lv_type instanceof RefType) ? ((RefType) lv_type).id() : lv_type.getClass().getName(),
                            (rv_type instanceof RefType) ? ((RefType) rv_type).id() : rv_type.getClass().getName()
                    ),
                    SemanticError.INVALID_ASSIGNMENT
            );
        }
        return null;
    }

    @Override
    public String visit(AssignArrayStatement assignArrayStatement) {
        AstType lv_type;
        AstType rv_type;

        Variable lv = symbolTable.getVar(currentMethod, assignArrayStatement.lv());

        if (lv == null) {
            // Variable is not found in current scope - SEMANTIC ERROR #12
            throw new SemanticException(
                    String.format("Var %s doesn't exist in current scope (class: %s, method: %s).",
                            assignArrayStatement.lv(),
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : ""
                    ),
                    SemanticError.OBJ_DOESNT_EXIST
            );
        }

        lv_type = lv.getType();
        rv_type = getVarType(assignArrayStatement.rv().accept(this));

        // Validate LV is an int[]
        if (!(lv_type instanceof IntArrayAstType)){
            // LV is not an int[] - SEMANTIC ERROR #23
            throw new SemanticException(
                    String.format("Invalid array assignment (class: %s, method: %s). Type of %s: %s, expected: IntArrayAstType.",
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            lv.getSymbol(),
                            (lv_type instanceof RefType) ? ((RefType) lv_type).id() : lv_type.getClass().getName()
                    ),
                    SemanticError.INVALID_ASSIGNMENT
            );
        }

        String index_type = assignArrayStatement.index().accept(this);

        if (!(index_type.equals(VarType.INT.getType()))) {
            // Index is not an int - SEMANTIC ERROR #23
            throw new SemanticException(
                    String.format("Invalid index type (class: %s, method: %s).Got: %s, expected: int.",
                    this.currentClass != null ? this.currentClass.getName() : "",
                    this.currentMethod != null ? this.currentMethod.getName() : "",
                    index_type),
                    SemanticError.INVALID_ARRAY_INDEX
            );
        }

        if (!(rv_type instanceof IntAstType)) {
            // Index is not an int - SEMANTIC ERROR #23
            throw new SemanticException(
                    String.format("Invalid array assignment (class: %s, method: %s). Type of RV: %s, expected: int.",
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            (rv_type instanceof RefType) ? ((RefType) rv_type).id() : lv_type.getClass().getName()),
                    SemanticError.INVALID_ASSIGNMENT
            );
        }

        return null;
    }

    @Override
    public String visit(AndExpr e) {
        String e1_type = e.e1().accept(this);

        // Validate first argument of the operation
        if (!(e1_type.equals(VarType.BOOL.getType()))) {
            // e1 is not a boolean - SEMANTIC ERROR #21
            throw new SemanticException(
                    String.format("Type mismatch in AND operator (class: %s, method: %s). Type of e1: %s, expected: boolean.",
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            e1_type),
                    SemanticError.INVALID_TYPE
            );
        }

        String e2_type =e.e2().accept(this);

        // Validate second argument type
        if (!(e2_type.equals(VarType.BOOL.getType()))){
            // e1 is not a boolean - SEMANTIC ERROR #21
            throw new SemanticException(
                    String.format("Type mismatch in AND operator (class: %s, method: %s). Type of e2: %s, expected: boolean.",
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            e2_type),
                    SemanticError.INVALID_TYPE
            );
        }

        // && op returns a boolean
        return VarType.BOOL.getType();
    }

    @Override
    public String visit(LtExpr e) {
        String e1_type = e.e1().accept(this);

        // Validate first argument of the operation
        if (!(e1_type.equals(VarType.INT.getType()))) {
            // e1 is not a int - SEMANTIC ERROR #21
            throw new SemanticException(
                    String.format("Type mismatch in < operator (class: %s, method: %s). Type of e1: %s, expected: int.",
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            e1_type),
                    SemanticError.INVALID_TYPE
            );
        }

        String e2_type =e.e2().accept(this);

        if (!(e2_type.equals(VarType.INT.getType()))){
            // e1 is not a int - SEMANTIC ERROR #21
            throw new SemanticException(
                    String.format("Type mismatch in < operator (class: %s, method: %s). Type of e2: %s, expected: int.",
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            e2_type),
                    SemanticError.INVALID_TYPE
            );
        }

        // < op returns a boolean
        return VarType.BOOL.getType();
    }

    @Override
    public String visit(AddExpr e) {
        String e1_type = e.e1().accept(this);

        // Validate first argument of the operation
        if (!(e1_type.equals(VarType.INT.getType()))) {
            // e1 is not a int - SEMANTIC ERROR #21
            throw new SemanticException(
                    String.format("Type mismatch in + operator (class: %s, method: %s). Type of e1: %s, expected: int.",
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            e1_type),
                    SemanticError.INVALID_TYPE
            );
        }

        String e2_type =e.e2().accept(this);

        // Validate second argument type
        if (!(e2_type.equals(VarType.INT.getType()))){
            // e1 is not a int - SEMANTIC ERROR #21
            throw new SemanticException(
                    String.format("Type mismatch in + operator (class: %s, method: %s). Type of e2: %s, expected: int.",
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            e2_type),
                    SemanticError.INVALID_TYPE
            );
        }

        // + op returns int
        return VarType.INT.getType();
    }

    @Override
    public String visit(SubtractExpr e) {
        String e1_type = e.e1().accept(this);

        // Validate first argument of the operation
        if (!(e1_type.equals(VarType.INT.getType()))) {
            // e1 is not a int - SEMANTIC ERROR #21
            throw new SemanticException(
                    String.format("Type mismatch in - operator (class: %s, method: %s). Type of e1: %s, expected: int.",
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            e1_type),
                    SemanticError.INVALID_TYPE
            );
        }

        String e2_type =e.e2().accept(this);

        // Validate second argument type
        if (!(e2_type.equals(VarType.INT.getType()))){
            // e1 is not a int - SEMANTIC ERROR #21
            throw new SemanticException(
                    String.format("Type mismatch in - operator (class: %s, method: %s). Type of e2: %s, expected: int.",
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            e2_type),
                    SemanticError.INVALID_TYPE
            );
        }

        // - op returns int
        return VarType.INT.getType();

    }

    @Override
    public String visit(MultExpr e) {
        String e1_type = e.e1().accept(this);

        // Validate first argument of the operation
        if (!(e1_type.equals(VarType.INT.getType()))) {
            // e1 is not a int - SEMANTIC ERROR #21
            throw new SemanticException(
                    String.format("Type mismatch in * operator (class: %s, method: %s). Type of e1: %s, expected: int.",
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            e1_type),
                    SemanticError.INVALID_TYPE
            );
        }

        String e2_type =e.e2().accept(this);

        // Validate second argument type
        if (!(e2_type.equals(VarType.INT.getType()))){
            // e1 is not a int - SEMANTIC ERROR #21
            throw new SemanticException(
                    String.format("Type mismatch in * operator (class: %s, method: %s). Type of e2: %s, expected: int.",
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            e2_type),
                    SemanticError.INVALID_TYPE
            );
        }

        // * op returns int
        return VarType.INT.getType();
    }

    @Override
    public String visit(ArrayAccessExpr e) {
        String array_type = e.arrayExpr().accept(this);

        if (!(array_type.equals(VarType.INT_ARRAY.getType()))){
            // object is not an array - SEMANTIC ERROR #22
            throw new SemanticException(
                    String.format("Type mismatch (class: %s, method: %s). Got: %s, expected: int[].",
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            array_type
                            ),
                    SemanticError.INVALID_TYPE
            );
        }

        String index_type = e.indexExpr().accept(this);

        if (!(index_type.equals("int"))) {
            // index is not an int - SEMANTIC ERROR #22
            throw new SemanticException(
                    SemanticError.INVALID_ARRAY_INDEX,
                    new String[]{index_type}
                        );
        }

        return VarType.INT.getType();
    }

    @Override
    public String visit(ArrayLengthExpr e) {
        String array_type = e.arrayExpr().accept(this);

        if (!(array_type.equals(VarType.INT_ARRAY.getType()))){
            // object is not an array - SEMANTIC ERROR #13
            throw new SemanticException(
                    String.format("Type mismatch, cannot run method .length on non arrays (class: %s, method: %s). Got: %s, expected: int[].",
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            array_type
                    ),
                    SemanticError.INVALID_TYPE
            );
        }

        return VarType.INT.getType();
    }

    @Override
    public String visit(MethodCallExpr e) {
        // 10
        if (!(e.ownerExpr() instanceof NewObjectExpr) &&
        !(e.ownerExpr() instanceof IdentifierExpr) &&
        !(e.ownerExpr() instanceof ThisExpr)){
            // owner is not this / new / identifier - SEMANTIC ERROR #10
            throw new SemanticException(
                    String.format("Type mismatch (class: %s, method: %s). Method call owner type: %s, expected: this / new / reference.",
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            e.ownerExpr().getClass().getName()
                    ),
                    SemanticError.INVALID_TYPE
            );
        }

        if (e.ownerExpr() instanceof IdentifierExpr) {
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
                // Variable is not found in current scope - SEMANTIC ERROR #12
                throw new SemanticException(
                        String.format("Var %s doesn't exist in current scope (class: %s, method: %s).",
                                symbol,
                                this.currentClass != null ? this.currentClass.getName() : "",
                                this.currentMethod != null ? this.currentMethod.getName() : ""
                        ),
                        SemanticError.OBJ_DOESNT_EXIST
                );
            }
        }

        //11
        String owner_type = e.ownerExpr().accept(this);
        Class owner_class = symbolTable.getClass(owner_type);

        if(owner_class == null){
            // owner type is not of a declared class - SEMANTIC ERROR #11
            throw new SemanticException(
                    String.format("Invalid call to method %s (class: %s, method: %s). Call owner is not a reference type of a known class: %s.",
                            e.methodId(),
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            owner_type
                    ),
                    SemanticError.INVALID_METHOD_CALL
            );
        }

        Method method = owner_class.getMethod(e.methodId(), true);

        if(method == null){
            // Invalid call to method - SEMANTIC ERROR #11
            throw new SemanticException(
                    String.format("Invalid call to method %s (class: %s, method: %s). Method is not declared for type: %s.",
                            e.methodId(),
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            owner_type
                    ),
                    SemanticError.INVALID_METHOD_CALL
            );
        }

        var actuals = e.actuals();
        var methodArgs = method.getMethodDecl().formals();

        // Actual args for method call have incorrect length
        if (actuals.size() != methodArgs.size()) {
            // Invalid call to method - SEMANTIC ERROR #11
            throw new SemanticException(
                    String.format("Invalid call to method %s (class %s, method %s): unequal number of parameters.",
                            e.methodId(),
                            currentMethod.getParentClass().getName(),
                            currentMethod.getName()
                    ),
                    SemanticError.INVALID_METHOD_CALL
            );
        }

        // Validate same static types (exactly) for each argument
        for (int i = 0;  i < e.actuals().size(); i++) {
            var actualType = getVarType(actuals.get(i).accept(this));
            var paramType = method.getParamsArray().get(i).getType();

            // Validate that params are instances of the same AstType class
            if (!(actualType.getClass().equals(paramType.getClass()))){
                // Invalid call to method - SEMANTIC ERROR #11
                throw new SemanticException(
                        String.format("Invalid call to method %s (class %s, method %s): parameter type mismatch at argument %d. Got %s, expected %s.",
                                e.methodId(),
                                currentMethod.getParentClass().getName(),
                                currentMethod.getName(),
                                i,
                                actualType.getClass().getName(),
                                paramType.getClass().getName()
                        ),
                        SemanticError.INVALID_METHOD_CALL
                );
            }

            if (actualType instanceof RefType){
                // Validate that the actual class of the refType of both params are equal or sybtype
                if (!symbolTable.isSubtype(actualType, paramType)) {
                    // Invalid call to method - SEMANTIC ERROR #11
                    throw new SemanticException(
                            String.format("Invalid call to method %s (class %s, method %s): parameter type mismatch at argument %d. Got %s, expected %s.",
                                    e.methodId(),
                                    currentMethod.getParentClass().getName(),
                                    currentMethod.getName(),
                                    i,
                                    ((RefType) actualType).id(),
                                    ((RefType) paramType).id()
                            ),
                            SemanticError.INVALID_METHOD_CALL
                    );
                }
            }
        }

        var returnType = method.getMethodDecl().returnType();
        if (returnType instanceof IntAstType) return VarType.INT.getType();
        else if (returnType instanceof IntArrayAstType) return VarType.INT_ARRAY.getType();
        else if (returnType instanceof BoolAstType) return VarType.BOOL.getType();
        else if (returnType instanceof RefType) {
            var id = ((RefType) returnType).id();

            if (id == null) {
                // Doesn't suppose to happen
                var err = "Type for return is null and is illegal in MiniJava!";
                throw new SemanticException(err, SemanticError.UNKNOWN_ERROR);
            }

            else return id;
        }

        // Variable doesn't conform to any of the types MiniJava handles, probably should never happen
        var err = "Type for return " + returnType + " could not be inferred or is illegal in MiniJava!";
        throw new SemanticException(err, SemanticError.UNKNOWN_ERROR);
    }

    @Override
    public String visit(IntegerLiteralExpr e) {
        return VarType.INT.getType();
    }

    @Override
    public String visit(TrueExpr e) {
        return VarType.BOOL.getType();
    }

    @Override
    public String visit(FalseExpr e) {
        return VarType.BOOL.getType();
    }

    @Override
    public String visit(IdentifierExpr e) {
        var varName = e.id();
        AstType varType;
        Variable var = currentClass.getVar(varName);

        var symbol = e.id();
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
            // Variable is not found in current scope - SEMANTIC ERROR #12
            throw new SemanticException(
                    String.format("Var %s doesn't exist in current scope (class: %s, method: %s).",
                            symbol,
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : ""
                    ),
                    SemanticError.OBJ_DOESNT_EXIST
            );
        }

        varType = variable.getType();

        if (varType instanceof IntAstType) return VarType.INT.getType();
        else if (varType instanceof IntArrayAstType) return VarType.INT_ARRAY.getType();
        else if (varType instanceof BoolAstType) return VarType.BOOL.getType();
        else if (varType instanceof RefType) return ((RefType) varType).id();

        // Variable doesn't conform to any of the types MiniJava handles, probably should never happen
        var err = "Type for variable " + varName + " could not be inferred or is illegal in MiniJava!";
        throw new SemanticException(err, SemanticError.UNKNOWN_ERROR);
    }

    public String visit(ThisExpr e) {
        return this.currentClass.getName();
    }

    @Override
    public String visit(NewIntArrayExpr e) {
        String lengthType = e.lengthExpr().accept(this);
        if (!lengthType.equals(VarType.INT.getType())) {
            // length is not an int - SEMANTIC ERROR #25
            throw new SemanticException(
                    String.format("Type mismatch in array initialization (class: %s, method: %s). Got: %s, expected: int.",
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            lengthType
                        ),
                    SemanticError.INVALID_TYPE
            );
        }

        return VarType.INT_ARRAY.getType();
    }

    @Override
    public String visit(NewObjectExpr e) {

        if(symbolTable.getClass(e.classId()) == null){
            // Creating new instance of not declared type - SEMANTIC ERROR #9
            throw new SemanticException(
                    String.format("Invalid initialization of instance (class: %s, method: %s). Class %s was not declared.",
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            e.classId()
                    ),
                    SemanticError.OBJ_DOESNT_EXIST
            );
        }

        return e.classId();
    }

    @Override
    public String visit(NotExpr e) {
        String exprType = e.e().accept(this);

        if (!exprType.equals(VarType.BOOL.getType())){
            // expression is not a boolean - SEMANTIC ERROR #21
            throw new SemanticException(
                    String.format("Type mismatch in ! operation (class: %s, method: %s). Got: %s, expected: int.",
                            this.currentClass != null ? this.currentClass.getName() : "",
                            this.currentMethod != null ? this.currentMethod.getName() : "",
                            exprType
                    ),
                    SemanticError.INVALID_TYPE
            );
        }
        return VarType.BOOL.getType();
    }

    @Override
    public String visit(IntAstType t) {
        return VarType.INT.getType();
    }

    @Override
    public String visit(BoolAstType t) {
        return VarType.BOOL.getType();
    }

    @Override
    public String visit(IntArrayAstType t) {
        return VarType.INT_ARRAY.getType();
    }

    @Override
    public String visit(RefType t) {
        return t.id();
    }
}

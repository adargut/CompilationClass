package semanticanalysis;

public enum SemanticError {
    NAME_ALREADY_EXISTS("Name %s already exists."),
    PARENT_CLASS_DOESNT_EXISTS("Parent class %s was not found in hierarchy. Check if %s was declared after deriving class %s."),
    MAIN_CLASS_CANNOT_BE_EXTENDED("Main class cannot be extended."),
    FORMAL_OUTSIDE_METHOD("Formals cannot be declared outside of a method."),
    METHOD_OUTSIDE_CLASS("Methods cannot be declared outside of a class."),
    OVERLOADING_NOT_SUPPORTED("A method with name %s was already declared (overloading is not supported)."),
    THIS_IS_SAVED_WORD("Variables, formals, fields and methods cannot be named this (saved word)."),
    INVALID_OVERRIDING("Overriding of method %s is invalid."),
    IF_COND_NOT_BOOL("If condition must be a boolean."),
    WHILE_COND_NOT_BOOL("While condition must be a boolean."),
    INVALID_ASSIGNMENT("Assignment of variable %s is invalid."),
    INVALID_TYPE("Type mismatch. Got: %s, expected: %s."),
    INVALID_METHOD_CALL("Method %s call is invalid."),
    INVALID_ARRAY_INDEX("Invalid index type. Got: %s, expected: int."),
    OBJ_DOESNT_EXIST("Referenced object %s doesn't exist."),
    OBJ_NOT_INITIALIZED("%s is not initialized (class: %s, method: %s)."),
    UNKNOWN_ERROR("Unknown semantic error.");

    private String errorMsg;

    // getter method
    public String getErrorMsg() {
        return this.errorMsg;
    }

    private SemanticError(String representation) {
        this.errorMsg = representation;
    }

    @Override
    public String toString() {
        return this.errorMsg;
    }
}
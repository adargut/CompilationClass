package codegen;

import codegen.utils.JavaTypeToLLVMType;
import symboltable.Variable;

public final class Alloca implements Gen {
    private Variable variable;
    private static Alloca instance = null;

    // Singleton
    private Alloca() {}

    /** Get a singleton instance of Alloca */
    public static Alloca getInstance() {
        if (instance == null) {
            instance = new Alloca();
        }
        return instance;
    }

    /** Set the variable for Alloca */
    public void setVariable(Variable variable) {
        this.variable = variable;
    }

    /**
     * LLVM code generation for allocating local variables on the stack.
     * Example: %ptr = alloca i32
     */
    @Override
    public String generate() {
        var variableType = this.variable.getType();
        var variableSymbol = this.variable.getSymbol();
        var variableLLVMType = JavaTypeToLLVMType.getLLVMType(variableType);
        return "\t%" + variableSymbol + " = alloca " + variableLLVMType + "\n";
    }

    /**
     * LLVM code generation for allocating local variables on the stack to a specified register.
     * Example: %registerName = alloca i32
     */
    public String generate(String registerName) {
        var variableType = this.variable.getType();
        var variableSymbol = this.variable.getSymbol();
        var variableLLVMType = JavaTypeToLLVMType.getLLVMType(variableType);
        return "\t%" + registerName + " = alloca " + variableLLVMType + "\n";
    }
}
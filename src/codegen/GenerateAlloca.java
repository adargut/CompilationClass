package codegen;

import codegen.utils.JavaTypeToLLVMType;
import symboltable.Variable;

public final class GenerateAlloca {
    /**
     * LLVM code generation for allocating local variables on the stack.
     * Example: %ptr = alloca i32
     */
    public static String generateAlloca(Variable variable) {
        var variableType = variable.getType();
        var variableSymbol = variable.getSymbol();
        var variableLLVMType = JavaTypeToLLVMType.getLLVMType(variableType);
        return "%" + variableSymbol + " = alloca " + variableLLVMType;
    }
}

package codegen.utils;

import symboltable.Variable;

public final class JavaVariableToLLVM {
    /**
     * Translate some Java variable into its corresponding llvm form.
     * Example: int x -> i32 %x
     */
    public static String translateToLLVM(Variable variable) {
        var variableType = variable.getType();
        return JavaTypeToLLVMType.getLLVMType(variableType) + " %" + variable.getSymbol();
    }

    public static String translateToLLVM(Variable variable, String registerName) {
        var variableType = variable.getType();
        return JavaTypeToLLVMType.getLLVMType(variableType) + " %" + registerName;
    }
}

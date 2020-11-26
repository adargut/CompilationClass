package codegen;

import codegen.utils.JavaTypeToLLVMType;
import symboltable.Variable;

public final class Alloca implements Gen<Variable> {
    /**
     * LLVM code generation for allocating local variables on the stack.
     * Example: %ptr = alloca i32
     */
    @Override
    public String generate(Variable... vars) {
        if (vars.length != 1) {
            throw new IllegalArgumentException("Tried to call generate Alloca with "
                    + vars.length + " arguments instead of 1");
        }
        var variable = vars[0];
        var variableType = variable.getType();
        var variableSymbol = variable.getSymbol();
        var variableLLVMType = JavaTypeToLLVMType.getLLVMType(variableType);
        return "%" + variableSymbol + " = alloca " + variableLLVMType;
    }
}
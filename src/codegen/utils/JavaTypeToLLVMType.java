package codegen.utils;

import ast.AstType;
import ast.BoolAstType;
import ast.IntArrayAstType;
import ast.IntAstType;

public final class JavaTypeToLLVMType {
    public static LLVMType getLLVMType(AstType type) {
        if (type instanceof IntAstType) {
            return LLVMType.i32;
        }

        else if (type instanceof BoolAstType) {
            return LLVMType.i1;
        }

        else if (type instanceof IntArrayAstType) {
            return LLVMType.i32ptr;
        }

        else {
            return LLVMType.i8ptr;
        }
    }
}

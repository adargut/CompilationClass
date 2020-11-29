package codegen.vtable;

import codegen.utils.JavaTypeToLLVMType;
import codegen.utils.LLVMType;
import symboltable.Class;
import symboltable.Method;
import symboltable.SymbolTable;
import symboltable.Variable;

import java.util.LinkedHashMap;
import java.util.Map;

public class VTables {

    public LinkedHashMap<String, ClassVTable> classesTables;

    VTables() {
        classesTables = new LinkedHashMap<>();
    }

    public static class ClassVTable {
        public boolean isMainClass;
        public LinkedHashMap<String, Variable> fieldsTable;
        public LinkedHashMap<String, Method> methodsTable;

        ClassVTable() {
            boolean isMainClass;
            fieldsTable = new LinkedHashMap<>();
            methodsTable = new LinkedHashMap<>();
        }

        public int getMethodOffset(String methodName) {
            int offset = 0;

            for (Map.Entry<String, Method> entry : methodsTable.entrySet()) {
                var _methodName = entry.getKey();
                if (_methodName.equals(methodName)) return offset;
                offset += 8;
            }
            throw new RuntimeException("Method " + methodName + " was not found in the vtable!");
        }

        public int getVarOffset(String symbol) {
            // We are starting with +8 because the first 8 bytes are for the vtable ptr
            int offset = 8;

            for (Map.Entry entry : fieldsTable.entrySet()) {
                String fieldName = (String) entry.getKey();
                Variable field = (Variable) entry.getValue();

                if (fieldName.equals(symbol)) {
                    return offset;
                }

                switch (JavaTypeToLLVMType.getLLVMType(field.getType())) {
                    case i32: offset += 4;
                    break;
                    case i1: offset += 1;
                    break;
                    default: offset += 8;
                }
            }

            throw new RuntimeException(String.format("Variable %s was not found!", symbol));
        }

        public int getClassSize() {
            // We are starting with +8 because the first 8 bytes are for the vtable ptr
            int size = 8;

            for (Map.Entry entry : fieldsTable.entrySet()) {
                Variable field = (Variable) entry.getValue();

                switch (JavaTypeToLLVMType.getLLVMType(field.getType())) {
                    case i32: size += 4;
                    break;
                    case i1: size += 1;
                    break;
                    default: size += 8;
                }
            }

            return size;
        }
    }

    public static VTables createVTables(SymbolTable symbolTable) {
        VTables vTables = new VTables();

        for (Map.Entry entry : symbolTable.getClasses().entrySet()) {
            String className = (String) entry.getKey();
            Class currentClass = symbolTable.getClasses().get(className);
            ClassVTable classVTable = new ClassVTable();
            vTables.classesTables.put(className, classVTable);

            if (currentClass.isMainClass()) {
                classVTable.isMainClass = true;
                continue;
            }

            // Get all the accessible methods
            LinkedHashMap<String, Method> methodsMap = currentClass.getAllMethods();

            // Fet all the accessible fields
            LinkedHashMap<String, Variable> fieldsMap = currentClass.getAllVariables();

            classVTable.methodsTable = methodsMap;
            classVTable.fieldsTable = fieldsMap;


        }

        return vTables;
    }

    public String generate() {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry entry : this.classesTables.entrySet()) {
            String className = (String) entry.getKey();
            VTables.ClassVTable classVTable = this.classesTables.get(className);

            if (classVTable.isMainClass) {
//                builder.append("@." + className + "_vtable = global [0 x i8*] []\n");
                continue;
            }

            int numberOfMethods = classVTable.methodsTable.size();
            builder.append("@." + className + "_vtable = global [" + numberOfMethods + " x i8*] [\n");

            boolean isFirst = true;

            // Return type
            for (Map.Entry methodEntry : classVTable.methodsTable.entrySet()) {
                if (!isFirst) {
                    builder.append(",\n");
                }

                String methodName = (String) methodEntry.getKey();
                Method method = (Method) methodEntry.getValue();

                builder.append("\ti8* bitcast (" + JavaTypeToLLVMType.getLLVMType(method.getMethodDecl().returnType()) + " (i8*");

                // Params
                for (Map.Entry methodParam : method.getParams().entrySet()) {
                    String paramName = (String) methodParam.getKey();
                    Variable param = (Variable) methodParam.getValue();

                    builder.append(", " + JavaTypeToLLVMType.getLLVMType(param.getType()));
                }

                builder.append(")* @" + className + "." + methodName + " to i8*)");
                isFirst = false;
            }

            builder.append("\n]\n\n");
        }

        return builder.toString();
    }

}

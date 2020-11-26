package vtables;

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
    }

    VTables createVTables(SymbolTable symbolTable) {
        VTables vTables = new VTables();

        for (Map.Entry entry: symbolTable.getClasses().entrySet())
             {
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

}

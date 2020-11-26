package codegen.vtable;

import symboltable.Method;

import java.util.LinkedHashMap;
import java.util.Set;

public interface VTable {
    /** Get all dynamic methods for a class from the vtable. */
    public LinkedHashMap<String, Method> getMethods();

    /** Add a method to a class: returns true if success, and false otherwise. */
    public void addMethod(Method method);
}

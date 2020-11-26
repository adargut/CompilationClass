package codegen.vtable;

import symboltable.Method;
import symboltable.Class;

public interface VTable {
    /** Get all dynamic methods for a class from the vtable. */
    public Method[] getMethods();

    /** Add a method to a class: returns true if success, and false otherwise. */
    public Boolean addMethod(Method method);
}

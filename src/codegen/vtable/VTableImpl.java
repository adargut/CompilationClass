package codegen.vtable;

import symboltable.Class;
import symboltable.Method;

public class VTableImpl implements VTable {
    /** This is the class the vtable corresponds to. */
    Class _class;

    @Override
    public Method[] getMethods() {
        throw new java.lang.UnsupportedOperationException("Need to support getMethods for vtable");
    }

    @Override
    public Boolean addMethod(Method method) {
        throw new java.lang.UnsupportedOperationException("Need to support addMethod");
    }
}

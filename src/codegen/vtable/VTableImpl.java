package codegen.vtable;

import symboltable.Class;
import symboltable.Method;

import java.util.LinkedHashMap;
import java.util.Set;

public class VTableImpl implements VTable {
    /** This is the class the vtable corresponds to. */
    private Class _class;
    /** These are the methods of the class. */
    private LinkedHashMap<String, Method> classMethods;

    @Override
    public LinkedHashMap<String, Method> getMethods() {
        return this.classMethods;
    }

    @Override
    public void addMethod(Method method) {
        if (classMethods.get(method.getName()) != null) {
            // todo throw some exception here: class cannot have same method twice. maybe this can't happen?
        }
        this.classMethods.put(method.getName(), method);
    }
}

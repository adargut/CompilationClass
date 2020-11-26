package codegen.vtable;

import symboltable.Class;
import symboltable.Method;
import java.util.Set;

public class VTableImpl implements VTable {
    /** This is the class the vtable corresponds to. */
    private Class _class;
    /** These are the methods of the class. */
    private Set<Method> classMethods;

    @Override
    public Set<Method> getMethods() {
        return this.classMethods;
    }

    @Override
    public void addMethod(Method method) {
        if (classMethods.contains(method)) {
            // todo throw some exception here: class cannot have same method twice. maybe this can't happen?
        }
        this.classMethods.add(method);
    }
}

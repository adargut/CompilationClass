package symboltable;

import ast.ClassDecl;
import utils.TreeNode;

import java.util.*;

public class Class {
    private String name;
    private String parentName;
    private final HashMap<String, Method> methods;
    private final HashMap<String, Variable> fields;
    private TreeNode<Class> node;
    private ClassDecl classDecl;
    boolean isMainClass;

    public Class(String name, ClassDecl classDecl) {
        this.name = name;
        this.classDecl = classDecl;
        this.methods = new HashMap<String, Method>();
        this.fields = new HashMap<String, Variable>();
        this.isMainClass = false;
    }

    public Class(String name, ClassDecl classDecl, boolean isMainClass) {
        this.name = name;
        this.classDecl = classDecl;
        this.methods = new HashMap<String, Method>();
        this.fields = new HashMap<String, Variable>();
        this.isMainClass = isMainClass;
    }

    public Class(String name, ClassDecl classDecl, String parentName) {
        this(name, classDecl);
        this.parentName = parentName;
    }

    public Class(String name, ClassDecl classDecl, String parentName, boolean isMainClass) {
        this(name, classDecl, isMainClass);
        this.parentName = parentName;

    }

    public ClassDecl getClassDecl() {
        return classDecl;
    }

    public boolean isMainClass() {
        return isMainClass;
    }

    public void setClassDecl(ClassDecl classDecl) {
        this.classDecl = classDecl;
    }

    public HashMap<String, Method> getMethods() {
        // Return the methods sorted by the line number
        List<Map.Entry<String, Method> > list =
                new LinkedList<Map.Entry<String, Method> >(methods.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Method> >() {
            public int compare(Map.Entry<String, Method> method1,
                               Map.Entry<String, Method> method2)
            {
                if (method1.getValue().getLineNumber() == null ^ method2.getValue().getLineNumber() == null) {
                    return method1.getValue().getLineNumber() == null ? -1 : 1;
                }

                if (method1.getValue().getLineNumber() == null && method2.getValue().getLineNumber() == null) {
                    return 0;
                }
                return (method1.getValue().getLineNumber()).compareTo(method2.getValue().getLineNumber());
            }
        });

        HashMap<String, Method> temp = new LinkedHashMap<String, Method>();
        for (Map.Entry<String, Method> entry : list) {
            temp.put(entry.getKey(), entry.getValue());
        }
        return temp;
    }

    public HashMap<String, Variable> getFields() {
        // Return the fields sorted by the line number
        List<Map.Entry<String, Variable> > list =
                new LinkedList<Map.Entry<String, Variable> >(fields.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Variable> >() {
            public int compare(Map.Entry<String, Variable> o1,
                               Map.Entry<String, Variable> o2)
            {
                if (o1.getValue().getLineNumber() == null ^ o2.getValue().getLineNumber() == null) {
                    return o1.getValue().getLineNumber() == null ? -1 : 1;
                }

                if (o1.getValue().getLineNumber() == null && o2.getValue().getLineNumber() == null) {
                    return 0;
                }
                return (o1.getValue().getLineNumber()).compareTo(o2.getValue().getLineNumber());
            }
        });

        HashMap<String, Variable> temp = new LinkedHashMap<String, Variable>();
        for (Map.Entry<String, Variable> entry : list) {
            temp.put(entry.getKey(), entry.getValue());
        }
        return temp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TreeNode<Class> getNode() {
        return node;
    }

    public void setNode(TreeNode<Class> node) {
        this.node = node;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public Boolean addMethod(Method method) {
        if (this.methods.containsKey(method.getName())) return false;
        this.methods.put(method.getName(), method);
        return true;
    }

    public Boolean addMethod(String methodName, Method method) {
        if (this.methods.containsKey(methodName)) return false;
        this.methods.put(methodName, method);
        return true;
    }

    public Method getMethod(String methodName) {
        if (this.methods.containsKey(methodName)) {
            return this.methods.get(methodName);
        }

        return null;
    }

    public Boolean addVar(Variable variable) {
        if (this.fields.containsKey(variable.getSymbol())) return false;
        this.fields.put(variable.getSymbol(), variable);
        return true;
    }

    public Boolean addVar(String symbol, Variable variable) {
        if (this.fields.containsKey(symbol)) return false;
        this.fields.put(symbol, variable);
        return true;
    }

    public Variable getVar(String symbol) {
        if (this.fields.containsKey(symbol)) {
            return this.fields.get(symbol);
        }

        return null;
    }

    /**
     * Get a map of all the methods that are accessible in the current class, in the order
     * in which they are introduced (methods from parent classes are before current classes)
     * @return A map of the methods (key is the method name, value is Method instance matching that method)
     */
    public LinkedHashMap<String, Method> getAllMethods(){
        var methodsMap = new LinkedHashMap<String, Method>();
        getAllMethodsRecursive(this, methodsMap);
        return methodsMap;
    }

    /**
     * Recursively get all methods that are accessible in the current class, in the order in which
     * they are introduced, and fill a LinkedHashMap with the methods found (ordered by entrance time)
     * @param curr The current class
     * @param methodsMap The map to fill with methods
     */
    private void getAllMethodsRecursive(Class curr, LinkedHashMap<String, Method> methodsMap) {
        if (curr.node == null) {
            // Reached top level in the class hierarchy
        }

        else {
            // Go up to parent
            getAllMethodsRecursive(curr.node.getParent().getData(), methodsMap);
        }

        // Add the methods of the current class (if they override any method from one of the parents
        // then the relevant key will be updated, but the order will remain)
        // Note - the methods of the current class are ordered by their line number
        for (Method method: curr.getMethods().values()) {
            methodsMap.put(method.getName(), method);
        }
    }

    /**
     * Get a map of all the fields that are accessible in the current class, in the order
     * in which they are introduced (fields from parent classes are before current classes)
     * @return A map of the fields (key is the field name, value is Varialbe instance matching that field)
     */
    public LinkedHashMap<String, Variable> getAllVariables(){
        var variablesMap = new LinkedHashMap<String, Variable>();
        getAllVariablesRecursive(this, variablesMap);
        return variablesMap;
    }

    /**
     * Recursively get all fields that are accessible in the current class, in the order in which
     * they are introduced, and fill a LinkedHashMap with the fields found (ordered by entrance time)
     * @param curr The current class
     * @param variablesMap The map to fill with fields
     */
    private void getAllVariablesRecursive(Class curr, LinkedHashMap<String, Variable> variablesMap) {
        if (curr.node == null) {
            // Reached top level in the class hierarchy
        }

        else {
            // Go up to parent
            getAllVariablesRecursive(curr.node.getParent().getData(), variablesMap);
        }

        // Add the fields of the current class (if they override any fields from one of the parents
        // then the relevant key will be updated, but the order will remain)
        for (Variable field: curr.getFields().values()) {
            // Note - the fields of the current class are ordered by their line number
            variablesMap.put(field.getSymbol(), field);
        }

    }
}

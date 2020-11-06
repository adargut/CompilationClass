package symboltable;

import ast.ClassDecl;
import utils.TreeNode;

import java.util.HashMap;

public class Class {
    private String name;
    private String parentName;
    private final HashMap<String, Method> methods;
    private final HashMap<String, Variable> fields;
    private TreeNode<Class> node;
    private ClassDecl classDecl;

    public Class(String name, ClassDecl classDecl) {
        this.name = name;
        this.classDecl = classDecl;
        this.methods = new HashMap<String, Method>();
        this.fields = new HashMap<String, Variable>();
    }

    public Class(String name, ClassDecl classDecl, String parentName) {
        this(name, classDecl);
        this.parentName = parentName;
    }


    public ClassDecl getClassDecl() {
        return classDecl;
    }

    public void setClassDecl(ClassDecl classDecl) {
        this.classDecl = classDecl;
    }

    public HashMap<String, Method> getMethods() {
        return methods;
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
}

package symboltable;

import ast.MethodCallExpr;
import ast.MethodDecl;

import java.util.ArrayList;
import java.util.HashMap;

public class Method {
    private Boolean shouldRename = false;
    private String name;
    private Integer lineNumber;
    private MethodDecl methodDecl;
    private final HashMap<String, Variable> params;
    private final HashMap<String, Variable> variables;
    private final ArrayList<MethodCallExpr> calls;

    public Method(String name, Integer lineNumber) {
        this.name = name;
        this.lineNumber = lineNumber;
        this.variables = new HashMap<String, Variable>();
        this.params = new HashMap<String, Variable>();
        this.calls = new ArrayList<MethodCallExpr>();
    }

    public Method(String name, Integer lineNumber, MethodDecl methodDecl) {
        this(name, lineNumber);
        this.methodDecl = methodDecl;
    }

    public MethodDecl getMethodDecl() {
        return methodDecl;
    }

    public void setMethodDecl(MethodDecl methodDecl) {
        this.methodDecl = methodDecl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getShouldRename() {
        return shouldRename;
    }

    public void setShouldRename(Boolean shouldRename) {
        this.shouldRename = shouldRename;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Boolean addVar(Variable variable) {
        if (this.variables.containsKey(variable.getSymbol())) return false;
        this.variables.put(variable.getSymbol(), variable);
        return true;
    }

    public Boolean addVar(String symbol, Variable variable) {
        if (this.variables.containsKey(symbol)) return false;
        this.variables.put(symbol, variable);
        return true;
    }

    public Variable getVar(String symbol) {
        if (this.variables.containsKey(symbol)) {
            return this.variables.get(symbol);
        }

        return null;
    }

    public Boolean addParam(Variable variable) {
        if (this.params.containsKey(variable.getSymbol())) return false;
        this.params.put(variable.getSymbol(), variable);
        return true;
    }

    public Boolean addParam(String symbol, Variable variable) {
        if (this.params.containsKey(symbol)) return false;
        this.params.put(symbol, variable);
        return true;
    }

    public Variable getParam(String symbol) {
        if (this.params.containsKey(symbol)) {
            return this.params.get(symbol);
        }

        return null;
    }

    public void addCall(MethodCallExpr methodCallExpr) {
        this.calls.add(methodCallExpr);
    }
}
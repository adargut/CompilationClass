package symboltable;

import ast.MethodCallExpr;
import ast.MethodDecl;
import semanticanalysis.SemanticError;
import semanticanalysis.SemanticException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Method {
    private Boolean shouldRename = false;
    private String name;
    private Integer lineNumber;
    private MethodDecl methodDecl;
    private Class parentClass;
    private Method originalMethod;
    private final HashMap<String, Variable> params; // todo why is this a hashmap and not hashset (or array)?
    private final List<Variable> paramsArray; // todo check if this is a param?
    private final HashMap<String, Variable> variables;
    private final ArrayList<MethodCallExpr> calls;

    public Method(String name, Integer lineNumber, Class parentClass) {
        this.name = name;
        this.lineNumber = lineNumber;
        this.parentClass = parentClass;
        this.variables = new HashMap<String, Variable>();
        this.params = new HashMap<String, Variable>();
        this.paramsArray = new ArrayList<>();
        this.calls = new ArrayList<MethodCallExpr>();
    }

    public Method(String name, Integer lineNumber, Class parentClass, MethodDecl methodDecl) {
        this(name, lineNumber, parentClass);
        this.methodDecl = methodDecl;
    }

    public Method(String name, Integer lineNumber, Class parentClass, MethodDecl methodDecl, Method originalMethod) {
        this(name, lineNumber, parentClass, methodDecl);
        this.originalMethod = originalMethod;
    }

    public Class getParentClass() {
        return parentClass;
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

    public Method getOriginalMethod() {
        return originalMethod;
    }

    public void setOriginalMethod(Method originalMethod) {
        this.originalMethod = originalMethod;
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
        if (variable.getSymbol() == "this") {
            // this is a saved word - SEMANTIC ERROR #7
            throw new SemanticException(
                    SemanticError.THIS_IS_SAVED_WORD
            );
        }
        // A variable cannot be redeclared or override a param
        if (this.variables.containsKey(variable.getSymbol())) return false;
        else if (this.params.containsKey(variable.getSymbol())) return false;

        this.variables.put(variable.getSymbol(), variable);
        return true;
    }

    public Boolean addVar(String symbol, Variable variable) {
        if (variable.getSymbol() == "this") {
            // this is a saved word - SEMANTIC ERROR #7
            throw new SemanticException(
                    SemanticError.THIS_IS_SAVED_WORD
            );
        }

        // A variable cannot be redeclared or override a param
        if (this.variables.containsKey(symbol)) return false;
        else if (this.params.containsKey(symbol)) return false;

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
        if (variable.getSymbol().equals("this")) {
            // this is a saved word - SEMANTIC ERROR #7
            throw new SemanticException(
                    SemanticError.THIS_IS_SAVED_WORD
            );
        }

        // A variable cannot be redeclared or override a param
        if (this.variables.containsKey(variable.getSymbol())) return false;
        else if (this.params.containsKey(variable.getSymbol())) return false;

        this.params.put(variable.getSymbol(), variable);
        this.paramsArray.add(variable);
        return true;
    }

    public Boolean addParam(String symbol, Variable variable) {
        if (symbol.equals("this")) {
            // this is a saved word - SEMANTIC ERROR #7
            throw new SemanticException(
                    SemanticError.THIS_IS_SAVED_WORD
            );
        }

        // A variable cannot be redeclared or override a param
        if (this.variables.containsKey(symbol)) return false;
        else if (this.params.containsKey(symbol)) return false;

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

    public HashMap<String, Variable> getParams() {
        return this.params;
    }

    public List<Variable> getParamsArray() {
        return this.paramsArray;
    }
}

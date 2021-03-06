package symboltable;

import ast.AstType;
import ast.ClassDecl;
import ast.MethodDecl;
import ast.RefType;
import semanticanalysis.SemanticError;
import semanticanalysis.SemanticException;
import utils.Tree;
import utils.TreeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    public Tree<Class> classHierarchy;
    private HashMap<String, Class> classes;

    public SymbolTable() {
        this.classes = new HashMap<String, Class>();
        this.classHierarchy = new Tree<Class>(new TreeNode<Class>("root", new Class("root", null)));
    }

    public HashMap<String, Class> getClasses() {
        return classes;
    }

    public boolean addClass(String id, String parent, ClassDecl classDecl, boolean isMainClass) {
        if (this.classes.containsKey(id)) {
            // Duplicate - class with that name already exist
            return false;
        }

        var currentClass = new Class(id, classDecl, parent, isMainClass);

        TreeNode<Class> currentClassNode = new TreeNode<Class>(id, currentClass);

        if (parent == null) {
            // Add current class straight to root node.
            this.classHierarchy.getRoot().addChild(currentClassNode);
        } else {
            // The class is deriving from some other class
            TreeNode<Class> superNode = this.classHierarchy.findNode(parent);

            if (superNode == null) {
                // The class this class is deriving from hasn't been declared yet - SEMANTIC ERROR #1.
                throw new SemanticException(
                        SemanticError.PARENT_CLASS_DOESNT_EXISTS,
                        new Object[]{parent, parent, id}
                );
            }
            else if (superNode.getData().isMainClass()) {
                // Main class cannot be extended - SEMANTIC ERROR #2
                throw new SemanticException(SemanticError.MAIN_CLASS_CANNOT_BE_EXTENDED);
            }
            else {
                superNode.addChild(currentClassNode);
            }
        }

        currentClass.setNode(currentClassNode);
        this.classes.put(id, currentClass);

        return true;
    }

    public Class getClass(String id) {
        return this.classes.getOrDefault(id, null);
    }

    public Method getMethod(String methodName, Integer lineNumber) {
        for (Map.Entry mapElement : this.classes.entrySet()) {
            String className = (String) mapElement.getKey();
            Class currentClass = ((Class) mapElement.getValue());

            Method method = currentClass.getMethod(methodName);

            if (method != null && (lineNumber == null ||
                    (method.getLineNumber() != null && method.getLineNumber().equals(lineNumber)))) {
                return method;
            }
        }

        return null;
    }

    public Method getOverridenMethod(Method method) {
        Class current_class = method.getParentClass();
        while(current_class.getParentName() != null){
            current_class = getClass(current_class.getParentName());
            if (current_class.getMethod(method.getName()) != null){
                return current_class.getMethod(method.getName());
            }
        }
        return null;
    }

    /** return true iff class1 is a subtype of class2 */
    public boolean isSubclass(Class class1, Class class2){
        Class current_class = class1;
        while(current_class != null){
            if (current_class.getName().equals(class2.getName())) return true;
            current_class = getClass(current_class.getParentName());
        }
        return false;
    }

    /** return true iff type1 is a subtype of type2 */
    public boolean isSubtype(AstType type1, AstType type2){
        // Check if both types are of the same class
        if (!type1.getClass().equals(type2.getClass())) return false;

        if (type1 instanceof RefType){
            Class class1 = getClass(((RefType) type1).id());
            Class class2 = getClass(((RefType) type2).id());
            return isSubclass(class1, class2);
        }

        return true;
    }

    public Variable getVarByNameAndLine(String varName, Integer lineNumber) {
        Variable var;
        for (Map.Entry mapElement : this.classes.entrySet()) {
            String className = (String) mapElement.getKey();
            Class currentClass = ((Class) mapElement.getValue());

            for (Method method: currentClass.getMethods().values()) {
                // Search in params
                var = method.getParam(varName);

                if (var != null && var.getLineNumber() != null && var.getLineNumber().equals(lineNumber)) {
                    return var;
                }

                // Search in variables
                var = method.getVar(varName);

                if (var != null && var.getLineNumber() != null && var.getLineNumber().equals(lineNumber)) {
                    return var;
                }

            }

            // Search in fields
            var = currentClass.getVar(varName);

            if (var != null && var.getLineNumber() != null && var.getLineNumber().equals(lineNumber)) {
                return var;
            }

        }

        return null;
    }

    public Class getClassOfMethod(String methodName, Integer lineNumber) {
        for (Map.Entry mapElement : this.classes.entrySet()) {
            String className = (String) mapElement.getKey();
            Class currentClass = ((Class) mapElement.getValue());
            Method method = currentClass.getMethod(methodName);

            if (method != null && method.getLineNumber() != null && method.getLineNumber().equals(lineNumber)) {
                return currentClass;
            }
        }
        return null;
    }

    public ArrayList<MethodDecl> getAllMethodsDeclarations(String methodName, Integer lineNumber) {
        ArrayList<MethodDecl> methodDeclarations = new ArrayList<MethodDecl>();
        Class classScope = getClassOfMethod(methodName, lineNumber);

        if (classScope == null) {
            throw new RuntimeException(
                    String.format("Method with name %s and line number %d was not found!",
                            methodName, lineNumber
                    )
            );
        }

        TreeNode<Class> currentClassNode = classScope.getNode().getParent();
        TreeNode<Class> oldestValidParent = classScope.getNode();

        // Traverse up the tree
        while (!currentClassNode.equals(this.classHierarchy.getRoot())) {
            Method method = currentClassNode.getData().getMethod(methodName);

            if (method != null) {
                // Parent class contains a declaration of the method (the current method is overriding it)
                // Add the declaration to the list
                oldestValidParent = currentClassNode;
            }

            currentClassNode = currentClassNode.getParent();
        }

        // Traverse down the tree (search method declarations in all children)
        getAllMethodDeclarationsDownwards(methodName, oldestValidParent, methodDeclarations);
        return methodDeclarations;
    }

    private void getAllMethodDeclarationsDownwards(String methodName, TreeNode<Class> currentClassNode, ArrayList<MethodDecl> methodDeclarations) {
        if (currentClassNode == null) {
            return;
        }

        Method method = currentClassNode.getData().getMethod(methodName);

        if (method != null) {
            // Child class contains a declaration of the method (the child method is overriding it)
            // Add the declaration to the list
            methodDeclarations.add(method.getMethodDecl());
        }

        for (TreeNode<Class> child : currentClassNode.getChildren()) {
            getAllMethodDeclarationsDownwards(methodName, child, methodDeclarations);
        }
    }

    public ArrayList<Class> getAllRelevantClasses(String methodName, Integer lineNumber) {
        ArrayList<Class> relevantClasses = new ArrayList<Class>();
        Class classScope = getClassOfMethod(methodName, lineNumber);

        if (classScope == null) {
            throw new RuntimeException(
                    String.format("Method with name %s and line number %d was not found!",
                            methodName, lineNumber
                    )
            );
        }

        TreeNode<Class> currentClassNode = classScope.getNode().getParent();
        TreeNode<Class> oldestValidParent = classScope.getNode();

        // Traverse up the tree
        while (!currentClassNode.equals(this.classHierarchy.getRoot())) {
            Method method = currentClassNode.getData().getMethod(methodName);

            if (method != null) {
                // Parent class contains a declaration of the method (the current method is overriding it)
                // Add the declaration to the list
                oldestValidParent = currentClassNode;
            }

            currentClassNode = currentClassNode.getParent();
        }

        // Traverse down the tree (search method declarations in all children)
        getAllRelevantClassesDownwards(oldestValidParent, relevantClasses);
        return relevantClasses;
    }

    private void getAllRelevantClassesDownwards(TreeNode<Class> currentClassNode, ArrayList<Class> relevantClasses) {
        if (currentClassNode == null) {
            return;
        }

        relevantClasses.add(currentClassNode.getData());

        for (TreeNode<Class> child : currentClassNode.getChildren()) {
            getAllRelevantClassesDownwards(child, relevantClasses);
        }
    }

    public ArrayList<String> getAllRelevantClassNames(String methodName, Integer lineNumber) {
        ArrayList<String> relevantClassNames = new ArrayList<String>();
        Class classScope = getClassOfMethod(methodName, lineNumber);

        if (classScope == null) {
            throw new RuntimeException(
                    String.format("Method with name %s and line number %d was not found!",
                            methodName, lineNumber
                    )
            );
        }

        // Add the class declaration to the list
        relevantClassNames.add(classScope.getName());

        TreeNode<Class> currentClassNode = classScope.getNode().getParent();

        // Traverse up the tree
        while (!currentClassNode.equals(this.classHierarchy.getRoot())) {
            relevantClassNames.add(classScope.getName());
        }

        // Traverse down the tree
        getAllRelevantClassNamesDownwards(currentClassNode, relevantClassNames);
        return relevantClassNames;
    }

    private void getAllRelevantClassNamesDownwards(TreeNode<Class> currentClassNode, ArrayList<String> relevantClassNames) {
        if (currentClassNode == null) {
            return;
        }

        relevantClassNames.add(currentClassNode.getData().getName());

        for (TreeNode<Class> child : currentClassNode.getChildren()) {
            getAllRelevantClassNamesDownwards(child, relevantClassNames);
        }
    }

    public Variable getVar(Method methodScope, String symbol) {
        Variable variable = methodScope.getParam(symbol);

        // If we found the symbol in params - return it
        if (variable != null) {
            return variable;
        }

        // Search symbol in var declarations (method scope)
        variable = methodScope.getVar(symbol);

        if (variable != null) {
            return variable;
        }

        // Traverse up in class fields until root is reached
        return getVar(methodScope.getParentClass(), symbol);


    }

    public Variable getVar(Class classScope, String symbol) {
        if (classScope.equals(this.classHierarchy.getRoot().getData())) {
            // Reached top
            return null;
        }
        Variable variable = classScope.getVar(symbol);

        if (variable != null) {
            return variable;
        }

        return getVar(classScope.getNode().getParent().getData(), symbol);

    }
}

package symboltable;

import ast.ClassDecl;
import ast.MethodDecl;
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

    public boolean addClass(String id, String parent, ClassDecl classDecl) {
        if (this.classes.containsKey(id)) {
            // Duplicate - class with that name already exist
            return false;
        }

        var currentClass = new Class(id, classDecl, parent);

        TreeNode<Class> currentClassNode = new TreeNode<Class>(id, currentClass);

        if (parent == null) {
            // Add current class straight to root node.
            this.classHierarchy.getRoot().addChild(currentClassNode);
        } else {
            // The class is deriving from some other class
            TreeNode<Class> superNode = this.classHierarchy.findNode(parent);

            if (superNode == null) {
                // The class this class is deriving from hasn't been declared yet.
                throw new RuntimeException(
                        String.format("Parent class %s was not found in hierarchy. Check if %s was declared after deriving class %s.",
                                parent, parent, id
                        )
                );
            } else {
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

            if (method != null && method.getLineNumber() != null && method.getLineNumber().equals(lineNumber)) {
                return method;
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

        // Add the original declaration to the list
        methodDeclarations.add(classScope.getMethod(methodName).getMethodDecl());

        TreeNode<Class> currentClassNode = classScope.getNode().getParent();

        // Traverse up the tree
        while (!currentClassNode.equals(this.classHierarchy.getRoot())) {
            Method method = currentClassNode.getData().getMethod(methodName);

            if (method != null) {
                // Parent class contains a declaration of the method (the current method is overriding it)
                // Add the declaration to the list
                methodDeclarations.add(method.getMethodDecl());
            }
        }

        // Traverse down the tree (search method declarations in all children)
        getAllMethodDeclarationsDownwards(methodName, currentClassNode, methodDeclarations);
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

        // Add the class declaration to the list
        relevantClasses.add(classScope);

        TreeNode<Class> currentClassNode = classScope.getNode().getParent();

        // Traverse up the tree
        while (!currentClassNode.equals(this.classHierarchy.getRoot())) {
            relevantClasses.add(classScope);
        }

        // Traverse down the tree
        getAllRelevantClassesDownwards(currentClassNode, relevantClasses);
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
}

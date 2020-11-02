package ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class AstMethodReplaceVisitor implements Visitor { // todo rename this?
    private String methodName; // todo perhaps make this final?
    private Integer lineNumber; // todo same for this
    private String newName;  // todo me too..
    // todo maybe initialize all of these inside the constructor? not sure
    private ArrayList<ClassDecl> relevantClasses = new ArrayList<>(); // todo this should be a set, not arraylist
    private HashMap<String, ClassDecl> classMap = new HashMap<>();
    private HashMap<String, ClassDecl> variableMap = new HashMap<>();
    private ClassDecl currentClass;

    public AstMethodReplaceVisitor(String methodName, Integer lineNumber, String newName) {
        this.methodName = methodName;
        this.lineNumber = lineNumber;
        this.newName = newName;
    }

    @Override
    public void visit(Program program) {
        for (ClassDecl classdecl : program.classDecls()) {
            classdecl.accept(this);
        }
        program.mainClass().accept(this);
    }

    @Override
    public void visit(ClassDecl classDecl) {
        this.currentClass = classDecl;
        this.classMap.put(classDecl.name(), classDecl);

        if (classDecl.superName() != null) {
            for (ClassDecl relevantClass: this.relevantClasses) {
                if (relevantClass.name().equals(classDecl.superName())) {
                    // Current class is deriving from a relevant class -> current class is relevant
                    this.relevantClasses.add(this.currentClass);
                    break;
                }
            }
        }

        for (var fieldDecl : classDecl.fields()) {
            fieldDecl.accept(this);
        }
        for (var methodDecl : classDecl.methoddecls()) {
            methodDecl.accept(this);
        }
    }

    @Override
    public void visit(MainClass mainClass) {
        mainClass.mainStatement().accept(this);
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        if (methodDecl.name().equals(this.methodName)) {
            if (methodDecl.lineNumber != null && methodDecl.lineNumber.equals(this.lineNumber)) {
                // Found the first occurrence of the method to replace (the original definition)
                methodDecl.setName(this.newName);
                this.relevantClasses.add(this.currentClass);
            } else if (this.relevantClasses.contains(this.currentClass)) {
                // Method is in a relevant class (probably a derived class) - rename
                methodDecl.setName(this.newName);
                // TODO - Do we need to do - this.relevantClasses.add(this.currentClass)?
            }
        }

        methodDecl.returnType().accept(this);

        for (var stmt : methodDecl.body()) {
            stmt.accept(this);
        }

        methodDecl.ret().accept(this);

    }

    @Override
    public void visit(FormalArg formalArg) {
        formalArg.type().accept(this);
    }

    @Override
    public void visit(VarDecl varDecl) {
        if (varDecl.type() instanceof RefType) {
            this.variableMap.put(varDecl.name(), this.classMap.get(((RefType) varDecl.type()).id()));
        }
    }

    @Override
    public void visit(BlockStatement blockStatement) {
        for (var s : blockStatement.statements()) {
            s.accept(this);
        }
    }

    @Override
    public void visit(IfStatement ifStatement) {
        ifStatement.cond().accept(this);
        ifStatement.thencase().accept(this);
        ifStatement.elsecase().accept(this);
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        whileStatement.cond().accept(this);
        whileStatement.body().accept(this);
    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {
        sysoutStatement.arg().accept(this);
    }

    @Override
    public void visit(AssignStatement assignStatement) {
        assignStatement.rv().accept(this);
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        assignArrayStatement.index().accept(this);
        assignArrayStatement.rv().accept(this);
    }

    @Override
    public void visit(AndExpr e) {
    }

    @Override
    public void visit(LtExpr e) {
    }

    @Override
    public void visit(AddExpr e) {
    }

    @Override
    public void visit(SubtractExpr e) {
    }

    @Override
    public void visit(MultExpr e) {
    }

    @Override
    public void visit(ArrayAccessExpr e) {
        e.arrayExpr().accept(this);
        e.indexExpr().accept(this);
    }

    @Override
    public void visit(ArrayLengthExpr e) {
        e.arrayExpr().accept(this);
    }

    @Override
    public void visit(MethodCallExpr e) {
        if (e.ownerExpr() instanceof ThisExpr) {
            // Case #3
            // The expression the method is running on is this
            if (this.relevantClasses.contains(this.currentClass)) {
                // Current class is contained in the relevant classes list, so this is a relevant class!
                // So replace.
                e.setMethodId(this.newName);
            }
        }
        else if (e.ownerExpr() instanceof NewObjectExpr) {
            // Case #2
            // The expression is a initialization of a new object!
            if (this.relevantClasses.contains(this.classMap.get(((NewObjectExpr) e.ownerExpr()).classId()))) {
                // Initialized object is of class contained in the relevant classes list, so this is a relevant class!
                // So replace.
                e.setMethodId(this.newName);
            }
            // TODO: Figure out a way to get the var name from the owner Expr and check if its in the map. If it is - replace
//        else if (e.ownerExpr() is a variable in scope and in of type in relevant classes (by var mapping):
//            replace
        }
        e.ownerExpr().accept(this);

        for (Expr arg : e.actuals()) {
            arg.accept(this);
        }
    }

    @Override
    public void visit(IntegerLiteralExpr e) {
    }

    @Override
    public void visit(TrueExpr e) {
    }

    @Override
    public void visit(FalseExpr e) {
    }

    @Override
    public void visit(IdentifierExpr e) {
    }

    public void visit(ThisExpr e) {
    }

    @Override
    public void visit(NewIntArrayExpr e) {
        e.lengthExpr().accept(this);
    }

    @Override
    public void visit(NewObjectExpr e) {
    }

    @Override
    public void visit(NotExpr e) {
        e.e().accept(this);
    }

    @Override
    public void visit(IntAstType t) {
    }

    @Override
    public void visit(BoolAstType t) {
    }

    @Override
    public void visit(IntArrayAstType t) {
    }

    @Override
    public void visit(RefType t) {
    }
}

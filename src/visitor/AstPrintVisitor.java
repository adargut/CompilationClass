package visitor;

import ast.*;

public class AstPrintVisitor implements Visitor {
    private StringBuilder builder = new StringBuilder();

    private int indent = 0;

    public String getString() {
        return builder.toString();
    }

    private void appendWithIndent(String str) {
        builder.append("\t".repeat(indent));
        builder.append(str);
    }

    private void visitBinaryExpr(BinaryExpr e, String infixSymbol) {
        builder.append("(");
        e.e1().accept(this);
        builder.append(")");
        builder.append(" " + infixSymbol + " ");
        builder.append("(");
        e.e2().accept(this);
        builder.append(")");
    }


    @Override
    public String visit(Program program) {
        program.mainClass().accept(this);
        builder.append("\n");
        for (ClassDecl classdecl : program.classDecls()) {
            classdecl.accept(this);
            builder.append("\n");
        }
        return null;
    }

    @Override
    public String visit(ClassDecl classDecl) {
        appendWithIndent("class ");
        builder.append(classDecl.name());
        if (classDecl.superName() != null) {
            builder.append(" extends ");
            builder.append(classDecl.superName());
        }
        builder.append(" {\n");

        indent++;
        for (var fieldDecl : classDecl.fields()) {
            fieldDecl.accept(this);
            builder.append("\n");
        }
        for (var methodDecl : classDecl.methoddecls()) {
            methodDecl.accept(this);
            builder.append("\n");
        }
        indent--;
        appendWithIndent("}\n");
        return null;
    }

    @Override
    public String visit(MainClass mainClass) {
        appendWithIndent("class ");
        builder.append(mainClass.name());
        builder.append(" {\n");
        indent++;
        appendWithIndent("public static void main(String[] ");
        builder.append(mainClass.argsName());
        builder.append(") {");
        builder.append("\n");
        indent++;
        mainClass.mainStatement().accept(this);
        indent--;
        appendWithIndent("}\n");
        indent--;
        appendWithIndent("}\n");
        return null;
    }

    @Override
    public String visit(MethodDecl methodDecl) {
        appendWithIndent("public ");
        methodDecl.returnType().accept(this);
        builder.append(" ");
        builder.append(methodDecl.name());
        builder.append("(");

        String delim = "";
        for (var formal : methodDecl.formals()) {
            builder.append(delim);
            formal.accept(this);
            delim = ", ";
        }
        builder.append(") {\n");

        indent++;

        for (var varDecl : methodDecl.vardecls()) {
            varDecl.accept(this);
        }

        for (var stmt : methodDecl.body()) {
            stmt.accept(this);
        }

        appendWithIndent("return ");
        methodDecl.ret().accept(this);
        builder.append(";");
        builder.append("\n");

        indent--;
        appendWithIndent("}\n");
        return delim;
    }

    @Override
    public String visit(FormalArg formalArg) {
        formalArg.type().accept(this);
        builder.append(" ");
        builder.append(formalArg.name());
        return null;
    }

    @Override
    public String visit(VarDecl varDecl) {
        appendWithIndent("");
        varDecl.type().accept(this);
        builder.append(" ");
        builder.append(varDecl.name());
        builder.append(";\n");
        return null;
    }

    @Override
    public String visit(BlockStatement blockStatement) {
        appendWithIndent("{");
        indent++;
        for (var s : blockStatement.statements()) {
            builder.append("\n");
            s.accept(this);
        }
        indent--;
        builder.append("\n");
        appendWithIndent("}\n");
        return null;
    }

    @Override
    public String visit(IfStatement ifStatement) {
        appendWithIndent("if (");
        ifStatement.cond().accept(this);
        builder.append(")\n");
        indent++;
        ifStatement.thencase().accept(this);
        indent--;
        appendWithIndent("else\n");
        indent++;
        ifStatement.elsecase().accept(this);
        indent--;
        return null;
    }

    @Override
    public String visit(WhileStatement whileStatement) {
        appendWithIndent("while (");
        whileStatement.cond().accept(this);
        builder.append(")");
        indent++;
        whileStatement.body().accept(this);
        indent--;
        builder.append("\n");
        appendWithIndent("\n");
        return null;
    }

    @Override
    public String visit(SysoutStatement sysoutStatement) {
        appendWithIndent("System.out.println(");
        sysoutStatement.arg().accept(this);
        builder.append(");\n");
        return null;
    }

    @Override
    public String visit(AssignStatement assignStatement) {
        appendWithIndent("");
        builder.append(assignStatement.lv());
        builder.append(" = ");
        assignStatement.rv().accept(this);
        builder.append(";\n");
        return null;
    }

    @Override
    public String visit(AssignArrayStatement assignArrayStatement) {
        appendWithIndent("");
        builder.append(assignArrayStatement.lv());
        builder.append("[");
        assignArrayStatement.index().accept(this);
        builder.append("]");
        builder.append(" = ");
        assignArrayStatement.rv().accept(this);
        builder.append(";\n");
        return null;
    }

    @Override
    public String visit(AndExpr e) {
        visitBinaryExpr(e, "&&");
        return null;
    }

    @Override
    public String visit(LtExpr e) {
        visitBinaryExpr(e, "<");;
        return null;
    }

    @Override
    public String visit(AddExpr e) {
        visitBinaryExpr(e, "+");;
        return null;
    }

    @Override
    public String visit(SubtractExpr e) {
        visitBinaryExpr(e, "-");
        return null;
    }

    @Override
    public String visit(MultExpr e) {
        visitBinaryExpr(e, "*");
        return null;
    }

    @Override
    public String visit(ArrayAccessExpr e) {
        builder.append("(");
        e.arrayExpr().accept(this);
        builder.append(")");
        builder.append("[");
        e.indexExpr().accept(this);
        builder.append("]");
        return null;
    }

    @Override
    public String visit(ArrayLengthExpr e) {
        builder.append("(");
        e.arrayExpr().accept(this);
        builder.append(")");
        builder.append(".length");
        return null;
    }

    @Override
    public String visit(MethodCallExpr e) {
        builder.append("(");
        e.ownerExpr().accept(this);
        builder.append(")");
        builder.append(".");
        builder.append(e.methodId());
        builder.append("(");

        String delim = "";
        for (Expr arg : e.actuals()) {
            builder.append(delim);
            arg.accept(this);
            delim = ", ";
        }
        builder.append(")");
        return delim;
    }

    @Override
    public String visit(IntegerLiteralExpr e) {
        builder.append(e.num());
        return null;
    }

    @Override
    public String visit(TrueExpr e) {
        builder.append("true");
        return null;
    }

    @Override
    public String visit(FalseExpr e) {
        builder.append("false");
        return null;
    }

    @Override
    public String visit(IdentifierExpr e) {
        builder.append(e.id());
        return null;
    }

    public String visit(ThisExpr e) {
        builder.append("this");
        return null;
    }

    @Override
    public String visit(NewIntArrayExpr e) {
        builder.append("new int[");
        e.lengthExpr().accept(this);
        builder.append("]");
        return null;
    }

    @Override
    public String visit(NewObjectExpr e) {
        builder.append("new ");
        builder.append(e.classId());
        builder.append("()");
        return null;
    }

    @Override
    public String visit(NotExpr e) {
        builder.append("!(");
        e.e().accept(this);
        builder.append(")");
        return null;
    }

    @Override
    public String visit(IntAstType t) {
        builder.append("int");
        return null;
    }

    @Override
    public String visit(BoolAstType t) {
        builder.append("boolean");
        return null;
    }

    @Override
    public String visit(IntArrayAstType t) {
        builder.append("int[]");
        return null;
    }

    @Override
    public String visit(RefType t) {
        builder.append(t.id());
        return null;
    }
}

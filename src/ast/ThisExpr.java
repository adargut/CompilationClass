package ast;

import visitor.Visitor;

public class ThisExpr extends Expr {
    public ThisExpr() {
    }

    @Override
    public String accept(Visitor v) {
        return v.visit(this);
    }
}

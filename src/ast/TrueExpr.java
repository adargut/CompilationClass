package ast;

import visitor.Visitor;

public class TrueExpr extends Expr {

    public TrueExpr() {
    }

    @Override
    public String accept(Visitor v) {
        return v.visit(this);
    }
}

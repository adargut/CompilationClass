package ast;

import visitor.Visitor;

public class FalseExpr extends Expr {
    public FalseExpr() {
    }

    @Override
    public String accept(Visitor v) {
        return v.visit(this);
    }
}

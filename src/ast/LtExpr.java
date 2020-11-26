package ast;

import visitor.Visitor;

public class LtExpr extends BinaryExpr {

    // for deserialization only!
    public LtExpr() {
    }

    public LtExpr(Expr e1, Expr e2) {
        super(e1, e2);
    }

    @Override
    public String accept(Visitor v) {
        return v.visit(this);
    }
}

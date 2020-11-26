package ast;

import visitor.Visitor;

public class AndExpr extends BinaryExpr {

    // for deserialization only!
    public AndExpr() {
    }

    public AndExpr(Expr e1, Expr e2) {
        super(e1, e2);
    }

    @Override
    public String accept(Visitor v) {
        return v.visit(this);
    }
}

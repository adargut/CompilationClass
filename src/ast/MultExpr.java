package ast;

import visitor.Visitor;

public class MultExpr extends BinaryExpr {

    // for deserialization only!
    public MultExpr() {
    }

    public MultExpr(Expr e1, Expr e2) {
        super(e1, e2);
    }

    @Override
    public String accept(Visitor v) {
        return v.visit(this);
    }
}

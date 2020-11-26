package ast;

import visitor.Visitor;

import javax.xml.bind.annotation.XmlElement;

public class ArrayLengthExpr extends Expr {
    @XmlElement(required = true)
    private ExprWrapper arrayExpr;

    // for deserialization only!
    public ArrayLengthExpr() {
    }

    public ArrayLengthExpr(Expr arrayExpr) {
        this.arrayExpr = new ExprWrapper(arrayExpr);
    }

    @Override
    public String accept(Visitor v) {
        return v.visit(this);
    }

    public Expr arrayExpr() {
        return arrayExpr.e;
    }
}

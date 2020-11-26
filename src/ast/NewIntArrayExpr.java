package ast;

import visitor.Visitor;

import javax.xml.bind.annotation.XmlElement;

public class NewIntArrayExpr extends Expr {
    @XmlElement(required = true)
    private ExprWrapper lengthExpr;

    // for deserialization only!
    public NewIntArrayExpr() {
    }

    public NewIntArrayExpr(Expr lengthExpr) {
        this.lengthExpr = new ExprWrapper(lengthExpr);
    }

    @Override
    public String accept(Visitor v) {
        return v.visit(this);
    }

    public Expr lengthExpr() {
        return lengthExpr.e;
    }
}

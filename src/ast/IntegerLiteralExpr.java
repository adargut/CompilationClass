package ast;

import visitor.Visitor;

import javax.xml.bind.annotation.XmlElement;

public class IntegerLiteralExpr extends Expr {
    @XmlElement(required = true)
    private int num;

    // for deserialization only!
    public IntegerLiteralExpr() {
    }

    public IntegerLiteralExpr(int num) {
        this.num = num;
    }

    @Override
    public String accept(Visitor v) {
        return v.visit(this);
    }

    public int num() {
        return num;
    }
}

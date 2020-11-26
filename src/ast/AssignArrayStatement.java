package ast;

import visitor.Visitor;

import javax.xml.bind.annotation.XmlElement;

public class AssignArrayStatement extends Statement {
    @XmlElement(required = true)
    private String lv;
    @XmlElement(required = true)
    private ExprWrapper index;
    @XmlElement(required = true)
    private ExprWrapper rv;

    // for deserialization only!
    public AssignArrayStatement() {
    }

    public AssignArrayStatement(String lv, Expr index, Expr rv) {
        this.lv = lv;
        this.index = new ExprWrapper(index);
        this.rv = new ExprWrapper(rv);
    }

    @Override
    public String accept(Visitor v) {
        return v.visit(this);
    }

    public String lv() {
        return lv;
    }

    public void setLv(String lv) {
        this.lv = lv;
    }

    public Expr index() {
        return index.e;
    }

    public Expr rv() {
        return rv.e;
    }
}

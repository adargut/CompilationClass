package ast;

import visitor.Visitor;

import javax.xml.bind.annotation.XmlElement;

public class SysoutStatement extends Statement {
    @XmlElement(required = true)
    private ExprWrapper arg;

    // for deserialization only!
    public SysoutStatement() {
    }

    public SysoutStatement(Expr arg) {
        this.arg = new ExprWrapper(arg);
    }

    @Override
    public String accept(Visitor v) {
        return v.visit(this);
    }

    public Expr arg() {
        return arg.e;
    }
}

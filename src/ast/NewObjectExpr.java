package ast;

import visitor.Visitor;

import javax.xml.bind.annotation.XmlElement;

public class NewObjectExpr extends Expr {
    @XmlElement(required = true)
    private String classId;

    // for deserialization only!
    public NewObjectExpr() {
    }

    public NewObjectExpr(String classId) {
        this.classId = classId;
    }

    @Override
    public String accept(Visitor v) {
        return v.visit(this);
    }

    public String classId() {
        return classId;
    }
}

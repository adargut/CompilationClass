package ast;

import visitor.Visitor;

import javax.xml.bind.annotation.XmlElement;

public class RefType extends AstType {
    @XmlElement(required = true)
    private String id;

    // for deserialization only!
    public RefType() {
    }

    public RefType(String id) {
        super();
        this.setId(id);
    }

    @Override
    public String accept(Visitor v) {
        return v.visit(this);
    }

    public String id() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

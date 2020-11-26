package ast;

import visitor.Visitor;

import javax.xml.bind.annotation.*;
import java.util.List;

public class ClassDecl extends AstNode {
    @XmlElement(required = true)
    private String name;

    @XmlElement(required = false)
    private String superName;

    @XmlElementWrapper(name="fields", required=true)
    @XmlElement(name="field")
    private List<VarDecl> fields;

    @XmlElementWrapper(name="methoddecls", required=true)
    @XmlElement(name="methoddecl")
    private List<MethodDecl> methoddecls;

    // for deserialization only!
    public ClassDecl() {
    }

    public ClassDecl(String name, String superName, List<VarDecl> fields, List<MethodDecl> methoddecls) {
        super();
        this.name = name;
        this.superName = superName;
        this.fields = fields;
        this.methoddecls = methoddecls;
    }

    public String accept(Visitor v) {
        return v.visit(this);
    }

    public String name() {
        return name;
    }

    public String superName() {
        return superName;
    }

    public List<VarDecl> fields() {
        return fields;
    }

    public List<MethodDecl> methoddecls() {
        return methoddecls;
    }
}

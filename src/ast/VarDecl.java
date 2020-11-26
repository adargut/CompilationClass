package ast;

import visitor.Visitor;

public class VarDecl extends VariableIntroduction {

    // for deserialization only!
    public VarDecl() {
    }

    public VarDecl(AstType type, String name, Integer lineNumber) {
        // lineNumber = null means it won't be marshaled to the XML
        super(type, name, lineNumber);
    }

    @Override
    public String accept(Visitor v) {
        return v.visit(this);
    }
}

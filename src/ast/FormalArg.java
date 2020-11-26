package ast;

import visitor.Visitor;

public class FormalArg extends VariableIntroduction {

    // for deserialization only!
    public FormalArg() {
    }

    public FormalArg(AstType type, String name, Integer lineNumber) {
        // lineNumber = null means it won't be marshaled to the XML
        super(type, name, lineNumber);
    }

    @Override
    public String accept(Visitor v) {
        return v.visit(this);
    }
}

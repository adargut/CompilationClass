package ast;

import visitor.Visitor;

public class BoolAstType extends AstType {

    public BoolAstType() {
    }

    @Override
    public String accept(Visitor v) {
        return v.visit(this);
    }
}


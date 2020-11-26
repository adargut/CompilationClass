package ast;

import visitor.Visitor;

public class IntAstType extends AstType {

    public IntAstType() {
    }

    @Override
    public String accept(Visitor v) {
        return v.visit(this);
    }
}

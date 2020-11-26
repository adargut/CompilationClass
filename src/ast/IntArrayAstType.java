package ast;

import visitor.Visitor;

public class IntArrayAstType extends AstType {

    public IntArrayAstType() {
    }

    @Override
    public String accept(Visitor v) {
        return v.visit(this);
    }
}

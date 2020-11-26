package ast;

import visitor.Visitor;

import javax.xml.bind.annotation.XmlElement;

public class WhileStatement extends Statement {
    @XmlElement(required = true)
    private ExprWrapper cond;

    @XmlElement(required = true)
    private StatementWrapper body;

    // for deserialization only!
    public WhileStatement() {
    }

    public WhileStatement(Expr cond, Statement body) {
        this.cond = new ExprWrapper(cond);
        this.body = new StatementWrapper(body);
    }

    @Override
    public String accept(Visitor v) {
        return v.visit(this);
    }

    public Expr cond() {
        return cond.e;
    }

    public Statement body() {
        return body.s;
    }
}

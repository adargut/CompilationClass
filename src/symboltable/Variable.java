package symboltable;


import ast.AstType;

public class Variable {
    private Boolean isParam;
    private Boolean isField;
    private Boolean isLocalVariable;
    private String symbol;
    private Integer lineNumber;
    private AstType type;

    public Variable(String symbol, AstType type, Integer lineNumber) {
        this.symbol = symbol;
        this.lineNumber = lineNumber;
        this.type = type;
        this.isLocalVariable = true;
        this.isField = false;
        this.isParam = false;
    }

    public Variable(String symbol, AstType type, Integer lineNumber, Boolean isField, Boolean isLocalVariable, Boolean isParam) {
        this.symbol = symbol;
        this.lineNumber = lineNumber;
        this.type = type;
        this.isLocalVariable = isLocalVariable;
        this.isField = isField;
        this.isParam = isParam;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }
    public AstType getType() {
        return type;
    }

    public void setType(AstType type) {
        this.type = type;
    }

    public Boolean getParam() {
        return isParam;
    }

    public Boolean getField() {
        return isField;
    }

    public Boolean getLocalVariable() {
        return isLocalVariable;
    }
}

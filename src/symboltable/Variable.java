package symboltable;


import ast.AstType;

public class Variable {
    private Boolean shouldRename;
    private String symbol;
    private Integer lineNumber;
    private AstType type;

    public Variable(String symbol, AstType type, Integer lineNumber) {
        this.symbol = symbol;
        this.lineNumber = lineNumber;
        this.type = type;
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

    public Boolean getShouldRename() {
        return shouldRename;
    }

    public void setShouldRename(Boolean shouldRename) {
        this.shouldRename = shouldRename;
    }

}

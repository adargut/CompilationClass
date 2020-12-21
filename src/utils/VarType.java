package utils;

public enum VarType {
    INT("int"),
    BOOL("boolean"),
    INT_ARRAY("int[]"),
    REF("ref");

    private final String type;

    // getter method
    public String getType() {
        return this.type;
    }

    private VarType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }
}

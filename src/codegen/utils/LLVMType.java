package codegen.utils;

public enum LLVMType {
    i1("i1"),
    i8("i8"),
    i8ptr("i8*"),
    i32("i32"),
    i32ptr("i32*"),
    static_array("static_array");

    private String representation;

    // getter method
    public String getRepresentation() {
        return this.representation;
    }

    private LLVMType(String representation) {
        this.representation = representation;
    }

    @Override
    public String toString() {
        return this.representation;
    }
}

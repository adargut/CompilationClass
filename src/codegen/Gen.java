package codegen;

public interface Gen<T> {
    /** Generate a piece of code for llvm. */
    public String generate(T...args);
}

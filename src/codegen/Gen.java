package codegen;

public interface Gen<T> {
    public String generate(T...args);
}

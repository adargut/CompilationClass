package tests;

import ast.RefType;
import codegen.Alloca;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import symboltable.Variable;

class AllocaTest {
    @Test
    void testAlloca() {
        Variable v = new Variable("x", new RefType(), 10);
        var alloca = Alloca.getInstance();
        alloca.setVariable(v);
        var actual = alloca.generate();
        var expected = "%x = alloca i32";
        Assertions.assertEquals(expected, actual);
    }
}
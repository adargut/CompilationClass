package utils;

import ast.AstNode;
import ast.AstType;
import java.util.List;
import java.util.Map;

public interface Mapper {
    /**
     * Reset all previous mapping
     *
     */
    public void resetMapping();

    /**
     * Return the type of the variable at given line.
     *
     * @param varName - name of the variable of interest.
     * @return type of variable. // todo check if static or dynamic:
     */
    public String getMapping(String varName);

    /**
     * Create a new mapping for an integer and a line.
     *
     * @param varName - name of the variable of interest.
     * @param varType - the type of the variable
     */
    public void createMapping(String varName, String varType);
}

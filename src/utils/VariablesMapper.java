package utils;

import java.util.Map;
import ast.AstNode;

public class VariablesMapper implements Mapper {
    Map<String, String> variableMap;

    /**
     * @{inheritDoc}
     *
     */
    @Override
    public void resetMapping() {
        variableMap.clear();
    }

    /**
     * @{inheritDoc}
     * Implemented using binary search on the list associated with the given variable.
     *
     */
    @Override
    public String getMapping(String varName) {
        return variableMap.get(varName);
    }

    /**
     * @{inheritDoc}
     * Implemented by mapping a variable to a list of <Integer, String> pairs denoting the type it has at given line.
     * For example: X x; Y y; x = y; would generate {x: [X, 1], [Y, 2], y: [Y, 1]}
     *
     */
    @Override
    public void createMapping(String varName, String varType) {
        variableMap.put(varName, varType);
    }
}
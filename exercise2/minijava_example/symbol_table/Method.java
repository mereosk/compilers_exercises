package symbol_table;

import java.util.*;

public class Method {

    private String name;
    private String type;
    private int offset;

    private List<Variable> params, vars;

    public Method(String methodName, String methodType) {
        this.name = methodName;
        this.type = methodType;
        this.params = new ArrayList<>();
        this.vars = new ArrayList<>();
    }
    
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public List<Variable> getParams() {
        return params;
    }

    public List<Variable> getVariables() {
        return vars;
    }

    public int getNumParams(){
        return params.size();
    }

    public int getOffset() {
        return this.offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void insertParameter(Variable param) {
        // Insert the parameter in the list
        params.add(param);
    }

    public void insertVariable(Variable var) {
        // Insert the variable in the list
        vars.add(var);
    }

    // This function prints the method for debugging purposes
    public void printMethod() {

    }
}

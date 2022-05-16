package symbol_table;

import java.util.*;

public class Method {

    private String name;
    private String type;
    private int offset;
    private boolean overridden;

    private List<Variable> params, vars;

    public Method(String methodName, String methodType) {
        this.name = methodName;
        this.type = methodType;
        this.params = new ArrayList<>();
        this.vars = new ArrayList<>();
        this.overridden = false;
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

    public Variable getVar(String varName) {
        // First search the variables
        for(Iterator<Variable> iter = vars.iterator(); iter.hasNext(); ) {
            Variable var = iter.next();

            if(var.getName().equals(varName))
                return var;
        }

        // Second search the arguments
        for(Iterator<Variable> iter = params.iterator(); iter.hasNext(); ) {
            Variable param = iter.next();

            if(param.getName().equals(varName))
                return param;
        }

        // Variable doesn't exist in this method
        return null;
    }

    public int getNumParams(){
        return params.size();
    }

    public String getArgumentType(int index) {
        Variable var = params.get(index);
        return var.getType();
    }

    public int getOffset() {
        return this.offset;
    }

    public boolean isOverridden() {
        return overridden;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setOverridden() {
        this.overridden = true;
    }

    public void insertParameter(Variable param) throws Exception {
        String varName = param.getName();
        for(Variable tempVar: params) {
            String tempName = tempVar.getName();
            if(tempName.equals(varName))
                throw new Exception("Variable " + varName + " is already defined in method " + getName());
        }

        // Insert the parameter in the list
        params.add(param);
    }

    public void insertVariable(Variable var) throws Exception {
        String varName = var.getName();
        for(Variable tempVar: vars) {
            String tempName = tempVar.getName();
            if(tempName.equals(varName))
                throw new Exception("Variable " + varName + " is already defined in method " + getName());
        }

        for(Variable tempVar: params) {
            String tempName = tempVar.getName();
            if(tempName.equals(varName))
                throw new Exception("Variable " + varName + " is already defined in method " + getName());
        }
        // if(vars.contains(var) || params.contains(var))
            // throw new Exception("Variable " + varName + " is already defined in method " + getName());

        // Insert the variable in the list
        vars.add(var);
    }

    public void printOffset() {
        if(!overridden)
            System.out.println(name+" : " + offset);     
    }

    // This function prints the method for debugging purposes
    public void printMethod() {
        System.out.println("\t\t\tParameters:");
        for(Variable param: params) {
            param.printVariable();
        }
        System.out.println("\t\t\tVariables:");
        for(Variable var: vars) {
            var.printVariable();
        }
    }
}

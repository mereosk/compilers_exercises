package symbol_table;

import java.util.*;

public class Class {

    private String name;
    private int varOffset;
    private int methodOffset;
    private Map<String, Method> methods;
    private Map<String, Variable> variables;

    public Class(String className) {
        this.name = className;
        this.varOffset = 0;
        this.methodOffset = 0;
        this.methods = new HashMap<String, Method>();
        this.variables = new HashMap<String, Variable>();
    }

    public String getName() {
        return name;
    }

    // If the method exists, return it if not return null
    public Method getMethod(String methodId) {
        if(methods.containsKey(methodId))
            return methods.get(methodId);
        return null;
    }

    // If the variable exists, return it if not return null
    public Variable getVariable(String varId) {
        if(variables.containsKey(varId))
            return variables.get(varId);
        return null;
    }

    public void insertVariable(Variable var) {
        // Insert the var in the map
        variables.put(var.getName(), var);

        // Set the variable's offset
        var.setOffset(this.varOffset);

        // Increament the varOffset by the type of the variable
        switch (var.getType()) {
            case "int":         // Int thus increment by 4
                varOffset += 4;
            case "boolean":     // Boolean thus increment by 1
                varOffset += 1;
            default:            // Arrays are pointers thus increment by 8
                varOffset += 8;
        }
    }

    public void insertMethod(Method method) {
        // Insert the method in the map
        methods.put(method.getName(), method);

        // This is wrong, I don't check the superclass yet
        method.setOffset(methodOffset);
        // Methods are pointers thus increment by 8
        methodOffset += 8;
    }

    // This function prints the class for debugging purposes
    public void printClass() {
        // for (TypeKey name: example.keySet()) {
        //     String key = name.toString();
        //     String value = example.get(name).toString();
        //     System.out.println(key + " " + value);
        // }
    }
}

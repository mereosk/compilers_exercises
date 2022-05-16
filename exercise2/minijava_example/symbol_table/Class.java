package symbol_table;

import java.util.*;

public class Class {

    private String name;
    private int varOffset;
    private int methodOffset;
    private Map<String, Method> methods;
    private Map<String, Variable> variables;
    private String parentClassName;
    private SymbolTable parent;

    public Class(String className, int varOffset, int methodOffset, String parentClassName, SymbolTable parent) {
        this.name = className;
        this.varOffset = varOffset;
        this.methodOffset = methodOffset;
        this.methods = new LinkedHashMap<String, Method>();
        this.variables = new LinkedHashMap<String, Variable>();
        this.parentClassName = parentClassName;
        this.parent = parent;
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

    public int getVarOffset() {
        return varOffset;
    }

    public int getMethodOffset() {
        return methodOffset;
    }

    public String getparentClassName() {
        return parentClassName;
    }

    public void insertVariable(Variable var) throws Exception {
        String varName = var.getName();
        if(variables.containsKey(varName))
            throw new Exception("Variable " + varName + " is already defined in class " + getName());

        // Insert the var in the map
        variables.put(var.getName(), var);

        // Set the variable's offset
        // Variable tempVar = variables[var.getName()];
        var.setOffset(this.varOffset);

        // Increment the offset
        if (var.getType().equals("boolean"))
            varOffset += 1;                     // Boolean thus increment by 1
		else if (var.getType().equals("int"))   
            varOffset += 4;                     // Int thus increment by 4
		else
            varOffset += 8;                     // Arrays or Objects are pointers thus increment by 8
    }

    public void insertMethod(Method method) throws Exception {
        String methodName = method.getName();
        if(methods.containsKey(methodName))
            throw new Exception("Method " + methodName + " is already defined in class " + getName());

        // Insert the method in the map
        methods.put(method.getName(), method);

        if(parentClassName != null) {
            
            Class parentClass = parent.getClass(parentClassName);
            Method overriddenMethod = parentClass.getMethod(method.getName());
            
            if(overriddenMethod == null) {
                method.setOffset(methodOffset);
                // Methods are pointers thus increment the method offset by 8
                methodOffset += 8;
            }
            else {
                int overriddenMethodOffset = overriddenMethod.getOffset();
                method.setOffset(overriddenMethodOffset);
                method.setOverridden();
            }
        }
        else {
            method.setOffset(methodOffset);
            // Methods are pointers thus increment the method offset by 8
            methodOffset += 8;
        }
    }

    public void printOffset() {
        System.out.println("-----------Class " + name + "-----------");
        // Loop through all the fields and print their offset
        System.out.println("--Variables---");
        for(String fieldName: variables.keySet()) {
            Variable currentField = variables.get(fieldName);
            System.out.print(getName() + ".");
            currentField.printOffset();
        }
        // Loop through all the methods and print their offset
        System.out.println("---Methods---");
        for(String methodName: methods.keySet()) {
            Method currentMethod = methods.get(methodName);
            if(!currentMethod.isOverridden())
                System.out.print(getName() + ".");
            currentMethod.printOffset();
        }
        System.out.println();
    }

    // This function prints the class for debugging purposes
    public void printClass() {
        System.out.println("\tFields:");
        for(String fieldName: variables.keySet()) {
            Variable varToBePrinted = variables.get(fieldName);
            varToBePrinted.printVariable();
        }
        System.out.println("\tMethods:");
        for(String methodName: methods.keySet()) {
            System.out.println("\t\tMethod: "+methodName);
            Method methodToBePrinted = methods.get(methodName);
            methodToBePrinted.printMethod();
        }
    }
}

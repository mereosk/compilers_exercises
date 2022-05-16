package symbol_table;

import java.util.*;

public class SymbolTable {

    private Map<String, Class> classes;
    private Map<String, String> superClasses;

    public SymbolTable() {
        // Initialize the values in the constructor
        this.classes = new HashMap<>();
        this.superClasses = new HashMap<>();

        System.out.println("Im in the Symbol table constructor!");
    }

    public Class getClass(String className) {
        if(classes.containsKey(className))
            return classes.get(className);
        return null;
    }

    public void insertClass(String className, String superClassName) {

        int varOffset = 0, methodOffset = 0;
        String parentClass = null;

        // If superclass exists insert it in the corresponting map as a value
        // also pass the right offsets to the class which is the offsets of
        // the superclass
        if(superClassName!=null) {
            superClasses.put(className, superClassName);

            Class currentSuperClass = getClass(superClassName);

            varOffset = currentSuperClass.getVarOffset();
            methodOffset = currentSuperClass.getMethodOffset();

            parentClass = superClassName;
        }

        Class tempClass  = new Class(className, varOffset, methodOffset, parentClass, this);
        classes.put(className, tempClass);
    }

    public Variable findVariable(String nameOfId, String[] scope) {

        System.out.println(nameOfId);
        String methodName = scope[1];
        String className = scope[0];

        // Get the current class
        Class currentClass = getClass(className);

        // Search method's args and variables
        if(methodName != null) {
            Method currentMethod = currentClass.getMethod(methodName);
            Variable var = currentMethod.getVar(nameOfId);

            if(var!=null)
                return var;
        }

        // Search the class fields for the variable
        Variable var = currentClass.getVariable(nameOfId);

        if(var != null)
            return var;

        // If we didn't find it yet search the superclasses for its existance
        // of course if they exist
        String parentClass = currentClass.getparentClassName();
        if(parentClass == null)
            return null;
        scope[0] = parentClass;
        scope[1] = null;
        return findVariable(nameOfId, scope);
    }

    public void printOffset() {
        System.out.println("\n--------------------------------");
        System.out.println("Printing offsets\n");
        // Loop through all the classes
        for(String className: classes.keySet()) {
            Class currentClass = classes.get(className);
            currentClass.printOffset();
        }
    }

    // This function prints the symbol table for debugging purposes
    public void printSymbolTable() {
        System.out.println("\n--------------------------------");
        System.out.println("Printing the symbolTable");
        System.out.println("--------------------------------");
        for(String className: classes.keySet()) {
            System.out.println("Class: "+className);
            Class classToBePrinted = classes.get(className);
            classToBePrinted.printClass();
        }
    }

}

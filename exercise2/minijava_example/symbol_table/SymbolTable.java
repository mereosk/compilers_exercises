package symbol_table;

import java.util.*;

public class SymbolTable {

    private Map<String, Class> classes;
    private Map<String, String> superClasses;

    public SymbolTable() {
        // Initialize the values in the constructor
        this.classes = new HashMap<>();
        this.superClasses = new HashMap<>();

    }

    public Class getClass(String className) {
        if(classes.containsKey(className))
            return classes.get(className);
        return null;
    }

    public void insertClass(String className, String superClassName) throws Exception {

        int varOffset = 0, methodOffset = 0;
        String parentClass = null;

        if(classes.containsKey(className))
            throw new Exception("Class " + className + " is already declared");

        // If superclass exists insert it in the corresponting map as a value
        // also pass the right offsets to the class which is the offsets of
        // the superclass
        if(superClassName!=null) {
            // Classes parents are declared befor classes that have parents
            if(classes.containsKey(superClassName) == false)
                throw new Exception("Wrong super class " + superClassName + ", it is not declared");

            superClasses.put(className, superClassName);

            Class currentSuperClass = getClass(superClassName);

            varOffset = currentSuperClass.getVarOffset();
            methodOffset = currentSuperClass.getMethodOffset();

            parentClass = superClassName;
        }

        Class tempClass  = new Class(className, varOffset, methodOffset, parentClass, this);
        classes.put(className, tempClass);
    }

    public Method findMethod(String nameOfMethod, String objCaller) {

        // Get the current class
        Class currentClass = getClass(objCaller);

        Method currentMethod = currentClass.getMethod(nameOfMethod);

        if(currentMethod != null)
            return currentMethod;

        // If we didn't find it yet search the superclasses for its existance
        // of course if they exist
        String parentClass = currentClass.getparentClassName();
        if(parentClass == null)
            return null;
        objCaller = parentClass;
        return findMethod(nameOfMethod, objCaller);
    }

    public Variable findVariable(String nameOfId, String[] scope) {
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
        String[] newScope = new String[2];
        newScope[0] = parentClass;
        newScope[1] = null;
        return findVariable(nameOfId, newScope);
    }

    public boolean isParentClass(String candParent, String child) {
        String superClass = superClasses.get(child);

        if(superClass == null) {        // There are no superclass for the class
            return false;
        }
        else if(superClass.equals(candParent))  // There is a superclass and its the same as the candidate parent
            return true;
        else {                                  // Check if the superclass found is the child of the candidate parent
            return isParentClass(candParent, superClass);
        }
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

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

        Class tempClass  = new Class(className);
        classes.put(className, tempClass);

        // If superclass exists insert it in the corresponting map
        if(superClassName!=null) {
            superClasses.put(className, superClassName);
        }
    }

    // This function prints the symbol table for debugging purposes
    public void printSymbolTable() {

    }

}

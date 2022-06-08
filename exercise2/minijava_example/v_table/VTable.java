package v_table;

import java.util.*;
import symbol_table.*;

public class VTable {

    // This is a map that has as a key the name of the class and the 
    // values are maps whose keys are the method Class itself and their
    // are the owners of each method
    private Map<String, Map<Method, String>> classMapMethods;

    public VTable() {
        // Initialize the values in the constructor
        this.classMapMethods = new LinkedHashMap<>();
    }

    // This function returns the methods of the className's vTable
    // or null if there are no methods in the class
    public Map<Method, String> getMethods(String className) {
        Map<Method, String> methods = classMapMethods.get(className);

        return methods == null ? null : methods;
    }

    public void insertClass(String className) {
        classMapMethods.put(className, new LinkedHashMap<>());
    }
    
    public void insertMethod(Method method, String owner, String className) {
        // Get the vTable of class className
        Map<Method, String> vTableMethods = classMapMethods.get(className);
        
        vTableMethods.put(method, owner);
    }
}

package v_table;

import java.io.Writer;
import java.util.*;
import symbol_table.*;

public class VTable {

    // This is a map that has as a key the name of the class and the 
    // values are maps whose keys are the method Class itself and their
    // are the owners of each method
    private Map<String, Map<Method, String>> classMapMethods;
    // For each class save number of methods
    private Map<String, Integer> methodCounter;

    public VTable() {
        // Initialize the values in the constructor
        this.classMapMethods = new LinkedHashMap<>();
        this.methodCounter = new LinkedHashMap<>();
    }

    // This function returns the methods of the className's vTable
    // or null if there are no methods in the class
    public Map<Method, String> getMethods(String className) {
        Map<Method, String> methods = classMapMethods.get(className);

        return methods == null ? null : methods;
    }

    public void insertClass(String className) {
        classMapMethods.put(className, new LinkedHashMap<>());
        // Initialize the method counter
        methodCounter.put(className, 0);
    }
    
    public void insertMethod(Method method, String owner, String className) {
        // Get the vTable of class className
        Map<Method, String> vTableMethods = classMapMethods.get(className);
        
        vTableMethods.put(method, owner);
        // Increase the method counter
        incrementMethodCounter(methodCounter, className);
    }

    public void incrementMethodCounter(Map<String, Integer> map, String key) {
        methodCounter.merge(key, 1, Integer::sum);
    }

    public void printFunctions(){  // only for debuggind
        for(String className: classMapMethods.keySet()){
            System.out.println("Class: "+className+" - Functions: ");
            Map<Method, String> vTableFuncs = classMapMethods.get(className);
            for(Method method: vTableFuncs.keySet()) {
                System.out.println("Method: " + method.getName()+" - owner: " + vTableFuncs.get(method));
            }
        }
    }

    public void emitInfo(Writer writer, SymbolTable sTable) throws Exception{
        // Emit the first informations about the classes and methods, in the ll file

        // Loop through all the classes
        
        Set<String> classes = sTable.getClasses();
        Iterator<String> classIterator = classes.iterator();

        while(classIterator.hasNext()) {
            String className = classIterator.next();
            writer.write("@." + className + "_vtable = global [");
            
            writer.write(methodCounter.get(className) + " x i8*] []");
            writer.write("\n");
         }
        
    }
}

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
            
            // Get the methods
            Map<Method, String> methods =  getMethods(className);
            // Get the number of methods
            int methodCounter = methods.size();
            writer.write(methodCounter + " x i8*] [");

            // Iterate through all the methods and print some infos
            for (Map.Entry<Method, String> entry : methods.entrySet()) {
                // Emitting: i8* bitcast ({retValue} (i8*, {parameters})* @{ownerClass}.{methodName} to i8*)
                Method currentMethod = entry.getKey();
                String owner = entry.getValue();
                String methodType = currentMethod.getType();
                String methodName = currentMethod.getName();

                // First parameter is i8* which is this
                writer.write("i8* bitcast (" + convertTypeLLVM(methodType) +  " (i8*");

                // Emit parameters
                List<Variable> params = currentMethod.getParams();
                for (int i = 0; i < params.size(); i++) {
                    String paramType = params.get(i).getType();
                    writer.write("," + convertTypeLLVM(paramType));
                }
                // Emit )* @{ownerClass}.{methodName} to i8*), 
                String lastChar;
                methodCounter--;
                if(methodCounter > 0)
                    lastChar = ", ";
                else
                    lastChar = "";
                writer.write(")* @" + owner + "." + methodName + " to i8*)" + lastChar);

            }
            writer.write("]\n");
         }

         // Nextly print the boilerplate
         writer.write("\n\ndeclare i8* @calloc(i32, i32)\ndeclare i32 @printf(i8*, ...)\n"
         + "declare void @exit(i32)\n\n@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n"
         + "@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\ndefine void @print_int(i32 %i) {\n"
         + "    %_str = bitcast [4 x i8]* @_cint to i8*\n    call i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n"
         + "    ret void\n}\n\ndefine void @throw_oob() {\n    %_str = bitcast [15 x i8]* @_cOOB to i8*\n"
         + "    call i32 (i8*, ...) @printf(i8* %_str)\n    call void @exit(i32 1)\n    ret void\n}\n\n");
    }

    // Convert a java type to LLVM type
    public String convertTypeLLVM(String type) {
        if(type.equals("int"))
            return "i32";
        else if(type.equals("boolean"))
            return "i1";
        else if(type.equals("int[]") || type.equals("boolean[]"))
            return "i32*";
        else {  // The default class type
            return "i8*";
        }
    }
}

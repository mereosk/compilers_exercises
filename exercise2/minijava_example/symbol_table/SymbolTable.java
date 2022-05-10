package symbol_table;

import java.util.*;

public class SymbolTable {
    Map<String, Class> classes;
    Map<String, String> superClasses;

    public SymbolTable() {
        // Initialize the values in the constructor
        this.classes = new HashMap<>();
        this.superClasses = new HashMap<>();

        System.out.println("Im in the Symbol table constructor!");
    }
}

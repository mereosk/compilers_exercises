import my_visitors.*;
import symbol_table.*;
import syntaxtree.*;
import visitor.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws Exception {
        if(args.length < 1){
            System.err.println("Usage: java Main <file1> <file2> ... <fileN>");
            System.exit(1);
        }

        // Loop through all the arguments
        for(String arg: args) {
            FileInputStream fis = null;
            try{
                fis = new FileInputStream(arg);
                System.err.println("-------------------------------");
                System.err.println("Parsing the program " + arg);
                MiniJavaParser parser = new MiniJavaParser(fis);

                Goal root = parser.Goal();

                System.err.println("Program parsed successfully.");

                SymbolTable symTable = new SymbolTable();

                STFillVisitor smFilling = new STFillVisitor(symTable);
                root.accept(smFilling, null);

                // symTable.printSymbolTable();
                symTable.printOffset();

                TCVisitor typeChecking = new TCVisitor(symTable);
                root.accept(typeChecking, null);

                // symTable.printSymbolTable();
                // symTable.printOffset();
            }   // Catch the parse exception
            catch(ParseException ex){
                System.out.println(ex.getMessage());
            }   // Catch the file exception
            catch(FileNotFoundException ex){
                System.err.println(ex.getMessage());
            }
            catch(Exception e) {
                System.err.println("Error: " + e.getMessage() + "\n");
            }
            finally{
                try{
                    if(fis != null) fis.close();
                }
                catch(IOException ex){
                    System.err.println(ex.getMessage());
                }
            }
        }
    }
}
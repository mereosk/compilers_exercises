import my_visitors.*;
import symbol_table.*;
import v_table.*;
import syntaxtree.*;
import visitor.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class Main {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_GREEN = "\u001B[32m";

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
                // symTable.printOffset();
                // System.out.println("--------------------------------");
                // System.out.println(ANSI_GREEN + "Starting type checking the program" + ANSI_RESET);

                // TCVisitor typeChecking = new TCVisitor(symTable);
                // root.accept(typeChecking, null);

                // System.out.println(ANSI_GREEN + "Finished, everything went smoothly\n" + ANSI_RESET);

                // Now run the vistitor that fills the v_table
                VTable vTable = new VTable();
                FillVTableVisitor vtFilling = new FillVTableVisitor(vTable, symTable);
                root.accept(vtFilling, null);
                System.out.println("--------------------------------");
                System.out.println(ANSI_GREEN + "Filled the vTable successfully" + ANSI_RESET);

            }   // Catch the parse exception
            catch(ParseException ex){
                System.out.println(ex.getMessage());
            }   // Catch the file exception
            catch(FileNotFoundException ex){
                System.err.println(ex.getMessage());
            }
            catch(Exception e) {
                System.err.println(ANSI_RED + "\nError: " + e.getMessage() + "\n" + ANSI_RESET);
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
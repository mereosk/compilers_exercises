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

                MyVisitor eval = new MyVisitor();
                root.accept(eval, null);
            }   // Catch the parse exception
            catch(ParseException ex){
                System.out.println(ex.getMessage());
            }   // Catch the file exception
            catch(FileNotFoundException ex){
                System.err.println(ex.getMessage());
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
package my_visitors;

import symbol_table.*;
import v_table.*;
import syntaxtree.*;
import visitor.GJDepthFirst;
import java.io.Writer;


// This class is used in order to construct the reginsters in the
// section of the visit and pass them as return values
class Register {
    private String name;
    private String type;

    public Register(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        return name.equals(((Register) obj).name) && type.equals(((Register) obj).type);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}

public class LLVMVisitor extends GJDepthFirst<Register, Void> {
    private SymbolTable symTable;
    private String[] currentScope;
    private VTable vTable;
    private int regCounter = 0;
    private int labelCounter = 0;
    private Writer writer;

    public LLVMVisitor(SymbolTable symTable, VTable vTable, Writer writer) {
        this.symTable = symTable;
        this.currentScope = new String[2];
        this.vTable = vTable;
        this.writer = writer;
    }

    public String[] getScope() {
        return currentScope;
    }

    public String getClassScope() {
        return currentScope[0];
    }

    public String getMethodScope() {
        return currentScope[1];
    }

    public void setScopeClass(String className) {
        this.currentScope[0] = className;
    }

    public void setScopeMethod(String methodName) {
        this.currentScope[1] = methodName;
    }

    public String getNewRegister() {
        return "%-" + ++regCounter;
    }

    public String getNewLabel() {
        return "label-" + ++labelCounter;
    }

    public void emitLLVM(String strToWrite) throws Exception {
        writer.write(strToWrite);
    }

    // Convert a java type to LLVM type
    public String converTypeLLVM(String type) {
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

    // This function initialize the variables. If var is int or bool
    // initial value is 0, if it is a i32*/i8* init value is null
    String initializeValue(String type) {
        if(type.equals("i32") || type.equals("i1"))
            return "0";
        else 
            return "null";
    }

    /**
        * f0 -> "class"
        * f1 -> Identifier()
        * f2 -> "{"
        * f3 -> "public"
        * f4 -> "static"
        * f5 -> "void"
        * f6 -> "main"
        * f7 -> "("
        * f8 -> "String"
        * f9 -> "["
        * f10 -> "]"
        * f11 -> Identifier()
        * f12 -> ")"
        * f13 -> "{"
        * f14 -> ( VarDeclaration() )*
        * f15 -> ( Statement() )*
        * f16 -> "}"
        * f17 -> "}"
    */
    @Override
    public Register visit(MainClass n, Void argu) throws Exception {
        String className = n.f1.f0.tokenImage;

        // Firstly print the information of the classes and functions
        vTable.emitInfo(writer ,symTable);

        emitLLVM("define i32 @main() {\n");

        setScopeClass(className);
        setScopeMethod(null);

        // Code generate for the variabes inside main
        n.f14.accept(this, null);
        // Code generate for the statements inside main
        n.f15.accept(this, null);

        emitLLVM("\n\tret i32 0\n}\n");

        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    @Override
    public Register visit(ClassDeclaration n, Void argu) throws Exception {
        String className = n.f1.f0.tokenImage;

        setScopeClass(className);
        setScopeMethod(null);

        // Code generate for the methods in class
        n.f4.accept(this, null);

        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    @Override
    public Register visit(ClassExtendsDeclaration n, Void argu) throws Exception {
        String className = n.f1.f0.tokenImage;

        setScopeClass(className);
        setScopeMethod(null);

        // Code generate for the methods in class
        n.f6.accept(this, null);

        return null;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    @Override
    public Register visit(MethodDeclaration n, Void argu) throws Exception {
        // String argumentList = n.f4.present() ? n.f4.accept(this, null) : "";
        // // Argument list is a big string with arguments that are in form:
        // // int arg1, int arg2 etc
        // // So split the list in an array
        // String argArray[] = argumentList.split(",");
    

        // Register typeReg = n.f1.accept(this, null);
        // String type = typeReg.getType()
        // String name = n.f2.accept(this, null);


        // // Set the method's name as the scope
        // setScopeMethod(name);

        n.f7.accept(this, null);

        return null;
    }

    // /**
    //  * f0 -> FormalParameter()
    //  * f1 -> FormalParameterTail()
    //  */
    // @Override
    // public Register visit(FormalParameterList n, Void argu) throws Exception {
    //     String ret = n.f0.accept(this, null);

    //     if (n.f1 != null) {
    //         ret += n.f1.accept(this, null);
    //     }

    //     return ret;
    // }

    // /**
    //  * f0 -> FormalParameter()
    //  * f1 -> FormalParameterTail()
    //  */
    // public Register visit(FormalParameterTerm n, Void argu) throws Exception {
    //     return n.f1.accept(this, argu);
    // }

    // /**
    //  * f0 -> ","
    //  * f1 -> FormalParameter()
    //  */
    // @Override
    // public Register visit(FormalParameterTail n, Void argu) throws Exception {
    //     String ret = "";
    //     for ( Node node: n.f0.nodes) {
    //         ret += ", " + node.accept(this, null);
    //     }

    //     return ret;
    // }

    // /**
    //  * f0 -> Type()
    //  * f1 -> Identifier()
    //  */
    // @Override
    // public Register visit(FormalParameter n, Void argu) throws Exception{
    //     String type = n.f0.accept(this, null);
    //     String name = n.f1.accept(this, null);
    //     return type + " " + name;
    // }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    @Override
    public Register visit(VarDeclaration n, Void argu) throws Exception{
        Register typeReg = n.f0.accept(this, null);
        String type = typeReg.getType();

        Register nameReg = n.f1.accept(this, null);
        String name = nameReg.getName();

        String initValue = initializeValue(type);
        // Print the variable declaration in the file
        emitLLVM("\t"+name+" = alloca "+type+"\n");  // Allocate the space in memory for the var
        emitLLVM("\tstore "+type+" "+initValue+", "+type+"* "+name+"\n");  // Store the variable in memory

        return null;
    }

    public Register visit(Type n, Void argu) throws Exception {

		// .which = 0 -> ArrayType
		//        = 1 -> BooleanType
		//        = 2 -> IntegerType
		//        = 3 -> Identifier

        // I add this so that there will be different handling 
        // when type is an object
        if(n.f0.which == 3)
            return new Register(null, "i8*");
        else
            return super.visit(n, null);
	}

    @Override
    public Register visit(IntegerArrayType n, Void argu) {
        return new Register(null, "i32*");
    }

    public Register visit(BooleanArrayType n, Void argu)  {
        return new Register(null, "i32*");
    }

    public Register visit(BooleanType n, Void argu) {
        return new Register(null, "i1");
    }

    public Register visit(IntegerType n, Void argu) {
        return new Register(null, "i32");
    }

    @Override
    public Register visit(Identifier n, Void argu) {
        return new Register(getNewRegister(), n.f0.toString());
    }
}   

package my_visitors;

import symbol_table.Class;
import symbol_table.*;
import v_table.*;
import syntaxtree.*;
import visitor.GJDepthFirst;
import java.io.Writer;
import java.util.*;


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

    public void resetCounterReg() {
        regCounter = 0;
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
        int temp = regCounter;
        regCounter++;
        return "%_" + temp;
    }

    public String getNewLabel(String labelName) {
        int temp = labelCounter;
        labelCounter++;
        return labelName + temp;
    }

    public void emitLLVM(String strToWrite) throws Exception {
        writer.write(strToWrite);
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
        resetCounterReg();
        Register typeReg = n.f1.accept(this, null);
        String type = typeReg.getType();
        String methodName = n.f2.f0.tokenImage;
        
        // The name of the class is in the scope
        String currentClassName = getClassScope();

        // Get the method from the symbol table
        Class currentClass = symTable.getClass(currentClassName);
        Method currentMethod = currentClass.getMethod(methodName);

        emitLLVM("define " + type + " @" + currentClassName + "." + methodName + "(i8* %this");

        // Emit the parameters
        List<Variable> params = currentMethod.getParams();
        for (int i = 0; i < params.size(); i++) {
            String paramType = params.get(i).getType();
            String paramTypeLLVM = vTable.convertTypeLLVM(paramType);
            String paramName = params.get(i).getName();
            emitLLVM(", " + paramTypeLLVM + " %." + paramName);
        }
        emitLLVM(") {\n");

        // Now allocate space for the params and store them to memory
        for (int i = 0; i < params.size(); i++) {
            //%num = alloca i32 store i32 %.num, i32* %num
            String paramType = params.get(i).getType();
            String paramTypeLLVM = vTable.convertTypeLLVM(paramType);
            String paramName = params.get(i).getName();
            emitLLVM("\t%" + paramName + " = alloca " + paramTypeLLVM +"\n");
            emitLLVM("\tstore " + paramTypeLLVM + " %." + paramName + ", " + paramTypeLLVM + "* %" + paramName + "\n");
        }

        // Set the method's name as the scope
        setScopeMethod(methodName);

        n.f7.accept(this, null);
        n.f8.accept(this, null);

        // Load and return the return value
        Register returnRegister = n.f10.accept(this, null);
        
        String retName = returnRegister.getName();
        String retType = returnRegister.getType();

        emitLLVM("\n\tret " + type + " "  + retName);

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

        String name = n.f1.f0.tokenImage;

        String initValue = initializeValue(type);
        // Print the variable declaration in the file
        emitLLVM("\t%"+name+" = alloca "+type+"\n");  // Allocate the space in memory for the var
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

    /**
        * f0 -> Identifier()
        * f1 -> "="
        * f2 -> Expression()
        * f3 -> ";"
    */
    public Register visit(AssignmentStatement n, Void argu) throws Exception {
        Register leftOperant = n.f0.accept(this, null);
        Register rightOperant = n.f2.accept(this, null);

        // Get the types and names
        String leftType = leftOperant.getType();
        String leftName = leftOperant.getName();
        String rightType = rightOperant.getType();
        String rightName = rightOperant.getName();

        // store i32 1, i32* %num_aux
        emitLLVM("\tstore " + rightType + " " + rightName + ", " + leftType + "* " + leftName + "\n");
        
        return null;
    }

    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
    */
    public Register visit(IfStatement n, Void argu) throws Exception {
        Register ifExpr = n.f2.accept(this, null);
        // Get the types and names
        String ifExprType = ifExpr.getType();
        String ifExprName = ifExpr.getName();

        // Initialize the 3 labels
        String labThen = getNewLabel("if");
        String labElse = getNewLabel("if");
        String labEnd = getNewLabel("if");

        // eg. br i1 %_4, label %if0, label %if1
        emitLLVM("\tbr " + ifExprType + " " + ifExprName + ", label %" + labThen + ", label %" + labElse);
        emitLLVM(labThen + ":\n");
        n.f4.accept(this, null);

        // eg. br label %if2
        emitLLVM("\tbr label %" + labElse + "\n");
        emitLLVM(labElse+":\n");
        n.f6.accept(this, null);

        // eg. br label %if2
        emitLLVM("\tbr label %" + labEnd + "\n");
        emitLLVM(labEnd+":\n");

        return null;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
    */
    public Register visit(CompareExpression n, Void argu) throws Exception {
        Register cmp1 = n.f0.accept(this, null);
        Register cmp2 = n.f2.accept(this, null);
        // Get the type
        String type = cmp1.getType();
        // Get the leftish expr's name
        String nameL = cmp1.getName();
        // Get the rightish expr's name
        String nameR = cmp2.getName();

        String newRegister = getNewRegister();

        // %_7 = icmp slt i32 %_3, %_6
        emitLLVM("\t" + newRegister + " = icmp slt " + type + " " + nameL + ", " + nameR + "\n\n");

        return new Register(newRegister, "i1");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
    */
    public Register visit(PlusExpression n, Void argu) throws Exception {
        Register cmp1 = n.f0.accept(this, null);
        Register cmp2 = n.f2.accept(this, null);
        // Get the type
        String type = cmp1.getType();
        // Get the leftish expr's name
        String nameL = cmp1.getName();
        // Get the rightish expr's name
        String nameR = cmp2.getName();

        String newRegister = getNewRegister();

        // %_7 = add i32 %_3, %_6
        emitLLVM("\t" + newRegister + " = add " + type + " " + nameL + ", " + nameR + "\n\n");

        return new Register(newRegister, "i32");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
    */
    public Register visit(MinusExpression n, Void argu) throws Exception {
        Register cmp1 = n.f0.accept(this, null);
        Register cmp2 = n.f2.accept(this, null);
        // Get the type
        String type = cmp1.getType();
        // Get the leftish expr's name
        String nameL = cmp1.getName();
        // Get the rightish expr's name
        String nameR = cmp2.getName();

        String newRegister = getNewRegister();

        // %_7 = sub i32 %_3, %_6
        emitLLVM("\t" + newRegister + " = sub " + type + " " + nameL + ", " + nameR + "\n\n");

        return new Register(newRegister, "i32");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
    */
    public Register visit(TimesExpression n, Void argu) throws Exception {
        Register cmp1 = n.f0.accept(this, null);
        Register cmp2 = n.f2.accept(this, null);
        // Get the type
        String type = cmp1.getType();
        // Get the leftish expr's name
        String nameL = cmp1.getName();
        // Get the rightish expr's name
        String nameR = cmp2.getName();

        String newRegister = getNewRegister();

        // %_7 = sub i32 %_3, %_6
        emitLLVM("\t" + newRegister + " = mul " + type + " " + nameL + ", " + nameR + "\n\n");

        return new Register(newRegister, "i32");
    }

    @Override
    public Register visit(Identifier n, Void argu) throws Exception {
        String name = n.f0.tokenImage;
        
        // Find the variable
        Variable curVar = symTable.findVariable(name, getScope());

        String newReg = getNewRegister();
        // Take the type of the variable and convert it to llvm
        if(curVar == null) 
            throw new Exception("Undefined symbol " + name + currentScope[0]);
        String varType = curVar.getType();
        String varTypeLLVM = vTable.convertTypeLLVM(varType);
        // Take the name of the variable
        String varName = curVar.getName();
        // %_15 = load i32, i32* %num_aux
        emitLLVM("\t" + newReg + " = load " + varTypeLLVM + ", " + varTypeLLVM + "* %" + varName + "\n");

        return new Register(newReg, varTypeLLVM);
    }

    public Register visit(AllocationExpression n, Void argu) throws Exception {
        // String nameOfClass = n.f1.f0.tokenImage;

        // // Check in the symbol table if a class named "nameOfClass" exists
        // Class currentClass = symTable.getClass(nameOfClass);
        // if(currentClass == null)
        //     throw new Exception("Cannot allocate new object, because symbol " + nameOfClass + " is not a class");

        // // Instead of type return the name of the class
        return null;
    }

    // f0 -> PrimaryExpression()
	// f1 -> "."
	// f2 -> Identifier()
	// f3 -> "("
	// f4 -> ( ExpressionList() )?
	// f5 -> ")"
	public Register visit(MessageSend n, String argu) throws Exception {
        String methodname = n.f2.f0.tokenImage;
		return null;
	}

    public Register visit(ThisExpression n, String argu) {

		return null;
	}

    public Register visit(TrueLiteral n, Void argu) {
        return new Register("1", "i1");
    }

    public Register visit(FalseLiteral n, Void argu) {
        return new Register("0", "i1");
    }

    public Register visit(IntegerLiteral n, Void argu) throws Exception {
        return new Register(n.f0.toString(), "i32");
    }
}   

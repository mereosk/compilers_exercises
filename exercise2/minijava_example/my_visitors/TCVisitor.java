package my_visitors;

import symbol_table.*;
import symbol_table.Class;
import syntaxtree.*;
import visitor.GJDepthFirst;

public class TCVisitor extends GJDepthFirst<String, Void> {
    private SymbolTable symTable;
    private String[] currentScope;

    public TCVisitor(SymbolTable symTable) {
        this.symTable = symTable;
        this.currentScope = new String[2];
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

    public boolean sameType(String s1, String s2) throws Exception {
        if(s1.equals("boolean") && s2.equals("boolean"))
            return true;
        if(s1.equals("int") && s2.equals("int"))
            return true;
        if(s1.equals("boolean[]") && s2.equals("boolean[]"))
            return true;
        if(s1.equals("int[]") && s2.equals("int[]"))
            return true;
        
        // Reaching this point means that s1 and s2 are classes(objects)
        // so firstly check if the classes exist
        if(symTable.getClass(s1) == null || symTable.getClass(s1) == null)
            throw new Exception("Wrong type (" + s1 +", "+ s2 +") should be classes that already exist");

        // S1 and s2 are the same if they are the same
        if(s1.equals(s2))
            return true;

        // S1 and s2 are the same if s2 is the parend class of s1
        if(symTable.isParentClass(s1, s2))
            return true;

        return false;
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
    public String visit(MainClass n, Void argu) throws Exception {
        String className = n.f1.f0.tokenImage;

        setScopeClass(className);
        setScopeMethod(null);

        n.f15.accept(this, null);

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
    public String visit(ClassDeclaration n, Void argu) throws Exception {
        String className = n.f1.f0.tokenImage;

        setScopeClass(className);
        setScopeMethod(null);

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
    public String visit(ClassExtendsDeclaration n, Void argu) throws Exception {
        String className = n.f1.f0.tokenImage;

        setScopeClass(className);
        setScopeMethod(null);

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
    public String visit(MethodDeclaration n, Void argu) throws Exception {
        String type = n.f1.accept(this, null);
        String name = n.f2.f0.tokenImage;

        // Set the method's name as the scope
        setScopeMethod(name);

        n.f8.accept(this, null);

        // Check if the return type is correct
        String returnedType = n.f10.accept(this, null);

        if(sameType(returnedType, type) == false)
            throw new Exception("Incompatible types: " + returnedType + " cannot be converted to " + type);

        return null;
    }

    /**
        * f0 -> Identifier()
        * f1 -> "="
        * f2 -> Expression()
        * f3 -> ";"
    */
    public String visit(AssignmentStatement n, Void argu) throws Exception {
        String operant1 = n.f0.accept(this, null);
        String operant2 = n.f2.accept(this, null);

        if(sameType(operant1, operant2) == false)
            throw new Exception("Incompatible types in assignment: " + operant1 + " cannot be converted to " + operant2);

        return null;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
    */
    public String visit(ArrayAssignmentStatement n, Void argu) throws Exception {
        String arrayType = n.f0.accept(this, null);
        String arrayIndex = n.f2.accept(this, null);
        String value = n.f5.accept(this, null);

        if(arrayType.equals("boolean[]") == false && arrayType.equals("int[]") == false)
            throw new Exception("Incompatible type of array in assignment: should be either boolean[] or int[]");
        
        if(arrayIndex.equals("int") == false)
            throw new Exception("Invalid index type of array in assignment: should be int but it is " + arrayIndex);

        if(arrayIndex.equals("int") == false)
            throw new Exception("Invalid type of the second operant in assignemnt: should be int but it is "+ value);

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
    public String visit(IfStatement n, Void argu) throws Exception {
        String ifExprType = n.f2.accept(this, null);

        if(ifExprType.equals("boolean") == false)
            throw new Exception(ifExprType + "isn't accepted in an if condition, expression have to be boolean");

        n.f4.accept(this, null);
        n.f6.accept(this, null);

        return null;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
    */
    public String visit(WhileStatement n, Void argu) throws Exception {
        String whileExprType = n.f2.accept(this, null);

        if(whileExprType.equals("boolean") == false)
            throw new Exception(whileExprType + "isn't accepted in a while condition, expression have to be boolean");

        n.f4.accept(this, null);

        return null;
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
    */
    public String visit(PrintStatement n, Void argu) throws Exception {
        String prntExprType = n.f2.accept(this, null);

        if(prntExprType.equals("int") == false)
            throw new Exception("Print statement accepts only integers, not " + prntExprType);

        return null;
    }

    /**
    * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */
    public String visit(AndExpression n, Void argu) throws Exception {
        String c1 = n.f0.accept(this, null);
        String c2 = n.f2.accept(this, null);

        if(c1.equals("boolean") == false || c2.equals("boolean") == false)
            throw new Exception("Bad operant types for binary operator '+'. First type: " + c1 + " , second type: " + c2);

        return "boolean";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
    */
    public String visit(CompareExpression n, Void argu) throws Exception {
        String cmp1 = n.f0.accept(this, null);
        String cmp2 = n.f2.accept(this, null);

        if(cmp1.equals("int") == false || cmp2.equals("int") == false)
            throw new Exception("Bad operant types for binary operator '<'. First type: " + cmp1 + " , second type: " + cmp2);

        return "boolean";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
    */
    public String visit(PlusExpression n, Void argu) throws Exception {
        String plus1 = n.f0.accept(this, null);
        String plus2 = n.f2.accept(this, null);

        if(plus1.equals("int") == false || plus2.equals("int") == false)
            throw new Exception("Bad operant types for binary operator '+'. First type: " + plus1 + " , second type: " + plus2);

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
    */
    public String visit(MinusExpression n, Void argu) throws Exception {
        String minus1 = n.f0.accept(this, null);
        String minus2 = n.f2.accept(this, null);

        if(minus1.equals("int") == false || minus2.equals("int") == false)
            throw new Exception("Bad operant types for binary operator '-'. First type: " + minus1 + " , second type: " + minus2);

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
    */
    public String visit(TimesExpression n, Void argu) throws Exception {
        String times1 = n.f0.accept(this, null);
        String times2 = n.f2.accept(this, null);

        if(times1.equals("int") == false || times2.equals("int") == false)
            throw new Exception("Bad operant types for binary operator '-'. First type: " + times1 + " , second type: " + times2);

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
    */
    public String visit(ArrayLookup n, Void argu) throws Exception {
        String arrayType = n.f0.accept(this, null);
        String arrayIndex = n.f2.accept(this, null);

        if(arrayType.equals("int[]")) {
            if(arrayIndex.equals("int") == false)
                throw new Exception("Incompatible types: " + arrayIndex + " cannot be converted to int");

            return "int";
        }
        else if(arrayType.equals("boolean[]")) {
            if(arrayIndex.equals("int") == false)
                throw new Exception("Incompatible types: " + arrayIndex + " cannot be converted to int");

            return "boolean";
        }

        throw new Exception("Type in lookup expression is not array like");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
    */
    public String visit(ArrayLength n, Void argu) throws Exception {
        String arrayType = n.f0.accept(this, null);

        if(arrayType.equals("int[]") == false && arrayType.equals("boolean[]") == false)
            throw new Exception("The "+ arrayType +" is not array like");

        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
    */
    public String visit(MessageSend n, Void argu) throws Exception {
        String objectType = n.f0.accept(this, null);
        String methodName = n.f2.f0.tokenImage;
        
        // The above variable should be an object so check if the name is a class
        Class currentClass = symTable.getClass(objectType);
        if(currentClass == null)
            throw new Exception("Method " + methodName + " was called on object of invalid type, cannot dereference " + objectType);

        // Now search for the method
        Method currentMethod = symTable.findMethod(methodName, objectType);
        if(currentMethod == null)
            throw new Exception("Invalid name " + methodName +", method doesn't exist in " + objectType);

        // Now check the arguments
        String argumentList = n.f4.present() ? n.f4.accept(this, null) : "";
        // Argument list is a big string with arguments that are in form:
        // arg1, arg2 etc
        // So split the list in an array
        String argArray[] = argumentList.split(",");
        int calledNumParams;
        if(argumentList == "")
            calledNumParams = 0;
        else   
            calledNumParams = argArray.length;

        // Check if the number of the method is the correct number
        if(currentMethod.getNumParams() != calledNumParams)
            throw new Exception("Wrong number of arguments when calling method " + methodName);

        // Check if the argument's type is correct
        if(argumentList != "") {
            int numOfArg = 0;
            for(String arg : argArray) {
                arg = arg.trim();
                String type = currentMethod.getArgumentType(numOfArg);
                if(sameType(type, arg) == false)
                    throw new Exception("Wrong type of argument "+arg+", it should have been "+type);

                numOfArg+=1;
            }
        }

        // Return the method's type
        return currentMethod.getType();
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
    */
    public String visit(ExpressionList n, Void argu) throws Exception {
        String ret = n.f0.accept(this, null);

        if (n.f1 != null) {
            ret += n.f1.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> ( ExpressionTerm() )*
    */
    public String visit(ExpressionTail n, Void argu) throws Exception {
        String ret = "";
        for ( Node node: n.f0.nodes) {
            ret += ", " + node.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> ","
    * f1 -> Expression()
    */
    public String visit(ExpressionTerm n, Void argu) throws Exception {
        return n.f1.accept(this, argu);
    }

	public String visit(Type n, Void argu) throws Exception {

		// .which = 0 -> ArrayType
		//        = 1 -> BooleanType
		//        = 2 -> IntegerType
		//        = 3 -> Identifier
        if(n.f0.which == 3)
            return ((Identifier) n.f0.choice).f0.tokenImage;
        else
            return super.visit(n, null);
	}

    /**
        * f0 -> <INTEGER_LITERAL>
    */
    public String visit(IntegerLiteral n, Void argu) {
        return "int";
    }
  
    /**
        *f0 -> "true"
    */
    public String visit(TrueLiteral n, Void argu) {
        return "boolean";
    }
  
    /**
        *f0 -> "false"
    */
    public String visit(FalseLiteral n, Void argu) {
        return "boolean";
    }

    /**
    * f0 -> <IDENTIFIER>
    */
    @Override
    public String visit(Identifier n, Void argu) throws Exception {

        String id = n.f0.toString();

        // Find the variable that its name is id from the scope
        Variable var = symTable.findVariable(id, getScope());

        if(var == null) 
            throw new Exception("Undefined symbol " + id);

        // Return the type
        return var.getType();
    }

    /**
     * f0 -> "this"
    */
    public String visit(ThisExpression n, Void argu) throws Exception {
        // The this keyword refers to the current object, so return the class name from the scope
        return getClassScope();
    }

    /**
     * f0 -> "new"
     * f1 -> "boolean"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
    */
    public String visit(BooleanArrayAllocationExpression n, Void argu) throws Exception {
        String typeArrayIndex = n.f3.accept(this, null);

        if(typeArrayIndex.equals("int") == false)
            throw new Exception("Invalid index type of array in allocation: should be int but it is " + typeArrayIndex);

        return "boolean[]";
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
    */
    public String visit(IntegerArrayAllocationExpression n, Void argu) throws Exception {
        String typeArrayIndex = n.f3.accept(this, null);

        if(typeArrayIndex.equals("int") == false)
            throw new Exception("Invalid index type of array in allocation: should be int but it is " + typeArrayIndex);

        return "int[]";
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
    */
    public String visit(AllocationExpression n, Void argu) throws Exception {
        String nameOfClass = n.f1.f0.tokenImage;

        // Check in the symbol table if a class named "nameOfClass" exists
        Class currentClass = symTable.getClass(nameOfClass);
        if(currentClass == null)
            throw new Exception("Cannot allocate new object, because symbol " + nameOfClass + " is not a class");

        // Instead of type return the name of the class
        return nameOfClass;
    }

    /**
     * f0 -> "!"
     * f1 -> Clause()
    */
    public String visit(NotExpression n, Void argu) throws Exception {
        String clauseType = n.f1.accept(this, null);

        if(clauseType.equals("boolean") == false)
            throw new Exception("Bad operand type " + clauseType + " for unary operator '!'");

        return "boolean";
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
    */
    public String visit(BracketExpression n, Void argu) throws Exception {
        return n.f1.accept(this, null);
    }

    @Override
    public String visit(IntegerArrayType n, Void argu) {
        return "int[]";
    }

    public String visit(BooleanArrayType n, Void argu)  {
        return "boolean[]";
    }

    public String visit(BooleanType n, Void argu) {
        return "boolean";
    }

    public String visit(IntegerType n, Void argu) {
        return "int";
    }

    
}

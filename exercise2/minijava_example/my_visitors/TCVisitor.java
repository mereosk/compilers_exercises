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

    public boolean sameType(String s1, String s2){
        if(s1.equals("boolean") && s2.equals("boolean"))
            return true;
        if(s1.equals("int") && s2.equals("int"))
            return true;
        if(s1.equals("boolean[]") && s2.equals("boolean[]"))
            return true;
        if(s1.equals("int[]") && s2.equals("int[]"))
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
        System.out.println(className+"ds");

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
        System.out.println("sdf"+type);

        // Set the method's name as the scope
        setScopeMethod(name);

        n.f8.accept(this, null);

        // Check if the return type is correct
        String returnedType = n.f10.accept(this, null);
        System.out.println("sad"+returnedType);

        if(!sameType(returnedType, type))
            throw new Exception("Incompatible types: " + returnedType + " cannot be converted to " + type);

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

        if(!c1.equals("boolean") || !c2.equals("boolean"))
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

        if(!cmp1.equals("int") || !cmp2.equals("int"))
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

        if(!plus1.equals("int") || !plus2.equals("int"))
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

        if(!minus1.equals("int") || !minus2.equals("int"))
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

        if(!times1.equals("int") || !times2.equals("int"))
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
            if(!arrayIndex.equals("int"))
                throw new Exception("Incompatible types: " + arrayIndex + " cannot be converted to int");

            return "int";
        }
        else if(arrayType.equals("boolean[]")) {
            if(!arrayIndex.equals("int"))
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

        if(!arrayType.equals("int[]") && !arrayType.equals("boolean[]"))
            throw new Exception("The "+ arrayType +" is not array like");

        return "int";
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

    /**
    * f0 -> <IDENTIFIER>
    */
    @Override
    public String visit(Identifier n, Void argu) throws Exception {

        String id = n.f0.toString();
        System.out.println("Im here" + id);

        // Find the variable that its name is id from the scope
        Variable var = symTable.findVariable(id, getScope());

        if(var == null) 
            throw new Exception("Undefined symbol " + id);

        // Return the type
        return var.getType();
    }
}

package my_visitors;

import symbol_table.*;
import symbol_table.Class;
import syntaxtree.*;
import visitor.GJDepthFirst;

// Decl collector
public class STFillVisitor extends GJDepthFirst<String, Void>{

    private SymbolTable symTable;
    private String[] currentScope;

    public STFillVisitor(SymbolTable symTable) {
        this.symTable = symTable;
        this.currentScope = new String[2];
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
        String className = n.f1.accept(this, null);

        // Add the class in the symbol table 
        symTable.insertClass(className, null);

        // Add the method in the Class
        Class mainClass = symTable.getClass(className);
        Method tempMethod = new Method("main", "void");
        mainClass.insertMethod(tempMethod);

        // Add the variable in the main method
        Method mainMethod = mainClass.getMethod("main");
        String mainArgName = n.f11.accept(this, null);
        Variable mainArg = new Variable(mainArgName, "String[]");
        mainMethod.insertParameter(mainArg);

        setScopeClass(className);
        setScopeMethod(null);

        n.f14.accept(this, null);

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
        String className = n.f1.accept(this, null);

        // Add the class in the symbol table 
        symTable.insertClass(className, null);

        setScopeClass(className);
        setScopeMethod(null);

        n.f3.accept(this, null);
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
        String className = n.f1.accept(this, null);
        String superClassName = n.f3.accept(this, null);

        // Add the class in the symbol table 
        symTable.insertClass(className, superClassName);

        setScopeClass(className);
        setScopeMethod(null);

        n.f5.accept(this, null);
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
        String argumentList = n.f4.present() ? n.f4.accept(this, null) : "";
        // Argument list is a big string with arguments that are in form:
        // int arg1, int arg2 etc
        // So split the list in an array
        String argArray[] = argumentList.split(",");
    

        String type = n.f1.accept(this, null);
        String name = n.f2.accept(this, null);

        // Find the class of the method from the scope
        Class currentClass = symTable.getClass(getClassScope());
        // Add the mehtod in the current class
        Method currentMethod = new Method(name, type);
        currentClass.insertMethod(currentMethod);

        // Add the arguments in the class Method
        if(argumentList != "") {
            Method methodInClass = currentClass.getMethod(name);
            for(String arg : argArray) {
                arg = arg.trim();
                String[] typeAndName = arg.split(" ");
                Variable argToBeInserted = new Variable(typeAndName[1], typeAndName[0]);
                methodInClass.insertParameter(argToBeInserted);
            }
        }

        // Set the method's name as the scope
        setScopeMethod(name);

        n.f7.accept(this, null);

        return null;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterList n, Void argu) throws Exception {
        String ret = n.f0.accept(this, null);

        if (n.f1 != null) {
            ret += n.f1.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    public String visit(FormalParameterTerm n, Void argu) throws Exception {
        return n.f1.accept(this, argu);
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterTail n, Void argu) throws Exception {
        String ret = "";
        for ( Node node: n.f0.nodes) {
            ret += ", " + node.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n, Void argu) throws Exception{
        String type = n.f0.accept(this, null);
        String name = n.f1.accept(this, null);
        return type + " " + name;
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    @Override
    public String visit(VarDeclaration n, Void argu) throws Exception{
        String name = n.f1.accept(this, null);
        String type = n.f0.accept(this, null);

        // Get the current class from the scope
        Class currentClass = symTable.getClass(getClassScope());
        // Make the variable
        Variable var = new Variable(name, type);

        // Check it the type of scope is a method or a class
        if(getMethodScope() != null) {
            Method currentMethod = currentClass.getMethod(getMethodScope());
            currentMethod.insertVariable(var);
        }
        else {
            currentClass.insertVariable(var);
        }

        return null;
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

    @Override
    public String visit(Identifier n, Void argu) {
        return n.f0.toString();
    }
}
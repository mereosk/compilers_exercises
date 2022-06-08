package my_visitors;

import syntaxtree.*;
import v_table.*;
import symbol_table.*;
import symbol_table.Class;
import visitor.GJDepthFirst;
import java.util.*;

public class FillVTableVisitor extends GJDepthFirst<String, Void> {

    private VTable vTable;
    private SymbolTable symbolTable;
    private String[] currentScope;

    public FillVTableVisitor(VTable vTable, SymbolTable symbolTable) {
        this.vTable = vTable;
        this.symbolTable = symbolTable;
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

        // Initialise the vTable of the class Main
        vTable.insertClass(className);

        setScopeClass(className);
        setScopeMethod(null);

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

        // Initialise the vTable of the class className
        vTable.insertClass(className);

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
        String className = n.f1.accept(this, null);
        String superClassName = n.f3.accept(this, null);

        // Initialise the vTable of the class className
        vTable.insertClass(className);

        // Copy the methods of the parent class in current class vtable
        Map<Method, String> methods = vTable.getMethods(superClassName);

        if(methods != null)
            for (Map.Entry<Method, String> entry : methods.entrySet()) {
                // System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
                vTable.insertMethod(entry.getKey(), entry.getValue(), className);
            }

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
        String methodName = n.f2.accept(this, null);

        // Set the method's name as the scope
        setScopeMethod(methodName);

        // Insert the method in the vTable of the current class
        // which we will find from the scope
        String currentClassName = getClassScope();

        Class currentClass = symbolTable.getClass(currentClassName);
        Method currentMethod = currentClass.getMethod(methodName);

        System.out.println(methodName);
        vTable.insertMethod(currentMethod, currentClassName, currentClassName);

        return null;
    }

    @Override
    public String visit(Identifier n, Void argu) {
        return n.f0.toString();
    }
}

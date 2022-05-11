package symbol_table;

public class Variable {

    private String name;
    private String type;
    private int offset;

    public Variable(String variableName, String variableType) {
        this.name = variableName;
        this.type = variableType;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getOffset() {
        return this.offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    // This function prints the variable for debugging purposes
    public void printVariable() {

    }
}

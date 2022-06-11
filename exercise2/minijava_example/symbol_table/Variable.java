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

    public void printOffset() {
        System.out.println(name+" : " + offset);        
    }

    // This function prints the variable for debugging purposes
    public void printVariable() {
        System.out.println("\t\t\t\t" + type + " " + name);
    }

    @Override
    public boolean equals(Object o){
        if (this == o)
			return true;

		if (o == null)
			return false;

		if (getClass() != o.getClass())
			return false;

		return name.equals(((Variable) o).name);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash*31 + this.name.hashCode();
        return hash;
    }
}

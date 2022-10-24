public class Symbol {
    private String type;
    private int intType;
    private String content;
    private int area = 0;

    public Symbol(String type, int intType, Token token) {
        this.type = type;
        this.intType = intType;
        this.content = token.value;
    }

    public int getArea() {
        return area;
    }

    public int getIntType() {
        return intType;
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }
}

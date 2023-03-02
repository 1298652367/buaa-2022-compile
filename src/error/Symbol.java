package error;
import data.*;
public class Symbol {
    private String type;
    private int intType;
    private String content;
    private int areaID = 0;

    public Symbol(String type, int intType, Token token,int areaID) {
        this.type = type;
        this.intType = intType;
        this.content = token.value;
        this.areaID=areaID;
    }

    public int getAreaID() {
        return areaID;
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

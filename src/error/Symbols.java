package error;
import java.util.HashMap;
import data.*;

public class Symbols {
    private HashMap<String, Symbol> symbolHashMap;

    public Symbols() {
        symbolHashMap = new HashMap<>();
    }

    public void addSymbol(String type,int intType,Token token){
        symbolHashMap.put(token.getValue(),new Symbol(type,intType,token));
    }

    public boolean hasSymbol(Token token){
        return symbolHashMap.containsKey(token.getValue());
    }

    public boolean isConst(Token token){
        return symbolHashMap.get(token.value).getType().equals("const");
    }
    public Symbol getSymbol(Token token) {
        return symbolHashMap.get(token.getValue());
    }
    @Override
    public String toString() {
        return symbolHashMap.toString();
    }

    public void addLayer() {
    }
}

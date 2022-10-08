import java.util.HashMap;
import java.util.Map;

public class keywords {
    Map<String,String> keyWord;
    //存储关键字和运算符
    public keywords(){
        keyWord=new HashMap<>();
        keyWord.put("Ident","IDENFR");
        keyWord.put("IntConst","INTCON");
        keyWord.put("FormatString","STRCON");
        keyWord.put("main","MAINTK");
        keyWord.put("const","CONSTTK");
        keyWord.put("int","INTTK");
        keyWord.put("break","BREAKTK");
        keyWord.put("continue","CONTINUETK");
        keyWord.put("if","IFTK");
        keyWord.put("else","ELSETK");
        keyWord.put("!","NOT");
        keyWord.put("&&","AND");
        keyWord.put("||","OR");
        keyWord.put("while","WHILETK");
        keyWord.put("getint","GETINTTK");
        keyWord.put("printf","PRINTFTK");
        keyWord.put("return","RETURNTK");
        keyWord.put("+","PLUS");
        keyWord.put("-","MINU");
        keyWord.put("void","VOIDTK");
        keyWord.put("*","MULT");
        keyWord.put("/","DIV");
        keyWord.put("%","MOD");
        keyWord.put("<","LSS");
        keyWord.put("<=","LEQ");
        keyWord.put(">","GRE");
        keyWord.put(">=","GEQ");
        keyWord.put("==","EQL");
        keyWord.put("!=","NEQ");
        keyWord.put("=","ASSIGN");
        keyWord.put(";","SEMICN");
        keyWord.put(",","COMMA");
        keyWord.put("(","LPARENT");
        keyWord.put(")","RPARENT");
        keyWord.put("[","LBRACK");
        keyWord.put("]","RBRACK");
        keyWord.put("{","LBRACE");
        keyWord.put("}","RBRACE");
    }
}

public class Token {
    public String value; //单词的值
    public String type;//类别码
    public int lineNum;// 行数
    public int flag=1;

    public Token(String value,String type){
        this.value=value;
        this.type=type;
    }

    public Token(String value,String type,int lineNum){
        this.value=value;
        this.type=type;
        this.lineNum=lineNum;
    }

    public Token(int a){
        this.flag=a;
    }

    @Override
    public String toString()  {
        return this.type+" "+this.value;
    }

    public boolean typeEquals(String str) {
        return type.equals(str);
    }
    public boolean valueEquals(String str) {
        return value.equals(str);
    }

    public boolean typeSymbolizeStmt() {
        return type.equals("IDENFR")
                || type.equals("LBRACE")
                || type.equals("IFTK")
                || type.equals("ELSETK")
                || type.equals("WHILETK")
                || type.equals("BREAKTK")
                || type.equals("CONTINUETK")
                || type.equals("RETURNTK")
                || type.equals("PRINTFTK")
                || type.equals("SEMICN")
                || typeSymbolizeBeginOfExp();
    }
    public boolean typeSymbolizeExp() {
        return type.equals("LPARENT")
                || type.equals("IDENFR")
                || type.equals("INTCON")
                || type.equals("NOT")
                || type.equals("PLUS")
                || type.equals("MINU");
    }

    public boolean typeSymbolizeValidateStmt() {
        return type.equals("IFTK")
                || type.equals("ELSETK")
                || type.equals("WHILETK")
                || type.equals("BREAKTK")
                || type.equals("CONTINUETK")
                || type.equals("RETURNTK")
                || type.equals("PRINTFTK")
                || type.equals("SEMICN");
    }

    public boolean typeSymbolizeBeginOfExp() {
        return type.equals("LPARENT")
                || type.equals("IDENFR")
                || type.equals("INTCON")
                || type.equals("NOT")
                || type.equals("PLUS")
                || type.equals("MINU");
    }

    public boolean typeOfUnary() {
        return type.equals("PLUS")
                || type.equals("MINU")
                || type.equals("NOT");
    }

    public boolean typeOfNotInExp() {
        return type.equals("CONSTTK")
                || type.equals("INTTK")
                || type.equals("BREAKTK")
                || type.equals("CONTINUETK")
                || type.equals("IFTK")
                || type.equals("ELSETK")
                || type.equals("WHILETK")
                || type.equals("GETINTTK")
                || type.equals("PRINTFTK")
                || type.equals("RETURNTK");
    }


}

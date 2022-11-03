package data;
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

    public Token(String value, int lineNum) {
        this.value = value;
        this.lineNum = lineNum;
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


    public String getType() {
        return this.type;
    }

    public String getValue() {
        return this.value;
    }

    public int getLineNum() {
        return this.lineNum;
    }

    public int getFormatNum(){
        int n=0;
        for(int i=0;i<value.length();i++){
            if(i+1<value.length()){
                if(value.charAt(i)=='%' && value.charAt(i+1)=='d'){
                    n++;
                }
            }
        }
        return n;
    }

    public boolean isFormatIllegal(){
        for(int i=1;i<value.length()-1;i++){
            char c = value.charAt(i);
            if(!isIllagal(c)){
                if(c=='%'&& value.charAt(i+1)=='d'){
                    continue;
                }
                return true;
            }else{
                if(c=='\\'&& value.charAt(i+1)!='n' )
                    return true;
            }
        }
        return false;
    }

    public boolean isIllagal(char c){
        return c==32 || c==33|| (c>=40&&c<=126);
    }




}

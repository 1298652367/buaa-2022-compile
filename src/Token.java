public class Token {
    public String value; //单词的值
    public String sym;//类别码

    public Token(String value,String sym){
        this.value=value;
        this.sym=sym;
    }

    @Override
    public String toString()  {
        return this.sym+" "+this.value;
    }
}

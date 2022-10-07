import java.io.*;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Compiler {
    private int length; //预处理后的程序长度
    private int begin;//扫描后的起始位置
    Map<String,String> keyWord;
    //存储关键字和运算符

    //内部类
    public class Token{
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

    public int getLength(){
        return this.length;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public Compiler(){
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

    // 首先，对输入源程序做预处理，处理掉注释、换行符、制表符等等
    public ArrayList<Character> preProcess(ArrayList<Character> source) throws IllegalArgumentException{
        // 构造一个临时数组存储预处理后的源程序
        ArrayList<Character> temp = new ArrayList<Character>();
        int count = 0;
        boolean is_Normal = true; //当前内容不在一个字符串中

        // 逐个扫描源程序中的字符
        for (int i=0; i<source.size(); i++){
            if(source.get(i).equals('"')){
                is_Normal = false;
            }
            if(is_Normal) {
                switch (source.get(i)) {
                    //除单行注释
                    case '/':
                        if (source.get(i + 1) == '/') {
                            // 跳过单行注释
                            i = i + 2;
                            // 跳过这一行，直到遇到回车换行
                            while (source.get(i) != '\n') {
                                i++;
                            }
                        }
                        // 去除多行注释
                        else if (source.get(i + 1) == '*') {
                            // 跳过多行注释符号
                            i = i + 2;
                            // 当不满足匹配多行注释的时候，跳过
                            while (!(source.get(i) == '*' && source.get(i + 1) == '/')) {
                                i++;
                                // 检查有没有到程序的末尾
                                if (i >= source.size() - 2) {
                                    throw new IllegalArgumentException("源程序的注释不匹配!");
                                }
                            }
                            // 跳过多行注释符号
                            i = i + 1;
                        } else {
                            temp.add(source.get(i));
                            count++;
                        }
                        break;
                    case '\n':
                    case '\t':
                    case '\r':
                        temp.add(' ');
                        count++;
                        break;
                    default:
                        temp.add(source.get(i));
                        count++;
                        break;
                }
            }
            else {
                temp.add(source.get(i));
                count++;
                i++;
                while( i < source.size() && !source.get(i).equals('"')) {
                    temp.add(source.get(i));
                    count++;
                    i++;
                }
                temp.add(source.get(i));
                count++;
                is_Normal = true;
            }
        }
        // 设置源程序的长度为count
        this.length = count;
        return temp;
    }

    // 识别单词符号
    public Token scanner(ArrayList<Character> code, int begin){
        // 我们知道，要识别的PL/0语言中的单词符号有这几个类别：
        // 保留字、标识符、常数、运算符和界符
        // 可以进一步粗分为3类，字母开头（保留字和标识符）数字开头（常数）其他

        // 存储当前识别的单词符号
        char[] token = new char[100000];
        // 指向程序的索引
        int index1 = begin;
        // 指向存储单词的数组的索引
        int index2 = 0;

        // 除去单词前的空格
        while (index1<code.size() && code.get(index1) == ' ' ){
            index1++;
            // 越界直接返回
            if(index1==code.size()) return null;
        }


        // 如果单词的首字符为字母或者下划线
        if (Character.isLetter(code.get(index1)) || code.get(index1).equals('_')){
            // 当后续为字母,数字或下划线时存入
            while (Character.isLetter(code.get(index1)) || Character.isDigit(code.get(index1)) || code.get(index1).equals('_')){
                token[index2++] = code.get(index1++);
            }
            // 在map中查找，如果能查找到说明是保留字
            // 否则说明是标识符
            // 每次返回都要维护下index1
            this.setBegin(index1);
            for(Map.Entry<String,String> entry:keyWord.entrySet()){
                //是保留字
                if(String.valueOf(token).trim().equals(entry.getKey())){
                    return new Token(String.valueOf(token).trim(),entry.getValue());
                }
            }
            //是标识符
            return new Token(String.valueOf(token).trim(),"IDENFR");

        } else if (Character.isDigit(code.get(index1))){
            // 如果首字符是数字
            while (Character.isDigit(code.get(index1))){
                token[index2++] = code.get(index1++);
            }
            this.setBegin(index1);
            return new Token(String.valueOf(token).trim(), "INTCON");
        } else if(code.get(index1).equals('"')){
            //是字符串
            token[index2++] = code.get(index1++);
            while (!code.get(index1).equals('"')){
                token[index2++] = code.get(index1++);
            }
            token[index2++] = code.get(index1++);
            this.setBegin(index1);
            return new Token(String.valueOf(token).trim(), "STRCON");
        }else{
            switch (code.get(index1)){
                case '!':
                    if(code.get(index1 + 1) =='='){
                        this.setBegin(index1+2);
                        return new Token("!=",this.keyWord.get("!="));
                    }else{
                        this.setBegin(index1+1);
                        return new Token("!",this.keyWord.get("!"));
                    }

                case '<':
                    if (code.get(index1 + 1) == '='){
                        this.setBegin(index1+2);
                        return new Token("<=", this.keyWord.get("<="));
                    } else{
                        this.setBegin(index1+1);
                        return new Token("<", this.keyWord.get("<"));
                    }
                case '>':
                    if (code.get(index1 + 1) == '='){
                        this.setBegin(index1+2);
                        return new Token(">=", this.keyWord.get(">="));
                    } else{
                        this.setBegin(index1+1);
                        return new Token(">", this.keyWord.get(">"));
                    }
                case '=':
                    if (code.get(index1 + 1) == '='){
                        this.setBegin(index1+2);
                        return new Token("==", this.keyWord.get("=="));
                    } else {
                        this.setBegin(index1 + 1);
                        return new Token("=", this.keyWord.get("="));
                    }
                case '&':
                    this.setBegin(index1+2);
                    return new Token("&&", this.keyWord.get("&&"));
                case '|':
                    this.setBegin(index1+2);
                    return new Token("||", this.keyWord.get("||"));
                case '+':
                case '-':
                case '*':
                case '/':
                case '%':
                case ',':
                case ';':
                case '(':
                case ')':
                case '[':
                case ']':
                case '{':
                case '}':
                    this.setBegin(index1+1);
                    return new Token(String.valueOf(code.get(index1)), this.keyWord.get(String.valueOf(code.get(index1))));
                default:
                    return new Token("noneType", null);
            }
        }

    }

    public static void main(String[] args) throws IOException {
        ArrayList<Character> source = new ArrayList<Character>();
        char[] s= new char[1000005];
        Compiler cp=new Compiler();
        try(BufferedReader reader = new BufferedReader(new FileReader("./testfile.txt"));){
            // 读入源程序
            reader.read(s);
            int index = 0;
            while(s[index]!='\u0000'){
                source.add(s[index]);
                index++;
            }
            int a = source.size();
            // 对源程序做预处理
           source = cp.preProcess(source);
            // 开始识别单词

        } catch(IOException e){
            e.printStackTrace();
        }

        Token tempToken = cp.scanner(source, 0);

        FileWriter write = new FileWriter("./output.txt");
        BufferedWriter writer = new BufferedWriter(write);


        while (cp.getBegin() < cp.getLength()){
            String ans = new String();
            ans=tempToken.toString();
            writer.write(ans);
            writer.newLine();
            tempToken = cp.scanner(source, cp.getBegin());
            if(tempToken==null) break;
        }
        if(!(tempToken==null)) {
            String ans = new String();
            ans = tempToken.toString();
            writer.write(ans);
        }

        writer.flush();
        writer.close();

    }
}

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LexParser {
    private String code;
    private int lineNum = 1;

    private int begin;//扫描后的起始位置
    private ArrayList<Token> tokens = new ArrayList<>();

    keywords keywords = new keywords();
    boolean is_Normal = true; //当前内容不在字符串之中

    public LexParser(String code){
        this.code = code;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    // 识别单词符号
    public Token scanner(String code, int begin){
        // 保留字、标识符、常数、运算符和界符
        // 可以进一步粗分为3类，字母开头（保留字和标识符）数字开头（常数）其他

        // 存储当前识别的单词符号
        char[] token = new char[100000];
        // 指向程序的索引
        int index1 = begin;
        // 指向存储单词的数组的索引
        int index2 = 0;
        // 除去单词前的空格,制表符，等
        while (index1<code.length() &&
                (code.charAt(index1) == ' ' || (code.charAt(index1)=='\t')||(code.charAt(index1)=='\r')||(code.charAt(index1) == '\n'))){
            if(code.charAt(index1) == '\n') lineNum++;
            index1++;
            // 越界直接返回
            if(index1==code.length()) return null;
        }
        // 处理注释
        if (code.charAt(index1)=='/'){
            if(code.charAt(index1+1)=='/') { // 单行注释
                index1+=2;
                while (code.charAt(index1)!='\n'){
                    index1++;
                }
                this.setBegin(index1);
                return new Token(0);
            }else if(code.charAt(index1+1)=='*'){ //多行注释
                index1+=2;
                // 当不满足匹配多行注释的时候，跳过
                while (!(code.charAt(index1) == '*' && code.charAt(index1+1)== '/')) {
                    if(code.charAt(index1)=='\n') lineNum++;
                    index1++;
                    // 检查有没有到程序的末尾
                    if (index1 >= code.length() - 2) {
                        throw new IllegalArgumentException("源程序的注释不匹配!");
                    }
                }
                // 跳过多行注释符号
                index1+=2;
                this.setBegin(index1);
                return new Token(0);
            }
        }
        // 如果单词的首字符为字母或者下划线
        if (Character.isLetter(code.charAt(index1)) || code.charAt(index1)=='_'){
            // 当后续为字母,数字或下划线时存入
            while (Character.isLetter(code.charAt(index1)) || Character.isDigit(code.charAt(index1)) || code.charAt(index1)=='_'){
                token[index2++] = code.charAt(index1++);
            }
            // 在map中查找，如果能查找到说明是保留字
            // 否则说明是标识符
            // 每次返回都要维护下index1
            this.setBegin(index1);
            //是保留字
            for(Map.Entry<String,String> entry: keywords.keyWord.entrySet()) {
                String s = String.valueOf(token).trim().toLowerCase();
                if (s.equals(entry.getKey())) {
                    return new Token(s, entry.getValue(),lineNum);
                }
            }
            //是标识符
            return new Token(String.valueOf(token).trim(),"IDENFR",lineNum);

        } else if (Character.isDigit(code.charAt(index1))){
            // 如果首字符是数字
            while (Character.isDigit(code.charAt(index1))){
                token[index2++] = code.charAt(index1++);
            }
            this.setBegin(index1);
            return new Token(String.valueOf(token).trim(), "INTCON",lineNum);
        } else if(code.charAt(index1)=='"'){
            //是字符串
            token[index2++] = code.charAt(index1++);
            while (!(code.charAt(index1)=='"')){
                token[index2++] = code.charAt(index1++);
            }
            token[index2++] = code.charAt(index1++);
            this.setBegin(index1);
            return new Token(String.valueOf(token).trim(), "STRCON",lineNum);
        }else{
            switch (code.charAt(index1)){
                case '!':
                    if(code.charAt(index1 + 1) =='='){
                        this.setBegin(index1+2);
                        return new Token("!=",this.keywords.keyWord.get("!="),lineNum);
                    }else{
                        this.setBegin(index1+1);
                        return new Token("!",this.keywords.keyWord.get("!"),lineNum);
                    }

                case '<':
                    if (code.charAt(index1 + 1) == '='){
                        this.setBegin(index1+2);
                        return new Token("<=", this.keywords.keyWord.get("<="),lineNum);
                    } else{
                        this.setBegin(index1+1);
                        return new Token("<", this.keywords.keyWord.get("<"),lineNum);
                    }
                case '>':
                    if (code.charAt(index1 + 1) == '='){
                        this.setBegin(index1+2);
                        return new Token(">=", this.keywords.keyWord.get(">="),lineNum);
                    } else{
                        this.setBegin(index1+1);
                        return new Token(">", this.keywords.keyWord.get(">"),lineNum);
                    }
                case '=':
                    if (code.charAt(index1 + 1) == '='){
                        this.setBegin(index1+2);
                        return new Token("==", this.keywords.keyWord.get("=="),lineNum);
                    } else {
                        this.setBegin(index1 + 1);
                        return new Token("=", this.keywords.keyWord.get("="),lineNum);
                    }
                case '&':
                    this.setBegin(index1+2);
                    return new Token("&&", this.keywords.keyWord.get("&&"),lineNum);
                case '|':
                    this.setBegin(index1+2);
                    return new Token("||", this.keywords.keyWord.get("||"),lineNum);
                case '/':
                case '+':
                case '-':
                case '*':
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
                    return new Token(String.valueOf(code.charAt(index1)), this.keywords.keyWord.get(String.valueOf(code.charAt(index1))),lineNum);
                default:
                    return new Token("noneType", null);
            }
        }

    }

    // 制作单词表
    public void makeTokens(){
        Token tempToken = scanner(code, 0);
        while (getBegin() < code.length()){
//            System.out.println(code.charAt(getBegin()));
            if(tempToken.flag!=0)  tokens.add(tempToken);
            tempToken = scanner(code, getBegin());
            if(tempToken==null) break;
        }
        if(!(tempToken==null)) {
            tokens.add(tempToken);
        }
    }

    // 词法分析输出
    public void print(BufferedWriter writer) throws IOException {
        for(Token token:tokens){
            String ans = token.toString();
            writer.write(ans);
            writer.newLine();
        }
    }

    public ArrayList<Token> getTokens(){
        return tokens;
    }

}

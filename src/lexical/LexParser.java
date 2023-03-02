package lexical;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import data.*;

public class LexParser {
    private String code;
    private int lineNum = 1;
    private int index = 0;

    private ArrayList<Token> tokens = new ArrayList<>();
    
    public LexParser(String code) throws IOException {
        this.code = code;
        analyse();
    }
    private Character getChar(){
        if(index < code.length()){
            char c=code.charAt(index);
            if(c=='\n') lineNum++;
            index++;
            return c;
        }
        else return null;
    }
    private void unGetChar(){
        index--;
        char c=code.charAt(index);
        if (c=='\n') lineNum--;
    }
    private void analyse()throws IOException{
        Character c=null;
        while ((c = getChar()) != null) {
            if (c == ' ' || c == '\r' || c == '\t') {
                continue;
            } else if (c == '+' || c == '-' || c == '*' || c == '%') {
                tokens.add(new Token(c, lineNum));
            } else if (c == '/') {
                analyseSlash();
            } else if (c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}') {
                tokens.add(new Token(c, lineNum));
            } else if (c == '>' || c == '<' || c == '=' || c == '!') {
                analyseRelation(c);
            } else if (c == ',' || c == ';') {
                tokens.add(new Token(c, lineNum));
            } else if (c == '"') {
                analyseCitation();
            } else if (c == '&' || c == '|') {
                analyseLogic(c);
            } else if (Character.isDigit(c)) {
                analyseDigit(c);
            } else if (Character.isLetter(c) || c == '_') {
                analyseLetter(c);
            }

        }
    }
    private void analyseSlash() {
        Character c = getChar();
        if (c == '/') {
            do {
                c = getChar();
                if (c == null || c == '\n') {
                    return;
                    // 判断为//注释，结束分析
                }
            } while (true);
        } else if (c == '*') {
            do {
                c = getChar();
                if (c == null) {
                    return;
                }
                if (c == '*') {
                    c = getChar();
                    if (c == '/') {
                        return;
                        // 判断为/* */注释，直接结束分析
                    } else {
                        unGetChar();
                    }
                }
            } while (true);
        } else {
            tokens.add(new Token("/", lineNum));
            unGetChar();
        }
    }

    private void analyseRelation(char c) {
        if (c == '=') {
            c = getChar();
            if (c == '=') {
                tokens.add(new Token("==", lineNum));
            } else {
                unGetChar();
                tokens.add(new Token("=", lineNum));
                return;
            }
        } else if (c == '<') {
            c = getChar();
            if (c == '=') {
                tokens.add(new Token("<=", lineNum));
            } else {
                unGetChar();
                tokens.add(new Token("<", lineNum));
            }
        } else if (c == '>') {
            c = getChar();
            if (c == '=') {
                tokens.add(new Token(">=", lineNum));
            } else {
                unGetChar();
                tokens.add(new Token(">", lineNum));
            }
        } else {
            c = getChar();
            if (c == '=') {
                tokens.add(new Token("!=", lineNum));
            } else {
                unGetChar();
                tokens.add(new Token("!", lineNum));
            }
        }
    }

    private void analyseCitation() {
        Character c = null;
        StringBuffer buffer = new StringBuffer("");
        int flag = 0;
        while ((c = getChar()) != null) {
            if (c == '"') {
                tokens.add(new Token("STRCON", "\"" + buffer + "\"", lineNum));
                return;
            } else {
                if (c == '\\') {
                    flag = 1;
                } else {
                    if (flag > 0 && c == 'n') {
                        buffer.append("\n");
                    } else {
                        buffer.append(c);
                    }
                    flag = 0;
                }

            }
        }
    }
    private void analyseLogic(char pre) {
        Character c = null;
        if ((c = getChar()) != null) {
            if (pre == '&') {
                if (c == '&') {
                    tokens.add(new Token("&&", lineNum));
                } else {
                    unGetChar();
                    tokens.add(new Token("&", lineNum));
                }
            } else {
                if (c == '|') {
                    tokens.add(new Token("||", lineNum));
                } else {
                    unGetChar();
                    tokens.add(new Token("|", lineNum));
                }
            }
        }
    }

    private void analyseDigit(char pre) {
        StringBuilder builder = new StringBuilder("" + pre);
        Character c = null;
        while ((c = getChar()) != null) {
            if (Character.isDigit(c)) {
                builder.append(c);
            } else {
                unGetChar();
                tokens.add(new Token("INTCON", builder.toString(), lineNum));
                return;
            }
        }
    }

    private void analyseLetter(char pre) {
        StringBuilder builder = new StringBuilder("" + pre);
        Character c = null;
        while ((c = getChar()) != null) {
            if (Character.isLetter(c) || c == '_' || Character.isDigit(c)) {
                builder.append(c);
            } else {
                unGetChar();
                if (new keywords().isKey(builder.toString())) {
                    tokens.add(new Token(builder.toString(), lineNum));
                } else {
                    tokens.add(new Token("IDENFR", builder.toString(), lineNum));
                }
                return;
            }
        }
    }
    // 词法分析输出
    public void print(BufferedWriter writer) throws IOException {
        for(Token token:tokens){
            String ans = token.toString();
            writer.write(ans);
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

    public ArrayList<Token> getTokens(){
        return tokens;
    }

}

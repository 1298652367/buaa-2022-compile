import java.io.*;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Compiler {
    public static void main(String[] args) throws IOException {
        ArrayList<Character> source = new ArrayList<Character>();
        char[] s= new char[1000005];
        LexParser lp = new LexParser();

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
           source = lp.preProcess(source);
            // 开始识别单词

        } catch(IOException e){
            e.printStackTrace();
        }

        FileWriter write = new FileWriter("./output.txt");
        BufferedWriter writer = new BufferedWriter(write);

        lp.makeTokens(source);

        ArrayList<Token> a = lp.getTokens();
        GrammaticalAnalyser ga = new GrammaticalAnalyser(lp.getTokens());
        ga.print(writer);
//        lp.print(writer);

        writer.flush();
        writer.close();

    }
}

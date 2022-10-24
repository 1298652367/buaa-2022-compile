import java.io.*;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Compiler {
    public static void main(String[] args) throws IOException {
        String source = new String();
        FileReader read = new FileReader("./testfile.txt");
        BufferedReader reader = new BufferedReader(read);

        FileWriter write = new FileWriter("./output.txt");
        BufferedWriter writer = new BufferedWriter(write);

        FileWriter writeError = new FileWriter("./error.txt");
        BufferedWriter writerError = new BufferedWriter(writeError);

        fileProcess fp = new fileProcess(reader); //预处理

        source = fp.getCode();
        LexParser lp = new LexParser(source); //词法分析
        lp.makeTokens();
        ArrayList<Token> a = lp.getTokens();
        GrammaticalAnalyser ga = new GrammaticalAnalyser(lp.getTokens());//语法分析

//             lp.print(writer); //词法分析输出
        ga.printError(writerError); //错误处理输出
//        ga.print(writer); // 语法分析输出

    }
}

import java.io.*;
import java.util.ArrayList;

import data.*;
import lexical.*;
import syntax.*;
import llym_ir.*;

public class Compiler {
    public static void main(String[] args) throws IOException {
        String source = new String();
        FileReader read = new FileReader("./testfile.txt");
        BufferedReader reader = new BufferedReader(read);

        FileWriter write = new FileWriter("./output.txt");
        BufferedWriter writer = new BufferedWriter(write);

        FileWriter writeError = new FileWriter("./error.txt");
        BufferedWriter writerError = new BufferedWriter(writeError);

        FileWriter writeLLvm = new FileWriter("./llvm_ir.txt");
        BufferedWriter writerLLvm = new BufferedWriter(writeLLvm);

        fileProcess fp = new fileProcess(reader); //预处理

        source = fp.getCode();
        LexParser lp = new LexParser(source); //词法分析
        lp.makeTokens();
        ArrayList<Token> a = lp.getTokens();
        GrammaticalAnalyser ga = new GrammaticalAnalyser(lp.getTokens());//语法分析
//        SynAndError sae = new SynAndError(lp.getTokens());
//        lp.print(writer); //词法分析输出
//        sae.printError(writerError); //错误处理输出
        ga.print(writer); // 语法分析输出

        llvmGenerator Generator = new llvmGenerator(ga.getRootAst());
        Generator.print(writerLLvm);
    }
}

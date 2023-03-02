import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import data.*;
import error.SynAndError;
import lexical.*;
import syntax.*;
import CodeGeneration.*;
import llvm_ir.*;

public class Compiler {

    public static void main(String[] args) throws IOException {
        String source = new String();
        FileReader read = new FileReader("testfile.txt");
        BufferedReader reader = new BufferedReader(read);

        FileWriter write = new FileWriter("output.txt");
        BufferedWriter writer = new BufferedWriter(write);

        FileWriter writeError = new FileWriter("error.txt");
        BufferedWriter writerError = new BufferedWriter(writeError);

//        FileWriter writeLLvm = new FileWriter("llvm_ir.txt");
//        BufferedWriter writerLLvm = new BufferedWriter(writeLLvm);

        FileWriter writePcode = new FileWriter("pcoderesult.txt");
        BufferedWriter writerPcode = new BufferedWriter(writePcode);

        Scanner scanner = new Scanner(System.in);
        fileProcess fp = new fileProcess(reader); //预处理

        source = fp.getCode();
        LexParser lp = new LexParser(source); //词法分析

        ArrayList<Token> a = lp.getTokens();
//        GrammaticalAnalyser ga = new GrammaticalAnalyser(lp.getTokens());//语法分析
        SynAndError sae = new SynAndError(lp.getTokens());
        sae.print(writer);
//        lp.print(writer); //词法分析输出
        sae.printPCode();
        PCodeExecutor pCodeExecutor = new PCodeExecutor(sae.getCodes(),scanner);
        pCodeExecutor.run();
        pCodeExecutor.print(writerPcode);
//        lp.print(writer); //词法分析输出
//        sae.printError(writerError); //错误处理输出
//        ga.print(writer); // 语法分析输出

//        llvmGenerator Generator = new llvmGenerator(ga.getRootAst());
//        Generator.print(writerLLvm);
    }
}

package syntax;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import data.*;

public class GrammaticalAnalyser {
    private ArrayList<Token> tokens;
    private int index = 0;
    private Token nowToken;
    private ArrayList<String> grammar;

    AstNode RootAst = new AstNode("<CompUnit>");

    public AstNode getRootAst(){
        return this.RootAst;
    }

    public GrammaticalAnalyser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        grammar = new ArrayList<>();
        ComUnit(RootAst);
    }

    //读到终结符，将其加入语法grammar之中
    private void getToken(AstNode astNode){
        nowToken = tokens.get(index);
        grammar.add(nowToken.toString());
        astNode.addChild(new AstNode(nowToken.getValue(),nowToken.getType()));
        index++;
    }

    private void getTokenWithoutAdd(){
        nowToken = tokens.get(index);
        index++;
    }

    private Token getNextToken(){
        return tokens.get(index);
    }

    private Token getNext2Token(){
        return tokens.get(index+1);
    }

    private Token getNext3Token(){
        return tokens.get(index+2);
    }

    private void error(){
        System.out.println("error");
    }
    // 分析编译单元
    private void ComUnit(AstNode astNode){
        Token token = getNextToken();
        while (token.valueEquals("const") || (
                token.valueEquals("int") && getNext2Token().typeEquals("IDENFR") && !getNext3Token().valueEquals("("))) {

            Decl(astNode);
            token = getNextToken();
        }
        while(token.valueEquals("void") || (
                (token.valueEquals("int") && !getNext2Token().valueEquals("main")))) {
            FuncDef(astNode);
            token = getNextToken();
        }
        if(token.valueEquals("int") && getNext2Token().valueEquals("main")){
            MainFuncDef(astNode);
        }else {
            error();
        }
        grammar.add("<CompUnit>");
    }
    // 分析声明
    private void Decl(AstNode astNode){
        Token token = getNextToken();
        if(token.valueEquals("const")){
            ConstDecl(astNode);
        }else if(token.valueEquals("int")){
            VarDecl(astNode);
        }else{
            error();
        }
    }
    // 分析常量声明
    private void ConstDecl(AstNode astNode){
        AstNode a =new AstNode("<ConstDecl>");
        getToken(a);//const
        getToken(a);//int
        if(nowToken.valueEquals("int")){
            BType();
        }else{
            error();
        }
        ConstDef(a);
        Token token = getNextToken();
        while (token.valueEquals(",")){
            getToken(a);// ,
            ConstDef(a);
            token = getNextToken();
        }
        getToken(a);// ;
        astNode.addChild(a);
        grammar.add("<ConstDecl>");
    }
    // 分析变量声明
    private void VarDecl(AstNode astNode){
        AstNode a =new AstNode("<VarDecl>");
        getToken(a); // int
        if (nowToken.valueEquals("int")) {
            BType();
        } else {
            error();
        }
        VarDef(a);
        Token token = getNextToken();
        while(token.valueEquals(",")){
            getToken(a);//,
            VarDef(a);
            token = getNextToken();
        }
        getToken(a);// ;
        astNode.addChild(a);
        grammar.add("<VarDecl>");
    }
    // 分析基本类型
    private void BType(){
    }
    // 分析常数定义
    private void ConstDef(AstNode astNode){
        AstNode a =new AstNode("<ConstDef>");
        getToken(a);//Ident
        Token token = getNextToken();
        while(token.valueEquals("[")){
            getToken(a);// [
            ConstExp(getExp(),a);
            getToken(a);// ]
            if(!nowToken.valueEquals("]")) error();
            token = getNextToken();
        }
        getToken(a); // =
        ConstInitVal(a);
        astNode.addChild(a);
        grammar.add("<ConstDef>");
    }
    // 分析常量初值
    private void ConstInitVal(AstNode astNode){
        AstNode a =new AstNode("<ConstInitVal>");
        Token token = getNextToken();
        if(token.valueEquals("{")){
            getToken(a);// {
            token = getNextToken();
            if(!token.valueEquals("}")) {
                ConstInitVal(a);
                Token token1 = getNextToken();
                while (token1.valueEquals(",")){
                    getToken(a);// ,
                    ConstInitVal(a);
                    token1 = getNextToken();
                }
            }
            getToken(a);// }
        }else{
            ConstExp(getExp(),a);
        }
        astNode.addChild(a);
        grammar.add("<ConstInitVal>");
    }
    // 分析变量定义
    private void VarDef(AstNode astNode){
        AstNode a =new AstNode("<VarDef>");
        getToken(a);//Ident
        Token token = getNextToken();
        while(token.valueEquals("[")){
            getToken(a);// [
            ConstExp(getExp(),a);
            getToken(a);// ]
            token = getNextToken();
        }
        if(token.valueEquals("=")) {
            getToken(a); // =
            InitVal(a);
        }
        astNode.addChild(a);
        grammar.add("<VarDef>");
    }
    // 分析变量初值
    private void InitVal(AstNode astNode){
        AstNode a =new AstNode("<InitVal>");
        Token token = getNextToken();
        if(token.valueEquals("{")){
            getToken(a);//{
            token = getNextToken();
            if(!token.valueEquals("]")){
                InitVal(a);
                Token token1 = getNextToken();
                while(token1.valueEquals(",")){
                    getToken(a);// ,
                    InitVal(a);
                    token1 = getNextToken();
                }
                token1 = getNextToken();
            }
            getToken(a);// }
        }else {
            Exp(getExp(),a);
        }
        astNode.addChild(a);
        grammar.add("<InitVal>");
    }
    // 分析函数定义
    private void FuncDef(AstNode astNode){
        AstNode a =new AstNode("<FuncDef>");
        FuncType(a);
        getToken(a);//Ident
        if(nowToken.typeEquals("IDENFR")) {
            getToken(a); // (
            if (nowToken.valueEquals("(")) {
                Token token = getNextToken();
                if (!token.valueEquals(")")) {
                    FuncFParams(a);
                }
                getToken(a);// )
            }else error();
        }else error();
        Block(a);
        astNode.addChild(a);
        grammar.add("<FuncDef>");
    }
    // 分析主函数
    private void MainFuncDef(AstNode astNode){
        AstNode a =new AstNode("<MainFuncDef>");
        getToken(a);// int
        getToken(a);// main
        getToken(a);// (
        getToken(a);// )
        Block(a);
        astNode.addChild(a);
        grammar.add("<MainFuncDef>");
    }
    // 分析函数类型
    private void FuncType(AstNode astNode){
        AstNode a =new AstNode("<FuncType>");
        getToken(a);//int void
        astNode.addChild(a);
        grammar.add("<FuncType>");
    }
    // 分析函数形参
    private void FuncFParam(AstNode astNode){
        AstNode a =new AstNode("<FuncFParam>");
        getToken(a);// int void
        getToken(a);// Ident
        Token token = getNextToken();
        if(token.valueEquals("[")){
            getToken(a); //[
            getToken(a); //]
            token = getNextToken();
            while(token.valueEquals("[")){
                getToken(a);// [
                ConstExp(getExp(),a);
                getToken(a);// ]
                token = getNextToken();
            }
        }
        astNode.addChild(a);
        grammar.add("<FuncFParam>");
    }
    // 分析函数形参表
    private void FuncFParams(AstNode astNode){
        AstNode a =new AstNode("<FuncFParams>");
        FuncFParam(a);
        Token token = getNextToken();
        while(token.valueEquals(",")){
            getToken(a);// ,
            FuncFParam(a);
            token = getNextToken();
        }
        astNode.addChild(a);
        grammar.add("<FuncFParams>");
    }
    // 分析语句块
    private void Block(AstNode astNode){
        AstNode a =new AstNode("<Block>");
        getToken(a);// {
        Token token = getNextToken();
        while(token.valueEquals("const") || token.valueEquals("int") || token.typeSymbolizeStmt()){
            if(token.valueEquals("const") || token.valueEquals("int")){
                BlockItem(a);
            }else{
                Stmt(a);
            }
            token = getNextToken();
        }
        getToken(a);// }
        astNode.addChild(a);
        grammar.add("<Block>");
    }
    // 分析语句块项
    private void BlockItem(AstNode astNode){
        Token token = getNextToken();
        if(token.valueEquals("const") || token.valueEquals("int")){
            Decl(astNode);
        }else{
            Stmt(astNode);
        }
    }
    // 分析语句
    private void Stmt(AstNode astNode){
        AstNode a =new AstNode("<Stmt>");
        Token token = getNextToken();
        if(token.typeEquals("IDENFR")){
            ArrayList<Token> exp = getExp();
            if(!getNextToken().valueEquals(";")){
                LVal(exp,a);
                getToken(a);// =
                if(getNextToken().valueEquals("getint")){
                    getToken(a);// getint
                    getToken(a);// (
                    getToken(a);// )
                    getToken(a);// ;
                }else{
                    Exp(getExp(),a);
                    getToken(a);// ;
                }
            }else {
                Exp(exp,a);
                getToken(a);// ;
            }
        }
        else if(token.typeSymbolizeExp()){
            Exp(getExp(),a);
            getToken(a);//;
        }
        else if(token.valueEquals("{")){
            Block(a);
        }
        else if(token.valueEquals("if")){
            getToken(a);// if
            getToken(a);// (
            Cond(a);
            getToken(a);// )
            Stmt(a);
            token = getNextToken();
            if(token.valueEquals("else")){
                getToken(a);// else
                Stmt(a);
            }
        }
        else if(token.valueEquals("while")){
            getToken(a);// while
            getToken(a);// (
            Cond(a);
            getToken(a);// )
            Stmt(a);
        }
        else if(token.valueEquals("break")){
            getToken(a);// break
            getToken(a);// ;
        }
        else if(token.valueEquals("continue")){
            getToken(a);// continue
            getToken(a);// ;
        }
        else if(token.valueEquals("return")){
            getToken(a);// return
            token = getNextToken();
            if(token.typeSymbolizeExp()){
                Exp(getExp(),a);
            }
            getToken(a);// ;
        }
        else if(token.valueEquals("printf")){
            getToken(a);// printf
            getToken(a);// (
            getToken(a);// String
            token = getNextToken();
            while(token.valueEquals(",")){
                getToken(a);//,
                Exp(getExp(),a);
                token = getNextToken();
            }
            getToken(a);// )
            getToken(a);// ;
        }
        else if(token.valueEquals(";")){
            getToken(a);//;
        }
        astNode.addChild(a);
        grammar.add("<Stmt>");
    }
    // 分析数字
    private void Number(Token token,AstNode astNode){
        AstNode a =new AstNode("<Number>");
        grammar.add(token.toString());
        a.addChild(new AstNode(token.getValue(),token.getType()));
        astNode.addChild(a);
        grammar.add("<Number>");
    }
    // 获取表达式
    private ArrayList<Token> getExp(){
        ArrayList<Token> exp = new ArrayList<>();
        boolean inFun = false;
        int funcFlag = 0;
        int flag1 = 0;
        int flag2 = 0;
        Token token = getNextToken();
        while (true){
            if(token.valueEquals(";") || token.valueEquals("=") || token.valueEquals("}")){
                break;
            }
            if(token.valueEquals(",") && !inFun){
                break;
            }
            if(token.typeEquals("IDENFR")){
                if(getNext2Token().valueEquals("("))
                    inFun = true;
            }
            if(token.valueEquals("(")){
                flag1++;
                if(inFun){
                    funcFlag++;
                }
            }
            if(token.valueEquals(")")){
                flag1--;
                if(inFun){
                    funcFlag--;
                    if(funcFlag==0){
                        inFun = false;
                    }
                }
            }
            if(token.valueEquals("[")){
                flag2++;
            }
            if(token.valueEquals("]")){
                flag2--;
            }
            if(flag1<0 || flag2<0) break;
            getTokenWithoutAdd();
            exp.add(nowToken);
            token = getNextToken();
        }
        return exp;
    }
    // 分隔表达式
    private Exps divide(ArrayList<Token> exp, ArrayList<String> symbol){
        ArrayList<ArrayList<Token>> exps = new ArrayList<>();
        ArrayList<Token> exp1 = new ArrayList<>();
        ArrayList<Token> symblos = new ArrayList<>();
        boolean unaryFlag = false;
        int flag1 = 0;
        int flag2 = 0;
        for(int i=0; i<exp.size();i++){
            Token token = exp.get(i);
            if(token.valueEquals("(")){
                flag1++;
            }
            if(token.valueEquals(")")){
                flag1--;
            }
            if(token.valueEquals("[")){
                flag2++;
            }
            if(token.valueEquals("]")){
                flag2--;
            }
            if(symbol.contains(token.type) && flag1==0 && flag2==0){
                if(token.typeOfUnary()){
                    if(!unaryFlag){
                        exp1.add(token);
                        continue;
                    }
                }
                exps.add(exp1);
                symblos.add(token);
                exp1 = new ArrayList<>();
            }else{
                exp1.add(token);
            }
            unaryFlag = token.typeEquals("IDENFR") ||
                    token.valueEquals(")") ||
                    token.typeEquals("INTCON") ||
                    token.valueEquals("]");
        }
        exps.add(exp1);
        return new Exps(exps,symblos);
    }
    // 分析表达式
    private void Exp(ArrayList<Token> exp,AstNode astNode){
        AstNode a =new AstNode("<Exp>");
        AddExp(exp,a);
        astNode.addChild(a);
        grammar.add("<Exp>");
    }
    // 分析条件表达式
    private void Cond(AstNode astNode){
        AstNode a =new AstNode("<Cond>");
        LOrExp(getExp(),a);
        astNode.addChild(a);
        grammar.add("<Cond>");
    }
    // 分析左值表达式
    private void LVal(ArrayList<Token> exp,AstNode astNode){
        AstNode a = new AstNode("<LVal>");
        a.addChild(new AstNode(exp.get(0).getValue(),exp.get(0).getType()));
        grammar.add(exp.get(0).toString());//Ident

        if(exp.size()>0){
            ArrayList<Token> exp1 = new ArrayList<>();
            int flag = 0;
            for (int i = 1; i < exp.size(); i++) {
                Token token = exp.get(i);
                if (token.valueEquals("[")) {
                    flag++;
                    if (flag == 1) {
                        a.addChild(new AstNode(token.getValue(),token.getType()));
                        grammar.add(token.toString());
                        exp1 = new ArrayList<>();
                    } else {
                        exp1.add(token);
                    }
                } else if (token.valueEquals("]")) {
                    flag--;
                    if (flag == 0) {
                        Exp(exp1,a);
                        a.addChild(new AstNode(token.getValue(),token.getType()));
                        grammar.add(token.toString());
                    } else {
                        exp1.add(token);
                    }
                } else {
                    exp1.add(token);
                }
            }
        }
        astNode.addChild(a);
        grammar.add("<LVal>");
    }
    // 分析常量表达式
    private void ConstExp(ArrayList<Token> exp,AstNode astNode){
        AstNode a =new AstNode("<ConstExp>");
        AddExp(exp,a);
        astNode.addChild(a);
        grammar.add("<ConstExp>");
    }
    // 分析基本表达式
    private void PrimaryExp(ArrayList<Token> exp,AstNode astNode){
        AstNode a =new AstNode("<PrimaryExp>");
        Token token = exp.get(0);
        if(token.valueEquals("(")){

            grammar.add(token.toString()); // (
            a.addChild(new AstNode(token.getValue(),token.getType()));

            Exp(new ArrayList<>(exp.subList(1, exp.size() - 1)),a);

            grammar.add(exp.get(exp.size()-1).toString());
            a.addChild(new AstNode(exp.get(exp.size()-1).getValue(),exp.get(exp.size()-1).getType()));

        }else if(token.typeEquals("IDENFR")){
            LVal(exp,a);
        }else if(token.typeEquals("INTCON")){
            Number(exp.get(0),a);
        }else{
            error();
        }
        astNode.addChild(a);
        grammar.add("<PrimaryExp>");
    }
    // 分析一元表达式
    private void UnaryExp(ArrayList<Token> exp,AstNode astNode) {
        AstNode a =new AstNode("<UnaryExp>");
        Token token = exp.get(0);
        if (token.typeEquals("PLUS") || token.typeEquals("MINU") || token.typeEquals("NOT")) {
            //remove UnaryOp
            UnaryOp(exp.get(0),a);
            UnaryExp(new ArrayList<>(exp.subList(1, exp.size())),a);
        } else if (exp.size() == 1) {
            PrimaryExp(exp,a);
        } else {
            if (exp.get(0).typeEquals("IDENFR") && exp.get(1).typeEquals("LPARENT")) {
                //remove Ident ( )
                grammar.add(exp.get(0).toString());
                a.addChild(new AstNode(exp.get(0).getValue(),exp.get(0).getType()));
                grammar.add(exp.get(1).toString());
                a.addChild(new AstNode(exp.get(1).getValue(),exp.get(1).getType()));

                if (exp.size() > 3) {
                    FuncRParams(new ArrayList<>(exp.subList(2, exp.size() - 1)),a);
                }
                grammar.add(exp.get(exp.size() - 1).toString());
                a.addChild(new AstNode(exp.get(exp.size() - 1).getValue(),exp.get(exp.size() - 1).getType()));
            } else {
                PrimaryExp(exp,a);
            }
        }
        astNode.addChild(a);
        grammar.add("<UnaryExp>");
    }
    // 分析单目运算符
    private void UnaryOp(Token token,AstNode astNode){
        AstNode a =new AstNode("<UnaryOp>");
        grammar.add(token.toString());
        a.addChild(new AstNode(token.getValue(),token.getType()));
        astNode.addChild(a);
        grammar.add("<UnaryOp>");
    }
    // 分析函数实参表
    private void FuncRParams(ArrayList<Token> exp,AstNode astNode) {
        AstNode a =new AstNode("<FuncRParams>");
        Exps exps = divide(exp, new ArrayList<>(Arrays.asList("COMMA")));
        int i = 0;
        for (ArrayList<Token> exp1 : exps.getTokens()) {
            Exp(exp1,a);
            if (i < exps.getSymbols().size()) {
                a.addChild(new AstNode(exps.getSymbols().get(i).getValue(),exps.getSymbols().get(i).getType()));
                grammar.add(exps.getSymbols().get(i++).toString());
            }
        }
        astNode.addChild(a);
        grammar.add("<FuncRParams>");
    }
    // 分析乘除模表达式
    private void MulExp(ArrayList<Token> exp,AstNode astNode){
        Exps exps = divide(exp,new ArrayList<>(Arrays.asList("MULT","DIV","MOD")));
        int i = 0;
        for(ArrayList<Token> exp1:exps.getTokens()){
            AstNode a =new AstNode("<MulExp>");
            UnaryExp(exp1,a);
            astNode.addChild(a);
            grammar.add("<MulExp>");
            if(i < exps.getSymbols().size()){
                astNode.addChild(new AstNode(exps.getSymbols().get(i).getValue(),exps.getSymbols().get(i).getType()));
                grammar.add(exps.getSymbols().get(i++).toString());
            }
        }
    }
    // 分析加减表达式
    private void AddExp(ArrayList<Token> exp,AstNode astNode){
        Exps exps = divide(exp,new ArrayList<>(Arrays.asList("PLUS","MINU")));
        int i = 0;
        for(ArrayList<Token> exp1:exps.getTokens()){
            AstNode a =new AstNode("<AddExp>");
            MulExp(exp1,a);
            astNode.addChild(a);
            grammar.add("<AddExp>");
            if(i < exps.getSymbols().size()){
                astNode.addChild(new AstNode(exps.getSymbols().get(i).getValue(),exps.getSymbols().get(i).getType()));
                grammar.add(exps.getSymbols().get(i++).toString());
            }
        }

    }
    // 分析关系表达式
    private void RelExp(ArrayList<Token> exp,AstNode astNode){
        Exps exps = divide(exp,new ArrayList<>(Arrays.asList("LEQ","LSS","GRE","GEQ")));
        int i = 0;
        for(ArrayList<Token> exp1:exps.getTokens()){
            AstNode a =new AstNode("<RelExp>");
            AddExp(exp1,a);
            astNode.addChild(a);
            grammar.add("<RelExp>");
            if(i < exps.getSymbols().size()){
                astNode.addChild(new AstNode(exps.getSymbols().get(i).getValue(),exps.getSymbols().get(i).getType()));
                grammar.add(exps.getSymbols().get(i++).toString());
            }
        }
    }
    // 分析相等性表达式
    private void EqExp(ArrayList<Token> exp,AstNode astNode){
        Exps exps = divide(exp,new ArrayList<>(Arrays.asList("EQL","NEQ")));
        int i = 0;
        for(ArrayList<Token> exp1:exps.getTokens()){
            AstNode a =new AstNode("<EqExp>");
            RelExp(exp1,a);
            astNode.addChild(a);
            grammar.add("<EqExp>");
            if(i < exps.getSymbols().size()){
                astNode.addChild(new AstNode(exps.getSymbols().get(i).getValue(),exps.getSymbols().get(i).getType()));
                grammar.add(exps.getSymbols().get(i++).toString());
            }
        }
    }
    // 分析逻辑与表达式
    private void LAndExp(ArrayList<Token> exp,AstNode astNode){
        Exps exps = divide(exp,new ArrayList<>(Arrays.asList("AND")));
        int i = 0;
        for(ArrayList<Token> exp1:exps.getTokens()){
            AstNode a =new AstNode("<LAndExp>");
            EqExp(exp1,a);
            astNode.addChild(a);
            grammar.add("<LAndExp>");
            if(i < exps.getSymbols().size()){
                astNode.addChild(new AstNode(exps.getSymbols().get(i).getValue(),exps.getSymbols().get(i).getType()));
                grammar.add(exps.getSymbols().get(i++).toString());
            }
        }
    }
    // 分析逻辑或表达式
    private void LOrExp(ArrayList<Token> exp,AstNode astNode){
        Exps exps = divide(exp,new ArrayList<>(Arrays.asList("OR")));
        int i = 0;
        for(ArrayList<Token> exp1:exps.getTokens()){
            AstNode a =new AstNode("<LOrExp>");
            LAndExp(exp1,a);
            astNode.addChild(a);
            grammar.add("<LOrExp>");
            if(i < exps.getSymbols().size()){
                astNode.addChild(new AstNode(exps.getSymbols().get(i).getValue(),exps.getSymbols().get(i).getType()));
                grammar.add(exps.getSymbols().get(i++).toString());
            }
        }
    }

    // 输出语法分析结果
    public void print(BufferedWriter writer) throws IOException {
//        for (String str : grammar) {
//            writer.write(str + "\n");
//        }
        dfs(RootAst,writer);
        writer.flush();
        writer.close();
    }
    // 语法树输出结果
    public void output(AstNode astNode,BufferedWriter writer)throws IOException{
        if(astNode.getType()==null){
            writer.write(astNode.getContent()+"\n");
        }else{
            String str = astNode.toString();
           writer.write(str+"\n");
        }
    }
    // 深度优先输出
    private void dfs(AstNode astNode,BufferedWriter writer) throws IOException {
        if(astNode.getChilds().isEmpty()){
            output(astNode,writer);
        }else{
            for(AstNode astNode1:astNode.getChilds()){
                dfs(astNode1,writer);
            }
            output(astNode,writer);
        }
    }

}
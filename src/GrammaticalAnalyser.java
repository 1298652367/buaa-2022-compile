import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class GrammaticalAnalyser {
    private ArrayList<Token> tokens;
    private int index = 0;
    private Token nowToken;
    private ArrayList<String> grammar;

    public GrammaticalAnalyser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        grammar = new ArrayList<>();
        ComUnit();
    }

    //读到终结符，将其加入语法grammar之中
    private void getToken(){
        nowToken = tokens.get(index);
        grammar.add(nowToken.toString());
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
    private void ComUnit(){
        Token token = getNextToken();
        while (token.valueEquals("const") || (
                token.valueEquals("int") && getNext2Token().typeEquals("IDENFR") && !getNext3Token().valueEquals("("))) {

            Decl();
            token = getNextToken();
        }
        while(token.valueEquals("void") || (
                (token.valueEquals("int") && !getNext2Token().valueEquals("main")))) {
            FuncDef();
            token = getNextToken();
        }
        if(token.valueEquals("int") && getNext2Token().valueEquals("main")){
            MainFuncDef();
        }else {
            error();
        }
        grammar.add("<CompUnit>");
    }
    // 分析声明
    private void Decl(){
        Token token = getNextToken();
        if(token.valueEquals("const")){
            ConstDecl();
        }else if(token.valueEquals("int")){
            VarDecl();
        }else{
            error();
        }
    }
    // 分析常量声明
    private void ConstDecl(){
        getToken();//const
        getToken();//int
        if(nowToken.valueEquals("int")){
            BType();
        }else{
            error();
        }
        ConstDef();
        Token token = getNextToken();
        while (token.valueEquals(",")){
            getToken();// ,
            ConstDef();
            token = getNextToken();
        }
        getToken();// ;
        grammar.add("<ConstDecl>");
    }
    // 分析变量声明
    private void VarDecl(){
        getToken(); // int
        if (nowToken.valueEquals("int")) {
            BType();
        } else {
            error();
        }
        VarDef();
        Token token = getNextToken();
        while(token.valueEquals(",")){
            getToken();//,
            VarDef();
            token = getNextToken();
        }
        getToken();// ;
        grammar.add("<VarDecl>");
    }
    // 分析基本类型
    private void BType(){
    }
    // 分析常数定义
    private void ConstDef(){
        getToken();//Ident
        Token token = getNextToken();
        while(token.valueEquals("[")){
            getToken();// [
            ConstExp(getExp());
            getToken();// ]
            if(!nowToken.valueEquals("]")) error();
            token = getNextToken();
        }
        getToken(); // =
        ConstInitVal();
        grammar.add("<ConstDef>");
    }
    // 分析常量初值
    private void ConstInitVal(){
        Token token = getNextToken();
        if(token.valueEquals("{")){
            getToken();// {
            token = getNextToken();
            if(!token.valueEquals("}")) {
                ConstInitVal();
                Token token1 = getNextToken();
                while (token1.valueEquals(",")){
                    getToken();// ,
                    ConstInitVal();
                    token1 = getNextToken();
                }
            }
            getToken();// }
        }else{
            ConstExp(getExp());
        }
        grammar.add("<ConstInitVal>");
    }
    // 分析变量定义
    private void VarDef(){
        getToken();//Ident
        Token token = getNextToken();
        while(token.valueEquals("[")){
            getToken();// [
            ConstExp(getExp());
            getToken();// ]
            token = getNextToken();
        }
        if(token.valueEquals("=")) {
            getToken(); // =
            InitVal();
        }
        grammar.add("<VarDef>");
    }
    // 分析变量初值
    private void InitVal(){
        Token token = getNextToken();
        if(token.valueEquals("{")){
            getToken();//{
            token = getNextToken();
            if(!token.valueEquals("]")){
                InitVal();
                Token token1 = getNextToken();
                while(token1.valueEquals(",")){
                    getToken();// ,
                    InitVal();
                    token1 = getNextToken();
                }
                token1 = getNextToken();
            }
            getToken();// }
        }else {
            Exp(getExp());
        }
        grammar.add("<InitVal>");
    }
    // 分析函数定义
    private void FuncDef(){
        FuncType();
        getToken();//Ident
        if(nowToken.typeEquals("IDENFR")) {
            getToken(); // (
            if (nowToken.valueEquals("(")) {
               Token token = getNextToken();
                if (!token.valueEquals(")")) {
                   FuncFParams();
                }
                getToken();// )
            }else error();
        }else error();
        Block();
        grammar.add("<FuncDef>");

    }
    // 分析主函数
    private void MainFuncDef(){
        getToken();// int
        getToken();// main
        getToken();// (
        getToken();// )
        Block();
        grammar.add("<MainFuncDef>");
    }
    // 分析函数类型
    private void FuncType(){
        getToken();//int void
        grammar.add("<FuncType>");
    }
    // 分析函数形参
    private void FuncFParam(){
        getToken();// int void
        getToken();// Ident
        Token token = getNextToken();
        if(token.valueEquals("[")){
            getToken(); //[
            getToken(); //]
            token = getNextToken();
            while(token.valueEquals("[")){
                getToken();// [
                ConstExp(getExp());
                getToken();// ]
                token = getNextToken();
            }
        }
        grammar.add("<FuncFParam>");
    }
    // 分析函数形参表
    private void FuncFParams(){
        FuncFParam();
        Token token = getNextToken();
        while(token.valueEquals(",")){
            getToken();// ,
            FuncFParam();
            token = getNextToken();
        }
        grammar.add("<FuncFParams>");
    }
    // 分析语句块
    private void Block(){
        getToken();// {
        Token token = getNextToken();
        while(token.valueEquals("const") || token.valueEquals("int") || token.typeSymbolizeStmt()){
            if(token.valueEquals("const") || token.valueEquals("int")){
                BlockItem();
            }else{
                Stmt();
            }
            token = getNextToken();
        }
        getToken();// }
        grammar.add("<Block>");
    }
    // 分析语句块项
    private void BlockItem(){
        Token token = getNextToken();
        if(token.valueEquals("const") || token.valueEquals("int")){
            Decl();
        }else{
            Stmt();
        }
    }
    // 分析语句
    private void Stmt(){
        Token token = getNextToken();
        if(token.typeEquals("IDENFR")){
            ArrayList<Token> exp = getExp();
            if(!getNextToken().valueEquals(";")){
                LVal(exp);
                getToken();// =
                if(getNextToken().valueEquals("getint")){
                    getToken();// getint
                    getToken();// (
                    getToken();// )
                    getToken();// ;
                }else{
                    Exp(getExp());
                    getToken();// ;
                }
            }else {
                Exp(exp);
                getToken();// ;
            }
        }else if(token.typeSymbolizeExp()){
            Exp(getExp());
            getToken();//;
        }else if(token.valueEquals("{")){
            Block();
        }else if(token.valueEquals("if")){
            getToken();// if
            getToken();// (
            Cond();
            getToken();// )
            Stmt();
            token = getNextToken();
            if(token.valueEquals("else")){
                getToken();// else
                Stmt();
            }
        }else if(token.valueEquals("while")){
            getToken();// while
            getToken();// (
            Cond();
            getToken();// )
            Stmt();
        }else if(token.valueEquals("break")){
            getToken();// break
            getToken();// ;
        }else if(token.valueEquals("continue")){
            getToken();// continue
            getToken();// ;
        }else if(token.valueEquals("return")){
            getToken();// return
            token = getNextToken();
           if(token.typeSymbolizeExp()){
               Exp(getExp());
           }
           getToken();
        }else if(token.valueEquals("printf")){
            getToken();// printf
            getToken();// (
            getToken();// String
            token = getNextToken();
            while(token.valueEquals(",")){
                getToken();//,
                Exp(getExp());
                token = getNextToken();
            }
            getToken();// )
            getToken();// ;
        }else if(token.valueEquals(";")){
            getToken();//;
        }
        grammar.add("<Stmt>");
    }
    // 分析数字
    private void Number(Token token){
        grammar.add(token.toString());
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
    private void Exp(ArrayList<Token> exp){
        AddExp(exp);
        grammar.add("<Exp>");
    }
    // 分析条件表达式
    private void Cond(){
        LOrExp(getExp());
        grammar.add("<Cond>");
    }
    // 分析左值表达式
    private void LVal(ArrayList<Token> exp){
        grammar.add(exp.get(0).toString());//Ident
        if(exp.size()>0){
            ArrayList<Token> exp1 = new ArrayList<>();
            int flag = 0;
            for (int i = 1; i < exp.size(); i++) {
                Token token = exp.get(i);
                if (token.typeEquals("LBRACK")) {
                    flag++;
                    if (flag == 1) {
                        grammar.add(token.toString());
                        exp1 = new ArrayList<>();
                    } else {
                        exp1.add(token);
                    }
                } else if (token.typeEquals("RBRACK")) {
                    flag--;
                    if (flag == 0) {
                        Exp(exp1);
                        grammar.add(token.toString());
                    } else {
                        exp1.add(token);
                    }
                } else {
                    exp1.add(token);
                }
            }
        }
        grammar.add("<LVal>");
    }
    // 分析常量表达式
    private void ConstExp(ArrayList<Token> exp){
        AddExp(exp);
        grammar.add("<ConstExp>");
    }
    // 分析基本表达式
    private void PrimaryExp(ArrayList<Token> exp){
        Token token = exp.get(0);
        if(token.valueEquals("(")){
            grammar.add(token.toString()); // (
            Exp(new ArrayList<>(exp.subList(1, exp.size() - 1)));
            grammar.add(exp.get(exp.size()-1).toString());
        }else if(token.typeEquals("IDENFR")){
            LVal(exp);
        }else if(token.typeEquals("INTCON")){
            Number(exp.get(0));
        }else{
            error();
        }
        grammar.add("<PrimaryExp>");
    }
    // 分析一元表达式
    private void UnaryExp(ArrayList<Token> exp) {
        Token token = exp.get(0);
        if (token.typeEquals("PLUS") || token.typeEquals("MINU") || token.typeEquals("NOT")) {
            //remove UnaryOp
            UnaryOp(exp.get(0));
            UnaryExp(new ArrayList<>(exp.subList(1, exp.size())));
        } else if (exp.size() == 1) {
            PrimaryExp(exp);
        } else {
            if (exp.get(0).typeEquals("IDENFR") && exp.get(1).typeEquals("LPARENT")) {
                //remove Ident ( )
                grammar.add(exp.get(0).toString());
                grammar.add(exp.get(1).toString());
                if (exp.size() > 3) {
                    FuncRParams(new ArrayList<>(exp.subList(2, exp.size() - 1)));
                }
                grammar.add(exp.get(exp.size() - 1).toString());
            } else {
                PrimaryExp(exp);
            }
        }
        grammar.add("<UnaryExp>");
    }
    // 分析单目运算符
    private void UnaryOp(Token token){
        grammar.add(token.toString());
        grammar.add("<UnaryOp>");
    }
    // 分析函数实参表
    private void FuncRParams(ArrayList<Token> exp) {
        Exps exps = divide(exp, new ArrayList<>(Arrays.asList("COMMA")));
        int j = 0;
        for (ArrayList<Token> exp1 : exps.getTokens()) {
            Exp(exp1);
            if (j < exps.getSymbols().size()) {
                grammar.add(exps.getSymbols().get(j++).toString());
            }
        }
        grammar.add("<FuncRParams>");
    }
    // 分析乘除模表达式
    private void MulExp(ArrayList<Token> exp){
        Exps exps = divide(exp,new ArrayList<>(Arrays.asList("MULT","DIV","MOD")));
        int i = 0;
        for(ArrayList<Token> exp1:exps.getTokens()){
            UnaryExp(exp1);
            grammar.add("<MulExp>");
            if(i < exps.getSymbols().size()){
                grammar.add(exps.getSymbols().get(i++).toString());
            }
        }
    }
    // 分析加减表达式
    private void AddExp(ArrayList<Token> exp){
        Exps exps = divide(exp,new ArrayList<>(Arrays.asList("PLUS","MINU")));
        int i = 0;
        for(ArrayList<Token> exp1:exps.getTokens()){
            MulExp(exp1);
            grammar.add("<AddExp>");
            if(i < exps.getSymbols().size()){
                grammar.add(exps.getSymbols().get(i++).toString());
            }
        }

    }
    // 分析关系表达式
    private void RelExp(ArrayList<Token> exp){
        Exps exps = divide(exp,new ArrayList<>(Arrays.asList("LEQ","LSS","GRE","GEQ")));
        int i = 0;
        for(ArrayList<Token> exp1:exps.getTokens()){
            AddExp(exp1);
            grammar.add("<RelExp>");
            if(i < exps.getSymbols().size()){
                grammar.add(exps.getSymbols().get(i++).toString());
            }
        }
    }
    // 分析相等性表达式
    private void EqExp(ArrayList<Token> exp){
        Exps exps = divide(exp,new ArrayList<>(Arrays.asList("EQL","NEQ")));
        int i = 0;
        for(ArrayList<Token> exp1:exps.getTokens()){
            RelExp(exp1);
            grammar.add("<EqExp>");
            if(i < exps.getSymbols().size()){
                grammar.add(exps.getSymbols().get(i++).toString());
            }
        }
    }
    // 分析逻辑与表达式
    private void LAndExp(ArrayList<Token> exp){
        Exps exps = divide(exp,new ArrayList<>(Arrays.asList("AND")));
        int i = 0;
        for(ArrayList<Token> exp1:exps.getTokens()){
            EqExp(exp1);
            grammar.add("<LAndExp>");
            if(i < exps.getSymbols().size()){
                grammar.add(exps.getSymbols().get(i++).toString());
            }
        }
    }
    // 分析逻辑或表达式
    private void LOrExp(ArrayList<Token> exp){
        Exps exps = divide(exp,new ArrayList<>(Arrays.asList("OR")));
        int i = 0;
        for(ArrayList<Token> exp1:exps.getTokens()){
            LAndExp(exp1);
            grammar.add("<LOrExp>");
            if(i < exps.getSymbols().size()){
                grammar.add(exps.getSymbols().get(i++).toString());
            }
        }
    }

    // 输出语法分析结果
    public void print(BufferedWriter writer) throws IOException {
        for (String str : grammar) {
            writer.write(str + "\n");
        }
    }

}

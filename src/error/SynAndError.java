package error;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import data.*;
import lexical.*;
import syntax.*;
public class SynAndError {
    private ArrayList<Token> tokens;
    private int index = 0;
    private Token nowToken;
    private ArrayList<String> grammar;

    public ArrayList<Error> errors = new ArrayList<Error>();
    private HashMap<Integer, Symbols> symbols = new HashMap<>();
    private HashMap<String, Function> functions = new HashMap<>();
    private boolean needReturn = false;
    private int whileFlag = 0;
    private int area = -1;     //错误处理符号表结构

    public SynAndError(ArrayList<Token> tokens) {
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

    // 进入新的符号表
    private void addArea() {
        area++;
        symbols.put(area, new Symbols());
    }
    // 移除当前符号表
    private void removeArea() {
        symbols.remove(area);
        area--;
    }
    // 当前单词在表中
    private boolean hasSymbol(Token token) {
        for (Symbols s : symbols.values()) {
            if (s.hasSymbol(token)) {
                return true;
            }
        }
        return false;
    }
    // 当前区域存在这个单词
    private boolean hasSymbolInThisArea(Token token) {
        return symbols.get(area).hasSymbol(token);
    }

    private void addSymbol(Token token, String type, int intType) {
        symbols.get(area).addSymbol(type, intType, token);
    }
    private Symbol getSymbol(Token token) {
        Symbol symbol = null;
        for (Symbols s : symbols.values()) {
            if (s.hasSymbol(token)) {
                symbol = s.getSymbol(token);
            }
        }
        return symbol;
    }

    // 分析编译单元
    private void ComUnit(){
        addArea();

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
        removeArea();
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
    // 分析常量声明  --i
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
        checkSemicn();

        grammar.add("<ConstDecl>");
    }
    // 分析变量声明  --i
    private void VarDecl(){
        getToken(); // int
        VarDef();
        Token token = getNextToken();
        while(token.valueEquals(",")){
            getToken();//,
            VarDef();
            token = getNextToken();
        }
        checkSemicn();

        grammar.add("<VarDecl>");
    }
    // 分析基本类型
    private void BType(){
    }
    // 分析常数定义  --b,k
    private void ConstDef(){
        getToken();//Ident
        Token ident = nowToken;
        if(hasSymbolInThisArea(nowToken))
            error("b");
        Token token = getNextToken();
        int intType = 0;
        while(token.valueEquals("[")){
            intType++;
            getToken();// [
            ConstExp(getExp());
            checkBrack();
            token = getNextToken();
        }
        addSymbol(ident,"const",intType);
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
    // 分析变量定义  --b,k
    private void VarDef(){
        getToken();//Ident
        Token ident = nowToken;
        if(hasSymbolInThisArea(nowToken)) error("b");
        int intType = 0;
        Token token = getNextToken();
        while(token.valueEquals("[")){
            intType++;
            getToken();// [
            ConstExp(getExp());
            checkBrack();
            token = getNextToken();
        }
        if(token.valueEquals("=")) {
            getToken(); // =
            InitVal();
        }
        addSymbol(ident,"var",intType);
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
    // 分析函数定义  --b,g,j
    private void FuncDef(){
        Function function = null;
        ArrayList<Integer> paras = new ArrayList<>();
        String returnType = FuncType();
        getToken();//Ident
        if(functions.containsKey((nowToken.getValue()))){
            error("b");
        }
        function = new Function(nowToken,returnType);

        addArea();

        getToken(); // (
        Token token = getNextToken();
        if (token.valueEquals("void")||token.valueEquals("int")) {
            paras = FuncFParams();
        }
        checkParent();// )
        function.setParas(paras);
        functions.put(function.getContent(),function);
        needReturn = function.getReturnType().equals("int");
        boolean isReturn = Block(true);
        if(needReturn && !isReturn)
            error("g");

        removeArea();
        grammar.add("<FuncDef>");

    }
    // 分析主函数    --b,g,j
    private void MainFuncDef(){
        getToken();// int
        getToken();// main
        if(functions.containsKey(nowToken.getValue())) error("b");
        else {
            Function function = new Function(nowToken, "int");
            function.setParas(new ArrayList<>());
            functions.put("main", function);
        }
        getToken();// (
        checkParent();// )
        needReturn = true;
        boolean isReturn = Block(false);
        if(needReturn && !isReturn) error("g");
        grammar.add("<MainFuncDef>");
    }
    // 分析函数类型
    private String FuncType(){
        getToken();//int void
        grammar.add("<FuncType>");
        return nowToken.getValue();
    }
    // 分析函数形参  --b,k
    private int FuncFParam(){
        int paraType = 0;
        getToken();// int void
        getToken();// Ident
        Token ident = nowToken;
        if(hasSymbolInThisArea(ident))
            error("b");
        Token token = getNextToken();
        if(token.valueEquals("[")){
            paraType++;
            getToken(); //[
            checkBrack(); //]
            token = getNextToken();
            while(token.valueEquals("[")){
                paraType++;
                getToken();// [
                ConstExp(getExp());
                checkBrack();// ]
                token = getNextToken();
            }
        }
        addSymbol(ident,"para",paraType);
        grammar.add("<FuncFParam>");
        return paraType;
    }
    // 分析函数形参表
    private void FuncRParams(Token ident,ArrayList<Token> exp,ArrayList<Integer> paras){
        ArrayList<Integer> rparas = new ArrayList<>();
        Exps exps = divide(exp,new ArrayList<>(Arrays.asList("COMMA")));
        int i=0;
        for(ArrayList<Token> exp1: exps.getTokens()){
            int intType = Exp(exp1);
            rparas.add(intType);
            if(i<exps.getSymbols().size()){
                grammar.add(exps.getSymbols().get(i++).toString());
            }
        }
        if(paras!=null){
            if_match(ident,paras,rparas);
        }
        grammar.add("<FuncFParams>");
    }

    private ArrayList<Integer> FuncFParams(){
        ArrayList<Integer> paras = new ArrayList<>();
        int paraType = FuncFParam();
        paras.add(paraType);
        Token token = getNextToken();
        while (token.valueEquals(",")){
            getToken();//,
            paraType = FuncFParam();
            paras.add(paraType);
            token = getNextToken();
        }
        grammar.add("<FuncFParams>");
        return paras;
    }

    // 分析语句块
    private boolean Block(boolean isFunc){
        getToken();// {
        if(!isFunc) {
            addArea();
        }
        Token token = getNextToken();
        boolean isReturn = false;
        while(token.valueEquals("const") || token.valueEquals("int") || token.typeSymbolizeStmt()){
            if(token.valueEquals("const") || token.valueEquals("int")){
                isReturn = BlockItem();
            }else{
                isReturn = Stmt();
            }
            token = getNextToken();
        }
        getToken();// }
        if(!isFunc) removeArea();
        grammar.add("<Block>");
        return isReturn;
    }
    // 分析语句块项
    private boolean BlockItem(){
        Token token = getNextToken();
        boolean isReturn = false;
        if(token.valueEquals("const") || token.valueEquals("int")){
            Decl();
        }else{
            isReturn = Stmt();
        }
        return isReturn;
    }
    // 分析语句
    private boolean Stmt(){
        boolean isReturn = false;
        Token token = getNextToken();
        if(token.typeEquals("IDENFR")){     // h,i,j
            ArrayList<Token> exp = getExp();
            if(getNextToken().valueEquals("=")){
                LVal(exp);
                if(isConst(token))
                    error("h",token.getLineNum());
                getToken();// =
                if(getNextToken().valueEquals("getint")){
                    getToken();// getint
                    getToken();// (
                    checkParent();// )
                    checkSemicn();// ;
                }else{
                    Exp(getExp());
                    checkSemicn();// ;
                }
            }else {
                Exp(exp);
                checkSemicn();// ;
            }
        }
        else if(token.typeSymbolizeBeginOfExp()){ //h,i
            Exp(getExp());
            checkSemicn();//;
        }
        else if(token.valueEquals("{")){
            Block(false);
        }
        else if(token.valueEquals("if")){ // j
            getToken();// if
            getToken();// (
            Cond();
            checkParent();// )
            Stmt();
            token = getNextToken();
            if(token.valueEquals("else")){
                getToken();// else
                Stmt();
            }
        }
        else if(token.valueEquals("while")){ // j
            getToken();// while
            whileFlag++;
            getToken();// (
            Cond();
            checkParent();// )
            Stmt();
            whileFlag--;
        }
        else if(token.valueEquals("break")){ //i,m
            getToken();// break
            if(whileFlag == 0)
                error("m");
            checkSemicn();// ;
        }
        else if(token.valueEquals("continue")){ //i,m
            getToken();// continue
            if(whileFlag == 0)
                error("m");
            checkSemicn();// ;
        }
        else if(token.valueEquals("return")){ //f,i
            getToken();// return
            isReturn = true;
            token = getNextToken();
           if(token.typeSymbolizeBeginOfExp()){
               if(!needReturn){
                   error("f");
               }
               Exp(getExp());
           }
           checkSemicn();//;
        }
        else if(token.valueEquals("printf")){ //i,j,l
            getToken();// printf
            Token prt = nowToken;
            getToken();// (
            getToken();// String
            Token str = nowToken;//string
            token = getNextToken();
            int para = 0;
            while(token.valueEquals(",")){
                getToken();//,
                Exp(getExp());
                para++;
                token = getNextToken();
            }
            if(str.isFormatIllegal()){
                error("a", str.getLineNum());
            }
            if(para!= str.getFormatNum()){
                error("l",prt.getLineNum());
            }
            checkParent();// )
            checkSemicn();// ;
        }
        else if(token.valueEquals(";")){
            getToken();//;
        }
        grammar.add("<Stmt>");
        return isReturn;
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
        Token preToken=null;
        Token token = getNextToken();
        while (true){
            if(token.valueEquals(";") || token.valueEquals("=")
                    || token.valueEquals("}") || token.typeSymbolizeValidateStmt()){
                break;
            }
            if(token.valueEquals(",") && !inFun){
                break;
            }
            if( preToken !=null) {
                if((preToken.typeEquals("INTCON")||preToken.typeEquals("IDENFR"))
                    && (token.typeEquals("INTCON")|| token.typeEquals("IDENFR"))){
                    break;
                }
                if((preToken.valueEquals(")")||preToken.valueEquals("]"))
                        && (token.typeEquals("INTCON")|| token.typeEquals("IDENFR"))){
                    break;
                }
               if(flag1==0 && flag2==0){
                   if(preToken.typeEquals("INTCON")&&
                           (token.typeEquals("LBRACK")||token.typeEquals("LBRACE"))){
                       break;
                   }
               }
            }
            if(token.typeOfNotInExp()){
                break;
            }
            if (token.typeEquals("IDENFR")) {
                if (getNext2Token().valueEquals("("))
                    inFun = true;
            }
            if (token.valueEquals("(")) {
                flag1++;
                if (inFun) {
                    funcFlag++;
                }
            }
            if (token.valueEquals(")")) {
                flag1--;
                if (inFun) {
                    funcFlag--;
                    if (funcFlag == 0) {
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
        System.out.println(exp);
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
    private int Exp(ArrayList<Token> exp){
        int intType=AddExp(exp);
        grammar.add("<Exp>");
        return intType;
    }
    // 分析条件表达式
    private void Cond(){
        LOrExp(getExp());
        grammar.add("<Cond>");
    }
    // 分析左值表达式  --c,k
    private int LVal(ArrayList<Token> exp){
        int intType = 0;
        Token ident = exp.get(0);
        if(!hasSymbol(ident)) {
            error("c",ident.getLineNum());
        }
        grammar.add(ident.toString());//Ident
        if(exp.size()>1){
            ArrayList<Token> exp1 = new ArrayList<>();
            int flag = 0;
            for (int i = 1; i < exp.size(); i++) {
                Token token = exp.get(i);
                if (token.typeEquals("LBRACK")) {
                    intType++;
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
            if(flag>0){
                Exp(exp1);
                error("k",exp.get(exp.size()-1).getLineNum());
            }
        }
        grammar.add("<LVal>");
        if(hasSymbol(exp.get(0))){
            return getSymbol(exp.get(0)).getIntType()-intType;
        }else {
            return 0;
        }
    }
    // 分析常量表达式
    private void ConstExp(ArrayList<Token> exp){
        AddExp(exp);
        grammar.add("<ConstExp>");
    }
    // 分析基本表达式
    private int PrimaryExp(ArrayList<Token> exp){
        int intType = 0;
        Token token = exp.get(0);
        if(token.valueEquals("(")){
            grammar.add(token.toString()); // (
            Exp(new ArrayList<>(exp.subList(1, exp.size() - 1)));
            grammar.add(exp.get(exp.size()-1).toString());
        }else if(token.typeEquals("IDENFR")){
            intType = LVal(exp);
        }else if(token.typeEquals("INTCON")){
            Number(exp.get(0));
        }else{
            error();
        }
        grammar.add("<PrimaryExp>");
        return intType;
    }
    // 分析一元表达式  --c,d,e,j
    private int UnaryExp(ArrayList<Token> exp) {
        int intType = 0;
        Token token = exp.get(0);
        if (token.typeEquals("PLUS") || token.typeEquals("MINU") || token.typeEquals("NOT")) {
            //remove UnaryOp
            UnaryOp(exp.get(0));
            UnaryExp(new ArrayList<>(exp.subList(1, exp.size())));
        } else if (exp.size() == 1) {
            intType=PrimaryExp(exp);
        } else {
            if (exp.get(0).typeEquals("IDENFR") && exp.get(1).typeEquals("LPARENT")) {
                Token ident = exp.get(0);
                ArrayList<Integer> paras = null;
                if (!hasFunction(ident)) {
                    error("c", ident.getLineNum());
                } else {
                    paras = getFunction(ident).getParas();
                }
                if (!exp.get(exp.size() - 1).typeEquals("RPARENT")) {
                    exp.add(new Token(")", nowToken.getLineNum()));
                    error("j");
                }
                //remove Ident ( )
                grammar.add(exp.get(0).toString());
                grammar.add(exp.get(1).toString());
                if (exp.size() > 3) {
                    FuncRParams(ident,new ArrayList<>(exp.subList(2, exp.size() - 1)),paras);
                }else {
                    if(paras!=null){
                        if(paras.size()!=0){
                            error("d",ident.getLineNum());
                        }
                    }
                }
                grammar.add(exp.get(exp.size() - 1).toString());

                if(hasFunction(ident)){
                    if(getFunction(ident).getReturnType().equals("void")){
                        intType=-1;
                    }
                }
            } else {
                intType = PrimaryExp(exp);
            }
        }
        grammar.add("<UnaryExp>");
        return intType;
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
    private int MulExp(ArrayList<Token> exp){
        int intType = 0;
        Exps exps = divide(exp,new ArrayList<>(Arrays.asList("MULT","DIV","MOD")));
        int i = 0;
        for(ArrayList<Token> exp1:exps.getTokens()){
            intType = UnaryExp(exp1);
            grammar.add("<MulExp>");
            if(i < exps.getSymbols().size()){
                grammar.add(exps.getSymbols().get(i++).toString());
            }
        }
        return intType;
    }
    // 分析加减表达式
    private int AddExp(ArrayList<Token> exp){
        int intType = 0;
        Exps exps = divide(exp,new ArrayList<>(Arrays.asList("PLUS","MINU")));
        int i = 0;
        for(ArrayList<Token> exp1:exps.getTokens()){
            intType = MulExp(exp1);
            grammar.add("<AddExp>");
            if(i < exps.getSymbols().size()){
                grammar.add(exps.getSymbols().get(i++).toString());
            }
        }
        return intType;
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
        writer.flush();
        writer.close();
    }

    // 检查右小括号————j
    private void checkParent(){
        if(getNextToken().valueEquals(")")){
            getToken();
        }else{
            error("j");
        }
    }
    // 检查右中括号————k
    private void checkBrack(){
        if(getNextToken().valueEquals("]")){
            getToken();
        }else{
            error("k");
        }
    }
    // 检查分号————i
    private void checkSemicn(){
        if(getNextToken().valueEquals(";")){
            getToken();
        }else{
            error("i");
        }
    }
    // 错误处理
    private void error(){
        System.out.println("error");
    }
    private void error(String type){
        errors.add(new Error(nowToken.lineNum,type));
        System.out.println(nowToken.lineNum+ " "+ type);
    }
    private void error(String type,int lineNum){
        errors.add(new Error(lineNum,type));
        System.out.println(lineNum+ " "+ type);
    }

    // 函数
    private boolean hasFunction(Token token){
        return functions.containsKey(token.getValue());
    }
    private Function getFunction(Token token){
        return functions.getOrDefault(token.getValue(),null);
    }

    private boolean isConst(Token token){
        for(Symbols s:symbols.values()){
            if(s.hasSymbol(token)){
                if(s.isConst(token)){
                    return true;
                }
            }
        }
        return false;
    }

    // 检查函数参数是否匹配
    private void if_match(Token ident, ArrayList<Integer> paras,ArrayList<Integer> rparas){
        if(paras.size()!=rparas.size()){
            error("d",ident.getLineNum());
        }else{
            for (int i=0;i < paras.size();i++){
                if(!paras.get(i).equals(rparas.get(i))){
                    error("e",ident.getLineNum());
                }
            }
        }
    }

    // 输出错误处理结果
    public void printError(BufferedWriter writerError) throws IOException{
        errors.sort((e1, e2) -> e1.getN()- e2.getN());
        for(Error str : errors){
            writerError.write(str + "\n");
        }
        writerError.flush();
        writerError.close();
    }
}

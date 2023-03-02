package error;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import CodeGeneration.CodeType;
import CodeGeneration.LabelGenerator;
import CodeGeneration.PCode;
import data.*;
import lexical.*;
import syntax.*;
public class


SynAndError {
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

    private ArrayList<PCode> codes = new ArrayList<>();
    private LabelGenerator labelGenerator = new LabelGenerator();
    private ArrayList<HashMap<String, String>> ifLabels = new ArrayList<>();
    private ArrayList<HashMap<String, String>> whileLabels = new ArrayList<>();
    private ArrayList<HashMap<Integer, String>> condLabels = new ArrayList<>();
    private int areaID = -1;

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
        areaID++;
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

    private void addSymbol(Token token, String type, int intType,int areaID) {
        symbols.get(area).addSymbol(type, intType, token, areaID);
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
        codes.add(new PCode(CodeType.VAR,areaID+"_"+nowToken.getValue()));

        Token token = getNextToken();
        int intType = 0;
        while(token.valueEquals("[")){
            intType++;
            getToken();// [
            ConstExp(getExp());
            checkBrack();
            token = getNextToken();
        }

        if(intType > 0 ){
            codes.add(new PCode(CodeType.DIMVAR,areaID+"_"+ident.getValue(),intType));
        }
        addSymbol(ident,"const",intType,areaID);

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

        codes.add(new PCode(CodeType.VAR,areaID+"_"+nowToken.getValue()));

        int intType = 0;
        Token token = getNextToken();
        while(token.valueEquals("[")){
            intType++;
            getToken();// [
            ConstExp(getExp());
            checkBrack();
            token = getNextToken();
        }

        if(intType > 0){
            codes.add(new PCode(CodeType.DIMVAR,areaID+"_"+ident.getValue(),intType));
        }
        addSymbol(ident,"var",intType,areaID);

        if(token.valueEquals("=")) {
            getToken(); // =
            if(getNextToken().valueEquals("getint")){
                getToken();// getint
                getToken();// (
                checkParent();// )
                checkSemicn();// ;
                codes.add(new PCode(CodeType.GETINT));
            }
            else InitVal();
        }else{
            codes.add(new PCode(CodeType.PLACEHOLDER,areaID+"_"+ident.getValue(),intType));
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
            }
            getToken();// }
        }else {
            Exp(getExp());
        }
        grammar.add("<InitVal>");
    }
    // 分析函数定义  --b,g,j
    private void FuncDef(){
        int startIndex = index;
        Function function = null;
        ArrayList<Integer> paras = new ArrayList<>();
        String returnType = FuncType();
        getToken();//Ident
        if(functions.containsKey((nowToken.getValue()))){
            error("b");
        }
        PCode code = new PCode(CodeType.FUNC,nowToken.getValue());
        codes.add(code);
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
        code.setValue2(paras.size());
        codes.add(new PCode(CodeType.RET,0));
        codes.add(new PCode(CodeType.ENDFUNC));

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
        codes.add(new PCode(CodeType.MAIN,nowToken.getValue()));

        getToken();// (
        checkParent();// )
        needReturn = true;
        boolean isReturn = Block(false);
        if(needReturn && !isReturn) error("g");
        codes.add(new PCode(CodeType.EXIT));
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
        codes.add(new PCode(CodeType.PARA,areaID+"_"+ident.getValue(),paraType));
        addSymbol(ident,"para",paraType,areaID);
        grammar.add("<FuncFParam>");
        return paraType;
    }
    // 分析函数形参表
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
                Token ident = exp.get(0);
                int intType = LVal(exp);

                codes.add(new PCode(CodeType.ADDRESS,getSymbol(ident).getAreaID()+"_"+ident.getValue(),intType));

                if(isConst(token))
                    error("h",token.getLineNum());
                getToken();// =

                if(getNextToken().valueEquals("getint")){
                    getToken();// getint
                    getToken();// (
                    checkParent();// )
                    checkSemicn();// ;
                    codes.add(new PCode(CodeType.GETINT));
                }else{
                    Exp(getExp());
                    checkSemicn();// ;
                }
                codes.add(new PCode(CodeType.POP,getSymbol(ident).getAreaID()+"_"+ident.getValue()));

            }
            else {
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
            ifLabels.add(new HashMap<>());
            ifLabels.get(ifLabels.size()-1).put("if",labelGenerator.getLabel("if"));
            ifLabels.get(ifLabels.size()-1).put("else",labelGenerator.getLabel("else"));
            ifLabels.get(ifLabels.size()-1).put("if_end",labelGenerator.getLabel("if_end"));
            ifLabels.get(ifLabels.size()-1).put("if_block",labelGenerator.getLabel("if_block"));
            codes.add(new PCode(CodeType.LABEL,ifLabels.get(ifLabels.size()-1).get("if")));

            getToken();// if
            getToken();// (
            Cond("IFTK");
            checkParent();// )

            codes.add(new PCode(CodeType.JZ,ifLabels.get(ifLabels.size()-1).get("else")));
            codes.add(new PCode(CodeType.LABEL,ifLabels.get(ifLabels.size()-1).get("if_block")));

            Stmt();
            token = getNextToken();
            codes.add(new PCode(CodeType.JMP,ifLabels.get(ifLabels.size()-1).get("if_end")));
            codes.add(new PCode(CodeType.LABEL,ifLabels.get(ifLabels.size()-1).get("else")));

            if(token.valueEquals("else")){
                getToken();// else
                Stmt();
            }
            codes.add(new PCode(CodeType.LABEL,ifLabels.get(ifLabels.size()-1).get("if_end")));
            ifLabels.remove(ifLabels.size()-1);
        }
        else if(token.valueEquals("while")){ // j
            whileLabels.add(new HashMap<>());
            whileLabels.get(whileLabels.size()-1).put("while",labelGenerator.getLabel("while"));
            whileLabels.get(whileLabels.size()-1).put("while_end",labelGenerator.getLabel("while_end"));
            whileLabels.get(whileLabels.size()-1).put("while_block",labelGenerator.getLabel("while_block"));
            codes.add(new PCode(CodeType.LABEL,whileLabels.get(whileLabels.size()-1).get("while")));

            getToken();// while
            whileFlag++;
            getToken();// (
            Cond("WHILETK");
            checkParent();// )

            codes.add(new PCode(CodeType.JZ,whileLabels.get(whileLabels.size()-1).get("while_end")));
            codes.add(new PCode(CodeType.LABEL,whileLabels.get(whileLabels.size()-1).get("while_block")));

            Stmt();
            whileFlag--;
            codes.add(new PCode(CodeType.JMP,whileLabels.get(whileLabels.size()-1).get("while")));
            codes.add(new PCode(CodeType.LABEL,whileLabels.get(whileLabels.size()-1).get("while_end")));
            whileLabels.remove(whileLabels.size()-1);

        }
        else if(token.valueEquals("break")){ //i,m
            getToken();// break
            codes.add(new PCode(CodeType.JMP,whileLabels.get(whileLabels.size()-1).get("while_end")));

            if(whileFlag == 0)
                error("m");
            checkSemicn();// ;
        }
        else if(token.valueEquals("continue")){ //i,m
            getToken();// continue
            codes.add(new PCode(CodeType.JMP,whileLabels.get(whileLabels.size()-1).get("while")));

            if(whileFlag == 0)
                error("m");
            checkSemicn();// ;
        }
        else if(token.valueEquals("return")){ //f,i
            boolean flag=false;
            getToken();// return
            isReturn = true;
           if(getNextToken().typeSymbolizeBeginOfExp()){
               if(!needReturn){
                   error("f");
               }
               Exp(getExp());
               flag = true;
           }
           checkSemicn();
           codes.add(new PCode(CodeType.RET,flag ? 1 : 0));
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
            codes.add(new PCode(CodeType.PRINT,str.getValue(),para));
        }
        else if(token.valueEquals(";")){
            getToken();//;
        }
        grammar.add("<Stmt>");
        return isReturn;
    }
    // 分析数字
    private void Number(Token token){
        codes.add(new PCode(CodeType.PUSH,Integer.parseInt(token.getValue())));
        grammar.add(token.toString());
        grammar.add("<Number>");
    }
    // 分析bool
    private void Bool(Token token){
        if(token.valueEquals("true"))
            codes.add(new PCode(CodeType.PUSH,1));
        else codes.add(new PCode(CodeType.PUSH,0));
        grammar.add(token.toString());
        grammar.add("<Bool>");
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
            preToken=token;
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
                //UnaryOP
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
    private void Cond(String from){
        LOrExp(getExp(),from);
        grammar.add("<Cond>");
    }
    // 分析左值表达式  --c,k
    private int LVal(ArrayList<Token> exp){
        int intType = 0;
        Token ident = exp.get(0);

        if(!hasSymbol(ident)) {
            error("c",ident.getLineNum());
        }
        codes.add(new PCode(CodeType.PUSH,getSymbol(ident).getAreaID()+"_"+ident.getValue()));

        grammar.add(ident.toString());//Ident
        if(exp.size()>1){
            ArrayList<Token> exp1 = new ArrayList<>();
            int flag = 0;
            for (int i = 1; i < exp.size(); i++) {
                Token token = exp.get(i);
                if (token.typeEquals("LBRACK")) {
                    if(flag==0) intType++;
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
        if(hasSymbol(ident)){
            return getSymbol(ident).getIntType()-intType;
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
            grammar.add(exp.get(0).toString()); // (
            Exp(new ArrayList<>(exp.subList(1, exp.size() - 1)));
            grammar.add(exp.get(exp.size()-1).toString());

        }else if(token.typeEquals("IDENFR")){
            intType = LVal(exp);
            Token ident = exp.get(0);
            if(intType==0){
                codes.add(new PCode(CodeType.VALUE,getSymbol(ident).getAreaID()+"_"+ident.getValue(),intType));
            }else{
                codes.add(new PCode(CodeType.ADDRESS,getSymbol(ident).getAreaID()+"_"+ident.getValue(),intType));
            }
        }else if(token.typeEquals("INTCON")){
            Number(exp.get(0));
        }else if(token.typeEquals("BOOL")){
            Bool(exp.get(0));
        }
        else{
            error();
        }
        grammar.add("<PrimaryExp>");
        return intType;
    }
    // 分析一元表达式  --c,d,e,j
    private int UnaryExp(ArrayList<Token> exp) {
        int intType = 0;
        Token token = exp.get(0);
        if(token.typeEquals("MAXTK")){
            grammar.add(exp.get(0).toString()); //max
            grammar.add(exp.get(1).toString()); //(
            int i=2;
            ArrayList<Token> lval1 = new ArrayList<>();
            ArrayList<Token> lval2 = new ArrayList<>();
            while(!exp.get(i).valueEquals(",")){
                lval1.add(exp.get(i));
                i++;
            }
            LVal(lval1);
            grammar.add(exp.get(i).toString());//,
            while(!exp.get(i).valueEquals(")")){
                lval2.add(exp.get(i));
                i++;
            }
            LVal(lval2);
            grammar.add(exp.get(i).toString());//,
            codes.add(new PCode(CodeType.MAX));
        }
        else if (token.typeEquals("PLUS") || token.typeEquals("MINU") || token.typeEquals("NOT")) {
            //remove UnaryOp
            UnaryOp(exp.get(0));
            UnaryExp(new ArrayList<>(exp.subList(1, exp.size())));
            CodeType type;
            if(token.typeEquals("PLUS")){
                type = CodeType.POS;
            }else if(token.typeEquals("MINU")){
                type = CodeType.NEG;
            }else{
                type = CodeType.NOT;
            }
            codes.add(new PCode(type));
        } else if (exp.size() == 1) {
            //primary EXP
            intType=PrimaryExp(exp);
        }
        else {
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
                codes.add(new PCode(CodeType.CALL,ident.getValue()));
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
    private void FuncRParams(Token ident,ArrayList<Token> exp,ArrayList<Integer> paras) {
        Exps exps = divide(exp, new ArrayList<>(Arrays.asList("COMMA")));
        ArrayList<Integer> rparas = new ArrayList<>();
        int j = 0;
        for (ArrayList<Token> exp1 : exps.getTokens()) {
            int intType = Exp(exp1);
            rparas.add(intType);
            codes.add(new PCode(CodeType.RPARA,intType));
            if (j < exps.getSymbols().size()) {
                grammar.add(exps.getSymbols().get(j++).toString());
            }
        }
        if(paras != null){
            if_match(ident,paras,rparas);
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
            if(i > 0){
                CodeType type;
                if(exps.getSymbols().get(i-1).typeEquals("MULT")){
                    type=CodeType.MUL;
                }else if(exps.getSymbols().get(i-1).typeEquals("DIV")){
                    type=CodeType.DIV;
                } else{
                    type=CodeType.MOD;
                }
                codes.add(new PCode(type));
            }
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
            if(i > 0){
                if(exps.getSymbols().get(i-1).typeEquals("PLUS")){
                    codes.add(new PCode(CodeType.ADD));
                }else{
                    codes.add(new PCode(CodeType.SUB));
                }
            }
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
            if(i > 0){
                CodeType type;
                if(exps.getSymbols().get(i-1).typeEquals("LSS")){
                    type=CodeType.CMPLT;
                }else if(exps.getSymbols().get(i-1).typeEquals("LEQ")){
                    type=CodeType.CMPLE;
                }else if(exps.getSymbols().get(i-1).typeEquals("GRE")){
                    type=CodeType.CMPGT;
                }else{
                    type=CodeType.CMPGE;
                }
                codes.add(new PCode(type));
            }
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
            if(i > 0){
                CodeType type;
                if(exps.getSymbols().get(i-1).typeEquals("EQL")){
                    type=CodeType.CMPEQ;
                } else{
                    type=CodeType.CMPNE;
                }
                codes.add(new PCode(type));
            }
            grammar.add("<EqExp>");
            if(i < exps.getSymbols().size()){
                grammar.add(exps.getSymbols().get(i++).toString());
            }
        }
    }
    // 分析逻辑与表达式
    private void LAndExp(ArrayList<Token> exp,String from,String label){
        Exps exps = divide(exp,new ArrayList<>(Arrays.asList("AND")));
        int j = 0;
        for(int i=0;i<exps.getTokens().size();i++){
            ArrayList<Token> exp1 = exps.getTokens().get(i);
            EqExp(exp1);
            if(j > 0){
                codes.add(new PCode(CodeType.AND));
            }
            if(exps.getTokens().size() > 1 && i != exps.getTokens().size()-1){
                if(from.equals("IFTK")){
                    codes.add(new PCode(CodeType.JZ,label));
                }else{
                    codes.add(new PCode(CodeType.JZ,label));
                }
            }
            grammar.add("<LAndExp>");
            if(j < exps.getSymbols().size()){
                grammar.add(exps.getSymbols().get(j++).toString());
            }
        }
    }
    // 分析逻辑或表达式
    private void LOrExp(ArrayList<Token> exp,String from){
        Exps exps = divide(exp,new ArrayList<>(Arrays.asList("OR")));
        int j = 0;
        for(int i=0;i<exps.getTokens().size();i++){
            ArrayList<Token> exp1 = exps.getTokens().get(i);
            String label = labelGenerator.getLabel("cond_"+i);
            LAndExp(exp1,from,label);
            codes.add(new PCode(CodeType.LABEL,label));
            if(j > 0){
                codes.add(new PCode(CodeType.OR));
            }
            if(exps.getTokens().size() > 1 && i != exps.getTokens().size()-1){
                if(from.equals("IFTK")){
                    codes.add(new PCode(CodeType.JNZ,ifLabels.get(ifLabels.size()-1).get("if_block")));
                }else{
                    codes.add(new PCode(CodeType.JNZ,whileLabels.get(whileLabels.size()-1).get("while_block")));
                }
            }
            grammar.add("<LOrExp>");
            if(j < exps.getSymbols().size()){
                grammar.add(exps.getSymbols().get(j++).toString());
            }
        }
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

    // 输出语法分析结果
    public void print(BufferedWriter writer) throws IOException {
        for (String str : grammar) {
            writer.write(str + "\n");
        }
        writer.flush();
        writer.close();
    }
    public void printPCode() {
        for (PCode code : codes) {
            System.out.println(code);
        }
    }

    public ArrayList<PCode> getCodes(){ return  codes;}


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

package llym_ir;

import data.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;

public class llvmGenerator {
    AstNode root = null;
    int regId = 1;  //寄存器序号
    String ans ="";
    int area = 0;

    ArrayList <AstNode> stack = new ArrayList<>(); //栈式符号表
    HashMap <String,AstNode> global= new HashMap(); //全局符号表

    public llvmGenerator(AstNode root) {
        this.root = root;
        this.ans="declare i32 @getint()"+"\n" +
                "declare void @putint(i32)"+"\n"+
                "declare void @putch(i32)"+"\n"+
                "declare void @putstr(i8*)"+"\n";
        generate(root);
    }

    private void generate(AstNode astNode) {
        switch (astNode.getValue()) {
            case "<ConstDef>": ConstDef(astNode);break;
            case "<ConstInitVal>": ConstInitVal(astNode);break;
            case "<ConstExp>":ConstExp(astNode);break;
            case "<VarDef>":VarDef(astNode);break;
            case "<InitVal>":InitVal(astNode);break;
            case "<FuncDef>": FuncDef(astNode);break;
            case "<FuncFParams>": FuncFParams(astNode);break;
            case "<FuncFParam>": FuncFParam(astNode);break;
            case "<MainFuncDef>": MainFuncDef(astNode);break;
            case "<Block>": Block(astNode);break;
            case "<Stmt>": Stmt(astNode);break;
            case "<Number>": Number(astNode);break;
            case "<Exp>": ADD_Mul_Exp(astNode);break;
//            case "<Cond>":Cond(astNode);break;
            case "<LVal>":LVal(astNode);break;
            case "<FuncRParams>":FuncRParams(astNode);break;
            case "<PrimaryExp>":PrimaryExp(astNode);break;
            case "<UnaryExp>":UnaryExp(astNode);break;
            case "<MulExp>": ADD_Mul_Exp(astNode);break;
            case "<AddExp>":ADD_Mul_Exp(astNode);break;
//            case "<RelExp>": Rel_EqExp(astNode);break;
//            case "<EqExp>":Rel_EqExp(astNode);break;
//            case "<LAndExp>": LAndExp(astNode);break;
//            case "<LOrExp>":LOrExp(astNode);break;
            default: {
                for (AstNode a : astNode.getChilds()) {
                    generate(a);
                }
            }
        }
    }

    private void ConstDef(AstNode astNode){
        ArrayList<AstNode> a=astNode.getChilds();
        AstNode Ident = a.get(0);
        KeyValue k = Ident.getKey();
        if(area>0){
            ans+=("%v"+this.regId+" = alloca i32\n");
            Ident.setQuality("%v"+this.regId);
            Ident.setRegID("%v"+this.regId);
            regId++;
        }
        if(a.size()==3){ // 常数
            k.setDim(0);
            generate(a.get(2));
            k.setIntVal(a.get(2).getKey().getIntVal());
            if(area==0){
                ans+=("@"+Ident.getValue()+" = dso_local global i32 "+k.getIntVal()+"\n");
            }else{
                ans+=("store i32 "+a.get(2).getQuality()+", i32* "+Ident.getRegID()+"\n");
            }
        }
        if(area==0){
            global.put(Ident.getValue(),Ident);
        }else{
            Ident.setArea(this.area);
            stack.add(Ident);
        }
    }

    private void ConstExp(AstNode astNode){
        ArrayList<AstNode> a = astNode.getChilds();
        a.get(0).setInStack(astNode.isInStack());
        generate(a.get(0));
        String first = a.get(0).getQuality();
        if(a.size()>1){
            for(int i=1;i<a.size();i+=2){
                String op = a.get(i).getValue();//运算符
                a.get(i+1).setInStack(astNode.isInStack());
                generate(a.get(i+1));
                String second = a.get(i+1).getQuality();
                String llvm_op = getOp(op);
                if(area>0) {
                    ans += "%v" + this.regId + " = " + llvm_op + " i32 " + first + ", " + second + "\n";
                    a.get(i + 1).setRegID("%v"+ regId);
                    a.get(i + 1).setQuality("%v" + regId);
                    regId++;
                }
                else {
                    a.get(i+1).setQuality(Calculate(first,op,second));
                }
                first = a.get(i+1).getQuality();
            }
            astNode.setQuality(a.get(a.size()-1).getQuality());
        }else {
            astNode.setQuality(first);
        }
    }

    private void ConstInitVal(AstNode astNode){
        ArrayList<AstNode> a= astNode.getChilds();
        if(a.size()==1){
            generate(a.get(0));
            astNode.getKey().setIntVal(a.get(0).getQuality());
            astNode.setQuality(a.get(0).getQuality());
        }
    }

    private void VarDef(AstNode astNode){
        ArrayList<AstNode> a = astNode.getChilds();
        AstNode Ident = a.get(0);
        KeyValue k = Ident.getKey();
        if(a.size()==1||a.size()==3){ // 常数
            if(area>0){
                ans+=("%v"+this.regId+" = alloca i32\n");
                Ident.setQuality("%v"+this.regId);
                Ident.setRegID("%v"+this.regId);
                regId++;
            }
            k.setDim(0);
            if(a.size()==3) {
                generate(a.get(2));
                k.setIntVal(a.get(2).getKey().getIntVal());
                if(area>0){
                    ans+=("store i32 "+a.get(2).getQuality()+", i32* "+Ident.getRegID()+"\n");
                }
            }
            else if(a.size()==1){
                k.setIntVal("0");
            }
            if(area==0){
                ans+=("@"+Ident.getValue()+" = dso_local global i32 "+k.getIntVal()+"\n");
            }
            Ident.setKey(k);
            if(area==0){
                global.put(Ident.getValue(),Ident);
            }else{
                Ident.setArea(this.area);
                stack.add(Ident);
            }
        }
    }

    private void InitVal(AstNode astNode){
        ArrayList<AstNode> a= astNode.getChilds();
        if(a.size()==1){
            generate(a.get(0));
            astNode.getKey().setIntVal(a.get(0).getQuality());
            astNode.setQuality(a.get(0).getQuality());
        }
    }

    private void MainFuncDef( AstNode astNode){
        ans+=("\n"+"define dso_local i32 @main() {"+"\n");
        generate(astNode.getChilds().get(4));//Block
        ans+=("}\n");
    }

    private void Stmt(AstNode astNode){
        ArrayList<AstNode> a = astNode.getChilds();
        if(a.get(0).getValue().equals("<Block>")){
            generate(a.get(0));
        }
        else if(a.get(0).getValue().equals("return")){
            if(a.get(1).getValue().equals(";")){
                ans+="ret void\n";
            }
            else {
                generate(a.get(1));//Exp
                ans+=("ret i32 "+a.get(1).getQuality()+"\n");
            }
        }else if(a.get(0).getValue().equals("printf")){
            int nowNum = 4; // printf,(,str,/,
            String key = a.get(2).getValue();
            for(int i=1;i<key.length()-1;i++){
                if(key.charAt(i)=='%'&&key.charAt(i+1)=='d'){
                    i++;
                    generate(a.get(nowNum));
                    nowNum+=2;
                }
            }
            nowNum=4;
            for(int i=1;i<key.length()-1;i++){
                if(key.charAt(i)=='%' && key.charAt(i+1)=='d'){
                    ans+=("call void @putint(i32 "+a.get(nowNum).getQuality()+")\n");
                    i++;
                    nowNum+=2;
                }else if(key.charAt(i)=='\\' && key.charAt(i+1)=='n'){
                    ans+="call void @putch(i32 10)\n";
                    i++;
                }else{
                    ans+=("call void @putch(i32 "+(int) key.charAt(i)+")\n");
                }
            }
        }else if(a.get(0).getValue().equals("<LVal>")){
            generate(a.get(0));
            if(a.get(2).getValue().equals("<Exp>")){
                generate(a.get(2));//Exp
                ans+=("store i32 "+a.get(2).getQuality()+", i32* "+a.get(0).getRegID()+"\n");
            }
            else if(a.get(2).getValue().equals("getint")){
                ans+=("%v"+this.regId+" = call i32 @getint()"+"\n");
                ans+=("store i32 "+"%v"+this.regId+", i32* "+a.get(0).getRegID()+"\n");
                this.regId++;
            }

//        }else if(a.get(0).getValue().equals("if")){
//
//        }else if(a.get(0).getValue().equals("while")){
//
        }
    }

    private void Block(AstNode astNode){
        for(AstNode astNode1:astNode.getChilds()){
            if(astNode1.getValue().equals("{")){
                area++;
                if(area==1){
                    for(int i=stack.size()-1;i>=0;i--){
                        if(stack.get(i).getRegID().equals("") && stack.get(i).getArea()==1){
                            ans+="%v"+this.regId+" = alloca i32\n";
                            stack.get(i).setRegID("%v"+this.regId);
                            ans+="store i32 "+stack.get(i).getQuality()+", i32* "+stack.get(i).getRegID()+"\n";
                            regId++;
                        }
                    }
                }
            }else if(astNode1.getValue().equals("}")){
                for(int i=stack.size()-1;i>=0;i--){
                    if(stack.get(i).getArea()==area) stack.remove(i);
                }
                area--;
            }else{
                generate(astNode1);
            }
        }
    }

    private void ADD_Mul_Exp(AstNode astNode){
        ArrayList<AstNode> a = astNode.getChilds();
        a.get(0).setInStack(astNode.isInStack());
        generate(a.get(0));
        String first = a.get(0).getQuality();
        if(a.size()>1){
            for(int i=1;i<a.size();i+=2){
                String op = a.get(i).getValue();//运算符
                a.get(i+1).setInStack(astNode.isInStack());
                generate(a.get(i+1));
                String second = a.get(i+1).getQuality();
                String llvm_op = getOp(op);
                if(area>0) {
                    ans += "%v" + this.regId + " = " + llvm_op + " i32 " + first + ", " + second + "\n";
                    a.get(i + 1).setRegID("%v"+ regId);
                    a.get(i + 1).setQuality("%v" + regId);
                    regId++;
                }
                else {
                    a.get(i+1).setQuality(Calculate(first,op,second));
                }
                first = a.get(i+1).getQuality();
            }
            astNode.setQuality(a.get(a.size()-1).getQuality());
        }else {
            astNode.setQuality(first);
        }
    }

    private void UnaryExp(AstNode astNode){
        ArrayList<AstNode> a = astNode.getChilds();
        if(a.get(0).getValue().equals("<UnaryOp>")){
            generate(a.get(1));//UnaryExp
            if(a.get(0).getChilds().get(0).getValue().equals("+")){
                astNode.setQuality(a.get(1).getQuality());
            }else if(a.get(0).getChilds().get(0).getValue().equals("-")){
                if(area>0){
                    ans+="%v"+this.regId+" = sub i32 0, "+a.get(1).getQuality()+"\n";
                    astNode.setRegID("%v"+this.regId);
                    astNode.setQuality("%v"+this.regId);
                    regId++;
                }else{
                    astNode.setQuality(Calculate("0","-",a.get(1).getQuality()));
                }
            }else if(a.get(0).getChilds().get(0).getValue().equals("!")){
                ans+="%v"+this.regId+" = icmp eq i32 0, "+a.get(1).getQuality()+"\n";
                regId++;
                ans+="%v"+this.regId+" = sext i1 %v"+(this.regId-1)+" to i32\n";
                astNode.setRegID("%v"+this.regId);
                astNode.setQuality("%v"+this.regId);
                regId++;
            }
        }else if(a.get(0).getValue().equals("<PrimaryExp>")){
            a.get(0).setInStack(astNode.isInStack());
            generate(a.get(0));//PrimaryExp
            astNode.setQuality(a.get(0).getQuality());
        }else{
            AstNode ident=a.get(0);
            String identName = ident.getValue();
            AstNode identGlobe=global.get(identName);
            ident.setReturnType(identGlobe.getReturnType());
            if(ident.getReturnType().equals("i32")){
                if(a.get(2).getValue().equals(")")){
                    ans+=("%v"+this.regId+" = call "+ident.getReturnType()+" @"+ident.getValue()+"()\n");
                    astNode.setQuality("%v"+this.regId);
                    this.regId++;
                }
                else{
                    generate(a.get(2));
                    ans+=("%v"+this.regId+" = call "+ident.getReturnType()+" @"+ident.getValue()+"("+a.get(2).getQuality()+")\n");
                    astNode.setQuality("%v"+this.regId);
                    this.regId++;
                }
            }
            else if(ident.getReturnType().equals("void")){
                if(a.get(2).getValue().equals(")")){
                    ans+=("call "+ident.getReturnType()+" @"+ident.getValue()+"()\n");
                }
                else{
                    generate(a.get(2));
                    ans+=("call "+ident.getReturnType()+" @"+ident.getValue()+"("+a.get(2).getValue()+")\n");
                }
            }
        }
    }

    private void PrimaryExp(AstNode astNode){
        ArrayList<AstNode> a = astNode.getChilds();
        if(a.get(0).getValue().equals("<Number>")){
            generate(a.get(0));//Number
            astNode.setQuality(a.get(0).getQuality());
        }else if(a.get(0).getValue().equals("(")){
            generate(a.get(1));//Exp
            astNode.setQuality(a.get(1).getQuality());
        }else if(a.get(0).getValue().equals("<LVal>")){
            a.get(0).setInStack(astNode.isInStack());
            generate(a.get(0));
            astNode.setQuality(a.get(0).getQuality());
        }
    }

    private void Number(AstNode astNode){
        astNode.setQuality(astNode.getChilds().get(0).getValue());
    }

    private void LVal( AstNode astNode){
        ArrayList<AstNode> a = astNode.getChilds();
        AstNode Ident = a.get(0);
        boolean is_inStack = false;
        for(int i=stack.size()-1;i>=0;i--){
            if(stack.get(i).getValue().equals(Ident.getValue())){
                is_inStack = true;
                if(stack.get(i).getQuality().charAt(0)!='%'){
                    astNode.setQuality(stack.get(i).getQuality());
                }else{
                    ans+=("%v"+this.regId+" = load i32, i32* "+stack.get(i).getRegID()+"\n");
                    astNode.setQuality("%v"+regId);
                    astNode.setRegID(stack.get(i).getRegID());
                    regId++;
                }
                break;
            }
        }
        if(!is_inStack){
            if(area>0){
                ans+=("%v"+this.regId+" = load i32, i32* "+"@"+Ident.getValue()+"\n");
                astNode.setQuality("%v"+regId);
                astNode.setRegID("@"+Ident.getValue());
                regId++;
            }else {
                astNode.setQuality(global.get(Ident.getValue()).getKey().getIntVal());
            }
        }
    }

//    private void Cond(AstNode astNode){
//    }
//    private void Rel_EqExp(AstNode astNode){
//    }
//    private void LOrExp(AstNode astNode){
//    }
//    private void LAndExp(AstNode astNode){
//    }

    private void FuncDef(AstNode astNode){
        ArrayList<AstNode> a=astNode.getChilds();
        String Type = a.get(0).getChilds().get(0).getValue();
        AstNode ident = a.get(1);
        if(Type.equals("int")){Type="i32";}
        else if(Type.equals("void")){Type="void";}
        ident.setReturnType(Type);
        ans+=("define dso_local "+Type+" @"+ident.getValue());
        global.put(ident.getValue(),ident);
        if(a.get(2).getValue().equals("(")){
            ans+=("(");
            if(a.get(4).getValue().equals(")")){
                generate(a.get(3));
                ans+=(") {\n");
                generate(a.get(5));
            }
            else{
                ans+=(") {\n");
                generate(a.get(4));
            }
        }
        if(Type.equals("void")){
            ans+=("ret void\n");
        }
        ans+=("}\n");
    }

    private void FuncFParams(AstNode astNode){
        ArrayList<AstNode> a= astNode.getChilds();
        generate(a.get(0));
        for(int i=2;i<a.size();i+=2){
            ans+=", ";
            generate(a.get(i));
        }
    }

    private void FuncFParam(AstNode astNode){
        ArrayList<AstNode> a=astNode.getChilds();
        AstNode Ident = a.get(1);
        Ident.setArea(1);
        if(a.size()==2){
            ans+=("i32 %v"+this.regId);
            Ident.setQuality("%v"+this.regId);
            regId++;
            stack.add(Ident);
        }
    }

    private void FuncRParams(AstNode astNode){
        ArrayList<AstNode> a=astNode.getChilds();
        generate(a.get(0));
        String Value;
        Value ="i32 "+a.get(0).getQuality();
        for(int i=2;i<a.size();i+=2){
            generate(a.get(i));
        }
        for(int i=2;i<a.size();i+=2){
            Value = Value +", i32 "+a.get(i).getQuality();
        }
        astNode.setQuality(Value);
    }

    private String getOp(String s){
        String str="";
        switch (s){
            case "+": str="add";break;
            case "-": str="sub";break;
            case "*": str="mul";break;
            case "/": str="sdiv";break;
            case "%": str="srem";break;
            case "==":str="eq";break;
            case "!=":str="ne";break;
            case ">": str="sgt";break;
            case ">=":str="sge";break;
            case "<":str="slg";break;
            case "<=":str="sle";break;
            case "&&": str="and";break;
            case "||": str="or";break;
        }
        return str;
    }

    private String Calculate(String a,String op,String b){
        int aa = Integer.parseInt(a);
        int bb = Integer.parseInt(b);
        int c=0;
        switch (op){
            case "+":c=aa+bb;break;
            case "-":c=aa-bb;break;
            case "*":c=aa*bb;break;
            case "/":c=aa/bb;break;
            case "%":c=aa%bb;break;
            case "==":c=(aa==bb)?1:0;break;
            case "!=":c=(aa!=bb)?1:0;break;
            case ">":c=(aa>bb)?1:0;break;
            case ">=":c=(aa>=bb)?1:0;break;
            case "<":c=(aa<bb)?1:0;break;
            case "<=":c=(aa<=bb)?1:0;break;
        }
        return String.valueOf(c);
    }

    public void print(BufferedWriter llvm_ir) throws IOException {
        llvm_ir.write(ans);
        llvm_ir.flush();
        llvm_ir.close();
    }

}

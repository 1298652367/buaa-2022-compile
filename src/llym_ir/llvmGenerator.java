package llym_ir;

import data.AstNode;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
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
    public void generate(AstNode astNode) {
        switch (astNode.getValue()) {
//            case "<ConstDef>": ConstDef(astNode);break;
//            case "<ConstInitVal>": ConstInitVal(astNode);break;
//            case "<ConstExp>":ConstExp(astNode);break;
//            case "<VarDef>":VarDef(astNode);break;
//            case "<InitVal>":InitVal(astNode);break;
//            case "<FuncDef>": FuncDef(astNode);break;
//            case "<FuncFParams>": FuncFParams(astNode);break;
//            case "<FuncFParam>": FuncFParam(astNode);break;
            case "<MainFuncDef>": MainFuncDef(astNode);break;
            case "<Block>": Block(astNode);break;
            case "<Stmt>": Stmt(astNode);break;
            case "<Number>": Number(astNode);break;
            case "<Exp>": ADD_Mul_Exp(astNode);break;
//            case "<Cond>":Cond(astNode);break;
//            case "<LVal>":LVal(astNode);break;
//            case "<FuncRParams>":FuncRParams(astNode);break;
            case "<PrimaryExp>":PrimaryExp(astNode);break;
            case "<UnaryExp>":UnaryExp(astNode);break;
            case "<MulExp>": ADD_Mul_Exp(astNode);break;
            case "<AddExp>":ADD_Mul_Exp(astNode);break;
//            case "<RelExp>": RelExp(astNode);break;
//            case "<EqExp>":EqExp(astNode);break;
//            case "<LAndExp>": LAndExp(astNode);break;
//            case "<LOrExp>":LOrExp(astNode);break;
            default: {
                for (AstNode a : astNode.getChilds()) {
                    generate(a);
                }
            }
        }
    }

    public void MainFuncDef( AstNode astNode){
        ans+=("\n"+"define dso_local i32 @main() {"+"\n");
        generate(astNode.getChilds().get(4));//Block
        ans+=("}\n");
    }
    public void Stmt(AstNode astNode){
        ArrayList<AstNode> a = astNode.getChilds();
        if(a.get(0).getValue().equals("<Block>")){
            generate(a.get(0));
        }
        else if(a.get(0).getValue().equals("return")){
            if(a.get(1).getValue().equals(";")) return;
            else {
                generate(a.get(1));//Exp
                ans+="ret i32 "+a.get(1).getQuality()+"\n";
            }
        }
    }

    public void Block(AstNode astNode){
        for(AstNode astNode1:astNode.getChilds()){
            if(astNode1.getValue().equals("{")){
                area++;
                if(area==1){
                    for(int i=stack.size()-1;i>=0;i--){
                        if(stack.get(i).getRegID()==0 && stack.get(i).getArea()==1){
                            ans+="%v"+this.regId+" = alloca i32\n";
                            stack.get(i).setRegID(this.regId);
                            ans+="store i32 "+stack.get(i).getQuality()+", i32* %v"+stack.get(i).getRegID()+"\n";
                            regId++;
                        }
                    }
                }
            }else if(astNode.getValue().equals("}")){
                for(int i=stack.size()-1;i>=0;i--){
                    if(stack.get(i).getArea()==area) stack.remove(i);
                    area--;
                }
            }else{
                generate(astNode1);
            }
        }
    }

    public void ADD_Mul_Exp(AstNode astNode){
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
                    a.get(i + 1).setRegID(regId);
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

    public void UnaryExp(AstNode astNode){
        ArrayList<AstNode> a = astNode.getChilds();
        if(a.get(0).getValue().equals("<UnaryOp>")){
            generate(a.get(1));//UnaryExp
            if(a.get(0).getChilds().get(0).getValue().equals("+")){
                astNode.setQuality(a.get(1).getQuality());
            }else if(a.get(0).getChilds().get(0).getValue().equals("-")){
                if(area>0){
                    ans+="%v"+this.regId+" = sub i32 0, "+a.get(1).getQuality()+"\n";
                    astNode.setRegID(this.regId);
                    astNode.setQuality("%v"+this.regId);
                    regId++;
                }else{
                    astNode.setQuality(Calculate("0","-",a.get(1).getQuality()));
                }
            }else if(a.get(0).getChilds().get(0).getValue().equals("!")){
                ans+="%v"+this.regId+" = icmp eq i32 0, "+a.get(1).getQuality()+"\n";
                regId++;
                ans+="%v"+this.regId+" = sext i1 %v"+(this.regId-1)+" to i32\n";
                astNode.setRegID(this.regId);
                astNode.setQuality("%v"+this.regId);
                regId++;
            }
        }else if(a.get(0).getValue().equals("<PrimaryExp>")){
            a.get(0).setInStack(astNode.isInStack());
            generate(a.get(0));//PrimaryExp
            astNode.setQuality(a.get(0).getQuality());
        }else{

        }
    }

    public void PrimaryExp(AstNode astNode){
        ArrayList<AstNode> a = astNode.getChilds();
        if(a.get(0).getValue().equals("<Number>")){
            generate(a.get(0));//Number
            astNode.setQuality(a.get(0).getQuality());
        }else if(a.get(0).getValue().equals("(")){
            generate(a.get(1));//Exp
            astNode.setQuality(a.get(1).getQuality());
        }else if(a.get(0).getQuality().equals("<Lval>")){

        }
    }
    public void Number(AstNode astNode){
        astNode.setQuality(astNode.getChilds().get(0).getValue());
    }

    public String getOp(String s){
        switch (s){
            case "+": return "add";
            case "-": return"sub";
            case "*": return"mul";
            case "/": return"sdiv";
            case "%": return"srem";
            case "==":return"eq";
            case "!=":return"ne";
            case ">": return"sgt";
            case ">=":return"sge";
            case "<":return"slg";
            case "<=":return"sle";
            case "&&": return"and";
            case "||": return"or";
            default:
                throw new IllegalStateException("Unexpected value: " + s);
        }
    }

    public String Calculate(String a,String op,String b){
        int aa = Integer.parseInt(a);
        int bb = Integer.parseInt(b);
        int c=0;
        switch (op){
            case "+":c=aa+bb;
            case "-":c=aa-bb;
            case "*":c=aa*bb;
            case "/":c=aa/bb;
            case "%":c=aa%bb;
            case "==":c=(aa==bb)?1:0;
            case "!=":c=(aa!=bb)?1:0;
            case ">":c=(aa>bb)?1:0;
            case ">=":c=(aa>=bb)?1:0;
            case "<":c=(aa<bb)?1:0;
            case "<=":c=(aa<=bb)?1:0;
        }
        return String.valueOf(c);
    }

    public void print(BufferedWriter llvm_ir) throws IOException {
        llvm_ir.write(ans);
        llvm_ir.flush();
        llvm_ir.close();
    }

}

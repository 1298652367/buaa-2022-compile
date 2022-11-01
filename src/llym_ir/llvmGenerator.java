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
            case "<Exp>": Exp(astNode);break;
//            case "<Cond>":Cond(astNode);break;
//            case "<LVal>":LVal(astNode);break;
//            case "<FuncRParams>":FuncRParams(astNode);break;
            case "<PrimaryExp>":PrimaryExp(astNode);break;
            case "<UnaryExp>":UnaryExp(astNode);break;
            case "<MulExp>": MulExp(astNode);break;
            case "<AddExp>":AddExp(astNode);break;
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


    public void Exp(AstNode astNode){
        generate(astNode.getChilds().get(0));//add
        astNode.setQuality(astNode.getChilds().get(0).getQuality());
    }

    public void AddExp(AstNode astNode){
        generate(astNode.getChilds().get(0));//mul
        astNode.setQuality(astNode.getChilds().get(0).getQuality());
    }
    public void MulExp(AstNode astNode){
        generate(astNode.getChilds().get(0));//Unary
        astNode.setQuality(astNode.getChilds().get(0).getQuality());
    }
    public void UnaryExp(AstNode astNode){
        generate(astNode.getChilds().get(0));//primary
        astNode.setQuality(astNode.getChilds().get(0).getQuality());
    }
    public void PrimaryExp(AstNode astNode){
        generate(astNode.getChilds().get(0));//Number
        astNode.setQuality(astNode.getChilds().get(0).getQuality());
    }
    public void Number(AstNode astNode){
        astNode.setQuality(astNode.getChilds().get(0).getValue());
    }



    public void print(BufferedWriter llvm_ir) throws IOException {
        llvm_ir.write(ans);
        llvm_ir.flush();
        llvm_ir.close();
    }

}

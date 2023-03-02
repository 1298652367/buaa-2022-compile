package llvm_ir;

import data.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;

public class llvmGenerator {
    int level=0;
    AstNode Rootast = null;
    int regId=1;
    int nowtag=0;
    ArrayList <AstNode> stack = new ArrayList<>();
    HashMap <String,AstNode> global= new HashMap();
    String ans = "";

    public llvmGenerator(AstNode ast){
        ans+="declare i32 @getint()\n";
        ans+="declare void @putint(i32)\n";
        ans+="declare void @putch(i32)\n";
        ans+="declare void @putstr(i8*)\n";
        this.Rootast=ast;
        generate(this.Rootast);
    }

    public String tags(){
        String s="";
        for(int i=0;i<4*nowtag;i++){
            s+=" ";
        }
        return s;
    }

    public void generate(AstNode ast){
        if(ast.getContent().equals("<ConstDef>")){ConstDef(ast);}
        else if(ast.getContent().equals("<ConstInitVal>")){
            ConstInitVal(ast);
        }
        else if(ast.getContent().equals("<ConstExp>")){
            AddMulExp(ast);}
        else if(ast.getContent().equals("<VarDef>")){VarDef(ast);}
        else if(ast.getContent().equals("<InitVal>")){ConstInitVal(ast);}
        else if(ast.getContent().equals("<FuncDef>")){FuncDef(ast);}
        else if(ast.getContent().equals("<FuncFParams>")){FuncFParams(ast);}
        else if(ast.getContent().equals("<FuncFParam>")){FuncFParam(ast);}
        else if(ast.getContent().equals("<MainFuncDef>")){MainFuncDef(ast);}
        else if(ast.getContent().equals("<Block>")){Block(ast);}
        else if(ast.getContent().equals("<Stmt>")){Stmt(ast);}
        else if(ast.getContent().equals("<Number>")){Number1(ast);}
        else if(ast.getContent().equals("<Exp>")){AddMulExp(ast);}
        else if(ast.getContent().equals("<Cond>")){Cond(ast);}
        else if(ast.getContent().equals("<LVal>")){LVal(ast);}
        else if(ast.getContent().equals("<FuncRParams>")){FuncRParams(ast);}
        else if(ast.getContent().equals("<PrimaryExp>")){PrimaryExp(ast);}
        else if(ast.getContent().equals("<UnaryExp>")){UnaryExp(ast);}
        else if(ast.getContent().equals("<MulExp>")){AddMulExp(ast);}
        else if(ast.getContent().equals("<AddExp>")){AddMulExp(ast);}
        else if(ast.getContent().equals("<RelExp>")){RelEqExp(ast);}
        else if(ast.getContent().equals("<EqExp>")){RelEqExp(ast);}
        else if(ast.getContent().equals("<LAndExp>")){LAndExp(ast);}
        else if(ast.getContent().equals("<LOrExp>")){LOrExp(ast);}
        else{
            for(int i=0;i<ast.getChild().size();i++){
                generate(ast.getChild().get(i));
            }
        }
    }
    public void ConstDef(AstNode ast){
        ArrayList<AstNode> a=ast.getChild();
        AstNode ident = a.get(0);
        KeyValue k=ident.getKey();
        if(a.size()==3){
            if(level!=0) //非全局
            {
                ans+=tags()+"%v"+this.regId+" = alloca i32\n";
                ident.setType("%v"+this.regId);
                ident.setRegId("%v"+this.regId);
                this.regId++;
            }
            k.setDim(0);
            a.get(2).setKey(k);
            generate(a.get(2));
            k.setIntVal(a.get(2).getKey().getIntVal());
            if(level==0){
                ans+="@"+ident.getContent()+" = dso_local global i32 "+k.getIntVal()+"\n";
            }
            else{
                ans+=tags()+"store i32 "+a.get(2).getType()+", i32* "+ident.getRegId()+"\n";
            }
        }
        else if(a.size()==6){
            int l=this.level;
            this.level=0;
            generate(a.get(2));
            this.level=l;
            if(level!=0){
                ans+=tags()+"%v"+this.regId+" = alloca ["+a.get(2).getType()+" x i32]\n";
                ident.setType("%v"+this.regId);
                ident.setRegId("%v"+this.regId);
                this.regId++;
            }
            k.setDim(1);
            k.setD1(Integer.parseInt(a.get(2).getType()));
            a.get(5).setKey(k);
            generate(a.get(5));

            if(level==0){
                ans+="@"+ident.getContent()+" = dso_local constant ["+k.getD1()+" x i32] [";
                String []d1v = k.getD1Value();
                for(int i=0;i<k.getD1()-1;i++){
                    ans+="i32 "+d1v[i]+", ";
                }
                ans+="i32 "+k.getD1Value()[k.getD1()-1]+"]\n";
            }
            else{
                String []d1v = k.getD1Value();
                for(int i=0;i<k.getD1();i++){
                    if(!(d1v[i].equals("NuLL"))){
                        ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD1()+" x i32], ["+k.getD1()+" x i32]*"+ident.getRegId()+", i32 0, i32 "+i+"\n";
                        ans+=tags()+"store i32 "+d1v[i]+", i32* %v"+this.regId+"\n";
                        this.regId++;
                    }
                }
            }
        }
        else if(a.size()==9){
            int l=this.level;
            this.level=0;
            generate(a.get(2));
            generate(a.get(5));
            this.level=l;
            if(level!=0){
                ans+=tags()+"%v"+this.regId+" = alloca ["+a.get(2).getType()+" x [ "+a.get(5).getType() +" x i32]]\n";
                ident.setType("%v"+this.regId);
                ident.setRegId("%v"+this.regId);
                this.regId++;
            }
            k.setDim(2);
            k.setD1(Integer.parseInt(a.get(2).getType()));
            k.setD2(Integer.parseInt(a.get(5).getType()));
            a.get(8).setKey(k);
            generate(a.get(8));
            if(level==0){
                ans+="@"+ident.getContent()+" = dso_local constant ["+k.getD1()+" x ["+k.getD2()+" x i32]] [[";
                String [][]d2v = k.getD2Value();
                for(int i=0;i<k.getD1()-1;i++){
                    ans+=k.getD2()+" x i32] [";
                    for(int j=0;j<k.getD2()-1;j++){
                        ans+="i32 "+d2v[i][j]+", ";
                    }
                    ans+="i32 "+k.getD2Value()[i][k.getD2()-1]+"], [";
                }
                ans+=k.getD2()+" x i32] [";
                for(int j=0;j<k.getD2()-1;j++){
                    ans+="i32 "+d2v[k.getD1()-1][j]+", ";
                }
                ans+="i32 "+k.getD2Value()[k.getD1()-1][k.getD2()-1]+"]]\n";
            }
            else{
                String [][]d2v = k.getD2Value();
                for(int i=0;i<k.getD1();i++){
                    for(int j=0;j<k.getD2();j++){
                        if(!(d2v[i][j].equals("NuLL"))){
                            ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD1()+" x ["+k.getD2()+" x i32]], ["+k.getD1()+" x ["+k.getD2()+" x i32]]*"+ident.getRegId()+", i32 0, i32 "+i+", i32 "+j+"\n";
                            ans+=tags()+"store i32 "+d2v[i][j]+", i32* %v"+this.regId+"\n";
                            this.regId++;
                        }
                    }
                }
            }
        }
        ident.setKey(k);
        if(level==0){
            global.put(ident.getContent(),ident);
        }
        else{
            ident.setLevel(this.level);
            stack.add(ident);
        }
    }

    public void ConstInitVal(AstNode ast){
        ArrayList<AstNode> a=ast.getChild();
        KeyValue k= ast.getKey();
        if(k.getDim()==0){
            generate(a.get(0));
            ast.getKey().setIntVal(a.get(0).getType());
            ast.setType(a.get(0).getType());
        }
        else if(k.getDim()==1){
            int j=0;
            String[] d1v = k.getD1Value();
            for(int i=1;i<a.size()-1;i+=2){
                KeyValue k1=a.get(i).getKey();
                k1.setDim(0);
                a.get(i).setKey(k1);
                generate(a.get(i));
                d1v[j]=a.get(i).getType();
                j++;
            }
            if(j<k.getD1()){
                for(;j<k.getD1();j++){
                    if(this.level!=0){d1v[j]="NuLL";}
                    else{d1v[j]="0";}
                }
            }
            k.setD1Value(d1v);
        }
        else if(k.getDim()==2){
            int m=0;
            String[][] d2v = k.getD2Value();
            if(a.get(1).getChild().get(0).getContent().equals("{")){
                for(int i=1;i<a.size()-1;i+=2){
                    KeyValue k1=a.get(i).getKey();
                    k1.setDim(1);
                    k1.setD1(k.getD2());
                    a.get(i).setKey(k1);
                    generate(a.get(i));
                    String[] d=a.get(i).getKey().getD1Value();
                    for(int n=0;n<a.get(i).getKey().getD1();n++){
                        d2v[m][n]=d[n];
                    }
                    m++;
                }
            }
            else{
                int j=0;
                for(int i=1;i<a.size()-1;i+=2){
                    KeyValue k1=a.get(i).getKey();
                    k1.setDim(0);
                    a.get(i).setKey(k1);
                    generate(a.get(i));
                    d2v[0][j]=a.get(i).getType();
                    j++;
                }
                if(j<k.getD1()){
                    for(;j<k.getD1();j++){
                        if(this.level!=0){d2v[0][j]="NuLL";}
                        else{d2v[0][j]="0";}
                    }
                }
            }
            if(m<k.getD1()){
                for(;m<k.getD1();m++){
                    for(int n=0;n<k.getD2();n++){
                        if(this.level!=0){d2v[m][n]="NuLL";}
                        else{d2v[m][n]="0";}
                    }
                }
            }
        }
    }
    public void VarDef(AstNode ast){
        ArrayList<AstNode> a=ast.getChild();
        AstNode ident = a.get(0);
        KeyValue k=ident.getKey();
        if(a.size()==1||a.size()==3){
            if(level!=0){
                ans+=tags()+"%v"+this.regId+" = alloca i32\n";
                ident.setType("%v"+this.regId);
                ident.setRegId("%v"+this.regId);
                this.regId++;
            }
            k.setDim(0);
            if(a.size()==3){
                generate(a.get(2));
                k.setIntVal(a.get(2).getKey().getIntVal());
                if(level!=0){
                    ans+=tags()+"store i32 "+a.get(2).getType()+", i32* "+ident.getRegId()+"\n";
                }
            }
            else if(a.size()==1){
                k.setIntVal("0");
            }
            if(level==0){
                ans+="@"+ident.getContent()+" = dso_local global i32 "+k.getIntVal()+"\n";
            }
        }
        else if(a.size()==4||a.size()==6){
            int l=this.level;
            this.level=0;
            generate(a.get(2));
            this.level=l;
            if(level!=0){
                ans+=tags()+"%v"+this.regId+" = alloca ["+a.get(2).getType()+" x i32]\n";
                ident.setType("%v"+this.regId);
                ident.setRegId("%v"+this.regId);
                this.regId++;
            }
            k.setDim(1);
            k.setD1(Integer.parseInt(a.get(2).getType()));
            String []d1v = k.getD1Value();
            if(a.size()==6){
                a.get(5).setKey(k);
                generate(a.get(5));
            }
            else if(a.size()==4){
                for(int i=0;i<k.getD1();i++){
                    if(level==0){d1v[i]="0";}
                    else{d1v[i]="NuLL";}
                }
                k.setD1Value(d1v);
            }
            if(level==0){
                if(a.size()==6){
                    ans+="@"+ident.getContent()+" = dso_local global ["+k.getD1()+" x i32] [";
                    d1v=k.getD1Value();
                    for(int i=0;i<k.getD1()-1;i++){
                        ans+="i32 "+d1v[i]+", ";
                    }
                    ans+="i32 "+k.getD1Value()[k.getD1()-1]+"]\n";
                }
                else{
                    ans+="@"+ident.getContent()+" = dso_local global ["+k.getD1()+" x i32] zeroinitializer\n";
                }
            }
            else{
                d1v = k.getD1Value();
                for(int i=0;i<k.getD1();i++){
                    if(!(d1v[i].equals("NuLL"))){
                        ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD1()+" x i32], ["+k.getD1()+" x i32]*"+ident.getRegId()+", i32 0, i32 "+i+"\n";
                        ans+=tags()+"store i32 "+d1v[i]+", i32* %v"+this.regId+"\n";
                        this.regId++;
                    }
                }
            }
        }
        else if(a.size()==7||a.size()==9){
            int l=this.level;
            this.level=0;
            generate(a.get(2));
            generate(a.get(5));
            this.level=l;
            if(level!=0){
                ans+=tags()+"%v"+this.regId+" = alloca ["+a.get(2).getType()+" x [ "+a.get(5).getType() +" x i32]]\n";
                ident.setType("%v"+this.regId);
                ident.setRegId("%v"+this.regId);
                this.regId++;
            }
            k.setDim(2);
            k.setD1(Integer.parseInt(a.get(2).getType()));
            k.setD2(Integer.parseInt(a.get(5).getType()));
            String [][]d2v = k.getD2Value();
            if(a.size()==9){
                a.get(8).setKey(k);
                generate(a.get(8));
            }
            else if(a.size()==7){
                for(int i=0;i<k.getD1();i++){
                    for(int j=0;j<k.getD2();j++){
                        if(level==0){
                            d2v[i][j]="0";
                        }
                        else{
                            d2v[i][j]="NuLL";
                        }
                    }
                }
                k.setD2Value(d2v);
            }
            if(level==0){
                if(a.size()==9){
                    ans+="@"+ident.getContent()+" = dso_local global ["+k.getD1()+" x ["+k.getD2()+" x i32]] [[";
                    d2v=k.getD2Value();
                    for(int i=0;i<k.getD1()-1;i++){
                        ans+=k.getD2()+" x i32] [";
                        for(int j=0;j<k.getD2()-1;j++){
                            ans+="i32 "+d2v[i][j]+", ";
                        }
                        ans+="i32 "+k.getD2Value()[i][k.getD2()-1]+"], [";
                    }
                    ans+=k.getD2()+" x i32] [";
                    for(int j=0;j<k.getD2()-1;j++){
                        ans+="i32 "+d2v[k.getD1()-1][j]+", ";
                    }
                    ans+="i32 "+k.getD2Value()[k.getD1()-1][k.getD2()-1]+"]]\n";
                }
                else{
                    ans+="@"+ident.getContent()+" = dso_local global ["+k.getD1()+" x ["+k.getD2()+" x i32]] zeroinitializer\n";
                }
            }
            else{
                d2v = k.getD2Value();
                for(int i=0;i<k.getD1();i++){
                    for(int j=0;j<k.getD2();j++){
                        if(!(d2v[i][j].equals("NuLL"))){
                            ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD1()+" x ["+k.getD2()+" x i32]], ["+k.getD1()+" x ["+k.getD2()+" x i32]]*"+ident.getRegId()+", i32 0, i32 "+i+", i32 "+j+"\n";
                            ans+=tags()+"store i32 "+d2v[i][j]+", i32* %v"+this.regId+"\n";
                            this.regId++;
                        }
                    }
                }
            }
        }
        ident.setKey(k);
        if(level==0){
            global.put(ident.getContent(),ident);
        }
        else{
            ident.setLevel(this.level);
            stack.add(ident);
        }
    }
    public void FuncDef(AstNode ast){
        ArrayList<AstNode> a=ast.getChild();
        String Type = a.get(0).getChild().get(0).getContent();
        AstNode ident = a.get(1);
        if(Type.equals("int")){Type="i32";}
        else if(Type.equals("void")){Type="void";}
        ident.setReturnType(Type);
        ans+="define dso_local "+Type+" @"+ident.getContent();
        global.put(ident.getContent(),ident);
        if(a.get(2).getContent().equals("(")){
            ans+="(";
            if(a.get(4).getContent().equals(")")){
                generate(a.get(3));
                ans+=") {\n";
                generate(a.get(5));
            }
            else{
                ans+=") {\n";
                generate(a.get(4));
            }
        }
        if(Type.equals("void")){
            this.nowtag+=1;
            ans+=tags()+"ret void\n";
            this.nowtag-=1;
        }
        ans+="}\n";

    }
    public void FuncFParams(AstNode ast){
        ArrayList<AstNode> a=ast.getChild();
        generate(a.get(0));
        for(int i=2;i<a.size();i+=2){
            ans+=", ";
            generate(a.get(i));
        }
    }
    public void FuncFParam(AstNode ast){
        ArrayList<AstNode> a=ast.getChild();
        AstNode ident = a.get(1);
        ident.setLevel(1);
        if(a.size()==2){
            ans+="i32 %v"+this.regId;
            ident.setType("%v"+this.regId);
            ident.getKey().setAddrType("i32");
            this.regId++;
            stack.add(ident);
        }
        else if(a.size()==4){
            ans+="i32* %v"+this.regId;
            ident.setType("%v"+this.regId);
            ident.getKey().setAddrType("i32*");
            ident.getKey().setDim(1);
            ident.getKey().setD1(0);
            this.regId++;
            stack.add(ident);
        }
        else if(a.size()==7){
            generate(a.get(5));
            ans+="["+a.get(5).getType()+" x i32] *%v"+this.regId;
            ident.setType("%v"+this.regId);
            ident.getKey().setDim(2);
            ident.getKey().setD1(0);
            ident.getKey().setD2(Integer.parseInt(a.get(5).getType()));
            ident.getKey().setAddrType("["+a.get(5).getType()+" x i32]*");
            this.regId++;
            stack.add(ident);
        }

    }
    public void MainFuncDef(AstNode ast){
        ans+="\ndefine dso_local i32 @main() {\n";
        generate(ast.getChild().get(4));//Block
        ans+="}\n";
    }
    public void Block(AstNode ast){
        ArrayList<AstNode> a=ast.getChild();
        for(int i=0;i<a.size();i++){
            if(a.get(i).getContent().equals("{")){
                if(level==0){
                    nowtag+=1;
                }
                level+=1;
                if(level==1){
                    for(int j=stack.size()-1;j>=0;j--){
                        if(stack.get(j).getRegId().equals("")&&stack.get(j).getLevel()==1){
                            ans+=tags()+"%v"+this.regId+" = alloca "+stack.get(j).getKey().getAddrType()+"\n";
                            stack.get(j).setRegId("%v"+this.regId);
                            ans+=tags()+"store "+stack.get(j).getKey().getAddrType()+" "+stack.get(j).getType()+", "+stack.get(j).getKey().getAddrType()+" * "+stack.get(j).getRegId()+"\n";
                            this.regId++;
                        }
                    }
                }
            }
            else if(a.get(i).getContent().equals("}")){
                for(int j=stack.size()-1;j>=0;j--){
                    if(stack.get(j).getLevel()==this.level){stack.remove(j);}
                }
                level-=1;
                if(level==0){
                    nowtag-=1;
                }
            }
            else{
                a.get(i).setContinueId(ast.getContinueId());
                a.get(i).setBreakId(ast.getBreakId());
                generate(a.get(i));
            }
        }
    }
    public void Stmt(AstNode ast){
        ArrayList<AstNode> a=ast.getChild();
        if(a.get(0).getContent().equals("<Block>")){
            a.get(0).setContinueId(ast.getContinueId());
            a.get(0).setBreakId(ast.getBreakId());
            generate(a.get(0));
        }
        else if(a.get(0).getContent().equals("return")){
            if(a.get(1).getContent().equals(";")){
                ans+=tags()+"ret void\n";
            }
            else{
                generate(a.get(1));//Exp
                ans+=tags()+"ret i32 "+a.get(1).getType()+"\n";
            }

        }
        else if(a.get(0).getContent().equals("<LVal>")){
            generate(a.get(0));//LVal
            if(a.get(2).getContent().equals("<Exp>")){
                generate(a.get(2));//Exp
                ans+=tags()+"store i32 "+a.get(2).getType()+", i32* "+a.get(0).getRegId()+"\n";
            }
            else if(a.get(2).getContent().equals("getint")){
                ans+=tags()+"%v"+this.regId+" = call i32 @getint()"+"\n";
                ans+=tags()+"store i32 "+"%v"+this.regId+", i32* "+a.get(0).getRegId()+"\n";
                this.regId++;
            }
        }
        else if(a.get(0).getContent().equals("<Exp>")){generate(a.get(0));}
        else if(a.get(0).getContent().equals("printf")){
            int parNum=4;
            String s=a.get(2).getContent();
            for(int i=1;i<s.length()-1;i++){
                if(s.charAt(i)=='%'&&s.charAt(i+1)=='d'){
                    i++;
                    generate(a.get(parNum));
                    parNum+=2;
                }
            }
            parNum=4;
            for(int i=1;i<s.length()-1;i++){
                if(s.charAt(i)=='%'&&s.charAt(i+1)=='d'){
                    i++;
                    ans+=tags()+"call void @putint(i32 "+a.get(parNum).getType()+")\n";
                    parNum+=2;
                }
                else if(s.charAt(i)=='\\'&&s.charAt(i+1)=='n'){
                    i++;
                    ans+=tags()+"call void @putch(i32 10)\n";

                }
                else{
                    ans+=tags()+"call void @putch(i32 "+(int) s.charAt(i)+")\n";
                }
            }


        }
        else if(a.get(0).getContent().equals("if")){
            ans+=tags()+"br label %v"+this.regId+"\n";
            ans+="\nv"+this.regId+":\n";
            a.get(2).setYesId(this.regId+1);
            int YesId = this.regId+1;
            int NoId=0;
            int StmtId=0;
            if(a.size()>5){
                a.get(2).setNoId(this.regId+2);
                a.get(2).setStmtId(this.regId+3);
                a.get(4).setStmtId(this.regId+3);
                a.get(4).setContinueId(ast.getContinueId());
                a.get(4).setBreakId(ast.getBreakId());
                a.get(6).setStmtId(this.regId+3);
                a.get(6).setContinueId(ast.getContinueId());
                a.get(6).setBreakId(ast.getBreakId());
                NoId = this.regId+2;
                StmtId = this.regId+3;
                this.regId+=4;
            }
            else{
                a.get(2).setNoId(this.regId+2);
                a.get(2).setStmtId(this.regId+2);
                a.get(4).setStmtId(this.regId+2);
                a.get(4).setContinueId(ast.getContinueId());
                a.get(4).setBreakId(ast.getBreakId());
                StmtId = this.regId+2;
                this.regId+=3;
            }
            generate(a.get(2));
            ans+="\nv"+YesId+":\n";
            generate(a.get(4));
            if(a.size()>5){
                ans+="\nv"+NoId+":\n";
                generate(a.get(6));
            }
            ans+="\nv"+StmtId+":\n";
        }
        else if(a.get(0).getContent().equals("while")){
            ans+=tags()+"br label %v"+this.regId+"\n";
            ans+="\nv"+this.regId+":\n";
            int YesId = this.regId+1;
            int NoId=this.regId+2;
            int StmtId=this.regId+2;
            a.get(2).setYesId(this.regId+1);
            a.get(2).setNoId(this.regId+2);
            a.get(2).setStmtId(this.regId+2);
            a.get(4).setStmtId(this.regId);
            a.get(4).setBreakId(this.regId+2);
            a.get(4).setContinueId(this.regId);
            this.regId+=3;
            generate(a.get(2));
            ans+="\nv"+YesId+":\n";
            generate(a.get(4));
            ans+="\nv"+StmtId+":\n";
        }
        else if(a.get(0).getContent().equals("break")){
            ast.setStmtId(ast.getBreakId());
        }
        else if(a.get(0).getContent().equals("continue")){ast.setStmtId(ast.getContinueId());}
        if(ast.getStmtId()!=0){
            ans+=tags()+"br label %v"+ast.getStmtId()+"\n";
        }
    }

    public void LVal(AstNode ast){
        ArrayList<AstNode> a=ast.getChild();
        AstNode ident = a.get(0);
        String identName=ident.getContent();
        KeyValue k = ident.getKey();
        int check=0;
        for(int i=stack.size()-1;i>=0;i--){
            if(stack.get(i).getContent().equals(identName)){
                k=stack.get(i).getKey();
                if(a.size()==1){
                    if(stack.get(i).getKey().getDim()==0){
                        ans+=tags()+"%v"+this.regId+" = load i32, i32* "+stack.get(i).getRegId()+"\n";
                        k.setAddrType("i32");
                        ast.setType("%v"+this.regId);
                        ast.setRegId(stack.get(i).getRegId());
                        this.regId++;
                    }
                    else if(stack.get(i).getKey().getDim()==1){
                        if(k.getD1()!=0){
                            ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD1()+" x i32], ["+k.getD1()+" x i32]*"+stack.get(i).getRegId()+", i32 0, i32 0\n";
                            k.setAddrType("i32*");
                            ast.setType("%v"+(this.regId));
                            ast.setRegId("%v"+(this.regId));
                            this.regId+=2;
                        }
                        else{
                            k.setAddrType("i32*");
                            ans+=tags()+"%v"+this.regId+" = load "+k.getAddrType()+", "+k.getAddrType()+" * "+stack.get(i).getRegId()+"\n";
                            ast.setType("%v"+(this.regId));
                            ast.setRegId("%v"+(this.regId));
                            this.regId+=1;
                        }
                    }
                    else if(stack.get(i).getKey().getDim()==2){
                        if(k.getD1()!=0){
                            ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD1()+" x ["+k.getD2()+" x i32]], ["+k.getD1()+" x ["+k.getD2()+" x i32]]*"+stack.get(i).getRegId()+", i32 0, i32 0\n";
                            k.setAddrType("["+k.getD2()+" x i32]*");
                            ast.setType("%v"+(this.regId));
                            ast.setRegId("%v"+(this.regId));
                            this.regId++;
                        }
                        else{
                            ans+=tags()+"%v"+this.regId+" = load ["+k.getD2()+" x i32] *, ["+k.getD2()+" x i32]* * "+stack.get(i).getRegId()+"\n";
                            k.setAddrType("["+k.getD2()+" x i32]*");
                            ast.setType("%v"+(this.regId));
                            ast.setRegId(stack.get(i).getRegId());
                            this.regId++;
                        }
                    }
                }
                else if(a.size()==4){
                    if(stack.get(i).getKey().getDim()==1){
                        if(k.getD1()!=0){
                            generate(a.get(2));
                            ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD1()+" x i32], ["+k.getD1()+" x i32]*"+stack.get(i).getRegId()+", i32 0, i32 "+a.get(2).getType()+"\n";
                            ans+=tags()+"%v"+(this.regId+1)+" = load i32, i32* %v"+this.regId+"\n";
                            k.setAddrType("i32");
                            ast.setType("%v"+(this.regId+1));
                            ast.setRegId("%v"+(this.regId));
                            this.regId+=2;
                        }
                        else{
                            //int a[]; a
                            generate(a.get(2));
                            ans+=tags()+"%v"+this.regId+" = load i32*, i32* * "+stack.get(i).getRegId()+"\n";
                            ans+=tags()+"%v"+(this.regId+1)+" = getelementptr i32, i32* %v"+this.regId+", i32 "+a.get(2).getType()+"\n";
                            ans+=tags()+"%v"+(this.regId+2)+" = load i32, i32* %v"+(this.regId+1)+"\n";
                            ast.setType("%v"+(this.regId+2));
                            ast.setRegId("%v"+(this.regId+1));
                            k.setAddrType("i32");
                            this.regId+=3;
                        }
                    }
                    else if(stack.get(i).getKey().getDim()==2){
                        if(k.getD1()!=0){
                            generate(a.get(2));
                            ans+=tags()+"%v"+this.regId+" = mul i32 "+a.get(2).getType()+", "+k.getD2()+"\n";
                            this.regId++;
                            ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD1()+" x ["+k.getD2()+" x i32]], ["+k.getD1()+" x ["+k.getD2()+" x i32]]*"+stack.get(i).getRegId()+", i32 0, i32 0\n";
                            this.regId++;
                            ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD2()+" x i32], ["+k.getD2()+" x i32]* %v"+(this.regId-1)+", i32 0, i32 %v"+(this.regId-2)+"\n";
                            k.setAddrType("i32*");
                            ast.setType("%v"+(this.regId));
                            ast.setRegId("%v"+(this.regId));
                            this.regId+=1;
                        }
                        else{
                            // int a[]; a[2]
                            generate(a.get(2));
                            ans+=tags()+"%v"+this.regId+" = load ["+k.getD2()+" x i32] *, ["+k.getD2()+" x i32]* * "+stack.get(i).getRegId()+"\n";
                            this.regId++;
                            ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD2()+" x i32], ["+k.getD2()+" x i32]* %v"+(this.regId-1)+", i32 "+a.get(2).getType()+"\n";
                            this.regId++;
                            ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD2()+" x i32], ["+k.getD2()+" x i32]* %v"+(this.regId-1)+", i32 0, i32 0\n";
                            k.setAddrType("i32*");
                            ast.setType("%v"+(this.regId));
                            ast.setRegId("%v"+(this.regId));
                            this.regId++;
                        }
                    }
                }
                else if(a.size()==7){
                    generate(a.get(2));
                    generate(a.get(5));
                    if(k.getD1()!=0){
                        ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD1()+" x ["+k.getD2()+" x i32]], ["+k.getD1()+" x ["+k.getD2()+" x i32]]*"+stack.get(i).getRegId()+", i32 0, i32 "+a.get(2).getType()+", i32 "+a.get(5).getType()+"\n";
                        ans+=tags()+"%v"+(this.regId+1)+" = load i32, i32* %v"+this.regId+"\n";
                        k.setAddrType("i32");
                        ast.setType("%v"+(this.regId+1));
                        ast.setRegId("%v"+(this.regId));
                        this.regId+=2;
                    }
                    else{
                        ans+=tags()+"%v"+this.regId+" = load ["+k.getD2()+" x i32] *, ["+k.getD2()+" x i32]* * "+stack.get(i).getRegId()+"\n";
                        this.regId++;
                        ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD2()+" x i32], ["+k.getD2()+" x i32]* %v"+(this.regId-1)+", i32 "+a.get(2).getType()+"\n";
                        this.regId++;
                        ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD2()+" x i32], ["+k.getD2()+" x i32]* %v"+(this.regId-1)+", i32 0, i32 "+a.get(5).getType()+"\n";
                        ans+=tags()+"%v"+(this.regId+1)+" = load i32, i32 *%v"+(this.regId)+"\n";
                        k.setAddrType("i32");
                        ast.setType("%v"+(this.regId+1));
                        ast.setRegId("%v"+(this.regId));
                        this.regId+=2;
                    }
                }
                check=1;
                break;
            }
        }
        if(check==0){
            k=global.get(identName).getKey();
            if(level>0){
                if(a.size()==1){
                    if(k.getDim()==0){
                        ans+=tags()+"%v"+this.regId+" = load i32, i32* @"+identName+"\n";
                        k.setAddrType("i32");
                        ast.setType("%v"+this.regId);
                        ast.setRegId("@"+identName);
                        this.regId++;
                    }
                    else if(k.getDim()==1){
                        if(k.getD1()!=0){
                            ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD1()+" x i32], ["+k.getD1()+" x i32]* @"+identName+", i32 0, i32 0\n";
                            k.setAddrType("i32*");
                            ast.setType("%v"+(this.regId));
                            ast.setRegId("%v"+(this.regId));
                            this.regId+=2;
                        }
                        else{
                            k.setAddrType("i32*");
                            ans+=tags()+"%v"+this.regId+" = load "+k.getAddrType()+", "+k.getAddrType()+"* @"+identName+"\n";
                            ast.setType("%v"+(this.regId));
                            ast.setRegId("%v"+(this.regId));
                            this.regId+=1;
                        }
                    }
                    else if(k.getDim()==2){
                        if(k.getD1()!=0){
                            ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD1()+" x ["+k.getD2()+" x i32]], ["+k.getD1()+" x ["+k.getD2()+" x i32]]* @"+identName+", i32 0, i32 0\n";
                            k.setAddrType("["+k.getD2()+" x i32]*");
                            ast.setType("%v"+(this.regId));
                            ast.setRegId("%v"+(this.regId));
                            this.regId++;
                        }
                        else{
                            ans+=tags()+"%v"+this.regId+" = load ["+k.getD2()+" x i32] *, ["+k.getD2()+" x i32]* * @"+identName+"\n";
                            k.setAddrType("["+k.getD2()+" x i32]*");
                            ast.setType("%v"+(this.regId));
                            ast.setRegId("@"+identName);
                            this.regId++;
                        }
                    }
                }
                else if(a.size()==4){
                    if(k.getDim()==1){
                        if(k.getD1()!=0){
                            generate(a.get(2));
                            ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD1()+" x i32], ["+k.getD1()+" x i32]* @"+identName+", i32 0, i32 "+a.get(2).getType()+"\n";
                            ans+=tags()+"%v"+(this.regId+1)+" = load i32, i32* %v"+this.regId+"\n";
                            k.setAddrType("i32");
                            ast.setType("%v"+(this.regId+1));
                            ast.setRegId("%v"+(this.regId));
                            this.regId+=2;
                        }
                        else{
                            //int a[]; a
                            generate(a.get(2));
                            ans+=tags()+"%v"+this.regId+" = load i32*, i32* * @"+identName+"\n";
                            ans+=tags()+"%v"+(this.regId+1)+" = getelementptr i32, i32* %v"+this.regId+", i32 "+a.get(2).getType()+"\n";
                            ans+=tags()+"%v"+(this.regId+2)+" = load i32, i32* %v"+(this.regId+1)+"\n";
                            ast.setType("%v"+(this.regId+2));
                            ast.setRegId("%v"+(this.regId+1));
                            k.setAddrType("i32");
                            this.regId+=3;
                        }
                    }
                    else if(k.getDim()==2){
                        if(k.getD1()!=0){
                            generate(a.get(2));
                            ans+=tags()+"%v"+this.regId+" = mul i32 "+a.get(2).getType()+", "+k.getD2()+"\n";
                            this.regId++;
                            ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD1()+" x ["+k.getD2()+" x i32]], ["+k.getD1()+" x ["+k.getD2()+" x i32]]* @"+identName+", i32 0, i32 0\n";
                            this.regId++;
                            ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD2()+" x i32], ["+k.getD2()+" x i32]* %v"+(this.regId-1)+", i32 0, i32 %v"+(this.regId-2)+"\n";
                            k.setAddrType("i32*");
                            ast.setType("%v"+(this.regId));
                            ast.setRegId("%v"+(this.regId));
                            this.regId+=1;
                        }
                        else{
                            // int a[]; a[2]
                            generate(a.get(2));
                            ans+=tags()+"%v"+this.regId+" = load ["+k.getD2()+" x i32] *, ["+k.getD2()+" x i32]* * @"+identName+"\n";
                            this.regId++;
                            ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD2()+" x i32], ["+k.getD2()+" x i32]* %v"+(this.regId-1)+", i32 "+a.get(2).getType()+"\n";
                            this.regId++;
                            ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD2()+" x i32], ["+k.getD2()+" x i32]* %v"+(this.regId-1)+", i32 0, i32 0\n";
                            k.setAddrType("i32*");
                            ast.setType("%v"+(this.regId));
                            ast.setRegId("%v"+(this.regId));
                            this.regId++;
                        }
                    }
                }
                else if(a.size()==7){
                    generate(a.get(2));
                    generate(a.get(5));
                    if(k.getD1()!=0){
                        ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD1()+" x ["+k.getD2()+" x i32]], ["+k.getD1()+" x ["+k.getD2()+" x i32]]* @"+identName+", i32 0, i32 "+a.get(2).getType()+", i32 "+a.get(5).getType()+"\n";
                        ans+=tags()+"%v"+(this.regId+1)+" = load i32, i32* %v"+this.regId+"\n";
                        k.setAddrType("i32");
                        ast.setType("%v"+(this.regId+1));
                        ast.setRegId("%v"+(this.regId));
                        this.regId+=2;
                    }
                    else{
                        ans+=tags()+"%v"+this.regId+" = load ["+k.getD2()+" x i32] *, ["+k.getD2()+" x i32]* * @"+identName+"\n";
                        this.regId++;
                        ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD2()+" x i32], ["+k.getD2()+" x i32]* %v"+(this.regId-1)+", i32 "+a.get(2).getType()+"\n";
                        this.regId++;
                        ans+=tags()+"%v"+this.regId+" = getelementptr ["+k.getD2()+" x i32], ["+k.getD2()+" x i32]* %v"+(this.regId-1)+", i32 0, i32 "+a.get(5).getType()+"\n";
                        ans+=tags()+"%v"+(this.regId+1)+" = load i32, i32 *%v"+(this.regId)+"\n";
                        k.setAddrType("i32");
                        ast.setType("%v"+(this.regId+1));
                        ast.setRegId("%v"+(this.regId));
                        this.regId+=2;
                    }
                }
            }
            else{
                if(a.size()==1){
                    ast.setType(global.get(identName).getKey().getIntVal());
                }
                else if(a.size()==4){
                    generate(a.get(2));
                    ast.setType(global.get(identName).getKey().getD1Value()[Integer.parseInt(a.get(2).getType())]);
                }
                else if(a.size()==7){
                    generate(a.get(2));
                    generate(a.get(5));
                    ast.setType(global.get(identName).getKey().getD2Value()[Integer.parseInt(a.get(2).getType())][Integer.parseInt(a.get(5).getType())]);
                }
            }
        }
        ident.setKey(k);
        ast.setKey(k);
    }
    public void Cond(AstNode ast){
//        ast.getChild().get(0).setNoId(ast.getNoId());
//        ast.getChild().get(0).setYesId(ast.getYesId());
//        ast.getChild().get(0).setStmtId(ast.getStmtId());
        ArrayList<AstNode> a=ast.getChild();
        for(int i=0;i<a.size()-2;i+=2){
            if(a.get(i).getChild().get(0).getChild().size()==1){
                generate(a.get(i));//LOrExp-land
                ans+=tags()+"%v"+this.regId+" = icmp ne i32 0, "+a.get(i).getType()+"\n";
                ans+=tags()+"br i1 %v"+this.regId+", label %v"+ast.getYesId()+", label %v"+(this.regId+1)+"\n";
                this.regId+=2;
                ans+="\nv"+(this.regId-1)+":\n";
            }
            else{
                a.get(i).setYesId(ast.getYesId());
                a.get(i).setStmtId(ast.getStmtId());
                a.get(i).setNoId(this.regId);
                this.regId++;
                generate(a.get(i));//lor-land-land
                ans+="\nv"+a.get(i).getNoId()+":\n";
            }
        }
        ast.setType("%v"+this.regId);
        this.regId++;
    }
    public void PrimaryExp(AstNode ast){
        ArrayList<AstNode> a=ast.getChild();

        if(a.get(0).getContent().equals("<Number>")){
            generate(a.get(0));//Number
            ast.setType(a.get(0).getType());
        }
        else if(a.get(0).getContent().equals("(")){
            generate(a.get(1));//Exp
            ast.setType(a.get(1).getType());
        }
        else if(a.get(0).getContent().equals("<LVal>")){
            generate(a.get(0));//LVal
            ast.setType(a.get(0).getType());
            ast.setRegId(a.get(0).getRegId());
            ast.setKey(a.get(0).getKey());
        }
    }
    public void Number1(AstNode ast){
        ArrayList<AstNode> a=ast.getChild();
        ast.setType(a.get(0).getContent());
    }

    public void UnaryExp(AstNode ast){
        ArrayList<AstNode> a=ast.getChild();
        if(a.get(0).getContent().equals("<UnaryOp>")){
            generate(a.get(1));//UnaryExp
            if(a.get(0).getChild().get(0).getContent().equals("-")){
                if(level>0){
                    ans+=tags()+"%v"+this.regId+" = sub i32 0, "+a.get(1).getType()+"\n";
                    ast.setRegId("%v"+this.regId);
                    ast.setType("%v"+this.regId);
                    this.regId++;
                }
                else{
                    ast.setType(mathCalculate("0","-",a.get(1).getType()));
                }
            }
            else if(a.get(0).getChild().get(0).getContent().equals("+")){ast.setType(a.get(1).getType());}
            else if(a.get(0).getChild().get(0).getContent().equals("!")){
                ans+=tags()+"%v"+this.regId+" = icmp eq i32 0, "+a.get(1).getType()+"\n";
                this.regId++;
                ans+=tags()+"%v"+this.regId+" = zext i1 %v"+(this.regId-1)+" to i32\n";
                ast.setRegId("%v"+this.regId);
                ast.setType("%v"+this.regId);
                this.regId++;
            }
        }
        else if(a.get(0).getContent().equals("<PrimaryExp>")){
            generate(a.get(0));//PrimaryExp
            ast.setType(a.get(0).getType());
            ast.setRegId(a.get(0).getRegId());
            ast.setKey(a.get(0).getKey());
        }
        else if(a.size()>2&&a.get(1).getContent().equals("(")){
            AstNode ident=a.get(0);
            String identName = ident.getContent();
            AstNode identGlobe=global.get(identName);
            ident.setReturnType(identGlobe.getReturnType());
            if(ident.getReturnType().equals("i32")){
                if(a.get(2).getContent().equals(")")){
                    ans+=tags()+"%v"+this.regId+" = call "+ident.getReturnType()+" @"+ident.getContent()+"()\n";
                    ast.setType("%v"+this.regId);
                    this.regId++;
                }
                else{
                    generate(a.get(2));
                    ans+=tags()+"%v"+this.regId+" = call "+ident.getReturnType()+" @"+ident.getContent()+"("+a.get(2).getType()+")\n";
                    ast.setType("%v"+this.regId);
                    this.regId++;
                }
            }
            else if(ident.getReturnType().equals("void")){
                if(a.get(2).getContent().equals(")")){
                    ans+=tags()+"call "+ident.getReturnType()+" @"+ident.getContent()+"()\n";
                }
                else{
                    generate(a.get(2));
                    ans+=tags()+"call "+ident.getReturnType()+" @"+ident.getContent()+"("+a.get(2).getType()+")\n";
                }
            }

        }
    }

    public void FuncRParams(AstNode ast){
        ArrayList<AstNode> a=ast.getChild();
        generate(a.get(0));
        String Value;
        Value =a.get(0).getKey().getAddrType()+" "+a.get(0).getType();
        for(int i=2;i<a.size();i+=2){
            generate(a.get(i));
            Value=Value+", "+a.get(i).getKey().getAddrType()+" "+a.get(i).getType();
        }
        ast.setType(Value);
    }
    public void AddMulExp(AstNode ast){
        ArrayList<AstNode> a=ast.getChild();
        generate(a.get(0));//AddExp/MulExp
        String left=a.get(0).getType();
        if(a.size()>1){
            for(int i=1;i<a.size();i+=2){
                String op=a.get(i).getContent();
                generate(a.get(i+1));
                String right=a.get(i+1).getType();
                String opt=Operator(op);
                if(level>0){
                    ans+=tags()+"%v"+this.regId+" = "+opt+" i32 "+left+", "+right+"\n";
                    a.get(i+1).setRegId("%v"+this.regId);
                    a.get(i+1).setType("%v"+this.regId);
                    this.regId++;
                }
                else{
                    a.get(i+1).setType(mathCalculate(left,op,right));
                }
                left=a.get(i+1).getType();
            }
            ast.setType(a.get(a.size()-1).getType());
        }
        else{
            ast.setKey(a.get(0).getKey());
            ast.setType(left);
            ast.setRegId(a.get(0).getRegId());
        }
    }
    public void RelEqExp(AstNode ast){
        ArrayList<AstNode> a=ast.getChild();
        generate(a.get(0));//RelExp/EqExp
        String left=a.get(0).getType();
        if(a.size()>1){
            for(int i=1;i<a.size();i+=2){
                String op=a.get(i).getContent();
                generate(a.get(i+1));
                String right=a.get(i+1).getType();
                String opt=Operator(op);
                if(level>0){
                    ans+=tags()+"%v"+this.regId+" = icmp "+opt+" i32 "+left+", "+right+"\n";
                    this.regId++;
                    ans+=tags()+"%v"+this.regId+" = zext i1 %v"+(this.regId-1)+" to i32\n";
                    a.get(i+1).setRegId("%v"+this.regId);
                    a.get(i+1).setType("%v"+this.regId);
                    this.regId++;
                }
                else{
                    a.get(i+1).setType(mathCalculate(left,op,right));
                }
                left=a.get(i+1).getType();
            }
            ast.setType(a.get(a.size()-1).getType());
        }
        else{
            ast.setType(left);
        }
    }
    public void LAndExp(AstNode ast){
        ArrayList<AstNode> a=ast.getChild();
        if(a.size()==1){
            generate(a.get(0));
            ast.setType(a.get(0).getType());
        }
        else{
            for(int i=0;i<a.size();i+=2){
                generate(a.get(i));
                ans+=tags()+"%v"+this.regId+" = icmp ne i32 0, "+a.get(i).getType()+"\n";
                ans+=tags()+"br i1 %v"+this.regId+", label %v"+(this.regId+1)+", label %v"+ast.getNoId()+"\n";
                this.regId+=2;
                ans+="\nv"+(this.regId-1)+":\n";
            }
            int max=a.size()-1;
            generate(a.get(max));
            if(a.size()==1){
                ast.setType(a.get(max).getType());
            }
            ans+=tags()+"%v"+this.regId+" = icmp ne i32 0, "+a.get(max).getType()+"\n";
            ans+=tags()+"br i1 %v"+this.regId+", label %v"+ast.getYesId()+", label %v"+ast.getNoId()+"\n";
            this.regId+=1;

        }
    }

    public void LOrExp(AstNode ast){
        ArrayList<AstNode> a=ast.getChild();
        if(a.size()==1){
            generate(a.get(0));
            ast.setType(a.get(0).getType());
        }
        else{
            for(int i=0;i<a.size()-2;i+=2){
                generate(a.get(i));//LAndExp
                ans+=tags()+"%v"+this.regId+" = icmp ne i32 0, "+a.get(i).getType()+"\n";
                ans+=tags()+"br i1 %v"+this.regId+", label %v"+(this.regId+1)+", label %v"+ast.getNoId()+"\n";
                this.regId+=2;
                ans+="\nv"+(this.regId-1)+":\n";
            }
        }
    }

    public String Operator(String op){
        String opt="";
        switch(op){
            case "-":  opt="sub";break;
            case "*":  opt="mul";break;
            case "/":  opt="sdiv";break;
            case "%":  opt="srem";break;
            case "==": opt="eq";break;
            case "!=": opt="ne";break;
            case ">":  opt="sgt";break;
            case ">=": opt="sge";break;
            case "<":  opt="slt";break;
            case "<=": opt="sle";break;
            case "&&": opt="and";break;
            case "||": opt="or";break;
            case "+":  opt="add";break;
        }
        return opt;
    }
    public String mathCalculate(String left,String op,String right){
        int a=Integer.parseInt(left);
        int b=Integer.parseInt(right);
        int ans=0;
        switch(op){
            case "+":ans=a+b;break;
            case "-":ans=a-b;break;
            case "*":ans=a*b;break;
            case "/":ans=a/b;break;
            case "%":ans=a%b;break;
            case "==": ans=(a==b)?1:0;break;
            case "!=": ans=(a!=b)?1:0;break;
            case ">": ans=(a>b)?1:0;break;
            case ">=": ans=(a>=b)?1:0;break;
            case "<": ans=(a<b)?1:0;break;
            case "<=": ans=(a<=b)?1:0;break;
        }
        return ans+"";
    }
    public void print(BufferedWriter llvm_ir) throws IOException {
        llvm_ir.write(ans);
        llvm_ir.flush();
        llvm_ir.close();
    }

}

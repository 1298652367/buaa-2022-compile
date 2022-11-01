package data;

import java.util.ArrayList;

public class AstNode {
    private String value = "";
    ArrayList<AstNode> childs = new ArrayList<>();
    private String type = "";

    private String quality="";

    private int regID = 0;
    private int area = 0;

    boolean inStack=true;

    public void setRegID(int regID) {
        this.regID = regID;
    }

    public int getArea() {
        return area;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    @Override
    public String toString(){
        return this.type+" "+this.value;
    }
    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }


    AstNode fatherNode = null;

    public AstNode(String value){
        this.value = value;
        this.type=null;
    }
    public AstNode(String value,String type){
        this.value = value;
        this.type = type;
    }

    public void addChild(AstNode astNode){
        astNode.setFather(this);
        childs.add(astNode);
    }
    public void setFather(AstNode astNode){
        this.fatherNode = astNode;
    }

    public ArrayList<AstNode> getChilds() {
        return childs;
    }

    public AstNode getFatherNode() {
        return fatherNode;
    }

    public int getRegID() {
        return regID;
    }

    public boolean isInStack() {
        return inStack;
    }

    public void setInStack(boolean inStack) {
        this.inStack = inStack;
    }
}

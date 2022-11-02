package data;

import java.util.ArrayList;

public class AstNode {
    private String value = "";
    ArrayList<AstNode> childs = new ArrayList<>();
    private String type = "";

    private String quality="";
    KeyValue key = new KeyValue();

    public KeyValue getKey() {
        return key;
    }

    public void setKey(KeyValue key) {
        this.key = key;
    }

    private String regID = "";
    private int area = 0;

    boolean inStack=true;
    String returnType="";

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
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

    public String getRegID() {
        return regID;
    }

    public void setRegID(String regID) {
        this.regID = regID;
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

    public boolean isInStack() {
        return inStack;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public void setInStack(boolean inStack) {
        this.inStack = inStack;
    }
}

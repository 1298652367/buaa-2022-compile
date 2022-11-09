package llvm_ir.Values;

import llvm_ir.types.*;

import java.util.ArrayList;

public class Function extends Value{
    public ArrayList<BasicBlock> basicBlocks;
    public ArrayList<String> params;

    public Function(String name, Type type, Module parent) {
        super(name, type);
        basicBlocks = new ArrayList<>();
        parent.addFunction(this);
        params = new ArrayList<>();
        int size = ((FunctionType) (this.type)).params.size();
        for (int i = 0; i < size; i++) {
            params.add("%v" + i);
        }
    }

    public void addBasicBlock(BasicBlock basicBlock) {
        basicBlocks.add(basicBlock);
    }

    public void getOutputs(ArrayList<String> outputs) {
        StringBuilder sb = new StringBuilder();
        sb.append(type.string()).append(name).append("(");
        ArrayList<Type> params = ((FunctionType) (this.type)).params;
        for (int i = 0; i < params.size(); i++) {
            sb.append("i32 %v").append(i);
            if (i < params.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("){");
        outputs.add(sb.toString());
        for (BasicBlock basicBlock : basicBlocks) {
            basicBlock.getOutputs(outputs);
        }
        if (((FunctionType) (type)).retType instanceof VoidType) {
            outputs.add("ret void");
        }
        outputs.add("}");
    }
}

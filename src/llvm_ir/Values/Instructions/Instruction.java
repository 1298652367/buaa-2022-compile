package llvm_ir.Values.Instructions;

import llvm_ir.Values.BasicBlock;
import llvm_ir.Values.User;
import llvm_ir.types.Type;

import java.util.ArrayList;

public abstract class Instruction extends User {
    public BasicBlock parent;
    public TAG tag;

    public Instruction(String name, Type type, BasicBlock parent, int numOp, TAG tag) {
        super(name, type, numOp);
        this.parent = parent;
        parent.addInstruction(this);
        this.tag = tag;
    }

    public abstract void getOutputs(ArrayList<String> outputs);
}

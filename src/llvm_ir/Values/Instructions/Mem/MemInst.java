package llvm_ir.Values.Instructions.Mem;

import llvm_ir.Values.BasicBlock;
import llvm_ir.Values.Instructions.Instruction;
import llvm_ir.Values.Instructions.TAG;
import llvm_ir.types.Type;

import java.util.ArrayList;

public abstract class MemInst extends Instruction {

    public MemInst(Type type, BasicBlock parent, int numOp, TAG tag) {
        super("", type, parent, numOp, tag);
    }

    public abstract void getOutputs(ArrayList<String> outputs);
}

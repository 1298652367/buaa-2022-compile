package llvm_ir.Values.Instructions.Terminate;


import llvm_ir.Values.BasicBlock;
import llvm_ir.Values.Instructions.Instruction;
import llvm_ir.Values.Instructions.TAG;
import llvm_ir.types.Type;

import java.util.ArrayList;

public class TerminateInst extends Instruction {
    public TerminateInst(Type type, BasicBlock parent, int numOp, TAG tag) {
        super("", type, parent, numOp, tag);
    }


    public void getOutputs(ArrayList<String> outputs) {

    }
}

package llvm_ir.Values.Instructions.Mem;

import llvm_ir.Values.BasicBlock;
import llvm_ir.Values.Instructions.TAG;
import llvm_ir.Values.Value;
import llvm_ir.types.Type;

import java.util.ArrayList;

public class ZextInst extends MemInst {

    public ZextInst(Type type, BasicBlock parent, int numOp, Value value) {
        super(type, parent, numOp, TAG.zext);
        addOperand(value);
    }

    public void getOutputs(ArrayList<String> outputs){

    }

}

package llvm_ir.Values.Instructions.Mem;

import llvm_ir.Values.BasicBlock;
import llvm_ir.Values.Instructions.TAG;
import llvm_ir.types.Type;

import java.util.ArrayList;

public class AllocaInst extends MemInst {

    //这里name既是指令的名字，也代表着变量名
    public AllocaInst(String name, Type type, BasicBlock parent, int numOp) {
        super(type, parent, numOp, TAG.alloca);
        this.name = name;
    }

    public void getOutputs(ArrayList<String> outputs) {
        outputs.add(name + " = alloca i32");
    }
}

package llvm_ir.Values.Instructions.IO;


import llvm_ir.Values.BasicBlock;
import llvm_ir.Values.Instructions.Instruction;
import llvm_ir.Values.Instructions.TAG;
import llvm_ir.types.IntegerType;

import java.util.ArrayList;

public class Putint extends Instruction {
    public Putint(String name, BasicBlock parent) {
        super(name, IntegerType.i32, parent, 0, TAG.putint);
    }

    @Override
    public void getOutputs(ArrayList<String> outputs) {
        outputs.add("call void @putint(i32 " + name + ")");
    }
}

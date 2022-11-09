package llvm_ir.Values.Instructions.IO;

import llvm_ir.Values.BasicBlock;
import llvm_ir.Values.Instructions.Instruction;
import llvm_ir.Values.Instructions.TAG;
import llvm_ir.types.IntegerType;

import java.util.ArrayList;

public class Getint extends Instruction {
    public Getint(String name, BasicBlock parent) {
        super(name, IntegerType.i32, parent, 0, TAG.getint);
    }

    @Override
    public void getOutputs(ArrayList<String> outputs) {
        outputs.add(name + " = call i32 @getint()");
    }
}

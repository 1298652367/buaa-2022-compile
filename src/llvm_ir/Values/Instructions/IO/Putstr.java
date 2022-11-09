package llvm_ir.Values.Instructions.IO;

import llvm_ir.Values.BasicBlock;
import llvm_ir.Values.Instructions.Instruction;
import llvm_ir.Values.Instructions.TAG;
import llvm_ir.Values.Str;
import llvm_ir.types.IntegerType;

import java.util.ArrayList;

public class Putstr extends Instruction {

    private Str str;

    public Putstr(String name, Str str, BasicBlock parent) {
        super(name, IntegerType.i8, parent, 0, TAG.putstr);
        this.str = str;
    }

    @Override
    public void getOutputs(ArrayList<String> outputs) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" = getelementptr inbounds [").append(str.length).append
                (" x i8], [").append(str.length).append(" x i8]* ").append(str.name).append(", i32 0, i32 0");
        outputs.add(sb.toString());
        outputs.add("call void @putstr(i8* ".concat(name).concat(")"));
    }
}

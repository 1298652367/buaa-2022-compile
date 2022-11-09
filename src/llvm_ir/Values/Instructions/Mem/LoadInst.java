package llvm_ir.Values.Instructions.Mem;

import llvm_ir.Values.BasicBlock;
import llvm_ir.Values.Instructions.TAG;
import llvm_ir.Values.Value;
import llvm_ir.types.Type;

import java.util.ArrayList;

public class LoadInst extends MemInst {

    public LoadInst(String name, Type type, BasicBlock parent, int numOp, Value value) {
        super(type, parent, numOp, TAG.load);
        this.name = name;
        addOperand(value);
    }

    public void getOutputs(ArrayList<String> outputs) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" = load i32, i32* ").append(operands.get(0).getName());
        outputs.add(sb.toString());
    }

}

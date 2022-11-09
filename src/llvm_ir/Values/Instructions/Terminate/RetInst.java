package llvm_ir.Values.Instructions.Terminate;

import llvm_ir.Values.BasicBlock;
import llvm_ir.Values.Instructions.TAG;
import llvm_ir.Values.Value;
import llvm_ir.types.Type;
import llvm_ir.types.VoidType;

import java.util.ArrayList;

public class RetInst extends TerminateInst {

    public RetInst(Type type, BasicBlock parent, int numOp, Value value) {
        super(type, parent, numOp, TAG.ret);
        if (value != null) {
            addOperand(value);
        }
    }

    public void getOutputs(ArrayList<String> outputs) {
        if (type instanceof VoidType) {
            outputs.add("ret void");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("ret i32 ").append(operands.get(0).getName());
            outputs.add(sb.toString());
        }
    }

}

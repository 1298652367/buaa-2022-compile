package llvm_ir.Values.Instructions.Terminate;

import llvm_ir.Values.BasicBlock;
import llvm_ir.Values.Function;
import llvm_ir.Values.Instructions.TAG;
import llvm_ir.Values.Value;
import llvm_ir.types.FunctionType;
import llvm_ir.types.VoidType;

import java.util.ArrayList;

public class CallInst extends TerminateInst {

    //name是分配的虚拟寄存器
    public CallInst(String name, BasicBlock parent, ArrayList<Value> params, Function function) {
        super(function.type, parent, params.size() + 1, TAG.call);
        addOperand(function);
        this.name = name;
        for (Value param : params) {
            addOperand(param);
        }
    }

    public void getOutputs(ArrayList<String> outputs) {
        StringBuilder sb = new StringBuilder();
        Function function = ((Function) (operands.get(0)));
        if (((FunctionType) (function.type)).retType instanceof VoidType) {
            sb.append("call void @").append(function.name).append("(");
        } else {
            sb.append(name).append(" = call i32 @").append(function.name).append("(");
        }
        for (int i = 1; i < operands.size(); i++) {
            sb.append("i32 ").append(operands.get(i).name);
            if (i < operands.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        outputs.add(sb.toString());
    }

}

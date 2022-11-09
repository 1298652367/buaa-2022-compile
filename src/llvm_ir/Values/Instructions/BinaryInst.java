package llvm_ir.Values.Instructions;

import llvm_ir.Values.BasicBlock;
import llvm_ir.Values.Value;
import llvm_ir.types.Type;

import java.util.ArrayList;

public class BinaryInst extends Instruction {

    public BinaryInst(String name, Type type, BasicBlock parent, Value left, Value right, TAG tag) {
        super(name, type, parent, 2, tag);
        addOperand(left);
        addOperand(right);
    }

    @Override
    public void getOutputs(ArrayList<String> outputs) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" = ");
        switch (this.tag) {
            case add:
                sb.append("add i32 ");
                break;
            case sub:
                sb.append("sub i32 ");
                break;
            case mul:
                sb.append("mul i32 ");
                break;
            case sdiv:
                sb.append("sdiv i32 ");
                break;
            case mod:
                sb.append("srem i32 ");
                break;
            default:
                break;
        }
        sb.append(operands.get(0).name).append(",").append(operands.get(1).name);
        outputs.add(sb.toString());
    }

}

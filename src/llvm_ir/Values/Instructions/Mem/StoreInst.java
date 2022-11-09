package llvm_ir.Values.Instructions.Mem;

import llvm_ir.Values.BasicBlock;
import llvm_ir.Values.Constant.ConstantInt;
import llvm_ir.Values.Instructions.TAG;
import llvm_ir.Values.Value;
import llvm_ir.types.Type;

import java.util.ArrayList;

public class StoreInst extends MemInst {

    private Value left;
    private Value right;

    //name代表要赋值的区域，即原变量，而value代表新值所在区域
    public StoreInst(Type type, BasicBlock parent, int numOp, Value left, Value right) {
        super(type, parent, numOp, TAG.store);
        this.left = left;
        this.right = right;
    }

    public void getOutputs(ArrayList<String> outputs) {
        StringBuilder sb = new StringBuilder();
        sb.append("store i32 ");
        if (left instanceof ConstantInt) {
            sb.append(((ConstantInt) left).value);
        } else {
            sb.append((left.getName()));
        }
        sb.append(", i32* ").append(right.name);
        outputs.add(sb.toString());
    }

}
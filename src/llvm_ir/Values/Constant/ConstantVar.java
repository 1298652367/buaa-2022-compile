package llvm_ir.Values.Constant;

import llvm_ir.Values.User;
import llvm_ir.types.Type;

public class ConstantVar extends User {

    public int value;

    public ConstantVar(String name, Type type, int numOp, int value) {
        super(name, type, numOp);
        this.value = value;

    }
}

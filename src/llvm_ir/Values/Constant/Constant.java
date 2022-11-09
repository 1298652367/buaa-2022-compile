package llvm_ir.Values.Constant;

import llvm_ir.Values.User;
import llvm_ir.types.Type;

public class Constant extends User {

    public Constant(Type type, int numOp) {
        super("", type, numOp);
    }

}

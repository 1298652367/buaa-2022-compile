package llvm_ir.Values.Constant;

import llvm_ir.types.Type;

import java.util.ArrayList;

public class ConstantArray extends Constant {

    public ArrayList<Constant> constantArrays;

    public ConstantArray(Type type, int numOp) {
        super(type, numOp);
        constantArrays = new ArrayList<>();
    }
}

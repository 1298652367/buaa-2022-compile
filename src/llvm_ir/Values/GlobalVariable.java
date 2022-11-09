package llvm_ir.Values;

import llvm_ir.types.Type;

import java.util.ArrayList;

public class GlobalVariable extends User{
    private Module parent;
    private boolean isConst;
    private int value;

    public GlobalVariable(String name, Type type, boolean isConst, int value, Module parent) {
        super(name, type);
        this.isConst = isConst;
        this.value = value;
        this.parent = parent;
        parent.addGlobalVariable(this);
    }
    public void getOutputs(ArrayList<String> outputs) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" =");
        if (isConst) {
            sb.append(" constant i32 ");
        } else {
            sb.append(" global i32 ");
        }
        sb.append(value);
        outputs.add(sb.toString());
    }
}

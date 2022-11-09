package llvm_ir.types;

import java.util.ArrayList;

public class FunctionType extends Type{
    public Type retType;
    public ArrayList<Type> params;

    public FunctionType(Type retType, ArrayList<Type> params) {
        this.retType = retType;
        this.params = params;
    }

    public int getSize() {
        return 0;
    }

    @Override
    public boolean isFunctionType() {
        return true;
    }
    @Override
    public String string() {
        return "define dso_local " + retType;
    }

    @Override
    public Type getType() {
        return this;
    }
}

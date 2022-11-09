package llvm_ir.types;

public class PointerType extends Type{
    private Type pointType;

    public int getSize() {
        return 0;
    }

    public String string() {
        return "i32*";
    }

    @Override
    public Type getType() {
        return this;
    }
}

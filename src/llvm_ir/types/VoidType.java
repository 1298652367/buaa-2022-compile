package llvm_ir.types;

public class VoidType extends Type{
    public static final VoidType type = new VoidType();

    private VoidType() {
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public boolean isVoidType() {
        return true;
    }


    @Override
    public String string(){
        return "void";
    }
}

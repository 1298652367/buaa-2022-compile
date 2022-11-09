package llvm_ir.types;

public class LabelType extends Type{

    private static final LabelType labelType = new LabelType();
    public static Type labeltype;

    public LabelType() {

    }

    public String string(){
        return "label ";
    }

    @Override
    public Type getType() {
        return this;
    }

    public int getSize() {
        return 0;
    }
}

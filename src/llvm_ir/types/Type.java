package llvm_ir.types;

public abstract class Type {

    public boolean isArrayType() {
        return false;
    }

    public boolean isFunctionType() {
        return false;
    }

    public boolean isIntegerType() {
        return false;
    }

    public boolean isLabelType() {
        return false;
    }

    public boolean isPointerType() {
        return false;
    }

    public boolean isVoidType() {
        return false;
    }

    public abstract String string();

    public abstract Type getType();
}

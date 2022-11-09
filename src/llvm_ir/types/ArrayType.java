package llvm_ir.types;

import java.util.ArrayList;
import llvm_ir.Values.Value;


public class ArrayType extends Type{

    public int numOfElement;
    public Type typeOfElement;

    public ArrayType(int numOfElement, Type typeOfElement) {
        this.numOfElement = numOfElement;
        this.typeOfElement = typeOfElement;
    }

    public int getSize() {
        return 0;
    }

    public String string() {
        return null;
    }

    @Override
    public boolean isArrayType() {
        return true;
    }

    @Override
    public Type getType() {
        return this;
    }

}

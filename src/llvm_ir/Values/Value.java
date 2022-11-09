package llvm_ir.Values;

import llvm_ir.types.Type;

import java.util.ArrayList;

public class Value {
    public String name;
    public Type type;
    public ArrayList<User> users;
    public boolean isGlobal;

    public Value(String name, Type type) {
        this.name = name;
        this.type = type;
        users = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

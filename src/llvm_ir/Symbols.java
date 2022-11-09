package llvm_ir;

import llvm_ir.Values.Value;

import java.util.ArrayList;
import java.util.HashMap;

public class Symbols {
    private ArrayList<HashMap<String, Value>> tables;

    public Symbols() {
        tables = new ArrayList<>();
    }

    public HashMap<String, Value> top() {
        return tables.get(tables.size() - 1);
    }

    public Value find(String name) {
        for (int i = tables.size() - 1; i >= 0; i--) {
            Value t = tables.get(i).get(name);
            if (t != null) {
                return t;
            }
        }
        return null;
    }

    public void put(String name, Value v) {
        top().put(name, v);
    }

    public void addLayer() {
        tables.add(new HashMap<>());
    }

    public void popLayer() {
        tables.remove(tables.size() - 1);
    }

    public boolean isGlobal() {
        return tables.size() == 1;
    }
}

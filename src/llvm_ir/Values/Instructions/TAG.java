package llvm_ir.Values.Instructions;

public enum TAG {
    //terminate
    br, ret, call,
    //mem
    alloca, load, store, gep, phi, zext, memphi,
    //binary
    add, sub, mul, sdiv, mod, and, or, lt, le, ge, gt, eq, ne,
    getelementptr,
    //IO
    getint, putstr, putint,
}

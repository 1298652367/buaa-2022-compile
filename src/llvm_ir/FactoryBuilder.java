package llvm_ir;

import llvm_ir.Values.*;
import llvm_ir.Values.Constant.ConstantVar;
import llvm_ir.Values.Instructions.BinaryInst;
import llvm_ir.Values.Instructions.IO.Getint;
import llvm_ir.Values.Instructions.IO.Putint;
import llvm_ir.Values.Instructions.IO.Putstr;
import llvm_ir.Values.Instructions.Mem.AllocaInst;
import llvm_ir.Values.Instructions.Mem.LoadInst;
import llvm_ir.Values.Instructions.Mem.StoreInst;
import llvm_ir.Values.Instructions.Mem.ZextInst;
import llvm_ir.Values.Instructions.TAG;
import llvm_ir.Values.Instructions.Terminate.CallInst;
import llvm_ir.Values.Instructions.Terminate.RetInst;
import llvm_ir.Values.Module;
import llvm_ir.types.FunctionType;
import llvm_ir.types.IntegerType;
import llvm_ir.types.Type;


import java.util.ArrayList;

public class FactoryBuilder {

    public FactoryBuilder() {

    }

    public Function function(String name, Type retType, ArrayList<Type> funcParams, Module module) {
        return new Function(name, new FunctionType(retType, funcParams), module);
    }

    public ConstantVar constantVar(String name, int value) {
        return new ConstantVar(name, IntegerType.i32, 0, value);
    }

    public GlobalVariable globalVariable(String name, boolean isConst, int value, Module parent) {
        return new GlobalVariable(name, IntegerType.i32, isConst, value, parent);
    }

    //instructionBuilder
    public AllocaInst allocaInst(String name, BasicBlock parent, int numOp) {
        return new AllocaInst(name,IntegerType.i32, parent, numOp);
    }

    public StoreInst storeInst(BasicBlock parent, int numOp, Value left, Value right) {
        return new StoreInst(IntegerType.i32, parent, numOp, left, right);
    }

    public LoadInst loadInst(String name, BasicBlock parent, Value value) {
        return new LoadInst(name, IntegerType.i32, parent, 2, value);
    }

    public BinaryInst binaryInst(String name, BasicBlock parent, Value left, Value right, TAG tag) {
        return new BinaryInst(name, left.type, parent, left, right, tag);
    }

    public ZextInst zextInst(BasicBlock parent, Value value) {
        return new ZextInst(IntegerType.i32, parent, 1, value);
    }

    public CallInst callInst(String name, BasicBlock parent, ArrayList<Value> params, Function function) {
        return new CallInst(name, parent, params, function);
    }

    public RetInst retInst(Type type, BasicBlock parent, Value value) {
        return new RetInst(type, parent, 0, value);
    }

    public Putint putint(String name, BasicBlock parent) {
        return new Putint(name, parent);
    }

    public Putstr putstr(String name, Str str, BasicBlock parent) {
        return new Putstr(name, str, parent);
    }

    public Getint getint(String name, BasicBlock parent) {
        return new Getint(name, parent);
    }

    public Str str(String name, int length, String content, Module module) {
        return new Str(name, length, content, module);
    }

}

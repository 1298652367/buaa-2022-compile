package llvm_ir.Values;

import llvm_ir.Values.Instructions.Instruction;
import llvm_ir.types.LabelType;
import llvm_ir.types.Type;

import java.util.ArrayList;

public class BasicBlock extends Value{
    public Function parent;
    public ArrayList<Instruction> instructions;
    public ArrayList<BasicBlock> predecessors;
    public ArrayList<BasicBlock> successors;

    public BasicBlock(String name, Function parent) {
        super(name, LabelType.labeltype);
        instructions = new ArrayList<>();
        predecessors = new ArrayList<>();
        successors = new ArrayList<>();
        this.parent = parent;
        parent.addBasicBlock(this);
    }

    public void addInstruction(Instruction instruction) {
        instructions.add(instruction);
    }

    public void getOutputs(ArrayList<String> outputs) {
        for (Instruction instr : instructions) {
            instr.getOutputs(outputs);
        }
    }
}

package net.sothatsit.evaluate.tree;

import net.sothatsit.evaluate.tree.function.Function;

import java.util.Collections;
import java.util.List;

public class VariableNode implements Node {

    public final String name;
    public final int index;

    public VariableNode(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public boolean isConstant() {
        return false;
    }

    public List<Function> getAllUsedFunctions() {
        return Collections.emptyList();
    }

    public double evaluate(double... inputs) {
        return inputs[index];
    }

    public Node trySimplify() {
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}

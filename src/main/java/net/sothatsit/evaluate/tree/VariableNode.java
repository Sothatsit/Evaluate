package net.sothatsit.evaluate.tree;

public class VariableNode implements Node {

    public final String name;
    public final int index;

    public VariableNode(String name, int index) {
        this.name = name;
        this.index = index;
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public double evaluate(double[] inputs) {
        return inputs[index];
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VariableNode && ((VariableNode) obj).index == index;
    }

    @Override
    public String toString() {
        return name;
    }
}

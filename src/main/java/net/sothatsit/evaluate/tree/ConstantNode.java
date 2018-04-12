package net.sothatsit.evaluate.tree;

public class ConstantNode implements Node {

    public final String name;
    public final double value;

    public ConstantNode(double value) {
        this(null, value);
    }

    public ConstantNode(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public boolean isConstant() {
        return true;
    }

    public double evaluate(double... inputs) {
        return value;
    }

    public Node trySimplify() {
        return null;
    }

    @Override
    public String toString() {
        return (name != null ? name : Double.toString(value));
    }
}

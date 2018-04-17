package net.sothatsit.evaluate.tree;

public final class ConstantNode implements Node {

    public final String name;
    public final double value;

    public ConstantNode(double value) {
        this(null, value);
    }

    public ConstantNode(String name, double value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public double evaluate(double[] inputs) {
        return value;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ConstantNode && ((ConstantNode) obj).value == value;

    }

    @Override
    public String toString() {
        return (name != null ? name : Double.toString(value));
    }
}

package net.sothatsit.evaluate.tree.function;

public interface Function {

    public String getName();

    public int getArgumentCount();

    public boolean isOrderDependant();

    public double evaluate(double[] arguments);
}

package net.sothatsit.evaluate.tree.function;

public abstract class UnaryFunction implements Function {

    public int getArgumentCount() {
        return 1;
    }

    public double evaluate(double[] arguments) {
        return evaluate(arguments[0]);
    }

    public abstract double evaluate(double argument);
}

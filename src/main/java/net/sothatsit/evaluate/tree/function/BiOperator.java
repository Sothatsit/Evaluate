package net.sothatsit.evaluate.tree.function;

public abstract class BiOperator implements Operator {

    public int getArgumentCount() {
        return 2;
    }

    public double evaluate(double[] arguments) {
        return evaluate(arguments[0], arguments[1]);
    }

    public abstract double evaluate(double arg1, double arg2);
}

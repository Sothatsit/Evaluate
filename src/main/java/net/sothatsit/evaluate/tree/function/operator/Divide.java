package net.sothatsit.evaluate.tree.function.operator;

import net.sothatsit.evaluate.tree.function.BinaryFunction;

public class Divide extends BinaryFunction {

    public String getName() {
        return "divide";
    }

    public boolean isOrderDependant() {
        return true;
    }

    public double evaluate(double arg1, double arg2) {
        return arg1 / arg2;
    }
}

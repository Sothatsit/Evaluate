package net.sothatsit.evaluate.tree.function.operator;

import net.sothatsit.evaluate.tree.function.BinaryFunction;

public class Subtract extends BinaryFunction {

    public String getName() {
        return "subtract";
    }

    public boolean isOrderDependant() {
        return true;
    }

    public double evaluate(double arg1, double arg2) {
        return arg1 - arg2;
    }
}

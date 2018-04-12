package net.sothatsit.evaluate.tree.function.operator;

import net.sothatsit.evaluate.tree.function.BinaryFunction;

public class Power extends BinaryFunction {

    public String getName() {
        return "power";
    }

    public boolean isOrderDependant() {
        return true;
    }

    public double evaluate(double arg1, double arg2) {
        return Math.pow(arg1, arg2);
    }
}

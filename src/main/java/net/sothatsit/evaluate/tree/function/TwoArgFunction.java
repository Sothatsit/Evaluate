package net.sothatsit.evaluate.tree.function;

public abstract class TwoArgFunction extends AbstractFunction {

    public TwoArgFunction(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public final int getArgumentCount() {
        return 2;
    }

    @Override
    public final double evaluate(double[] arguments) {
        return evaluate(arguments[0], arguments[1]);
    }

    public abstract double evaluate(double arg1, double arg2);
}

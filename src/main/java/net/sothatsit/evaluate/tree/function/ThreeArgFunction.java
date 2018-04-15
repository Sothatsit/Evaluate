package net.sothatsit.evaluate.tree.function;

public abstract class ThreeArgFunction extends AbstractFunction {

    public ThreeArgFunction(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public final int getArgumentCount() {
        return 3;
    }

    @Override
    public final double evaluate(double[] arguments) {
        return evaluate(arguments[0], arguments[1], arguments[2]);
    }

    public abstract double evaluate(double arg1, double arg2, double arg3);
}

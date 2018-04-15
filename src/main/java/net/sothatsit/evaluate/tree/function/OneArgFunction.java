package net.sothatsit.evaluate.tree.function;

public abstract class OneArgFunction extends AbstractFunction {

    public OneArgFunction(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public final int getArgumentCount() {
        return 1;
    }

    @Override
    public final double evaluate(double[] arguments) {
        return evaluate(arguments[0]);
    }

    public abstract double evaluate(double arg);
}

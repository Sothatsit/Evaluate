package net.sothatsit.evaluate.tree.function;

import net.sothatsit.evaluate.optimiser.OptimiseOptions;

public interface Function {

    public String getName();

    public String[] getAliases();

    public int getArgumentCount();

    public OptimiseOptions getOptimiseOptions();

    public double evaluate(double[] arguments);
}

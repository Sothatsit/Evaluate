package net.sothatsit.evaluate.tree.function;

import net.sothatsit.evaluate.optimiser.OptimiseOptions;

public abstract class AbstractFunction implements Function {

    private final String name;
    private final String[] aliases;

    public AbstractFunction(String name, String... aliases) {
        this.name = name;
        this.aliases = aliases;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String[] getAliases() {
        return aliases;
    }

    @Override
    public OptimiseOptions getOptimiseOptions() {
        return OptimiseOptions.DEFAULT;
    }
}

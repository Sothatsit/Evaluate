package net.sothatsit.evaluate.tree.function;

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
    public boolean isOrderDependant() {
        return true;
    }

    @Override
    public boolean isPure() {
        return true;
    }
}

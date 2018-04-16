package net.sothatsit.evaluate.tree;

import net.sothatsit.evaluate.tree.function.Function;

public abstract class FunctionNode implements Node {

    @Override
    public int getHeight() {
        int max = 0;

        for(Node argument : getArguments()) {
            max = Math.max(max, argument.getHeight());
        }

        return max + 1;
    }

    public abstract Function getFunction();

    public abstract Node[] getArguments();
}

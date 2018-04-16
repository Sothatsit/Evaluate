package net.sothatsit.evaluate.optimiser;

import net.sothatsit.evaluate.tree.Expression;
import net.sothatsit.evaluate.tree.Node;

public abstract class Optimiser {

    private final CompositeOptimiser parent;

    public Optimiser() {
        this(null);
    }

    public Optimiser(CompositeOptimiser parent) {
        this.parent = parent;
    }

    public void optimise(Expression expression) {
        expression.root = optimise(expression.root);
    }

    public Node fullyOptimise(Node node) {
        return (parent != null ? parent : this).optimise(node);
    }

    public abstract Node optimise(Node node);
}

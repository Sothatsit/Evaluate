package net.sothatsit.evaluate.optimiser;

import net.sothatsit.evaluate.tree.Node;

import java.util.ArrayList;
import java.util.List;

public class CompositeOptimiser extends Optimiser {

    private final List<Optimiser> optimisers = new ArrayList<>();

    public CompositeOptimiser() {
        super();
    }

    public CompositeOptimiser(CompositeOptimiser parent) {
        super(parent);
    }

    public void add(Optimiser optimiser) {
        optimisers.add(optimiser);
    }

    public Node optimise(Node node) {
        for(Optimiser optimiser : optimisers) {
            node = optimiser.optimise(node);
        }

        return node;
    }

    public static CompositeOptimiser none() {
        return new CompositeOptimiser();
    }

    public static CompositeOptimiser all() {
        CompositeOptimiser optimiser = new CompositeOptimiser();

        optimiser.add(new SimpleOptimiser(optimiser));

        return optimiser;
    }
}

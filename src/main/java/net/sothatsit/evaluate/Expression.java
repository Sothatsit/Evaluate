package net.sothatsit.evaluate;

import net.sothatsit.evaluate.tree.Node;
import net.sothatsit.evaluate.tree.function.Function;

import java.util.ArrayList;
import java.util.List;

public class Expression {

    public final Node root;
    public final List<String> arguments;
    public final List<Function> functionsUsed;

    public Expression(Node root, List<String> arguments) {
        this.root = root;
        this.arguments = new ArrayList<>(arguments);
        this.functionsUsed = new ArrayList<>();
    }

    public int getArgumentCount() {
        return arguments.size();
    }

    public int getArgumentIndex(String name) {
        if(!arguments.contains(name))
            throw new IllegalArgumentException("Unknown variable " + name);

        return arguments.indexOf(name);
    }

    public double evaluate(double[] inputs) {
        return root.evaluate(inputs);
    }
}

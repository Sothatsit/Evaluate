package net.sothatsit.evaluate.tree;

import java.util.ArrayList;
import java.util.List;

public class Expression {

    public Node root;
    public final List<String> arguments;

    public Expression(Node root, List<String> arguments) {
        this.root = root;
        this.arguments = new ArrayList<>(arguments);
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

    @Override
    public String toString() {
        return root.toString();
    }
}

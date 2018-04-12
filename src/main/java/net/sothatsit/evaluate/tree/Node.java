package net.sothatsit.evaluate.tree;

import net.sothatsit.evaluate.tree.function.Function;

import java.util.List;

public interface Node {

    public boolean isConstant();

    public List<Function> getAllUsedFunctions();

    public double evaluate(double... inputs);

    /**
     * @return A new simplified Node if one exists, or else null
     */
    public Node trySimplify();

    public static Node simplifyOrItself(Node node) {
        Node simplified = node.trySimplify();

        return (simplified != null ? simplified : node);
    }
}

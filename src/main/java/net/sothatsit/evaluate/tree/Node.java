package net.sothatsit.evaluate.tree;

public interface Node {

    public boolean isConstant();

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

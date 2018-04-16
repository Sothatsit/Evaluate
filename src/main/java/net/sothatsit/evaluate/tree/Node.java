package net.sothatsit.evaluate.tree;

import java.util.Comparator;

public interface Node {

    /**
     * Get the maximum depth of the tree below this node.
     */
    public int getHeight();

    public double evaluate(double[] inputs);

    public static class NodeComparator implements Comparator<Node> {

        @Override
        public int compare(Node one, Node two) {
            return Integer.compare(two.getHeight(), one.getHeight());
        }
    }
}

package net.sothatsit.evaluate.tree;

import java.util.Comparator;

public interface Node {

    /**
     * Get the maximum depth of the tree below this node.
     */
    public int getHeight();

    public double evaluate(double[] inputs);

    public static class NodeComparator implements Comparator<Node> {

        private final int heightMultiplier;

        public NodeComparator() {
            this(true);
        }

        public NodeComparator(boolean byDescendingHeight) {
            this.heightMultiplier = (byDescendingHeight ? -1 : 1);
        }

        @Override
        public int compare(Node one, Node two) {
            if(one instanceof VariableNode && two instanceof VariableNode)
                return Integer.compare(((VariableNode) one).index, ((VariableNode) two).index);

            return heightMultiplier * Integer.compare(one.getHeight(), two.getHeight());
        }
    }
}

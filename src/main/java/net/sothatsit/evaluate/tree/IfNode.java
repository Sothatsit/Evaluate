package net.sothatsit.evaluate.tree;

public final class IfNode implements Node {

    public final Node condition;
    public final Node thenNode;
    public final Node elseNode;

    public IfNode(Node condition, Node thenNode, Node elseNode) {
        this.condition = condition;
        this.thenNode = thenNode;
        this.elseNode = elseNode;
    }

    @Override
    public int getHeight() {
        return 1 + Math.max(condition.getHeight(), Math.max(thenNode.getHeight(), elseNode.getHeight()));
    }

    @Override
    public double evaluate(double[] inputs) {
        if(condition.evaluate(inputs) != 0) {
            return thenNode.evaluate(inputs);
        } else {
            return elseNode.evaluate(inputs);
        }
    }

    @Override
    public int hashCode() {
        return 37 * condition.hashCode() + 359 * thenNode.hashCode() + 953 * elseNode.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof IfNode))
            return false;

        IfNode other = (IfNode) obj;

        return condition.equals(other.condition) && thenNode.equals(other.thenNode) && elseNode.equals(other.elseNode);
    }

    @Override
    public String toString() {
        return "if(" + condition + ", " + thenNode + ", " + elseNode + ")";
    }
}

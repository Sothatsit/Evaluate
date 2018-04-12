package net.sothatsit.evaluate.parser;

import net.sothatsit.evaluate.tree.Node;

public interface Token {

    public boolean isNode();
    public boolean isOperator();

    public Node getNode();
    public Operator getOperator();

    public static class NodeToken implements Token {

        public final Node node;

        public NodeToken(Node node) {
            this.node = node;
        }

        public boolean isNode()     { return true;  }
        public boolean isOperator() { return false; }

        public Node getNode() {
            return node;
        }

        public Operator getOperator() {
            throw new UnsupportedOperationException("This is not an operator token");
        }
    }

    public static class OperatorToken implements Token {

        public final Operator operator;

        public OperatorToken(Operator operator) {
            this.operator = operator;
        }

        public boolean isNode()     { return false;  }
        public boolean isOperator() { return true; }

        public Node getNode() {
            throw new UnsupportedOperationException("This is not a node token");
        }

        public Operator getOperator() {
            return operator;
        }
    }

}

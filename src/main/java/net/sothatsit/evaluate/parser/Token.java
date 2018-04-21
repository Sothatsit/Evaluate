package net.sothatsit.evaluate.parser;

import net.sothatsit.evaluate.tree.*;
import net.sothatsit.evaluate.tree.function.Function;

public class Token {

    private final Node node;
    private final Operator operator;

    public final int startIndex;
    public final int endIndex;

    public Token(Node node, int startIndex, int endIndex) {
        this.node = node;
        this.operator = null;

        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public Token(Operator operator, int startIndex, int endIndex) {
        this.node = null;
        this.operator = operator;

        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public ParseException error(StringStream stream, String reason) {
        return stream.error(reason, startIndex, endIndex);
    }

    public boolean isNode() {
        return node != null;
    }

    public boolean isOperator() {
        return operator != null;
    }

    public Node getNode() {
        if(node == null)
            throw new UnsupportedOperationException("This token does not contain a node");

        return node;
    }

    public Operator getOperator() {
        if(operator == null)
            throw new UnsupportedOperationException("This token does not contain an operator");

        return operator;
    }

    public static Token node(Node node, int startIndex, int endIndex) {
        return new Token(node, startIndex, endIndex);
    }

    public static Token ifStatement(Node condition, Node thenNode, Node elseNode, int startIndex, int endIndex) {
        return new Token(new IfNode(condition, thenNode, elseNode), startIndex, endIndex);
    }

    public static Token function(Function function, Node[] arguments, int startIndex, int endIndex) {
        return new Token(new FunctionNode(function, arguments), startIndex, endIndex);
    }

    public static Token operator(Operator operator, int startIndex, int endIndex) {
        return new Token(operator, startIndex, endIndex);
    }

    public static Token variable(String name, int varIndex, int startIndex, int endIndex) {
        return new Token(new VariableNode(name, varIndex), startIndex, endIndex);
    }

    public static Token constant(double value, int startIndex, int endIndex) {
        return new Token(new ConstantNode(value), startIndex, endIndex);
    }

    public static Token constant(String name, double value, int startIndex, int endIndex) {
        return new Token(new ConstantNode(name, value), startIndex, endIndex);
    }
}

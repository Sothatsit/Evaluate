package net.sothatsit.evaluate.parser;

import net.sothatsit.evaluate.tree.Node;
import net.sothatsit.evaluate.tree.function.Function;
import net.sothatsit.evaluate.tree.function.operator.*;

public interface Operator {

    public static Operator[] all() {
        return new Operator[] {
                add, subtract, multiply, divide, power
        };
    }

    public static final Operator add      = new TwoOperator("add",      "+", 0,  Add.fn);
    public static final Operator subtract = new TwoOperator("subtract", "-", 0,  Subtract.fn);
    public static final Operator multiply = new TwoOperator("multiply", "*", 5,  Multiply.fn);
    public static final Operator divide   = new TwoOperator("divide",   "/", 5,  Divide.fn);
    public static final Operator power    = new TwoOperator("power",    "^", 10, Power.fn);

    public String getName();

    public String getOperatorString();

    public int getPrecedence();

    public boolean requiresLeft();

    public boolean requiresRight();

    public int getArgumentCount();

    public Token getToken(Token operatorToken, Token[] arguments);

    public static abstract class AbstractOperator implements Operator {

        private final String name;
        private final String operatorString;
        private final int precedence;
        private final Function function;

        public AbstractOperator(String name, String operatorString, int precedence, Function function) {
            this.name = name;
            this.operatorString = operatorString;
            this.precedence = precedence;
            this.function = function;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOperatorString() {
            return operatorString;
        }

        @Override
        public int getPrecedence() {
            return precedence;
        }

        @Override
        public int getArgumentCount() {
            return (requiresLeft() ? 1 : 0) + (requiresRight() ? 1 : 0);
        }

        @Override
        public Token getToken(Token operatorToken, Token[] arguments) {
            int minStart = operatorToken.startIndex;
            int maxEnd = operatorToken.endIndex;

            Node[] nodes = new Node[arguments.length];

            for(int index = 0; index < arguments.length; ++index) {
                Token token = arguments[index];

                minStart = Math.min(minStart, token.startIndex);
                maxEnd = Math.max(maxEnd, token.endIndex);

                nodes[index] = token.getNode();
            }

            return Token.function(function, nodes, minStart, maxEnd);
        }
    }

    public static class LeftOperator extends AbstractOperator {

        public LeftOperator(String name, String operatorString, int precedence, Function function) {
            super(name, operatorString, precedence, function);
        }

        @Override
        public boolean requiresLeft() {
            return true;
        }

        @Override
        public boolean requiresRight() {
            return false;
        }
    }

    public static class RightOperator extends AbstractOperator {

        public RightOperator(String name, String operatorString, int precedence, Function function) {
            super(name, operatorString, precedence, function);
        }

        @Override
        public boolean requiresLeft() {
            return false;
        }

        @Override
        public boolean requiresRight() {
            return true;
        }
    }

    public static class TwoOperator extends AbstractOperator {

        public TwoOperator(String name, String operatorString, int precedence, Function function) {
            super(name, operatorString, precedence, function);
        }

        @Override
        public boolean requiresLeft() {
            return true;
        }

        @Override
        public boolean requiresRight() {
            return true;
        }
    }
}

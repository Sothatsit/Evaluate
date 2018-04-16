package net.sothatsit.evaluate.parser;

import net.sothatsit.evaluate.tree.FunctionNode;
import net.sothatsit.evaluate.tree.Node;
import net.sothatsit.evaluate.tree.function.Function;
import net.sothatsit.evaluate.tree.function.operator.*;

public enum BaseOperator {

    ADD      ('+', 0, Add.fn),
    SUBTRACT ('-', 0, Subtract.fn),
    MULTIPLY ('*', 1, Multiply.fn),
    DIVIDE   ('/', 1, Divide.fn),
    POWER    ('^', 2, Power.fn);

    public final char character;
    public final int precedence;
    public final Function function;

    private BaseOperator(char character, int precedence, Function function) {
        this.character = character;
        this.precedence = precedence;
        this.function = function;
    }

    public static BaseOperator find(char character) {
        for(BaseOperator operator : values()) {
            if(operator.character == character)
                return operator;
        }
        return null;
    }

    public static BaseOperator find(Function function) {
        for(BaseOperator operator : values()) {
            if(operator.function == function)
                return operator;
        }
        return null;
    }

    public static int getMaxPrecedence() {
        int max = -1;
        for(BaseOperator operator : values()) {
            max = Math.max(max, operator.precedence);
        }
        return max;
    }

    public static int getPrecedence(Function function) {
        BaseOperator operator = find(function);

        return (operator == null ? getMaxPrecedence() : operator.precedence);
    }

    private static int getPrecedence(Node node) {
        if(!(node instanceof FunctionNode))
            return getMaxPrecedence();

        return getPrecedence(((FunctionNode) node).getFunction());
    }

    public static String toString(FunctionNode node) {
        BaseOperator operator = find(node.getFunction());

        if(operator == null)
            throw new IllegalArgumentException(node + " is not a base operator");


        Node[] arguments = node.getArguments();
        StringBuilder builder = new StringBuilder();
        {
            int precedence = operator.precedence;
            boolean orderDependant = operator.function.getOptimiseOptions().isOrderDependant;
            String separator = " " + operator.character + " ";

            for(int index = 0; index < arguments.length; ++index) {
                Node argument = arguments[index];
                boolean brackets =
                        (orderDependant && index != 0 && argument instanceof FunctionNode)
                        || getPrecedence(argument) < precedence;

                if(index != 0) builder.append(separator);
                if(brackets) builder.append("(");

                builder.append(arguments[index]);

                if(brackets) builder.append(")");
            }
        }
        return builder.toString();
    }
}

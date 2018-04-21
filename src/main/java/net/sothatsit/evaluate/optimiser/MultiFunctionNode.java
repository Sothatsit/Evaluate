package net.sothatsit.evaluate.optimiser;

import net.sothatsit.evaluate.tree.AbstractFunctionNode;
import net.sothatsit.evaluate.tree.Node;
import net.sothatsit.evaluate.tree.FunctionNode;
import net.sothatsit.evaluate.tree.function.Function;
import net.sothatsit.evaluate.tree.function.TwoArgFunction;

import java.util.Arrays;
import java.util.HashSet;

/**
 * An intermediary node for use in optimisation.
 */
public class MultiFunctionNode extends AbstractFunctionNode {

    public final TwoArgFunction function;
    public Node[] arguments;

    public MultiFunctionNode(TwoArgFunction function, Node... arguments) {
        if(arguments.length < function.getArgumentCount()) {
            throw new IllegalArgumentException("Function " + function.getName() +
                    " requires at least " + function.getArgumentCount() + " arguments," +
                    " whereas " + arguments.length + " were given");
        }

        OptimiseOptions options = function.getOptimiseOptions();

        if(!options.isPure)
            throw new IllegalArgumentException("MultiFunctionNode can only be used for pure functions");

        if(options.isOrderDependant) {
            throw new IllegalArgumentException("MultiFunctionNode can only be used for " +
                                               "functions that are not order-dependant");
        }

        this.function = function;
        this.arguments = arguments;
    }

    @Override
    public Function getFunction() {
        return function;
    }

    @Override
    public Node[] getArguments() {
        return arguments;
    }

    @Override
    public double evaluate(double[] inputs) {
        double value = arguments[0].evaluate(inputs);

        for(int index = 1; index < arguments.length; ++index) {
            value = function.evaluate(value, arguments[index].evaluate(inputs));
        }

        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof FunctionNode))
            return false;

        FunctionNode other = (FunctionNode) obj;

        if(function != other.function || arguments.length != other.arguments.length)
            return false;

        HashSet<Node> thisArgs = new HashSet<>(Arrays.asList(arguments));
        HashSet<Node> otherArgs = new HashSet<>(Arrays.asList(other.arguments));

        return thisArgs.equals(otherArgs);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(function.getName());
        builder.append("(");

        for(int index = 0; index < arguments.length; ++index) {
            if(index != 0) {
                builder.append(", ");
            }

            builder.append(arguments[index]);
        }

        builder.append(")");

        return builder.toString();
    }
}

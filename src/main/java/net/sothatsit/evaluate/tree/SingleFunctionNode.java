package net.sothatsit.evaluate.tree;

import net.sothatsit.evaluate.parser.BaseOperator;
import net.sothatsit.evaluate.tree.function.Function;

public final class SingleFunctionNode extends FunctionNode {

    public final Function function;
    public final Node[] arguments;

    public SingleFunctionNode(Function function, Node... arguments) {
        if(arguments.length != function.getArgumentCount()) {
            throw new IllegalArgumentException("Function " + function.getName() +
                    " requires " + function.getArgumentCount() + " arguments," +
                    " whereas " + arguments.length + " were given");
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
        double[] argumentValues = new double[arguments.length];

        for(int index = 0; index < arguments.length; ++index) {
            argumentValues[index] = arguments[index].evaluate(inputs);
        }

        return function.evaluate(argumentValues);
    }

    @Override
    public int hashCode() {
        int hashCode = 29;

        hashCode *= 37;
        hashCode += function.getName().hashCode();

        for(Node argument : arguments) {
            hashCode *= 37;
            hashCode += argument.hashCode();
        }

        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof SingleFunctionNode))
            return false;

        SingleFunctionNode other = (SingleFunctionNode) obj;

        if(function != other.function || arguments.length != other.arguments.length)
            return false;

        for(int index = 0; index < arguments.length; ++index) {
            if(!arguments[index].equals(other.arguments[index]))
                return false;
        }

        return true;
    }

    @Override
    public String toString() {
        if(BaseOperator.find(function) != null)
            return BaseOperator.toString(this);

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

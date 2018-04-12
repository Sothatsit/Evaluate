package net.sothatsit.evaluate.tree;

import net.sothatsit.evaluate.tree.function.Function;
import net.sothatsit.evaluate.tree.function.operator.*;

import java.util.ArrayList;
import java.util.List;

public class FunctionNode implements Node {

    public final Function function;
    public final Node[] arguments;

    public FunctionNode(Function function, Node... arguments) {
        if(arguments.length != function.getArgumentCount())
            throw new IllegalArgumentException("Function " + function.getName() +
                                               " requires " + function.getArgumentCount() + " arguments," +
                                               " whereas " + arguments.length + " were given");

        this.function = function;
        this.arguments = arguments;
    }

    public boolean isConstant() {
        for(Node argument : arguments) {
            if(!argument.isConstant())
                return false;
        }

        return true;
    }

    public List<Function> getAllUsedFunctions() {
        List<Function> functions = new ArrayList<>();

        functions.add(function);

        for(Node argument : arguments) {
            for(Function function : argument.getAllUsedFunctions()) {
                if(functions.contains(function))
                    continue;

                functions.add(function);
            }
        }

        return functions;
    }

    public double evaluate(double... inputs) {
        double[] argumentValues = new double[arguments.length];

        for(int index = 0; index < arguments.length; ++index) {
            argumentValues[index] = arguments[index].evaluate(inputs);
        }

        return function.evaluate(argumentValues);
    }

    private boolean isSameFunction(Node node) {
        return node instanceof FunctionNode && ((FunctionNode) node).function.getClass().equals(function.getClass());
    }

    private boolean reorderArguments() {
        if(function.isOrderDependant())
            return false;

        boolean modified = false;

        { // Move constants to the front
            int start = 0;
            int end = arguments.length - 1;

            while(true) {
                while(start < end && arguments[start].isConstant()) {
                    start += 1;
                }

                if(start == end)
                    break;

                while(start < end && !arguments[end].isConstant()) {
                    end -= 1;
                }

                if(start == end)
                    break;

                Node temp = arguments[start];
                arguments[start] = arguments[end];
                arguments[end] = temp;

                modified = true;
            }
        }

        { // Move arguments of the same function to the back
            int start = 0;
            int end = arguments.length - 1;

            while(true) {
                while(start < end && !isSameFunction(arguments[start])) {
                    start += 1;
                }

                if(start == end)
                    break;

                while(start < end && isSameFunction(arguments[end])) {
                    end -= 1;
                }

                if(start == end)
                    break;

                Node temp = arguments[start];
                arguments[start] = arguments[end];
                arguments[end] = temp;

                modified = true;
            }
        }

        // (6.0 + e) + ((3.0 * (e ^ 140.0)) + c)
        // c + ((3.0 * (e ^ 140.0)) + (6.0 + e))

        // TODO: Make this work for more than one argument
        if(arguments.length == 2 && isSameFunction(arguments[0]) && isSameFunction(arguments[1])) {
            FunctionNode node1 = (FunctionNode) arguments[0];
            FunctionNode node2 = (FunctionNode) arguments[1];

            arguments[0] = node2.arguments[1];
            node2.arguments[1] = node1;
        }

        return modified;
    }

    public Node trySimplify() {
        if(isConstant())
            return new ConstantNode(evaluate());

        if(function instanceof Subtract && arguments[1].isConstant()) {
            Node constant = new ConstantNode(-arguments[1].evaluate());
            Node node     = new FunctionNode(new Add(), arguments[0], constant);

            return Node.simplifyOrItself(node);
        }

        if(function instanceof Divide && arguments[1].isConstant()) {
            Node constant = new ConstantNode(1.0 / arguments[1].evaluate());
            Node node     = new FunctionNode(new Add(), arguments[0], constant);

            return Node.simplifyOrItself(node);
        }

        boolean simplified = reorderArguments();

        for(int index = 0; index < arguments.length; ++index) {
            Node argument = arguments[index];
            Node simple = argument.trySimplify();

            if(simple != null) {
                arguments[index] = simple;
                simplified = true;
                argument = simple;
            }

            // TODO: Make it work for functions of more than two parameters
            if(function.isOrderDependant() || !isSameFunction(argument) || arguments.length != 2)
                continue;

            Node[] functionArguments = ((FunctionNode) argument).arguments;

            if(!functionArguments[0].isConstant())
                continue;

            if(arguments[0].isConstant()) {
                double value = function.evaluate(new double[] {
                        functionArguments[0].evaluate(), arguments[0].evaluate()
                });

                arguments[0] = new ConstantNode(value);
                arguments[1] = functionArguments[1];
                simplified = true;
            } else if(index != 0) {
                Node temp = arguments[0];
                arguments[0] = functionArguments[0];
                functionArguments[0] = temp;
            }
        }

        if(simplified) {
            reorderArguments();
        }

        if(function instanceof Add && arguments[0].isConstant() && arguments[0].evaluate() == 0.0)
            return Node.simplifyOrItself(arguments[1]);

        if(function instanceof Multiply && arguments[0].isConstant() && arguments[0].evaluate() == 1.0)
            return Node.simplifyOrItself(arguments[1]);

        return (simplified ? this : null);
    }

    @Override
    public String toString() {
        if(function instanceof Add)      return "(" + arguments[0].toString() + " + " + arguments[1].toString() + ")";
        if(function instanceof Subtract) return "(" + arguments[0].toString() + " - " + arguments[1].toString() + ")";
        if(function instanceof Multiply) return "(" + arguments[0].toString() + " * " + arguments[1].toString() + ")";
        if(function instanceof Divide)   return "(" + arguments[0].toString() + " / " + arguments[1].toString() + ")";
        if(function instanceof Power)    return "(" + arguments[0].toString() + " ^ " + arguments[1].toString() + ")";

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

package net.sothatsit.evaluate.optimiser;

import net.sothatsit.evaluate.tree.*;
import net.sothatsit.evaluate.tree.function.Function;
import net.sothatsit.evaluate.tree.function.TwoArgFunction;
import net.sothatsit.evaluate.tree.function.operator.*;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class SimpleOptimiser extends Optimiser {

    public SimpleOptimiser() {
        super();
    }

    public SimpleOptimiser(CompositeOptimiser parent) {
        super(parent);
    }

    public Node optimise(Node node) {
        node = transformSubtractions(node);
        node = transformDivides(node);
        node = placeMultiFunctions(node);
        node = collapseConstants(node);
        node = collectDivides(node);
        node = reorderArguments(node);
        node = removeNoOps(node);
        node = removeMultiFunctions(node);

        return node;
    }

    /**
     * Remove multi-functions and replaces them with streaks of single functions.
     */
    protected Node removeMultiFunctions(Node node) {
        if(!(node instanceof FunctionNode))
            return node;

        FunctionNode functionNode = ((FunctionNode) node);
        Function function = functionNode.getFunction();
        Node[] argumentArray = functionNode.getArguments();

        {// Call removeMultiFunctions for all children of this node
            for(int index = 0; index < argumentArray.length; ++index) {
                argumentArray[index] = removeMultiFunctions(argumentArray[index]);
            }
        }

        if(!(node instanceof MultiFunctionNode))
            return node;

        List<Node> arguments = new ArrayList<>();

        while(argumentArray.length > 1) {
            arguments.clear();

            for(int index = 0; index < argumentArray.length - 1; index += 2) {
                Node left = argumentArray[index];
                Node right = argumentArray[index + 1];

                arguments.add(new SingleFunctionNode(function, left, right));
            }

            if((argumentArray.length & 1) == 1) {
                arguments.add(argumentArray[argumentArray.length - 1]);
            }

            argumentArray = arguments.toArray(new Node[arguments.size()]);
        }

        return arguments.get(0);
    }

    /**
     * Remove operations which have no affect.
     *
     * e.g. (a + 0) -> a
     *      (a * 1) -> a
     *      (a / 1) -> a
     *      (a ^ 1) -> a
     */
    protected Node removeNoOps(Node node) {
        if(!(node instanceof FunctionNode))
            return node;

        FunctionNode functionNode = ((FunctionNode) node);
        Function function = functionNode.getFunction();
        Node[] arguments = functionNode.getArguments();

        {// Call removeNoOps for all children of this node
            for(int index = 0; index < arguments.length; ++index) {
                arguments[index] = removeNoOps(arguments[index]);
            }
        }

        if(function instanceof Add) {
            Node lastArgument = arguments[arguments.length - 1];

            if(!isConstantOfValue(lastArgument, 0.0))
                return node;

            if(arguments.length == 2)
                return arguments[0];

            return new MultiFunctionNode(Add.fn, Arrays.copyOfRange(arguments, 0, arguments.length - 1));
        }

        if(function instanceof Multiply) {
            Node lastArgument = arguments[arguments.length - 1];

            if(!isConstantOfValue(lastArgument, 1.0))
                return node;

            if(arguments.length == 2)
                return arguments[0];

            return new MultiFunctionNode(Multiply.fn, Arrays.copyOfRange(arguments, 0, arguments.length - 1));
        }

        if(function instanceof Divide) {
            if(!isConstantOfValue(arguments[1], 1.0))
                return node;

            return arguments[1];
        }

        if(function instanceof Power) {
            if(!isConstantOfValue(arguments[1], 1.0))
                return node;

            return arguments[1];
        }

        return node;
    }

    /**
     * Re-orders the arguments of order-independent functions within the equation stored
     * in {@param node} such that more complicated sub-expressions are evaluated first.
     *
     * This helps to reduce the maximum stack size required for compiled functions.
     *
     * e.g. (a * (b * b)) -> ((b * b) * a)
     */
    protected Node reorderArguments(Node node) {
        if(!(node instanceof FunctionNode))
            return node;

        FunctionNode functionNode = ((FunctionNode) node);
        Function function = functionNode.getFunction();
        Node[] arguments = functionNode.getArguments();

        if(!function.getOptimiseOptions().isOrderDependant) {
            Collections.sort(Arrays.asList(arguments), new Node.NodeComparator());
        }

        for(Node argument : arguments) {
            reorderArguments(argument);
        }

        return node;
    }

    /**
     * Finds constant parts of the equation stored in {@param node} and collapses them into constant nodes.
     *
     * e.g. sin(acos(-1.0) / 2) -> 1.0
     *      (2 * a * b * 3) -> (a * b * 6)
     */
    protected Node collapseConstants(Node node) {
        if(node instanceof ConstantNode)
            return node;

        if(isConstant(node))
            return new ConstantNode(node.evaluate(new double[0]));

        if(node instanceof SingleFunctionNode) {
            Node[] arguments = ((SingleFunctionNode) node).arguments;

            for(int index = 0; index < arguments.length; ++index) {
                arguments[index] = collapseConstants(arguments[index]);
            }

            return node;
        }

        if(!(node instanceof MultiFunctionNode))
            return node;

        TwoArgFunction function = ((MultiFunctionNode) node).function;
        List<ConstantNode> constants = new ArrayList<>();
        List<Node> nonConstants = new ArrayList<>();

        for(Node argument : ((MultiFunctionNode) node).arguments) {
            argument = collapseConstants(argument);

            if(argument instanceof ConstantNode) {
                constants.add((ConstantNode) argument);
            } else {
                nonConstants.add(argument);
            }
        }

        if(constants.size() > 1) {
            double value = constants.get(0).value;

            for(int index = 1; index < constants.size(); ++index) {
                value = function.evaluate(value, constants.get(index).value);
            }

            nonConstants.add(new ConstantNode(value));
        } else {
            nonConstants.addAll(constants);
        }

        ((MultiFunctionNode) node).arguments = nonConstants.toArray(new Node[nonConstants.size()]);

        return node;
    }

    /**
     * Whether when evaluated {@param node} will always output the same value.
     */
    protected boolean isConstant(Node node) {
        if(node instanceof ConstantNode)
            return true;

        if(node instanceof VariableNode)
            return false;

        Node[] arguments;

        if(node instanceof SingleFunctionNode) {
            if(!((SingleFunctionNode) node).function.getOptimiseOptions().isPure)
                return false;

            arguments = ((SingleFunctionNode) node).arguments;
        } else if(node instanceof MultiFunctionNode) {
            arguments = ((MultiFunctionNode) node).arguments;
        } else {
            throw new IllegalArgumentException("Unknown type of Node " + node + " (" + node.getClass() + ")");
        }

        for(Node argument : arguments) {
            if(!isConstant(argument))
                return false;
        }

        return true;
    }

    /**
     * Finds and replaces streaks of order-independant functions with MultiFunctionNode's.
     *
     * e.g. ((a + b) + (c + d)) -> (a + b + c + d)
     *      ((a * b) * (c * d)) -> (a * b * c * d)
     */
    protected Node placeMultiFunctions(Node node) {
        if(!(node instanceof FunctionNode))
            return node;

        FunctionNode functionNode = (FunctionNode) node;
        Function function = functionNode.getFunction();

        if(!(function instanceof TwoArgFunction) || function.getOptimiseOptions().isOrderDependant) {
            Node[] arguments = functionNode.getArguments();
            for(int index = 0; index < arguments.length; ++index) {
                arguments[index] = placeMultiFunctions(arguments[index]);
            }
            return node;
        }

        List<Node> arguments = new ArrayList<>();
        Queue<Node> nodeQueue = new LinkedBlockingQueue<>();

        nodeQueue.add(node);

        while(!nodeQueue.isEmpty()) {
            Node argument = nodeQueue.poll();

            if(!isFunction(argument, function)) {
                arguments.add(argument);
                continue;
            }

            Collections.addAll(nodeQueue, ((FunctionNode) argument).getArguments());
        }

        for(int index = 0; index < arguments.size(); ++index) {
            arguments.set(index, placeMultiFunctions(arguments.get(index)));
        }

        Node[] argumentArray = arguments.toArray(new Node[arguments.size()]);
        return new MultiFunctionNode((TwoArgFunction) function, argumentArray);
    }

    /**
     * Finds divides within multiply functions and places them as a divide of the multiplication
     *
     * e.g. ((1.0 / a) * b) -> (b / a)
     *      ((1.0 / a) * (1.0 / b)) -> (1.0 / (a * b))
     *      (a * (1.0 / b) * c * (1.0 / d)) -> ((a * c) / (b * d))
     */
    protected Node collectDivides(Node node) {
        if(!(node instanceof FunctionNode))
            return node;

        { // Recursively run this for every child of this node.
            Node[] arguments = ((FunctionNode) node).getArguments();

            for(int index = 0; index < arguments.length; ++index) {
                arguments[index] = collectDivides(arguments[index]);
            }
        }

        if(!(node instanceof MultiFunctionNode))
            return node;

        MultiFunctionNode functionNode = (MultiFunctionNode) node;

        List<Node> numerator = new ArrayList<>();
        List<Node> denominator = new ArrayList<>(); {
            for(Node argument : functionNode.getArguments()) {
                if(isFunction(argument, Divide.fn)) {
                    Node[] arguments = ((FunctionNode) argument).getArguments();

                    if(isConstantOfValue(arguments[0], 1.0)) {
                        denominator.add(arguments[1]);
                        continue;
                    }
                }

                numerator.add(argument);
            }
        }

        if(denominator.size() == 0)
            return node;

        Node denominatorNode;

        if(denominator.size() == 1) {
            denominatorNode = denominator.get(0);
        } else {
            Node[] nodes = denominator.toArray(new Node[denominator.size()]);
            denominatorNode = new MultiFunctionNode(Multiply.fn, nodes);

            // The construction of the denominator is not clear at this
            // point and therefore optimising it as its own unit is best.
            denominatorNode = fullyOptimise(denominatorNode);
        }

        Node numeratorNode;

        if(numerator.size() == 0) {
            numeratorNode = new ConstantNode(1.0);
        } else if(numerator.size() == 1) {
            numeratorNode = numerator.get(0);
        } else {
            Node[] nodes = numerator.toArray(new Node[numerator.size()]);
            numeratorNode = new MultiFunctionNode(Multiply.fn, nodes);
        }

        return new SingleFunctionNode(Divide.fn, numeratorNode, denominatorNode);
    }

    /**
     * Transforms divides into one divided by the denominator times the numerator.
     *
     * This simplifies the optimisation of chains of multiplications and divisions.
     *
     * This must be cleaned up later using {@link #collectDivides(Node)}.
     *
     * e.g. (a / b) -> (b * (1 / a))
     *      (a / (b / c)) -> ((1 / ((1 / c) * b)) * a)
     */
    protected Node transformDivides(Node node) {
        if(!(node instanceof FunctionNode))
            return node;

        FunctionNode functionNode = (FunctionNode) node;
        Function function = functionNode.getFunction();
        Node[] arguments = functionNode.getArguments();

        if(!(function instanceof Divide)) {
            for(int index = 0; index < arguments.length; ++index) {
                arguments[index] = transformDivides(arguments[index]);
            }
            return node;
        }

        arguments[0] = transformDivides(arguments[0]);
        arguments[1] = transformDivides(arguments[1]);

        /**
         * (a / (b / c))
         *   -> (c * (a / b))
         *   -> (c * ((1.0 / b) * a))
         */
        if(isFunction(arguments[1], Divide.fn)) {
            FunctionNode divide = (FunctionNode) arguments[1];

            SingleFunctionNode multiply = new SingleFunctionNode(Multiply.fn, divide.getArguments()[1], node);
            arguments[1] = divide.getArguments()[0];

            return transformDivides(multiply);
        }

        /**
         * (1 / a) should remain as (1 / a).
         */
        if(isConstantOfValue(arguments[0], 1.0))
            return node;

        SingleFunctionNode multiply = new SingleFunctionNode(Multiply.fn, node, arguments[0]);
        arguments[0] = new ConstantNode(1.0);

        return multiply;
    }

    /**
     * Transform subtractions of a constant into additions of a negative constant.
     *
     * This simplifies the optimisation of chains of additions and subtractions.
     *
     * e.g. (a - 2) -> (a + (-2))
     */
    protected Node transformSubtractions(Node node) {
        if(!(node instanceof FunctionNode))
            return node;

        FunctionNode functionNode = (FunctionNode) node;
        Function function = functionNode.getFunction();
        Node[] arguments = functionNode.getArguments();

        if(!(function instanceof Subtract)) {
            for(int index = 0; index < arguments.length; ++index) {
                arguments[index] = transformSubtractions(arguments[index]);
            }

            return node;
        }

        if(!isConstant(arguments[1]))
            return node;

        arguments[1] = new ConstantNode((-1) * arguments[1].evaluate(new double[0]));

        return new SingleFunctionNode(Add.fn, arguments);
    }

    /**
     * If {@param node} is a FunctionNode with function {@param function}.
     */
    private static boolean isFunction(Node node, Function function) {
        return (node instanceof FunctionNode && ((FunctionNode) node).getFunction() == function);
    }

    /**
     * If {@param node} is a constant with value {@param value}.
     */
    private static boolean isConstantOfValue(Node node, double value) {
        return (node instanceof ConstantNode && ((ConstantNode) node).value == value);
    }
}

package net.sothatsit.evaluate.parser;

import net.sothatsit.evaluate.PrefixTree;
import net.sothatsit.evaluate.optimiser.CompositeOptimiser;
import net.sothatsit.evaluate.optimiser.Optimiser;
import net.sothatsit.evaluate.tree.*;
import net.sothatsit.evaluate.tree.function.Function;
import net.sothatsit.evaluate.tree.function.MathFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionParser {

    public static void main(String[] args) {
        ExpressionParser parser = new ExpressionParser();

        parser.addArgument("a");
        parser.addArgument("b");
        parser.addArgument("c");

        String equation = "sin(a + sin(b) + 1.2E-234)";

        Node expression;

        try {
            expression = parser.parseNode(new StringStream(equation));
        } catch(ParseException e) {
            e.printError();
            return;
        }

        System.out.println(expression);

        double[] inputs = new double[] {
                Math.random(), Math.random(), Math.random()
        };

        System.out.println();
        System.out.println("if(" + inputs[0] + ", " + inputs[1] + ", " + inputs[2] + ")" +
                           " = " + expression.evaluate(inputs));
    }

    private final Optimiser optimiser;

    private final Map<String, Function> functions = new HashMap<>();
    private final Map<String, Double> constants = new HashMap<>();
    private final PrefixTree<Operator> operators = new PrefixTree<>();
    private final List<String> arguments = new ArrayList<>();

    private final Map<String, Node> intermediateVariables = new HashMap<>();

    public ExpressionParser() {
        this(CompositeOptimiser.all());
    }

    public ExpressionParser(Optimiser optimiser) {
        this.optimiser = optimiser;

        addFunctions(MathFunctions.all());
        addOperators(Operator.all());
    }

    public int addArgument(String name) {
        int index = arguments.size();
        arguments.add(name);

        return index;
    }

    public void addFunction(Function function) {
        functions.put(function.getName(), function);

        for(String alias : function.getAliases()) {
            functions.put(alias, function);
        }
    }

    public void addFunctions(Function... functions) {
        for(Function function : functions) {
            addFunction(function);
        }
    }

    public void addConstant(String name, double value) {
        constants.put(name, value);
    }

    public void addOperator(Operator operator) {
        operators.add(operator.getOperatorString(), operator);
    }

    public void addOperators(Operator... operators) {
        for(Operator operator : operators) {
            addOperator(operator);
        }
    }

    public void addIntermediateVariable(String name, String equation) {
        StringStream stream = new StringStream(equation);

        intermediateVariables.put(name, parseNode(stream));
    }

    public Expression parse(String equation) {
        StringStream stream = new StringStream(equation);
        Expression expression = new Expression(parseNode(stream), arguments);

        optimiser.optimise(expression);

        return expression;
    }

    private Node parseNode(StringStream stream) {
        int fromIndex = stream.getCurrentIndex();
        List<Token> tokens = tokenize(stream);
        int toIndex = stream.getCurrentIndex();

        if(tokens.size() == 0)
            throw stream.error("Empty expression", fromIndex, toIndex);

        while(true) {
            int index = findHighestPrecedence(tokens);

            // No more operators
            if(index < 0)
                break;

            Token token = tokens.remove(index);
            Operator operator = token.getOperator();
            String operatorString = "\"" + operator.getOperatorString() + "\"";

            Token[] arguments = new Token[operator.getArgumentCount()];
            int argumentIndex = 0;
            int insertIndex = index;

            if(operator.requiresRight()) {
                if(index == tokens.size())
                    throw token.error(stream, "Operator " + operatorString + " requires a value to its right");

                Token right = tokens.get(index);

                if(right.isOperator())
                    throw token.error(stream, "Operator " + operatorString + " requires a value to its right");

                arguments[argumentIndex++] = tokens.remove(index);
            }

            if(operator.requiresLeft()) {
                if(index == 0)
                    throw token.error(stream, "Operator " + operatorString + " requires a value to its left");

                Token left = tokens.get(index - 1);

                if(left.isOperator())
                    throw token.error(stream, "Operator " + operatorString + " requires a value to its left");

                arguments[argumentIndex] = tokens.remove(index - 1);
                insertIndex -= 1;
            }

            Token newToken = operator.getToken(token, arguments);
            tokens.add(insertIndex, newToken);
        }

        if(tokens.size() > 1) {
            int errorFromIndex = tokens.get(0).endIndex;
            int errorToIndex = tokens.get(1).startIndex;

            throw stream.error("Missing operator", errorFromIndex, errorToIndex);
        }

        return tokens.get(0).getNode();
    }

    private int findHighestPrecedence(List<Token> tokens) {
        int highestIndex = -1;
        int highestPrecedence = -1;
        boolean highestRequiresRightOnly = false;

        for(int index = 0; index < tokens.size(); ++index) {
            Token token = tokens.get(index);

            if(!token.isOperator())
                continue;

            Operator operator = token.getOperator();
            int precedence = operator.getPrecedence();

            // We want to evaluate right-to-left if an operator requires only a right argument
            if(precedence < highestPrecedence || (precedence == highestPrecedence && !highestRequiresRightOnly))
                continue;

            highestIndex = index;
            highestPrecedence = operator.getPrecedence();
            highestRequiresRightOnly = !operator.requiresLeft() && operator.requiresRight();
        }

        return highestIndex;
    }

    private List<Token> tokenize(StringStream stream) {
        List<Token> tokens = new ArrayList<>();

        while(stream.hasNext()) {
            stream.consumeWhitespace();

            if(!stream.hasNext())
                break;

            char ch = stream.next();

            // Brackets
            if(ch == '(') {
                tokens.add(parseBrackets(stream));
                continue;
            }

            // Numbers
            if('0' <= ch && ch <= '9') {
                tokens.add(parseNumber(stream));
                continue;
            }

            // Operators
            Token operator = parseOperator(stream);
            if(operator != null) {
                tokens.add(operator);
                continue;
            }

            // Functions, ifs, constants and variables
            if(Character.isLetter(ch)) {
                tokens.add(parseIdentifier(stream));
                continue;
            }

            throw stream.error("Unexpected character \"" + ch + "\"");
        }

        return tokens;
    }

    private Token parseOperator(StringStream stream) {
        int fromIndex = stream.getCurrentIndex();

        PrefixTree<Operator> tree = operators;

        while(tree != null) {
            char ch = stream.consume();
            Operator operator = tree.get(ch);

            if(operator != null)
                return Token.operator(operator, fromIndex, stream.getCurrentIndex());

            tree = tree.getSubTree(ch);
        }

        stream.returnTo(fromIndex);
        return null;
    }

    private Token parseBrackets(StringStream stream) {
        int fromIndex = stream.getCurrentIndex();
        StringStream brackets = stream.consumeBrackets();
        int toIndex = stream.getCurrentIndex();

        return Token.node(parseNode(brackets), fromIndex, toIndex);
    }

    private Token parseNumber(StringStream stream) {
        int fromIndex = stream.getCurrentIndex();
        double number = stream.consumeNumber();
        int toIndex = stream.getCurrentIndex();

        return Token.constant(number, fromIndex, toIndex);
    }

    private Token parseIdentifier(StringStream stream) {
        int fromIndex = stream.getCurrentIndex();
        String identifier = stream.consumeIdentifier();
        int toIndex = stream.getCurrentIndex();

        // Functions and ifs
        if(stream.hasNext() && stream.next() == '(')
            return parseFunction(stream, fromIndex, identifier);

        // Constants
        if(constants.containsKey(identifier)) {
            double value = constants.get(identifier);

            return Token.constant(identifier, value, fromIndex, toIndex);
        }

        // Intermediate variables
        if(intermediateVariables.containsKey(identifier)) {
            Node intermediate = intermediateVariables.get(identifier);

            return Token.node(intermediate, fromIndex, toIndex);
        }

        // Input Variable
        if(arguments.contains(identifier)) {
            int varIndex = arguments.indexOf(identifier);

            return Token.variable(identifier, varIndex, fromIndex, toIndex);
        }

        throw stream.error("Unknown variable or constant \"" + identifier + "\"", fromIndex, toIndex);
    }

    private Token parseFunction(StringStream stream, int fromIndex, String identifier) {
        StringStream[] argumentStrings = stream.consumeFunctionArguments();
        int toIndex = stream.getCurrentIndex();

        Node[] arguments = new Node[argumentStrings.length];
        for(int index = 0; index < arguments.length; ++index) {
            arguments[index] = parseNode(argumentStrings[index]);
        }

        if(identifier.equals("if")) {
            if(arguments.length != 3)
                throw stream.error("if statements require 3 arguments", fromIndex, toIndex);

            return Token.ifStatement(arguments[0], arguments[1], arguments[2], fromIndex, toIndex);
        }

        Function function = functions.get(identifier);

        if(function == null)
            throw stream.error("Unknown function " + identifier, fromIndex, toIndex);

        try {
            return Token.function(function, arguments, fromIndex, toIndex);
        } catch(IllegalArgumentException e) {
            throw stream.error(e.getMessage(), fromIndex, toIndex);
        }
    }
}

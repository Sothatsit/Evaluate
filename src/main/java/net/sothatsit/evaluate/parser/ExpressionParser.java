package net.sothatsit.evaluate.parser;

import net.sothatsit.evaluate.tree.Expression;
import net.sothatsit.evaluate.tree.ConstantNode;
import net.sothatsit.evaluate.tree.FunctionNode;
import net.sothatsit.evaluate.tree.Node;
import net.sothatsit.evaluate.tree.VariableNode;
import net.sothatsit.evaluate.tree.function.Function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionParser {

    public static void main(String[] args) {
        ExpressionParser parser = new ExpressionParser();

        String equation = "3 * (1 * 2 * 3 * 4 * (e + 3 + 2 - 4)) + 1 + (c + 2 + e^(4*3+2^7) * 3)";

        Node expression = parser.parseNode(equation);
        Node simplified = parser.parseNode(equation).trySimplify();

        System.out.println(expression);
        System.out.println(simplified);
    }

    private final Map<String, Function> functions = new HashMap<>();
    private final List<String> arguments = new ArrayList<>();

    public void addFunction(String identifier, Function function) {
        functions.put(identifier, function);
    }

    private int getArgumentIndex(String name) {
        if(!arguments.contains(name)) {
            arguments.add(name);
        }

        return arguments.indexOf(name);
    }

    public Expression parse(String equation) {
        Node root = Node.simplifyOrItself(parseNode(equation));

        return new Expression(root, arguments);
    }

    private Node parseNode(String equation) {
        List<Token> tokens = tokenize(equation);

        if(tokens.size() == 0)
            throw new IllegalArgumentException("Empty expression");

        while(true) {
            int operatorIndex = -1;
            BaseOperator operator = null;

            for(int index = 0; index < tokens.size(); ++index) {
                Token token = tokens.get(index);

                if(!token.isOperator())
                    continue;

                BaseOperator possibleOperator = token.getOperator();

                if(operator == null || possibleOperator.precedence > operator.precedence) {
                    operatorIndex = index;
                    operator = possibleOperator;
                }
            }

            if(operator == null)
                break;

            if(operatorIndex == 0 || operatorIndex == tokens.size() - 1)
                throw new IllegalArgumentException("Operator must have two operands");

            Token left = tokens.get(operatorIndex - 1);
            Token right = tokens.get(operatorIndex + 1);

            if(left.isOperator() || right.isOperator())
                throw new IllegalArgumentException("Two adjacent operators");

            FunctionNode node = new FunctionNode(operator.function, left.getNode(), right.getNode());

            tokens.remove(operatorIndex + 1);
            tokens.remove(operatorIndex);
            tokens.remove(operatorIndex - 1);
            tokens.add(operatorIndex - 1, new Token.NodeToken(node));
        }

        if(tokens.size() > 1)
            throw new IllegalArgumentException("Missing operator");

        return tokens.get(0).getNode();
    }

    private List<Token> tokenize(String equation) {
        List<Token> tokens = new ArrayList<>();

        for(int index = 0; index < equation.length(); ++index) {
            char character = equation.charAt(index);

            if(character == '(') {
                String brackets = extractBrackets(equation, index);
                index += brackets.length() + 1;

                Node subExpression = parseNode(brackets);

                tokens.add(new Token.NodeToken(subExpression));
                continue;
            }

            if(isDigit(character)) {
                String number = extractNumber(equation, index);

                index += number.length();
                index -= 1;

                double value = Double.valueOf(number);
                Node constant = new ConstantNode(value);

                tokens.add(new Token.NodeToken(constant));
                continue;
            }

            if(isLetter(character)) {
                String identifier = extractIdentifier(equation, index);

                index += identifier.length();
                index -= 1;

                if(functions.containsKey(identifier) && equation.charAt(index + 1) == '(') {
                    index += 1;

                    String brackets = extractBrackets(equation, index);

                    Function function = functions.get(identifier);
                    Node[] arguments = extractFunctionArguments(brackets);
                    FunctionNode node = new FunctionNode(function, arguments);

                    index += brackets.length();
                    index += 1;

                    tokens.add(new Token.NodeToken(node));
                    continue;
                }

                int variableIndex = getArgumentIndex(identifier);

                tokens.add(new Token.NodeToken(new VariableNode(identifier, variableIndex)));
                continue;
            }

            BaseOperator operator = BaseOperator.find(character);

            if(operator != null) {
                tokens.add(new Token.OperatorToken(operator));
                continue;
            }

            if(Character.isWhitespace(character))
                continue;

            throw new IllegalArgumentException("Unexpected character " + character);
        }

        return tokens;
    }

    private Node[] extractFunctionArguments(String brackets) {
        if(brackets.trim().length() == 0)
            return new Node[0];

        int depth = 0;
        int splitFrom = 0;

        List<Node> arguments = new ArrayList<>();

        for(int index = 0; index < brackets.length(); ++index) {
            char character = brackets.charAt(index);

            if(character == '(') depth += 1;
            if(character == ')') depth -= 1;

            if(character == ',' && depth == 0) {
                String argument = brackets.substring(splitFrom, index);
                Node expression = parseNode(argument);

                arguments.add(expression);

                splitFrom = index + 1;
            }
        }

        String argument = brackets.substring(splitFrom);
        Node expression = parseNode(argument);

        arguments.add(expression);

        return arguments.toArray(new Node[arguments.size()]);
    }

    private String extractIdentifier(String equation, int fromIndex) {
        if(!isLetter(equation.charAt(fromIndex)))
            throw new IllegalArgumentException("Expected letter (a-zA-Z)");

        int index;

        for(index = fromIndex + 1; index < equation.length(); ++index) {
            char character = equation.charAt(index);

            if(isLetter(character) || isDigit(character) || character == '_')
                continue;

            break;
        }

        return equation.substring(fromIndex, index);
    }

    private String extractNumber(String equation, int fromIndex) {
        if(!isDigit(equation.charAt(fromIndex)))
            throw new IllegalArgumentException("Expected digit (0-9)");

        int index;

        boolean seenDecimalPoint = false;
        boolean seenExponent = false;
        boolean seenExponentDecimalPoint = false;

        for(index = fromIndex + 1; index < equation.length(); ++index) {
            char character = equation.charAt(index);

            if(isDigit(character))
                continue;

            if(character == '.') {
                if(!seenExponent) {
                    if(seenDecimalPoint)
                        throw new IllegalArgumentException("Unexpected decimal point");

                    seenDecimalPoint = true;
                    continue;
                } else {
                    if(seenExponentDecimalPoint)
                        throw new IllegalArgumentException("Unexpected decimal point");

                    seenExponentDecimalPoint = true;
                    continue;
                }
            }

            if(character == 'e' || character == 'E') {
                if(seenExponent)
                    throw new IllegalArgumentException("Unexpected exponent");

                seenExponent = true;
                continue;
            }

            if(character == '+' || character == '-') {
                char lastCharacter = equation.charAt(index - 1);

                if(lastCharacter != 'e')
                    break;

                continue;
            }

            break;
        }

        // We don't want to include this e as it's probably being used as a constant.
        if(equation.charAt(index - 1) == 'e') {
            index -= 1;
        }

        return equation.substring(fromIndex, index);
    }

    private String extractBrackets(String equation, int fromIndex) {
        if(equation.charAt(fromIndex) != '(')
            throw new IllegalArgumentException("Expected opening bracket");

        int depth = 1;

        for(int index = fromIndex + 1; index < equation.length(); ++index) {
            char character = equation.charAt(index);

            if(character == '(') {
                depth += 1;
            }

            if(character == ')') {
                depth -= 1;

                if(depth == 0)
                    return equation.substring(fromIndex + 1, index);
            }
        }

        throw new IllegalArgumentException("Expected closing bracket");
    }

    private static boolean isLetter(char character) {
        return ('a' <= character && character <= 'z') || ('A' <= character && character <= 'Z');
    }

    private static boolean isDigit(char character) {
        return '0' <= character && character <= '9';
    }
}

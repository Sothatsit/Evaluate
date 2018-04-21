package net.sothatsit.evaluate.parser;

import java.util.ArrayList;
import java.util.List;

public class StringStream {

    public final String input;
    public final int startIndex;
    public final int endIndex;
    private int index;

    public StringStream(String input) {
        this(input, 0, input.length());
    }

    public StringStream(String input, int startIndex, int endIndex) {
        this.input = input;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.index = startIndex;
    }

    public boolean isEmpty() {
        for(int index = startIndex; index < endIndex; ++index) {
            char ch = input.charAt(index);

            if(!isWhitespace(ch))
                return false;
        }
        return true;
    }

    public int getCurrentIndex() {
        return index;
    }

    public void returnTo(int index) {
        this.index = index;
    }

    public ParseException error(String reason) {
        return new ParseException(reason, this, index, index + 1);
    }

    public ParseException error(String reason, int fromIndex, int toIndex) {
        return new ParseException(reason, this, fromIndex, toIndex);
    }

    public boolean hasNext() {
        return index < endIndex;
    }

    public char next() {
        if(!hasNext())
            throw error("Unexpected end of expression");

        return input.charAt(index);
    }

    public char next(String state) {
        return next(state, index, index);
    }

    public char next(String state, int fromIndex) {
        return next(state, fromIndex, index);
    }

    public char next(String state, int fromIndex, int toIndex) {
        if(!hasNext())
            throw error("Unexpected end of expression while " + state, fromIndex, toIndex);

        return input.charAt(index);
    }

    public boolean hasNext(int count) {
        return index + count <= input.length();
    }

    public String next(int count) {
        if(!hasNext())
            throw error("Unexpected end of expression", index, input.length());

        return input.substring(index, index + count);
    }

    public char consume() {
        char ch = next();

        this.index += 1;

        return ch;
    }

    public String consume(int count) {
        String string = next(count);

        this.index += count;

        return string;
    }

    public void consumeWhitespace() {
        while(hasNext() && isWhitespace(next())) {
            index += 1;
        }
    }

    private static boolean isWhitespace(char ch) {
        return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r' || ch == '\b' || ch == '\f';
    }

    public String consumeIdentifier() {
        char first = next("parsing identifier");

        if(!Character.isLetter(first))
            throw new IllegalStateException("Attempted to consume identifier when not positioned over a first letter");

        int fromIndex = index;

        while(isIdentifierChar(next())) {
            index += 1;

            if(!hasNext())
                break;
        }

        return input.substring(fromIndex, index);
    }

    private static boolean isIdentifierChar(char ch) {
        return Character.isLetter(ch) || Character.isDigit(ch) || ch == '_';
    }

    public double consumeNumber() {
        char first = next("parsing number");

        if(first < '0' || first > '9')
            throw new IllegalStateException("Attempted to consume number when not positioned over a number");

        int fromIndex = index;

        boolean seenExponent = false;
        boolean seenDecimalPoint = false;

        while(hasNext()) {
            char next = next();

            if('0' <= next && next <= '9') {
                index += 1;
                continue;
            }

            if(next == 'e' || next == 'E') {
                if(seenExponent)
                    throw error("Unexpected second exponent when parsing number");

                index += 1;
                seenExponent = true;
                seenDecimalPoint = false;

                next = next("parsing number's exponent", fromIndex);
                if(next == '+' || next == '-') {
                    index += 1;
                }

                continue;
            }

            if(next == '.') {
                if(seenDecimalPoint)
                    throw error("Unexpected second decimal point when parsing number");

                index += 1;
                seenDecimalPoint = true;
                continue;
            }

            break;
        }

        String number = input.substring(fromIndex, index);

        try {
            return Double.valueOf(number);
        } catch(NumberFormatException e) {
            String message = e.getMessage();
            String comma = (!message.isEmpty() ? ", " : "");

            throw error("Unable to parse number \"" + number + "\"" + comma + message, fromIndex, index);
        }
    }

    public StringStream consumeBrackets() {
        char first = next("parsing brackets");

        if(first != '(')
            throw new IllegalStateException("Attempted to consume brackets when not positioned over brackets");

        int fromIndex = index;
        int depth = 0;

        while(true) {
            char next = next("parsing brackets", fromIndex);

            index += 1;

            if(next == '(') {
                depth += 1;
                continue;
            }

            if(next == ')') {
                depth -= 1;

                if(depth == 0)
                    break;

                continue;
            }
        }

        return new StringStream(input, fromIndex + 1, index - 1);
    }

    public StringStream[] consumeFunctionArguments() {
        char first = next("parsing brackets");

        if(first != '(')
            throw new IllegalStateException("Attempted to consume brackets when not positioned over brackets");

        int fromIndex = index;

        index += 1;

        List<StringStream> arguments = new ArrayList<>();
        int splitFrom = index;
        int depth = 1;

        while(true) {
            char next = next("parsing brackets", fromIndex);

            index += 1;

            if(next == '(') {
                depth += 1;
                continue;
            }

            if(next == ')') {
                depth -= 1;

                if(depth != 0)
                    continue;

                break;
            }

            if(next == ',' && depth == 1) {
                arguments.add(new StringStream(input, splitFrom, index - 1));

                splitFrom = index;
                continue;
            }
        }

        arguments.add(new StringStream(input, splitFrom, index - 1));

        if(arguments.size() == 1 && arguments.get(0).isEmpty())
            return new StringStream[0];

        return arguments.toArray(new StringStream[arguments.size()]);
    }
}

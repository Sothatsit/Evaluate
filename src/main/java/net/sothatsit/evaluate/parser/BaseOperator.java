package net.sothatsit.evaluate.parser;

import net.sothatsit.evaluate.tree.function.Function;
import net.sothatsit.evaluate.tree.function.operator.*;

public enum BaseOperator {

    ADD('+', 0, new Add()),
    SUBTRACT('-', 0, new Subtract()),

    MULTIPLY('*', 1, new Multiply()),
    DIVIDE('/', 1, new Divide()),

    POWER('^', 2, new Power());

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
}

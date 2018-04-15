package net.sothatsit.evaluate.compiler;

import net.sothatsit.evaluate.tree.Expression;

public abstract class CompiledExpression {

    public final Expression expression;

    public CompiledExpression(Expression expression) {
        this.expression = expression;
    }

    public abstract double evaluate(double... inputs);
}

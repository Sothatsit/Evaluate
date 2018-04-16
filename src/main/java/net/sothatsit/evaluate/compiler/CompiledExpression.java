package net.sothatsit.evaluate.compiler;

import net.sothatsit.evaluate.tree.Expression;

public abstract class CompiledExpression {

    public final Expression expression;
    public final double[] inputs;

    public CompiledExpression(Expression expression, int inputCount) {
        this.expression = expression;
        this.inputs = new double[inputCount];
    }

    public void setVariable(int index, double value) {
        inputs[index] = value;
    }

    public abstract double evaluate();
}

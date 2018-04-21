package net.sothatsit.evaluate.compiler;

public abstract class CompiledExpression {

    public final double[] inputs;
    public final double[] outputs;

    public CompiledExpression(int inputCount, int outputCount) {
        this.inputs = new double[inputCount];
        this.outputs = new double[outputCount];
    }

    public final void setVariable(int index, double value) {
        inputs[index] = value;
    }

    public final double getOutput(int index) {
        return outputs[index];
    }

    public abstract void evaluate();
}

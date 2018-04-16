package net.sothatsit.evaluate.tree.function.operator;

import net.sothatsit.evaluate.compiler.MethodCompiler;
import net.sothatsit.evaluate.tree.function.CompilableTwoArgFunction;

public class Divide extends CompilableTwoArgFunction {

    public static final Divide fn = new Divide();

    private Divide() {
        super("divide");
    }

    @Override
    public double evaluate(double arg1, double arg2) {
        return arg1 / arg2;
    }

    @Override
    public void compile(MethodCompiler mc) {
        mc.divide();
    }
}

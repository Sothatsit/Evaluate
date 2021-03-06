package net.sothatsit.evaluate.tree.function.operator;

import net.sothatsit.evaluate.compiler.MethodCompiler;
import net.sothatsit.evaluate.optimiser.OptimiseOptions;
import net.sothatsit.evaluate.tree.function.CompilableTwoArgFunction;

public class Add extends CompilableTwoArgFunction {

    public static final Add fn = new Add();

    private Add() {
        super("add");
    }

    @Override
    public OptimiseOptions getOptimiseOptions() {
        return super.getOptimiseOptions()
                    .withIsOrderDependant(false);
    }

    @Override
    public double evaluate(double arg1, double arg2) {
        return arg1 + arg2;
    }

    @Override
    public void compile(MethodCompiler mc) {
        mc.add();
    }
}

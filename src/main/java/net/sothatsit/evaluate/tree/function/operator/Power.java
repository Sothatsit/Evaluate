package net.sothatsit.evaluate.tree.function.operator;

import net.sothatsit.evaluate.compiler.MethodCompiler;
import net.sothatsit.evaluate.tree.function.CompilableTwoArgFunction;

public class Power extends CompilableTwoArgFunction {

    public static final Power fn = new Power();

    private Power() {
        super("power");
    }

    @Override
    public double evaluate(double arg1, double arg2) {
        return Math.pow(arg1, arg2);
    }

    @Override
    public void compile(MethodCompiler mc) {
        mc.staticMethod(Math.class, "pow", 2);
    }
}

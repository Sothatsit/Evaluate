package net.sothatsit.evaluate.compiler;

import net.sothatsit.evaluate.tree.function.Function;

public interface Compilable extends Function {

    public void compile(MethodCompiler mc);
}

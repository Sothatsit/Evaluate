package net.sothatsit.evaluate.tree.function;

import net.sothatsit.evaluate.compiler.Compilable;

public abstract class CompilableTwoArgFunction extends TwoArgFunction implements Compilable {

    public CompilableTwoArgFunction(String name, String... aliases) {
        super(name, aliases);
    }
}

package net.sothatsit.evaluate.tree.function;

import net.sothatsit.evaluate.compiler.Compilable;

public abstract class CompilableNoArgFunction extends NoArgFunction implements Compilable {

    public CompilableNoArgFunction(String name, String... aliases) {
        super(name, aliases);
    }
}

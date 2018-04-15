package net.sothatsit.evaluate.tree.function;

import net.sothatsit.evaluate.compiler.Compilable;

public abstract class CompilableThreeArgFunction extends ThreeArgFunction implements Compilable {

    public CompilableThreeArgFunction(String name, String... aliases) {
        super(name, aliases);
    }
}

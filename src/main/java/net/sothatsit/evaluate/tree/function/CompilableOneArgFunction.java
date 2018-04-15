package net.sothatsit.evaluate.tree.function;

import net.sothatsit.evaluate.compiler.Compilable;

public abstract class CompilableOneArgFunction extends OneArgFunction implements Compilable {

    public CompilableOneArgFunction(String name, String... aliases) {
        super(name, aliases);
    }
}

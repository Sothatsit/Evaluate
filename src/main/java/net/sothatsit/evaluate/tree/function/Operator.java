package net.sothatsit.evaluate.tree.function;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import net.sothatsit.evaluate.tree.function.Function;

public interface Operator extends Function {

    public void visitInstructions(MethodVisitor mv);
}

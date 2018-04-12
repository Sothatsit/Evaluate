package net.sothatsit.evaluate.tree.function;

import jdk.internal.org.objectweb.asm.MethodVisitor;

public interface Operator extends Function {

    public void visitInstructions(MethodVisitor mv);
}

package net.sothatsit.evaluate.tree.function.operator;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import net.sothatsit.evaluate.tree.function.BiOperator;

import static jdk.internal.org.objectweb.asm.Opcodes.DMUL;

public class Multiply extends BiOperator {

    public String getName() {
        return "multiply";
    }

    public boolean isOrderDependant() {
        return false;
    }

    public double evaluate(double arg1, double arg2) {
        return arg1 * arg2;
    }

    public void visitInstructions(MethodVisitor mv) {
        mv.visitInsn(DMUL);
    }
}

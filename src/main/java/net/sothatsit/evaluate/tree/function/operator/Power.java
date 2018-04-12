package net.sothatsit.evaluate.tree.function.operator;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;
import net.sothatsit.evaluate.tree.function.BiOperator;

import static jdk.internal.org.objectweb.asm.Opcodes.INVOKESTATIC;

public class Power extends BiOperator {

    public String getName() {
        return "power";
    }

    public boolean isOrderDependant() {
        return true;
    }

    public double evaluate(double arg1, double arg2) {
        return Math.pow(arg1, arg2);
    }

    public void visitInstructions(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Math.class), "pow", "(DD)D", false);
    }
}

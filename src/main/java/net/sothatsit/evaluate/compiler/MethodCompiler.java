package net.sothatsit.evaluate.compiler;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.Arrays;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

public class MethodCompiler {

    public final String className;
    public final MethodVisitor mv;

    public MethodCompiler(String className, MethodVisitor mv) {
        this.className = className;
        this.mv = mv;
    }

    public void insn(int opcode) {
        mv.visitInsn(opcode);
    }

    public void intInsn(int opcode, int value) {
        mv.visitIntInsn(opcode, value);
    }

    public void varInsn(int opcode, int index) {
        mv.visitVarInsn(opcode, index);
    }

    public void add() {
        insn(DADD);
    }

    public void subtract() {
        insn(DSUB);
    }

    public void multiply() {
        insn(DMUL);
    }

    public void divide() {
        insn(DDIV);
    }

    public void perform(Compilable function) {
        function.compile(this);
    }

    public void duplicate() {
        insn(DUP);
    }

    public void pop() {
        insn(POP);
    }

    public void moveOneUnder() {
        insn(DUP2_X2);
        insn(POP2);
    }

    public void loadConstant(int constant) {
        mv.visitLdcInsn(constant);
    }

    public void loadConstant(double constant) {
        mv.visitLdcInsn(constant);
    }

    public void loadArgument(int argumentIndex) {
        loadField(Type.getInternalName(CompiledExpression.class), "inputs", "[D");
        mv.visitLdcInsn(argumentIndex);
        insn(DALOAD);
    }

    public void loadThis() {
        varInsn(ALOAD, 0);
    }

    public void storeTemp(int tempIndex) {
        varInsn(DSTORE, 2 + tempIndex);
    }

    public void loadTemp(int tempIndex) {
        varInsn(DLOAD, 2 + tempIndex);
    }

    public void storeTempReference(int tempIndex) {
        varInsn(ASTORE, 2 + tempIndex);
    }

    public void loadTempReference(int tempIndex) {
        varInsn(ALOAD, 2 + tempIndex);
    }

    public void loadField(String name, String desc) {
        loadField(className, name, desc);
    }

    public void loadField(String className, String name, String desc) {
        loadThis();
        mv.visitFieldInsn(GETFIELD, className, name, desc);
    }

    public void staticMethod(Class<?> clazz, String name, int argumentCount) {
        Class<?>[] parameterTypes = new Class<?>[argumentCount];
        Arrays.fill(parameterTypes, double.class);

        Method method;

        try {
            method = clazz.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Unable to find method " + name + " in " + clazz, e);
        }

        if(!method.getReturnType().equals(double.class))
            throw new IllegalArgumentException("Method " + name + " in " + clazz + " must return a double");

        mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(clazz), name, Type.getMethodDescriptor(method), false);
    }

    protected void method(Class<?> clazz, String name, int argumentCount) {
        Class<?>[] parameterTypes = new Class<?>[argumentCount];
        Arrays.fill(parameterTypes, double.class);

        method(clazz, name, parameterTypes);
    }

    protected void method(Class<?> clazz, String name, Class<?>... parameterTypes) {
        Method method;

        try {
            method = clazz.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Unable to find method " + name + " in " + clazz, e);
        }

        if(!method.getReturnType().equals(double.class))
            throw new IllegalArgumentException("Method " + name + " in " + clazz + " must return a double");

        int opcode = (clazz.isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL);

        mv.visitMethodInsn(opcode, Type.getInternalName(clazz), name, Type.getMethodDescriptor(method), clazz.isInterface());
    }
}

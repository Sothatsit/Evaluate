package net.sothatsit.evaluate.compiler;

import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.Arrays;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

public class MethodCompiler {

    public final String className;
    public final Class<?> returnType;

    public final MethodVisitor mv;
    public final Locals locals;

    private MethodCompiler(String className, Class<?> returnType, MethodVisitor mv) {
        this.className = className;
        this.returnType = returnType;

        this.mv = mv;
        this.locals = new Locals(this);
    }

    private static String getDescriptor(Class<?> clazz) {
        // Type.getDescriptor does not deal with primitive arrays correctly
        if(clazz.isArray())
            return "[" + getDescriptor(clazz.getComponentType());

        return Type.getDescriptor(clazz);
    }

    public static MethodCompiler begin(ClassVisitor cv, String className,
                                       Class<?> returnType, String methodName, Class<?>... parameterTypes) {

        StringBuilder desc = new StringBuilder();
        {
            desc.append("(");
            for(Class<?> parameter : parameterTypes) {
                desc.append(getDescriptor(parameter));
            }
            desc.append(")");
            desc.append(getDescriptor(returnType));
        }

        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, methodName, desc.toString(), null, null);

        return new MethodCompiler(className, returnType, mv);
    }

    public void end() {
        if(returnType.equals(int.class)) {
            mv.visitInsn(IRETURN);
        } else if(returnType.equals(long.class)) {
            mv.visitInsn(LRETURN);
        } else if(returnType.equals(float.class)) {
            mv.visitInsn(FRETURN);
        } else if(returnType.equals(double.class)) {
            mv.visitInsn(DRETURN);
        } else if(returnType.equals(void.class)) {
            mv.visitInsn(RETURN);
        } else {
            mv.visitInsn(ARETURN);
        }

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    //
    // SIMPLE INSTRUCTIONS
    //

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

    public void remainder() {
        insn(DREM);
    }

    public void perform(Compilable function) {
        function.compile(this);
    }

    public void loadConstant(int constant) {
        mv.visitLdcInsn(constant);
    }

    public void loadConstant(double constant) {
        mv.visitLdcInsn(constant);
    }



    //
    // OBJECTS
    //

    public void loadThis() {
        varInsn(ALOAD, 0);
    }

    public void loadArgument(int argumentIndex) {
        loadField(Type.getInternalName(CompiledExpression.class), "inputs", "[D");
        loadConstant(argumentIndex);
        insn(DALOAD);
    }

    public void loadField(String name, String desc) {
        loadField(className, name, desc);
    }

    public void loadField(String className, String name, String desc) {
        loadThis();
        mv.visitFieldInsn(GETFIELD, className, name, desc);
    }

    public void storeField(String name, String desc) {
        storeField(className, name, desc);
    }

    public void storeField(String className, String name, String desc) {
        loadThis();
        mv.visitFieldInsn(PUTFIELD, className, name, desc);
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

package net.sothatsit.evaluate.compiler;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import static jdk.internal.org.objectweb.asm.Opcodes.*;

import jdk.internal.org.objectweb.asm.Type;
import net.sothatsit.evaluate.tree.*;
import net.sothatsit.evaluate.tree.function.*;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class ExpressionCompiler {

    private static final String EXPRESSION_TYPE_DESC = "L" + Type.getInternalName(Expression.class) + ";";
    private final ExpressionLoader loader = new ExpressionLoader();

    protected static Class<?> getFunctionReferenceClass(Function function) {
        if(function instanceof ThreeArgFunction)
            return ThreeArgFunction.class;

        if(function instanceof TwoArgFunction)
            return TwoArgFunction.class;

        if(function instanceof OneArgFunction)
            return OneArgFunction.class;

        else return Function.class;
    }

    private static String getFunctionDesc(Function function) {
        return "L" + Type.getInternalName(getFunctionReferenceClass(function)) + ";";
    }

    public CompiledExpression compile(Expression expression) {
        String name = loader.getNextName();
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        String className = name.replace('.', '/');
        String classSuper = Type.getInternalName(CompiledExpression.class);
        cw.visit(V1_8, ACC_PUBLIC, className, null, classSuper, null);

        Set<Function> unorderedFields = new HashSet<>();
        { // Find all the external functions that the expression uses
            Queue<Node> toCheck = new LinkedBlockingQueue<>();
            toCheck.add(expression.root);

            while(!toCheck.isEmpty()) {
                Node node = toCheck.poll();

                if(!(node instanceof FunctionNode))
                    continue;

                FunctionNode functionNode = (FunctionNode) node;
                Collections.addAll(toCheck, functionNode.getArguments());

                if(functionNode.getFunction() instanceof Compilable)
                    continue;

                unorderedFields.add(functionNode.getFunction());
            }
        }
        List<Function> fields = new ArrayList<>(unorderedFields);

        cw.visitField(ACC_PRIVATE, "inputs", "[D", null, null).visitEnd();
        for(Function function : fields) {
            cw.visitField(ACC_PRIVATE, function.getName(), getFunctionDesc(function), null, null).visitEnd();
        }

        { // Constructor
            StringBuilder desc = new StringBuilder();
            {
                desc.append("(");
                desc.append(EXPRESSION_TYPE_DESC);
                desc.append("I");

                for(Function function : fields) {
                    desc.append(getFunctionDesc(function));
                }

                desc.append(")V");
            }

            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", desc.toString(), null, null);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ILOAD, 2);
            mv.visitMethodInsn(INVOKESPECIAL, classSuper, "<init>", "(" + EXPRESSION_TYPE_DESC + "I)V", false);

            for(int index = 0; index < fields.size(); ++index) {
                Function function = fields.get(index);

                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 3 + index);
                mv.visitFieldInsn(PUTFIELD, className, function.getName(), getFunctionDesc(function));
            }

            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        { // public double evaluate(double[] inputs);
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "evaluate", "()D", null, null);
            MethodCompiler mc = new MethodCompiler(className, mv);

            visitNode(mc, expression.root);

            mv.visitInsn(DRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        cw.visitEnd();

        return loader.load(name, expression, 2, fields, cw.toByteArray());
    }

    private void visitNode(MethodCompiler mc, Node node) {
        if(node instanceof ConstantNode) {
            mc.loadConstant(((ConstantNode) node).value);
            return;
        }

        if(node instanceof VariableNode) {
            mc.loadArgument(((VariableNode) node).index);
            return;
        }

        SingleFunctionNode functionNode = (SingleFunctionNode) node;
        Function function = functionNode.function;
        Node[] arguments = functionNode.arguments;

        if(!(function instanceof Compilable)) {
            mc.loadField(function.getName(), getFunctionDesc(function));
        }

        boolean varArgs = !(function instanceof Compilable       ||
                            function instanceof ThreeArgFunction ||
                            function instanceof TwoArgFunction   ||
                            function instanceof OneArgFunction   ||
                            function instanceof NoArgFunction);

        if(varArgs) {
            mc.loadConstant(arguments.length);
            mc.intInsn(NEWARRAY, T_DOUBLE);
        }

        for(int index = 0; index < arguments.length; ++index) {
            Node argument = arguments[index];

            if(varArgs) {
                mc.duplicate();
                mc.loadConstant(index);
            }

            visitNode(mc, argument);

            if(varArgs) {
                mc.insn(DASTORE);
            }
        }

        if(function instanceof Compilable) {
            ((Compilable) function).compile(mc);
        } else if(function instanceof ThreeArgFunction) {
            mc.method(ThreeArgFunction.class, "evaluate", 3);
        } else if(function instanceof TwoArgFunction) {
            mc.method(TwoArgFunction.class, "evaluate", 2);
        } else if(function instanceof OneArgFunction) {
            mc.method(OneArgFunction.class, "evaluate", 1);
        } else if(function instanceof NoArgFunction) {
            mc.method(NoArgFunction.class, "evaluate", 0);
        } else {
            mc.method(Function.class, "evaluate", double[].class);
        }
    }
}

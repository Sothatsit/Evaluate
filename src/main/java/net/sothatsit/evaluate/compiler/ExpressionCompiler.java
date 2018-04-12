package net.sothatsit.evaluate.compiler;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import static jdk.internal.org.objectweb.asm.Opcodes.*;

import jdk.internal.org.objectweb.asm.Type;
import net.sothatsit.evaluate.tree.Expression;
import net.sothatsit.evaluate.parser.ExpressionParser;
import net.sothatsit.evaluate.tree.ConstantNode;
import net.sothatsit.evaluate.tree.FunctionNode;
import net.sothatsit.evaluate.tree.Node;
import net.sothatsit.evaluate.tree.VariableNode;
import net.sothatsit.evaluate.tree.function.Function;
import net.sothatsit.evaluate.tree.function.Operator;

import java.util.*;

public class ExpressionCompiler {

    private static final String FUNCTION_TYPE_DESC = "L" + Type.getInternalName(Function.class) + ";";

    public static void main(String[] args) {
        ExpressionParser parser = new ExpressionParser();
        parser.addFunction("ln", new Function() {
            public String getName() { return "ln"; }
            public int getArgumentCount() { return 1; }
            public boolean isOrderDependant() { return true; }

            public double evaluate(double[] arguments) {
                return Math.log(arguments[0]);
            }
        });

        Expression expression = parser.parse("ln(a) * 3 + 5 * (a + 4)");

        ExpressionCompiler compiler = new ExpressionCompiler();
        CompiledExpression compiled = compiler.compile(expression);

        System.out.println(compiled.getClass());
        double[] inputs = new double[] {Math.E};

        System.out.println(expression.evaluate(inputs) + " = " + compiled.evaluate(inputs));

        long count = 10_000_000;

        long expressionTime;
        {
            // Warmup
            for(int i=0; i < count; ++i) {
                expression.evaluate(inputs);
            }

            long start = System.nanoTime();
            for(int i=0; i < count; ++i) {
                expression.evaluate(inputs);
            }
            long end = System.nanoTime();

            expressionTime = end - start;
        }

        long compiledTime;
        {
            // Warmup
            for(int i=0; i < count; ++i) {
                compiled.evaluate(inputs);
            }

            long start = System.nanoTime();
            for(int i=0; i < count; ++i) {
                compiled.evaluate(inputs);
            }
            long end = System.nanoTime();

            compiledTime = end - start;
        }

        double expressionMs = expressionTime / 1_000_000d;
        double compiledMs = compiledTime / 1_000_000d;

        double expressionPer = (double) expressionTime / count;
        double compiledPer = (double) compiledTime / count;

        System.out.println("Expression took " + expressionMs + "ms (" + expressionPer + " ns per op)");
        System.out.println("Compiled took " + compiledMs + "ms (" + compiledPer + " ns per op)");
    }

    private final ExpressionLoader loader = new ExpressionLoader();

    public CompiledExpression compile(Expression expression) {
        String name = loader.getNextName();
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        String className = name.replace('.', '/');
        String classSuper = Type.getInternalName(CompiledExpression.class);
        cw.visit(V1_8, ACC_PUBLIC, className, null, classSuper, null);

        List<Function> fields = new ArrayList<>(expression.root.getAllUsedFunctions());
        { // We don't need to store references to operators
            Iterator<Function> iterator = fields.iterator();
            while(iterator.hasNext()) {
                if(!(iterator.next() instanceof Operator))
                    continue;

                iterator.remove();
            }
        }

        for(Function function : fields) {
            cw.visitField(ACC_PRIVATE, function.getName(), FUNCTION_TYPE_DESC, null, null).visitEnd();
        }

        { // Constructor
            StringBuilder desc = new StringBuilder();
            {
                desc.append("(");

                for(int i=0; i < fields.size(); ++i) {
                    desc.append(FUNCTION_TYPE_DESC);
                }

                desc.append(")V");
            }

            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", desc.toString(), null, null);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, classSuper, "<init>", "()V", false);

            for(int index = 0; index < fields.size(); ++index) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, index + 1);
                mv.visitFieldInsn(PUTFIELD, className, fields.get(index).getName(), FUNCTION_TYPE_DESC);
            }

            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        { // public double evaluate(double[] inputs);
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "evaluate", "([D)D", null, null);

            visitNode(className, mv, expression.root);

            mv.visitInsn(DRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        cw.visitEnd();

        return loader.load(name, fields, cw.toByteArray());
    }

    private void visitNode(String className, MethodVisitor mv, Node node) {
        if(node instanceof ConstantNode) {
            mv.visitLdcInsn(((ConstantNode) node).value);
            return;
        }

        if(node instanceof VariableNode) {
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(((VariableNode) node).index);
            mv.visitInsn(DALOAD);
            return;
        }

        FunctionNode functionNode = (FunctionNode) node;
        Function function = functionNode.function;
        Node[] arguments = functionNode.arguments;

        for(Node argument : arguments) {
            visitNode(className, mv, argument);
        }

        if(function instanceof Operator) {
            ((Operator) function).visitInstructions(mv);
        } else {
            mv.visitLdcInsn(arguments.length);
            mv.visitIntInsn(NEWARRAY, T_DOUBLE);
            mv.visitVarInsn(ASTORE, 2);

            for(int index = arguments.length - 1; index >= 0; --index) {
                mv.visitVarInsn(DSTORE, 3);

                mv.visitVarInsn(ALOAD, 2);
                mv.visitLdcInsn(index);
                mv.visitVarInsn(DLOAD, 3);

                mv.visitInsn(DASTORE);
            }

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, function.getName(), FUNCTION_TYPE_DESC);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Function.class), "evaluate", "([D)D", true);
        }
    }

    private void visitArgumentLoad(MethodVisitor mv, int argumentIndex) {
        mv.visitVarInsn(ALOAD, 1);
        mv.visitLdcInsn(argumentIndex);
        mv.visitInsn(DALOAD);
    }
}

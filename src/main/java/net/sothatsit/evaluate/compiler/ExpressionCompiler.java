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
import net.sothatsit.evaluate.tree.function.*;

import java.util.*;

public class ExpressionCompiler {

    private static final String EXPRESSION_TYPE_DESC = "L" + Type.getInternalName(Expression.class) + ";";

    public static void main(String[] args) {
        ExpressionParser parser = new ExpressionParser();
        parser.addFunctions(MathFunctions.get());
        parser.addFunction(new OneArgFunction("apple") {
            @Override
            public double evaluate(double arg) {
                return 2 * arg + 4;
            }
        });

        String eqn = "apple(apple(apple(apple(apple(a)))))";
        Expression expression = parser.parse(eqn);

        ExpressionCompiler compiler = new ExpressionCompiler();
        CompiledExpression compiled = compiler.compile(expression);

        System.out.println(compiled.getClass());
        double[] inputs = new double[] {Math.E, 2 * Math.E};

        System.out.println(expression.evaluate(inputs) + " = " + compiled.evaluate(inputs));

        long count = 100_000_000;

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

        List<Function> fields = new ArrayList<>(expression.root.getAllUsedFunctions());
        { // We don't need to store references to operators
            Iterator<Function> iterator = fields.iterator();
            while(iterator.hasNext()) {
                if(!(iterator.next() instanceof Compilable))
                    continue;

                iterator.remove();
            }
        }

        for(Function function : fields) {
            cw.visitField(ACC_PRIVATE, function.getName(), getFunctionDesc(function), null, null).visitEnd();
        }

        { // Constructor
            StringBuilder desc = new StringBuilder();
            {
                desc.append("(");
                desc.append(EXPRESSION_TYPE_DESC);

                for(Function function : fields) {
                    desc.append(getFunctionDesc(function));
                }

                desc.append(")V");
            }

            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", desc.toString(), null, null);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESPECIAL, classSuper, "<init>", "(" + EXPRESSION_TYPE_DESC + ")V", false);

            for(int index = 0; index < fields.size(); ++index) {
                Function function = fields.get(index);

                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 2 + index);
                mv.visitFieldInsn(PUTFIELD, className, function.getName(), getFunctionDesc(function));
            }

            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        { // public double evaluate(double[] inputs);
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "evaluate", "([D)D", null, null);
            MethodCompiler mc = new MethodCompiler(className, mv);

            visitNode(mc, expression.root);

            mv.visitInsn(DRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        cw.visitEnd();

        return loader.load(name, expression, fields, cw.toByteArray());
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

        FunctionNode functionNode = (FunctionNode) node;
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

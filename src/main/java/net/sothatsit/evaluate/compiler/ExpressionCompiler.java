package net.sothatsit.evaluate.compiler;

import jdk.internal.org.objectweb.asm.ClassWriter;
import static jdk.internal.org.objectweb.asm.Opcodes.*;

import jdk.internal.org.objectweb.asm.Type;
import net.sothatsit.evaluate.tree.*;
import net.sothatsit.evaluate.tree.function.*;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class ExpressionCompiler {

    private final ExpressionLoader loader = new ExpressionLoader();
    private final List<Expression> outputs = new ArrayList<>();

    public int addOutput(Expression expression) {
        outputs.add(expression);

        Collections.sort(outputs);

        return outputs.size() - 1;
    }

    public int getInputCount() {
        int max = 0;
        for(Expression expression : outputs) {
            max = Math.max(max, expression.getArgumentCount());
        }
        return max;
    }

    private List<Function> findNeededFunctionReferences() {
        Set<Function> necessary = new HashSet<>();
        { // Find all the external functions that the expression uses
            Queue<Node> toCheck = new LinkedBlockingQueue<>();

            for(Expression expression : outputs) {
                toCheck.add(expression.root);
            }

            while(!toCheck.isEmpty()) {
                Node node = toCheck.poll();

                if(!(node instanceof AbstractFunctionNode))
                    continue;

                AbstractFunctionNode functionNode = (AbstractFunctionNode) node;
                Collections.addAll(toCheck, functionNode.getArguments());

                if(functionNode.getFunction() instanceof Compilable)
                    continue;

                necessary.add(functionNode.getFunction());
            }
        }

        return new ArrayList<>(necessary);
    }

    public CompiledExpression compile() {
        String name = loader.getNextName();
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        String className = name.replace('.', '/');
        String classSuper = Type.getInternalName(CompiledExpression.class);
        cw.visit(V1_8, ACC_PUBLIC, className, null, classSuper, null);

        List<Function> fields = findNeededFunctionReferences();
        { // Fields
            for(Function function : fields) {
                cw.visitField(ACC_PRIVATE | ACC_FINAL, function.getName(), getFunctionDesc(function), null, null).visitEnd();
            }
        }

        { // Constructor
            Class<?>[] parameterTypes = new Class<?>[2 + fields.size()];
            {
                parameterTypes[0] = int.class;
                parameterTypes[1] = int.class;
                for(int index = 0; index < fields.size(); ++index) {
                    parameterTypes[2 + index] = getFunctionReferenceClass(fields.get(index));
                }
            }

            MethodCompiler mc = MethodCompiler.begin(cw, className, void.class, "<init>", parameterTypes);

            mc.loadThis();
            mc.varInsn(ILOAD, 1);
            mc.varInsn(ILOAD, 2);

            mc.mv.visitMethodInsn(INVOKESPECIAL, classSuper, "<init>", "(II)V", false);

            for(int index = 0; index < fields.size(); ++index) {
                Function function = fields.get(index);

                mc.loadThis();
                mc.varInsn(ALOAD, 3 + index);
                mc.storeField(function.getName(), getFunctionDesc(function));
            }

            mc.end();
        }

        { // public void evaluate():
            MethodCompiler mc = MethodCompiler.begin(cw, className, void.class, "evaluate");

            compileMethod(mc);

            mc.end();
        }

        cw.visitEnd();

        return loader.load(name, fields, getInputCount(), outputs.size(), cw.toByteArray());
    }

    private void compileMethod(MethodCompiler mc) {
        Map<Node, Integer> subtreeFrequencies = new HashMap<>();
        {
            Queue<Node> toCheck = new LinkedBlockingQueue<>();

            for(Expression output : outputs) {
                toCheck.add(output.root);
            }

            while(!toCheck.isEmpty()) {
                Node check = toCheck.poll();
                Integer count = subtreeFrequencies.get(check);

                if(count == null) {
                    subtreeFrequencies.put(check, 1);

                    if(check instanceof AbstractFunctionNode) {
                        Collections.addAll(toCheck, ((AbstractFunctionNode) check).getArguments());
                    }
                } else {
                    subtreeFrequencies.put(check, 1 + count);
                }
            }
        }

        List<Node> commonTerms = new ArrayList<>();
        {
            for(Map.Entry<Node, Integer> entry : subtreeFrequencies.entrySet()) {
                if(entry.getKey() instanceof ConstantNode)
                    continue;

                // It takes more uses of a variable to outweigh the cost of storing it as a local.
                if(entry.getKey() instanceof VariableNode && entry.getValue() < 3)
                    continue;

                if(entry.getValue() < 2)
                    continue;

                commonTerms.add(entry.getKey());
            }

            Collections.sort(commonTerms, new Node.NodeComparator(false));
        }

        Map<Node, Integer> preComputedTerms = new HashMap<>();
        {
            for (Node term : commonTerms) {
                visitNode(preComputedTerms, mc, term);

                int index = mc.locals.newDoubleVariable();
                mc.locals.storeVariable(index);
                preComputedTerms.put(term, index);

                // System.err.println("Pre-computed " + term);
            }
        }

        for(int index = 0; index < outputs.size(); ++index) {
            Expression output = outputs.get(index);

            visitNode(preComputedTerms, mc, output.root);

            mc.loadField(Type.getInternalName(CompiledExpression.class), "outputs", "[D");
            mc.insn(DUP_X2);
            mc.insn(POP);

            mc.loadConstant(index);
            mc.insn(DUP_X2);
            mc.insn(POP);

            mc.insn(DASTORE);
        }
    }

    private void visitNode(Map<Node, Integer> preComputedTerms, MethodCompiler mc, Node node) {
        if(preComputedTerms.containsKey(node)) {
            mc.locals.loadVariable(preComputedTerms.get(node));
            return;
        }

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

        boolean varArgs = !(function instanceof Compilable       ||
                            function instanceof ThreeArgFunction ||
                            function instanceof TwoArgFunction   ||
                            function instanceof OneArgFunction   ||
                            function instanceof NoArgFunction);

        if(!(function instanceof Compilable)) {
            mc.loadField(function.getName(), getFunctionDesc(function));
        }

        if(varArgs) {
            mc.loadConstant(arguments.length);
            mc.intInsn(NEWARRAY, T_DOUBLE);
        }

        for(int index = 0; index < arguments.length; ++index) {
            Node argument = arguments[index];

            if(varArgs) {
                mc.insn(DUP);
                mc.loadConstant(index);
            }

            visitNode(preComputedTerms, mc, argument);

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
}

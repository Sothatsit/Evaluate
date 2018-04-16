package net.sothatsit.evaluate;

import net.benmann.evald.Evald;
import net.benmann.evald.Library;
import net.sothatsit.evaluate.benchmark.Benchmark;
import net.sothatsit.evaluate.compiler.CompiledExpression;
import net.sothatsit.evaluate.compiler.ExpressionCompiler;
import net.sothatsit.evaluate.parser.ExpressionParser;
import net.sothatsit.evaluate.tree.function.MathFunctions;

public class Evaluate {

    public static void main(String[] args) {
        String equation = "a * b + b / (a + 2 * b + (b*b*b*b)*a)";

        checkEquivalence(equation);
        benchmark(equation, 100, 1_000_000);
    }

    private static boolean doublesEqual(double a, double b, double delta) {
        return Math.abs(a - b) <= delta;
    }

    public static void checkEquivalence(String equation) {
        int checks = 1_000_000;

        Evald evald;
        int evald_aIndex;
        int evald_bIndex;
        {
            evald = new Evald();
            evald.addLibrary(Library.ALL);

            evald_aIndex = evald.addVariable("a");
            evald_bIndex = evald.addVariable("b");

            evald.parse(equation);
        }

        CompiledExpression evaluate;
        int evaluate_aIndex;
        int evaluate_bIndex;
        {
            ExpressionParser parser = new ExpressionParser();
            ExpressionCompiler compiler = new ExpressionCompiler();
            parser.addFunctions(MathFunctions.get());

            evaluate_aIndex = parser.addArgument("a");
            evaluate_bIndex = parser.addArgument("b");

            evaluate = compiler.compile(parser.parse(equation));
        }

        for(int index=0; index < checks; ++index) {
            double a = Math.random();
            double b = Math.random();

            evald.setVariable(evald_aIndex, a);
            evald.setVariable(evald_bIndex, b);

            evaluate.setVariable(evaluate_aIndex, a);
            evaluate.setVariable(evaluate_bIndex, b);

            double evaldResult = evald.evaluate();
            double evaluateResult = evaluate.evaluate();

            if(!doublesEqual(evaldResult, evaluateResult, 1E-15)) {
                System.err.println("Results did not match at index " + index + ": " + evaldResult + " != " + evaluateResult);
                System.err.println("  a = " + a + ", b = " + b);

                break;
            }
        }

        System.out.println("All " + checks + " random tests of evald and Evaluate were equal");
    }

    public static void benchmark(String equation, int trials, int operationsPerTrial) {
        double[] evaldOutputs = new double[operationsPerTrial];
        double[] evaluateOutputs = new double[operationsPerTrial];

        Runnable testEvald;
        {
            Evald evald = new Evald();
            evald.addLibrary(Library.ALL);

            int aIndex = evald.addVariable("a");
            int bIndex = evald.addVariable("b");

            evald.parse(equation);

            testEvald = () -> {
                for (int index = 0; index < operationsPerTrial; ++index) {
                    evald.setVariable(aIndex, Math.random());
                    evald.setVariable(bIndex, Math.random());

                    evaldOutputs[index] = evald.evaluate();
                }
            };
        }

        Runnable testEvaluate;
        {
            ExpressionParser parser = new ExpressionParser();
            ExpressionCompiler compiler = new ExpressionCompiler();
            parser.addFunctions(MathFunctions.get());

            int aIndex = parser.addArgument("a");
            int bIndex = parser.addArgument("b");

            CompiledExpression expression = compiler.compile(parser.parse(equation));

            testEvaluate = () -> {
                for (int index = 0; index < operationsPerTrial; ++index) {
                    expression.setVariable(aIndex, Math.random());
                    expression.setVariable(bIndex, Math.random());

                    evaluateOutputs[index] = expression.evaluate();
                }
            };
        }

        System.out.println();
        System.out.println("Running " + trials + " trials at " + operationsPerTrial + " operations per trial...");
        System.out.println();

        Benchmark benchmark = new Benchmark();

        benchmark.addTestCase("evald", testEvald);
        benchmark.addTestCase("Evaluate", testEvaluate);

        benchmark.warmup();
        benchmark.runTrials(trials);

        System.out.print(benchmark.toString());

        blackhole(evaldOutputs);
        blackhole(evaluateOutputs);
    }

    /**
     * Get rid of unused warnings.
     */
    @SuppressWarnings("unused")
    private static void blackhole(Object obj) {}
}

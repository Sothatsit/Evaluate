package net.sothatsit.evaluate;

import net.benmann.evald.Evald;
import net.benmann.evald.Library;
import net.sothatsit.evaluate.benchmark.Benchmark;
import net.sothatsit.evaluate.compiler.CompiledExpression;
import net.sothatsit.evaluate.compiler.ExpressionCompiler;
import net.sothatsit.evaluate.parser.ExpressionParser;
import net.sothatsit.evaluate.tree.Expression;
import net.sothatsit.evaluate.tree.function.MathFunctions;

public class Evaluate {

    public static void main(String[] args) {
        String x = "17 * 6 * a + 12334 * b";
        String y = "(b^5.5 + 7) / a / 2";
        String equation = "x * y + y / (x + 2 * y + (y*y*y*y)*x)";

        benchmarkMultiStage(x, y, equation, 100, 1_000_000);

        //checkEquivalence(equation);
        //benchmark(equation, 100, 1_000_000);
    }

    public static void benchmarkMultiStage(String xEquation, String yEquation, String equation, int trials, int operationsPerTrial) {
        double[] evaldOutputs = new double[operationsPerTrial];
        double[] evaluateOutputs = new double[operationsPerTrial];

        Runnable testEvald;
        {
            long start = System.nanoTime();

            Evald x = new Evald();
            x.addLibrary(Library.CORE);

            int x_aIndex = x.addVariable("a");
            int x_bIndex = x.addVariable("b");

            x.parse(xEquation);

            Evald y = new Evald();
            y.addLibrary(Library.CORE);

            int y_aIndex = y.addVariable("a");
            int y_bIndex = y.addVariable("b");

            y.parse(yEquation);

            Evald evald = new Evald();
            evald.addLibrary(Library.CORE);

            int xIndex = evald.addVariable("x");
            int yIndex = evald.addVariable("y");

            evald.parse(equation);

            long end = System.nanoTime();
            System.out.println("evald parsed expressions in " + ((end - start) / 1_000_000d) + "ms");

            testEvald = () -> {
                double a, b, xRes, yRes;

                for (int index = 0; index < operationsPerTrial; ++index) {
                    a = Math.random();
                    b = Math.random();

                    x.setVariable(x_aIndex, a);
                    x.setVariable(x_bIndex, b);

                    y.setVariable(y_aIndex, a);
                    y.setVariable(y_bIndex, b);

                    xRes = x.evaluate();
                    yRes = y.evaluate();

                    evald.setVariable(xIndex, xRes);
                    evald.setVariable(yIndex, yRes);

                    evaldOutputs[index] = evald.evaluate();
                }
            };
        }

        Runnable testEvaluate;
        {
            long start = System.nanoTime();

            String allInOne = equation.replace("x", "(" + xEquation + ")").replace("y", "(" + yEquation + ")");

            ExpressionParser parser = new ExpressionParser();
            ExpressionCompiler compiler = new ExpressionCompiler();
            parser.addFunctions(MathFunctions.get());

            int aIndex = parser.addArgument("a");
            int bIndex = parser.addArgument("b");

            CompiledExpression expression = compiler.compile(parser.parse(allInOne));

            long end = System.nanoTime();
            System.out.println("Evaluate parsed expressions in " + ((end - start) / 1_000_000d) + "ms");

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

            Expression parsed;
            {
                long start = System.nanoTime();

                parsed = parser.parse(equation);

                long end = System.nanoTime();
                System.out.println("Parsing took " + ((end - start) / 1_000_000d) + "ms");
            }

            {
                long start = System.nanoTime();

                evaluate = compiler.compile(parsed);

                long end = System.nanoTime();
                System.out.println("Compilation took " + ((end - start) / 1_000_000d) + "ms");
            }
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

            if(!doublesEqual(evaldResult, evaluateResult, 1E-10)) {
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

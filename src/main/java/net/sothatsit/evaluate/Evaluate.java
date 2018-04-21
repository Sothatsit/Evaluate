package net.sothatsit.evaluate;

import net.benmann.evald.MultiEvald;
import net.benmann.evald.Evald;
import net.benmann.evald.Library;
import net.sothatsit.evaluate.benchmark.Benchmark;
import net.sothatsit.evaluate.compiler.CompiledExpression;
import net.sothatsit.evaluate.compiler.ExpressionCompiler;
import net.sothatsit.evaluate.optimiser.CompositeOptimiser;
import net.sothatsit.evaluate.parser.ExpressionParser;
import net.sothatsit.evaluate.tree.Expression;
import net.sothatsit.evaluate.tree.function.MathFunctions;

public class Evaluate {

    public static void main(String[] args) {
        benchmarkMultiStage(100, 1_000_000);

        //checkEquivalence(equation);
        //benchmark(equation, 100, 1_000_000);
    }

    public static void benchmarkMultiStage(int trials, int operationsPerTrial) {
        // String xEqn = "sin(a)";
        // String yEqn = "cos(b)";
        // String out1Eqn = "x ^ y";
        // String out2Eqn = "y ^ x";

        String xEqn = "(a + b + a * b) / (a + 2 * b) / b";
        String yEqn = "(a + 2 * b) / (a + b + a * b) / a";
        String out1Eqn = "2 * x * y";
        String out2Eqn = "2 / x / y";

        Runnable testEvald;
        {
            long start = System.nanoTime();

            Evald x = new Evald();
            x.addLibrary(Library.ALL);

            final int x_aIndex = x.addVariable("a");
            final int x_bIndex = x.addVariable("b");

            x.parse(xEqn);

            Evald y = new Evald();
            y.addLibrary(Library.ALL);

            final int y_aIndex = y.addVariable("a");
            final int y_bIndex = y.addVariable("b");

            y.parse(yEqn);

            Evald out1 = new Evald();
            out1.addLibrary(Library.ALL);

            final int aIndex_1 = out1.addVariable("a");
            final int bIndex_1 = out1.addVariable("b");
            final int xIndex_1 = out1.addVariable("x");
            final int yIndex_1 = out1.addVariable("y");

            out1.parse(out1Eqn);

            Evald out2 = new Evald();
            out2.addLibrary(Library.ALL);

            final int aIndex_2 = out2.addVariable("a");
            final int bIndex_2 = out2.addVariable("b");
            final int xIndex_2 = out2.addVariable("x");
            final int yIndex_2 = out2.addVariable("y");

            out2.parse(out2Eqn);

            long end = System.nanoTime();
            System.out.println("evald parsed expressions in " + ((end - start) / 1_000_000d) + "ms");

            testEvald = () -> {
                final double[] outputs = new double[2];

                for (int index = 0; index < operationsPerTrial; ++index) {
                    double a = Math.random();
                    double b = Math.random();

                    x.setVariable(x_aIndex, a);
                    x.setVariable(x_bIndex, b);

                    y.setVariable(y_aIndex, a);
                    y.setVariable(y_bIndex, b);

                    double xRes = x.evaluate();
                    double yRes = y.evaluate();

                    out1.setVariable(aIndex_1, a);
                    out1.setVariable(bIndex_1, b);
                    out1.setVariable(xIndex_1, xRes);
                    out1.setVariable(yIndex_1, yRes);

                    out2.setVariable(aIndex_2, a);
                    out2.setVariable(bIndex_2, b);
                    out2.setVariable(xIndex_2, xRes);
                    out2.setVariable(yIndex_2, yRes);

                    outputs[0] = out1.evaluate();
                    outputs[1] = out2.evaluate();
                }

                blackhole(outputs);
            };
        }

        Runnable testMultiEvald;
        {
            long start = System.nanoTime();

            MultiEvald evald = new MultiEvald();
            evald.addLibrary(Library.ALL);

            final int aIndex = evald.addVariable("a");
            final int bIndex = evald.addVariable("b");

            evald.parseIntermediate("x", xEqn);
            evald.parseIntermediate("y", yEqn);

            final int out1Index = evald.parseOutput("out1", out1Eqn);
            final int out2Index = evald.parseOutput("out2", out2Eqn);

            long end = System.nanoTime();
            System.out.println("multi-evald parsed expressions in " + ((end - start) / 1_000_000d) + "ms");

            testMultiEvald = () -> {
                final double[] outputs = new double[2];

                for (int index = 0; index < operationsPerTrial; ++index) {
                    double a = Math.random();
                    double b = Math.random();

                    evald.setVariable(aIndex, a);
                    evald.setVariable(bIndex, b);

                    evald.evaluate();

                    outputs[0] = evald.getOutput(out1Index);
                    outputs[1] = evald.getOutput(out2Index);
                }

                blackhole(outputs);
            };
        }

        Runnable testEvaluate;
        {
            long start = System.nanoTime();

            ExpressionParser parser = new ExpressionParser(CompositeOptimiser.all());
            ExpressionCompiler compiler = new ExpressionCompiler();
            parser.addFunctions(MathFunctions.all());

            int aIndex = parser.addArgument("a");
            int bIndex = parser.addArgument("b");

            parser.addIntermediateVariable("x", xEqn);
            parser.addIntermediateVariable("y", yEqn);

            Expression outputOne = parser.parse(out1Eqn);
            Expression outputTwo = parser.parse(out2Eqn);

            compiler.addOutput(outputOne);
            compiler.addOutput(outputTwo);

            CompiledExpression expression = compiler.compile();

            long end = System.nanoTime();
            System.out.println("Evaluate parsed expressions in " + ((end - start) / 1_000_000d) + "ms");

            testEvaluate = () -> {
                final double[] outputs = new double[2];

                for (int index = 0; index < operationsPerTrial; ++index) {
                    expression.setVariable(aIndex, Math.random());
                    expression.setVariable(bIndex, Math.random());

                    expression.evaluate();

                    outputs[0] = expression.getOutput(0);
                    outputs[1] = expression.getOutput(1);
                }

                blackhole(outputs);
            };
        }

        Runnable testManual;
        {
            final double[] outputs = new double[2];

            Runnable evaluate = () -> {
                double a = Math.random();
                double b = Math.random();

                double x = (a + b + a * b) / (a + 2 * b) / b;
                double y = (a + 2 * b) / (a + b + a * b) / a;

                outputs[0] = 2 * x * y;
                outputs[1] = 2 / x / y;
            };

            testManual = () -> {
                for(int index = 0; index < operationsPerTrial; ++index) {
                    evaluate.run();
                }

                blackhole(outputs);
            };
        }

        System.out.println();
        System.out.println("Running " + trials + " trials at " + operationsPerTrial + " operations per trial...");
        System.out.println();

        Benchmark benchmark = new Benchmark();

        benchmark.addTestCase("evald", testEvald);
        benchmark.addTestCase("multi-evald", testMultiEvald);
        //benchmark.addTestCase("Evaluate", testEvaluate);
        //benchmark.addTestCase("manual", testManual);

        benchmark.warmup();
        benchmark.runTrials(trials);

        System.out.print(benchmark.toString());
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
            parser.addFunctions(MathFunctions.all());

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

                compiler.addOutput(parsed);

                evaluate = compiler.compile();

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

            evaluate.evaluate();
            double evaluateResult = evaluate.getOutput(0);

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
            parser.addFunctions(MathFunctions.all());

            int aIndex = parser.addArgument("a");
            int bIndex = parser.addArgument("b");

            compiler.addOutput(parser.parse(equation));
            CompiledExpression expression = compiler.compile();

            testEvaluate = () -> {
                for (int index = 0; index < operationsPerTrial; ++index) {
                    expression.setVariable(aIndex, Math.random());
                    expression.setVariable(bIndex, Math.random());

                    expression.evaluate();
                    evaluateOutputs[index] = expression.getOutput(0);
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

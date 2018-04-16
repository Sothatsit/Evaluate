package net.sothatsit.evaluate.benchmark;

import java.util.ArrayList;
import java.util.List;

public class Benchmark {

    private final List<TestCase> testCases = new ArrayList<>();
    private int order = 0;

    public void addTestCase(String name, Runnable function) {
        testCases.add(new TestCase(name, function));
    }

    public void reset() {
        for(TestCase testCase : testCases) {
            testCase.stats.reset();
        }
    }

    public void warmup() {
        for(TestCase testCase : testCases) {
            testCase.function.run();
        }
    }

    public void runTrial() {
        order += 1;

        for(int index = 0; index < testCases.size(); ++index) {
            TestCase testCase = testCases.get((order + index) % testCases.size());

            long start = System.nanoTime();
            {
                testCase.function.run();
            }
            long end = System.nanoTime();
            double durationMS = (end - start) / 1_000_000d;

            testCase.stats.addTrial(durationMS);
        }
    }

    public void runTrials(int trialCount) {
        for(int i=0; i < trialCount; ++i) {
            runTrial();
        }
    }

    private static class TestCase {

        public final String name;
        public final Runnable function;
        public final Stats stats;

        public TestCase(String name, Runnable function) {
            this.name = name;
            this.function = function;
            this.stats = new Stats();
        }

        @Override
        public String toString() {
            return stats.toString(name, " ms");
        }
    }

    private static String addSideBySide(String one, int gap, String two) {
        String[] oneLines = one.split("\n");
        String[] twoLines = two.split("\n");

        int largestLine = 0;

        for(String line : oneLines) {
            largestLine = Math.max(largestLine, line.length());
        }

        StringBuilder builder = new StringBuilder();
        int rowCount = Math.max(oneLines.length, twoLines.length);

        for(int row = 0; row < rowCount; ++row) {
            int length = 0;

            if(row < oneLines.length) {
                length += oneLines[row].length();
                builder.append(oneLines[row]);
            }

            while(length < largestLine + gap) {
                builder.append(' ');
                length += 1;
            }

            if(row < twoLines.length) {
                builder.append(twoLines[row]);
            }

            builder.append('\n');
        }

        return builder.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        TestCase firstTestCase = testCases.get(0);
        double overallMin = firstTestCase.stats.getMin();
        double overallMax = firstTestCase.stats.getMax();

        for(int index = 1; index < testCases.size(); ++index) {
            TestCase testCase = testCases.get(index);
            double min = testCase.stats.getMin();
            double max = testCase.stats.getMax();

            if(min < overallMin) overallMin = min;
            if(max > overallMax) overallMax = max;
        }

        for(TestCase testCase : testCases) {
            double[] frequencies = testCase.stats.getNormalisedFrequencies(overallMin, overallMax, 50);
            String barGraph = BarGraph.createBarGraph(testCase.name, frequencies, 5);
            String info = testCase.toString();

            builder.append(addSideBySide(barGraph, 2, info));
            builder.append("\n\n");
        }

        return builder.toString();
    }
}

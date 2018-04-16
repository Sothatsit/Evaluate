package net.sothatsit.evaluate.benchmark;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Stats {

    private final List<Double> trials = new ArrayList<>();

    public void addTrial(double trial) {
        trials.add(trial);
    }

    public void addTrials(double... trials) {
        for(double trial : trials) {
            addTrial(trial);
        }
    }

    public void reset() {
        trials.clear();
    }

    public List<Double> getTrials() {
        return trials;
    }

    public int getEntryCount() {
        return trials.size();
    }

    public double getMin() {
        if(getEntryCount() == 0)
            throw new IllegalStateException("There are no trials");

        double min = trials.get(0);

        for(int index = 1; index < trials.size(); ++index) {
            if(trials.get(index) >= min)
                continue;

            min = trials.get(index);
        }

        return min;
    }

    public double getMax() {
        if(getEntryCount() == 0)
            throw new IllegalStateException("There are no trials");

        double max = trials.get(0);

        for(int index = 1; index < trials.size(); ++index) {
            if(trials.get(index) <= max)
                continue;

            max = trials.get(index);
        }

        return max;
    }

    public double getAggregate() {
        if(getEntryCount() == 0)
            throw new IllegalStateException("There are no trials");

        double aggregate = 0;

        for(double trial : trials) {
            aggregate += trial;
        }

        return aggregate;
    }

    public double getMean() {
        if(getEntryCount() == 0)
            throw new IllegalStateException("There are no trials");

        return getAggregate() / getEntryCount();
    }

    public double getMedian() {
        if(getEntryCount() == 0)
            throw new IllegalStateException("There are no trials");

        Collections.sort(trials);

        int middle = getEntryCount() / 2;

        if((getEntryCount() & 1) == 1)
            return trials.get(middle);

        return (trials.get(middle - 1) + trials.get(middle)) / 2;
    }

    public double getStdDeviation() {
        if(getEntryCount() == 0)
            throw new IllegalStateException("There are no trials");

        double mean = getMean();
        double sum = 0;

        for(double trial : trials) {
            double diff = trial - mean;

            sum += diff * diff;
        }

        return Math.sqrt(sum / getEntryCount());
    }

    private static String toDisplay(double number) {
        BigDecimal bigDecimal = new BigDecimal(number);
        bigDecimal = bigDecimal.round(new MathContext(4));
        return bigDecimal.toString();
    }

    public int[] getFrequencies(int columns) {
        return getFrequencies(getMin(), getMax(), columns);
    }

    public int[] getFrequencies(double min, double max, int columns) {
        int[] columnFrequencies = new int[columns];

        for(double entry : trials) {
            double normalised = (entry - min) / (max - min);
            int column = (int) Math.floor(normalised * columns);

            // Special case for values at max of range
            if(column == columns) {
                column = columns - 1;
            }

            columnFrequencies[column] += 1;
        }

        return columnFrequencies;
    }

    public double[] getNormalisedFrequencies(int columns) {
        return getNormalisedFrequencies(getMin(), getMax(), columns);
    }

    public double[] getNormalisedFrequencies(double min, double max, int columns) {
        int[] frequencies = getFrequencies(min, max, columns);

        int maxFrequency = 0;

        for(int frequency : frequencies) {
            if(frequency <= maxFrequency)
                continue;

            maxFrequency = frequency;
        }

        double[] normalised = new double[columns];

        for(int index = 0; index < columns; ++index) {
            normalised[index] = (double) frequencies[index] / maxFrequency;
        }

        return normalised;
    }

    @Override
    public String toString() {
        return toString("Results", "");
    }

    public String toString(String label, String units) {
        String min          = toDisplay(getMin())          + units;
        String max          = toDisplay(getMax())          + units;
        String mean         = toDisplay(getMean())         + units;
        String median       = toDisplay(getMedian())       + units;
        String stdDeviation = toDisplay(getStdDeviation()) + units;

        return label + ", " + getEntryCount() + " trials\n" +
               "  mean           = " + mean + "\n" +
               "  median         = " + median + "\n" +
               "  std. deviation = " + stdDeviation + "\n" +
               "  range          = [" + min + " -> " + max + "]" + "\n";
    }
}

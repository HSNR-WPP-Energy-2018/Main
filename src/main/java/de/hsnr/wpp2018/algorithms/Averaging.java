package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.base.Algorithm;
import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.evaluation.Rating;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class Averaging implements Algorithm<Averaging.Configuration> {

    public TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, Configuration configuration) {
        TreeMap<LocalDateTime, Consumption> results = new TreeMap<>();
        LocalDateTime time = data.firstKey(), end = data.lastKey();
        while (!time.isAfter(end)) {
            results.put(time, data.getOrDefault(time, new Consumption(interpolateValue(data, configuration, time), true)));
            time = time.plusSeconds(configuration.getInterval());
        }
        return results;
    }

    private double interpolateValue(TreeMap<LocalDateTime, Consumption> data, Configuration configuration, LocalDateTime key) {
        double sum = 0, weightSum = 0;
        for (ConfigurationInterval interval : configuration.getNeighborIntervals()) {
            sum += interpolateInterval(data, key, interval.getNeighbors(), interval.isNeighborsWeighted(), interval.getInterval());
            weightSum += interval.getWeight();
        }
        return (weightSum > 0) ? (sum / weightSum) : sum;
    }

    private double interpolateInterval(TreeMap<LocalDateTime, Consumption> data, LocalDateTime key, int neighbors, boolean weighted, int interval) {
        long diffInSeconds = Math.abs(Duration.between(key.plusSeconds(interval), key).getSeconds());
        double weightedCount = 0, sum = 0;
        LocalDateTime left = key, right = key;
        for (int i = 0; i < neighbors; i++) {
            left = left.minusSeconds(diffInSeconds);
            right = right.plusSeconds(diffInSeconds);
            double elementWeight = weighted ? (1 / (2D * i)) : 1;
            if (data.containsKey(left)) {
                weightedCount += elementWeight;
                sum += data.get(left).getValue();
            }
            if (data.containsKey(right)) {
                weightedCount += elementWeight;
                sum += data.get(right).getValue();
            }
        }
        return (weightedCount > 0) ? (sum / weightedCount) : 0;
    }

    public static class Optimizer {
        private List<Integer> intervals;
        private int minNeighbors;
        private int maxNeighbors;
        private double minWeight;
        private double maxWeight;
        private double weightStep;

        private Configuration currentConfiguration;
        private Configuration bestConfiguration;
        private double bestScore = Double.MAX_VALUE;

        public Optimizer(int interval, List<Integer> intervals, int minNeighbors, int maxNeighbors, double minWeight, double maxWeight, double weightStep) {
            this.intervals = intervals;
            this.minNeighbors = minNeighbors;
            this.maxNeighbors = maxNeighbors;
            this.minWeight = minWeight;
            this.maxWeight = maxWeight;
            this.weightStep = weightStep;
            ArrayList<ConfigurationInterval> localIntervals = new ArrayList<>();
            for (Integer adjuster : intervals) {
                localIntervals.add(new ConfigurationInterval(minNeighbors, adjuster, false, minWeight));
            }
            this.currentConfiguration = new Configuration(interval, localIntervals);
        }

        public Configuration optimize(TreeMap<LocalDateTime, Consumption> original, TreeMap<LocalDateTime, Consumption> data) {
            return optimize(original, data, 0);
        }

        private Configuration optimize(TreeMap<LocalDateTime, Consumption> original, TreeMap<LocalDateTime, Consumption> data, int currentIndex) {
            ConfigurationInterval currentInterval = this.currentConfiguration.getNeighborIntervals().get(currentIndex);
            for (int n = minNeighbors; n <= maxNeighbors; n++) {
                currentInterval.neighbors = n;
                double weight = minWeight;
                while (weight <= maxWeight) {
                    currentInterval.weight = weight;
                    currentInterval.neighborsWeighted = false;
                    if (currentIndex >= (this.intervals.size() - 1)) {
                        evaluate(original, data);
                    } else {
                        optimize(original, data, currentIndex + 1);
                    }
                    currentInterval.neighborsWeighted = true;
                    if (currentIndex >= (this.intervals.size() - 1)) {
                        evaluate(original, data);
                    } else {
                        optimize(original, data, currentIndex + 1);
                    }
                    weight += weightStep;
                }
            }
            return bestConfiguration;
        }

        private void evaluate(TreeMap<LocalDateTime, Consumption> original, TreeMap<LocalDateTime, Consumption> data) {
            TreeMap<LocalDateTime, Consumption> interpolated = new Averaging().interpolate(data, this.currentConfiguration);
            double score = Rating.calculateDifference(original, interpolated);
            System.out.println("score: " + score + " with config: " + currentConfiguration);
            if (score < bestScore) {
                bestScore = score;
                bestConfiguration = this.currentConfiguration.copy();
            }
        }
    }

    public static class Configuration extends Algorithm.Configuration {
        private List<ConfigurationInterval> neighborIntervals;

        public Configuration(int interval, List<ConfigurationInterval> neighborIntervals) {
            super(interval);
            this.neighborIntervals = neighborIntervals;
        }

        public List<ConfigurationInterval> getNeighborIntervals() {
            return neighborIntervals;
        }

        public Configuration copy() {
            ArrayList<ConfigurationInterval> intervals = new ArrayList<>();
            for (ConfigurationInterval interval : this.getNeighborIntervals()) {
                intervals.add(new ConfigurationInterval(interval.getNeighbors(), interval.getInterval(), interval.isNeighborsWeighted(), interval.getWeight()));
            }
            return new Configuration(this.getInterval(), intervals);
        }

        @Override
        public String toString() {
            StringBuilder intervals = new StringBuilder();
            for (ConfigurationInterval interval : getNeighborIntervals()) {
                if (intervals.length() != 0) {
                    intervals.append(", ");
                }
                intervals.append(interval);
            }
            return "Configuration(interval= " + this.getInterval() + ", intervals=[" + intervals + "])";
        }
    }

    public static class ConfigurationInterval {
        private int neighbors; // for every direction
        private boolean neighborsWeighted; // weight neighbors by distance (eg. 0.24 - 0.33 - 0.5 - ELEMENT - 0.5 - 0.33 - 0.25)
        private int interval; // interval in seconds
        private double weight; // weight for this value

        public ConfigurationInterval(int neighbors, int interval, boolean neighborsWeighted, double weight) {
            this.neighbors = neighbors;
            this.interval = interval;
            this.neighborsWeighted = neighborsWeighted;
            this.weight = weight;
        }

        public ConfigurationInterval(int neighbors, int interval) {
            this.neighbors = neighbors;
            this.interval = interval;
        }

        public int getNeighbors() {
            return neighbors;
        }

        public int getInterval() {
            return interval;
        }

        public boolean isNeighborsWeighted() {
            return neighborsWeighted;
        }

        public double getWeight() {
            return weight;
        }

        @Override
        public String toString() {
            return "ConfigurationInterval{" +
                    "neighbors=" + neighbors +
                    ", neighborsWeighted=" + neighborsWeighted +
                    ", interval=" + interval + " seconds" +
                    ", weight=" + weight +
                    '}';
        }
    }
}
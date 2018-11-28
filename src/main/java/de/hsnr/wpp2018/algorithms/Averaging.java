package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.Algorithm;
import de.hsnr.wpp2018.RangeAdjuster;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TreeMap;

public class Averaging implements Algorithm<Averaging.Configuration> {

    public TreeMap<LocalDateTime, Double> interpolate(TreeMap<LocalDateTime, Double> data, Configuration configuration) {
        TreeMap<LocalDateTime, Double> results = new TreeMap<>();
        LocalDateTime time = data.firstKey(), end = data.lastKey();
        while (!time.isAfter(end)) {
            results.put(time, data.getOrDefault(time, interpolateValue(data, configuration, time)));
            time = time.plusSeconds(configuration.getInterval());
        }
        return results;
    }

    private double interpolateValue(TreeMap<LocalDateTime, Double> data, Configuration configuration, LocalDateTime key) {
        double sum = 0, weightSum = 0;
        for (ConfigurationInterval interval : configuration.getNeighborIntervals()) {
            sum += interpolateInterval(data, key, interval.getNeighbors(), interval.isNeighborsWeighted(), interval.getAdjuster());
            weightSum += interval.getWeight();
        }
        return (weightSum > 0) ? (sum / weightSum) : sum;
    }

    private double interpolateInterval(TreeMap<LocalDateTime, Double> data, LocalDateTime key, int neighbors, boolean weighted, RangeAdjuster adjuster) {
        long diffInSeconds = Math.abs(Duration.between(adjuster.nextRange(key), key).getSeconds());
        double weightedCount = 0, sum = 0;
        LocalDateTime left = key, right = key;
        for (int i = 0; i < neighbors; i++) {
            left = left.minusSeconds(diffInSeconds);
            right = right.plusSeconds(diffInSeconds);
            double elementWeight = weighted ? (1 / (2D * i)) : 1;
            if (data.containsKey(left)) {
                weightedCount += elementWeight;
                sum += data.get(left);
            }
            if (data.containsKey(right)) {
                weightedCount += elementWeight;
                sum += data.get(right);
            }
        }
        return (weightedCount > 0) ? (sum / weightedCount) : 0;
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
    }

    public static class ConfigurationInterval {
        private int neighbors; // for every direction
        private boolean neighborsWeighted; // weight neighbors by distance (eg. 0.24 - 0.33 - 0.5 - ELEMENT - 0.5 - 0.33 - 0.25)
        private RangeAdjuster adjuster;
        private double weight; // weight for this value

        public ConfigurationInterval(int neighbors, RangeAdjuster adjuster, boolean neighborsWeighted, double weight) {
            this.neighbors = neighbors;
            this.adjuster= adjuster;
            this.neighborsWeighted = neighborsWeighted;
            this.weight = weight;
        }

        public ConfigurationInterval(int neighbors, RangeAdjuster adjuster) {
            this.neighbors = neighbors;
            this.adjuster = adjuster;
        }

        public int getNeighbors() {
            return neighbors;
        }

        public RangeAdjuster getAdjuster() {
            return adjuster;
        }

        public boolean isNeighborsWeighted() {
            return neighborsWeighted;
        }

        public double getWeight() {
            return weight;
        }
    }
}
package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.base.Algorithm;
import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.base.ParserException;
import de.hsnr.wpp2018.base.ParserHelper;
import de.hsnr.wpp2018.evaluation.Rating;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Averaging algorithm
 */
public class Averaging implements Algorithm<Averaging.Configuration> {
    public static final String NAME = "averaging";

    /**
     * {@inheritDoc}
     */
    public TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, Configuration configuration) {
        TreeMap<LocalDateTime, Consumption> results = new TreeMap<>();
        // get user defined times or use data start and end
        LocalDateTime time = configuration.hasStart() ? configuration.getStart() : data.firstKey();
        LocalDateTime end = configuration.hasEnd() ? configuration.getEnd() : data.lastKey();
        while (!time.isAfter(end)) {
            results.put(time, data.getOrDefault(time, new Consumption(interpolateValue(data, configuration, time), true)));
            time = time.plusSeconds(configuration.getInterval());
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationExplanation() {
        return "interval=<int>;{start=<date>;end=<date>;}neighbors={<neighbors|int>:<interval|int)>:<neighborsWeighted|boolean>:<weight|double>}[sep=,]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, Map<String, String> configuration) throws ParserException {
        List<ConfigurationInterval> neighbors = new ArrayList<>();
        for (String neighbor : ParserHelper.getString(configuration, "neighbors").split("[,]")) {
            String[] parts = neighbor.split("[:]");
            if (parts.length != 4) {
                throw new ParserException("neighbor needs to have four parts");
            }
            neighbors.add(new ConfigurationInterval(
                    ParserHelper.getInteger(parts[0]),
                    ParserHelper.getInteger(parts[1]),
                    ParserHelper.getBoolean(parts[2]),
                    ParserHelper.getDouble(parts[3])
            ));
        }
        return interpolate(data, new Configuration(Algorithm.Configuration.parse(configuration), neighbors));
    }

    /**
     * Interpolation for a single value inside the provided data identified by the key
     *
     * @param data          source data
     * @param configuration algorithm configuration
     * @param key           element key
     * @return interpolated value for the specified key
     */
    private double interpolateValue(TreeMap<LocalDateTime, Consumption> data, Configuration configuration, LocalDateTime key) {
        double sum = 0, weightSum = 0;
        for (ConfigurationInterval interval : configuration.getNeighborIntervals()) {
            sum += interpolateInterval(data, key, interval.getNeighbors(), interval.isNeighborsWeighted(), interval.getInterval());
            weightSum += interval.getWeight();
        }
        return (weightSum > 0) ? (sum / weightSum) : sum;
    }

    /**
     * Part of the interpolation for a single value. This method interpolates one interval configuration
     *
     * @param data      source data
     * @param key       element key
     * @param neighbors number of neighbors to use for interpolation
     * @param weighted  interpolate weighted - see {@link ConfigurationInterval#neighborsWeighted} for explanation
     * @param interval  interval - number of seconds between two elements
     * @return interpolated value
     */
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

    /**
     * Extended averaging configuration additionally additionally containing the configuration of neighbours to use
     */
    public static class Configuration extends Algorithm.Configuration {
        private List<ConfigurationInterval> neighborIntervals;

        public Configuration(int interval, List<ConfigurationInterval> neighborIntervals) {
            super(interval);
            this.neighborIntervals = neighborIntervals;
        }

        public Configuration(int interval, LocalDateTime start, LocalDateTime end, List<ConfigurationInterval> neighborIntervals) {
            super(interval, start, end);
            this.neighborIntervals = neighborIntervals;
        }

        public Configuration(Algorithm.Configuration base, List<ConfigurationInterval> neighborIntervals) {
            super(base.getInterval(), base.getStart(), base.getEnd());
            this.neighborIntervals = neighborIntervals;
        }

        public List<ConfigurationInterval> getNeighborIntervals() {
            return neighborIntervals;
        }

        /**
         * Copy this configuration into a new and independent object
         *
         * @return copy not referencing any internal pointers
         */
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

    /**
     * Single configuration interval for one type of distance between elements to interpolate
     */
    public static class ConfigurationInterval {
        private int neighbors; // for every direction
        private boolean neighborsWeighted; // weight neighbors by distance (eg. 0.25 - 0.33 - 0.5 - ELEMENT - 0.5 - 0.33 - 0.25)
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

    /**
     * Simple attempt of an optimiser that tries every configuration possible within a defined boundary
     */
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

        /**
         * Entry point for optimisation. Returns the best configuration
         *
         * @param original original data
         * @param data     data to be interpolated
         * @return best configuration for given tuple of complete and fragmentary data
         */
        public Configuration optimize(TreeMap<LocalDateTime, Consumption> original, TreeMap<LocalDateTime, Consumption> data) {
            return optimize(original, data, 0);
        }

        /**
         * Optimisation step
         *
         * @param original     original data
         * @param data         data to be interpolated
         * @param currentIndex current ascending index controlling the adjustments to be made
         * @return best configuration for given tuple of complete and fragmentary data
         */
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

        /**
         * Evaluate score for the given tuple of complete and fragmentary data. Updates the object member containing the best configuration if the score is better than all generated before
         *
         * @param original original complete data
         * @param data     data to be interpolated
         */
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
}
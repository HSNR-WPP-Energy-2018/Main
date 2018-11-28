package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.Algorithm;

import java.time.LocalDateTime;
import java.util.TreeMap;

public class Averaging implements Algorithm {

    public TreeMap<LocalDateTime, Double> interpolate(TreeMap<LocalDateTime, Double> data, Algorithm.Configuration configuration) throws ConfigurationException {
        if (!(configuration instanceof Configuration)) {
            throw new ConfigurationException(Configuration.class);
        }
        Configuration config = (Configuration) configuration;
        TreeMap<LocalDateTime, Double> results = new TreeMap<>();
        LocalDateTime time = data.firstKey(), end = data.lastKey();
        while (!time.isAfter(end)) {
            results.put(time, data.getOrDefault(time, interpolateValue(data, time, configuration.getInterval(), config.getNeighbors())));
            time = time.plusSeconds(configuration.getInterval());
        }
        return results;
    }

    private double interpolateValue(TreeMap<LocalDateTime, Double> data, LocalDateTime key, int interval, int neighbors/* TODO: more configuration parameter */) {
        int found = 0;
        double sum = 0;
        LocalDateTime time = key.minusSeconds(interval);
        for (int i = 0; i < neighbors; i++) {
            if (data.containsKey(time)) {
                sum += data.get(time);
                found++;
            }
            time = time.minusSeconds(interval);
        }
        time = key.minusSeconds(interval);
        for (int i = 0; i < neighbors; i++) {
            if (data.containsKey(time)) {
                sum += data.get(time);
                found++;
            }
            time = time.plusSeconds(interval);
        }
        return (found == 0) ? 0 : sum / found;
    }

    public static class Configuration extends Algorithm.Configuration {
        private int neighbors;

        public Configuration(int interval, int neighbors) {
            super(interval);
            this.neighbors = neighbors;
        }

        public int getNeighbors() {
            return neighbors;
        }
    }
}
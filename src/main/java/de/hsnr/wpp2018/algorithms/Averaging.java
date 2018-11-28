package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.Algorithm;

import java.time.LocalDateTime;
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
        int found = 0;
        double sum = 0;
        LocalDateTime time = key.minusSeconds(configuration.getInterval());
        for (int i = 0; i < configuration.getNeighbors(); i++) {
            if (data.containsKey(time)) {
                sum += data.get(time);
                found++;
            }
            time = time.minusSeconds(configuration.getInterval());
        }
        time = key.minusSeconds(configuration.getInterval());
        for (int i = 0; i < configuration.getNeighbors(); i++) {
            if (data.containsKey(time)) {
                sum += data.get(time);
                found++;
            }
            time = time.plusSeconds(configuration.getInterval());
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
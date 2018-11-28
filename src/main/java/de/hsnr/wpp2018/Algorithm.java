package de.hsnr.wpp2018;

import java.time.LocalDateTime;
import java.util.TreeMap;

public interface Algorithm<T extends Algorithm.Configuration> {

    TreeMap<LocalDateTime, Double> interpolate(TreeMap<LocalDateTime, Double> data, T configuration);

    class Configuration {
        private int interval;

        public Configuration(int interval) {
            this.interval = interval;
        }

        public int getInterval() {
            return interval;
        }
    }
}
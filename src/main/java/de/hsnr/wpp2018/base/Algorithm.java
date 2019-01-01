package de.hsnr.wpp2018.base;

import java.time.LocalDateTime;
import java.util.TreeMap;

public interface Algorithm<T extends Algorithm.Configuration> {

    TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, T configuration);

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
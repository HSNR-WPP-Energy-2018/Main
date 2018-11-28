package de.hsnr.wpp2018;

import java.time.LocalDateTime;
import java.util.TreeMap;

public interface Algorithm {

    TreeMap<LocalDateTime, Double> interpolate(TreeMap<LocalDateTime, Double> data, Configuration configuration) throws ConfigurationException;

    class Configuration {
        private int interval;

        public Configuration(int interval) {
            this.interval = interval;
        }

        public int getInterval() {
            return interval;
        }
    }

    class ConfigurationException extends Exception {

        public ConfigurationException(Class clazz) {
            super("instance of " + clazz + " required");
        }
    }
}

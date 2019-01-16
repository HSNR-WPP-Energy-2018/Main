package de.hsnr.wpp2018.base;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

public interface Algorithm<T extends Algorithm.Configuration> {

    TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, T configuration);

    String getConfigurationExplanation();

    TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, Map<String, String> configuration) throws ParserException;

    class Configuration {

        public static Configuration parse(Map<String, String> configuration) throws ParserException {
            int interval = ParserHelper.getInteger(configuration, "interval", 0);
            LocalDateTime start = configuration.containsKey("start") ? ParserHelper.getDate(configuration, "start") : null;
            LocalDateTime end = configuration.containsKey("end") ? ParserHelper.getDate(configuration, "end") : null;
            return new Configuration(interval, start, end);
        }

        private int interval;
        private LocalDateTime start;
        private LocalDateTime end;

        public Configuration(int interval) {
            this.interval = interval;
        }

        public Configuration(int interval, LocalDateTime start, LocalDateTime end) {
            this.interval = interval;
            this.start = start;
            this.end = end;
        }

        public int getInterval() {
            return interval;
        }

        public boolean hasStart() {
            return start != null;
        }

        public LocalDateTime getStart() {
            return start;
        }

        public boolean hasEnd() {
            return end != null;
        }

        public LocalDateTime getEnd() {
            return end;
        }
    }
}
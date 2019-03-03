package de.hsnr.wpp2018.base;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

/**
 * Base class for all interpolation algorithm. This interface defines a mutual accessor for all algorithms. This way all of them can be accessed the same way
 *
 * @param <T> Configuration class derived from {@link Configuration}
 */
public interface Algorithm<T extends Algorithm.Configuration> {

    /**
     * Interpolation with configuration class
     *
     * @param data          data to be interpolated
     * @param configuration algorithm configuration
     * @return interpolated result data
     */
    TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, T configuration);

    /**
     * Explanation of the syntax required for the configuration
     *
     * @return explanation string
     */
    String getConfigurationExplanation();

    /**
     * Interpolation with configuration map consisting of string keys and string values
     *
     * @param data          data to be interpolated
     * @param configuration map of configuration parameter
     * @return interpolated result data
     * @throws ParserException on malformed configuration map
     */
    TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, Map<String, String> configuration) throws ParserException;

    /**
     * Basic configuration class containing the most basic configuration elements required by every algorithm
     */
    class Configuration {
        /**
         * Parsing a configuration string into a configuration object
         *
         * @param configuration configuration map
         * @return instantiated configuration
         * @throws ParserException on malformed configuration map
         */
        public static Configuration parse(Map<String, String> configuration) throws ParserException {
            int interval = ParserHelper.getInteger(configuration, "interval", 0);
            LocalDateTime start = configuration.containsKey("start") ? ParserHelper.getDate(configuration, "start") : null;
            LocalDateTime end = configuration.containsKey("end") ? ParserHelper.getDate(configuration, "end") : null;
            return new Configuration(interval, start, end);
        }

        private int interval;
        private LocalDateTime start;
        private LocalDateTime end;

        /**
         * Constructor without user defined dates. Instead they will be derived from the input data start and end
         *
         * @param interval distance between two elements
         */
        public Configuration(int interval) {
            this.interval = interval;
        }

        /**
         * Constructor with user defined date
         *
         * @param interval distance between two elements
         * @param start    start time
         * @param end      end time
         */
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
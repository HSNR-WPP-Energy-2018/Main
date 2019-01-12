package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.base.Algorithm;
import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.base.ParserException;
import de.hsnr.wpp2018.database.Database;
import de.hsnr.wpp2018.database.Descriptor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DatabaseInterface implements Algorithm<DatabaseInterface.Configuration> {
    public static final String NAME = "database";

    @Override
    public TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, Configuration configuration) {
        LocalDateTime start = configuration.hasStart() ? configuration.getStart() : data.firstKey();
        LocalDateTime end = configuration.hasEnd() ? configuration.getEnd() : data.lastKey();
        return configuration.getDatabase().interpolate(configuration.getDescriptors(), start, end, configuration.getInterval(), data);
    }

    @Override
    public String getConfigurationExplanation() {
        return "interval=<int>;{start=<date>;end=<date>;}database=<folder path|string>;descriptors=";
    }

    @Override
    public TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, Map<String, String> configuration) throws ParserException {
        Database database = new Database();
        // TODO: read database
        List<Descriptor> descriptors = new ArrayList<>();
        // TODO: fill descriptors
        return interpolate(data, new Configuration(Algorithm.Configuration.parse(configuration), database, descriptors));
    }

    public static class Configuration extends Algorithm.Configuration {

        private Database database;
        private List<Descriptor> descriptors;

        public Configuration(int interval, Database database, List<Descriptor> descriptors) {
            super(interval);
            this.database = database;
            this.descriptors = descriptors;
        }

        public Configuration(Algorithm.Configuration base, Database database, List<Descriptor> descriptors) {
            super(base.getInterval(), base.getStart(), base.getEnd());
            this.database = database;
            this.descriptors = descriptors;
        }

        public Configuration(int interval, LocalDateTime start, LocalDateTime end, Database database, List<Descriptor> descriptors) {
            super(interval, start, end);
            this.database = database;
            this.descriptors = descriptors;
        }

        public Database getDatabase() {
            return database;
        }

        public List<Descriptor> getDescriptors() {
            return descriptors;
        }
    }
}

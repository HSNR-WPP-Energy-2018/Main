package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.base.Algorithm;
import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.base.ParserException;
import de.hsnr.wpp2018.base.ParserHelper;
import de.hsnr.wpp2018.database.Database;
import de.hsnr.wpp2018.database.Descriptor;
import de.hsnr.wpp2018.database.Element;
import de.hsnr.wpp2018.io.database.Parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Database interpolation algorithm interface
 */
public class DatabaseInterface implements Algorithm<DatabaseInterface.Configuration> {
    public static final String NAME = "database";

    /**
     * {@inheritDoc}
     */
    @Override
    public TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, Configuration configuration) {
        LocalDateTime start = configuration.hasStart() ? configuration.getStart() : data.firstKey();
        LocalDateTime end = configuration.hasEnd() ? configuration.getEnd() : data.lastKey();
        return configuration.getDatabase().interpolate(configuration.getDescriptors(), start, end, configuration.getInterval(), data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationExplanation() {
        return "interval=<int>;{start=<date>;end=<date>;}database=<folder path|string>;descriptors=<descriptor strings, connected by \"/\">";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, Map<String, String> configuration) throws ParserException {
        Database database = new Database();
        File folder = new File(ParserHelper.getString(configuration, "database"));
        if (!folder.exists() || !folder.isDirectory()) {
            throw new ParserException("database path not found or is not a folder");
        }
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isFile() && file.getName().endsWith("." + Element.EXTENSION)) {
                try {
                    database.addElement(Parser.parse(file));
                } catch (FileNotFoundException ignored) {}
            }
        }
        List<Descriptor> descriptors = new ArrayList<>();
        for (String descriptor : ParserHelper.getString(configuration, "descriptors").split("[/]")) {
            descriptors.add(Descriptor.parse(descriptor));
        }
        return interpolate(data, new Configuration(Algorithm.Configuration.parse(configuration), database, descriptors));
    }

    /**
     * Extended database configuration containing a reference to the database and a list of descriptors for the dataset to be interpolated
     */
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

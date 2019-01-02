package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.base.Algorithm;
import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.database.Database;
import de.hsnr.wpp2018.database.Descriptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.TreeMap;

public class DatabaseInterface implements Algorithm<DatabaseInterface.Configuration> {

    private Database database;

    public DatabaseInterface(Database database) {
        this.database = database;
    }

    public Database getDatabase() {
        return database;
    }

    @Override
    public TreeMap<LocalDateTime, Consumption> interpolate(TreeMap<LocalDateTime, Consumption> data, Configuration configuration) {
        return this.getDatabase().interpolate(configuration.getDescriptors(), data);
    }

    public static class Configuration extends Algorithm.Configuration {

        private List<Descriptor> descriptors;

        public Configuration(int interval, List<Descriptor> descriptors) {
            super(interval);
            this.descriptors = descriptors;
        }

        public List<Descriptor> getDescriptors() {
            return descriptors;
        }
    }
}

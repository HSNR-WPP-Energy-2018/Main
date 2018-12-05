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

    class Household {
        //hier kann dann evtl auch später eine Info über das Berufsbild oder das Reiseverhalten der Personen ein, sodass man zB weiß, ob sie in den Ferien oft zu Hause sind
        private int numberOfPersons;
        private double livingSpace;

        public Household(int numberOfPersons, double livingSpace)
        {
            this.numberOfPersons = numberOfPersons;
            this.livingSpace = livingSpace;
        }

        public int getNumberOfPersons() {
            return numberOfPersons;
        }

        public double getLivingSpace() {
            return livingSpace;
        }
    }
}
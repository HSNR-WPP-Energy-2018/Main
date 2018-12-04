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
        private int number_of_persons;
        private double living_space;

        public Household(int number_of_persons, double living_space)
        {
            this.number_of_persons = number_of_persons;
            this.living_space = living_space;
        }

        public int getNumber_of_persons() {
            return number_of_persons;
        }

        public double getLiving_space() {
            return living_space;
        }
    }
}
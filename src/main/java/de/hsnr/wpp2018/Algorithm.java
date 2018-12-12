package de.hsnr.wpp2018;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.TreeMap;

public interface Algorithm<T extends Algorithm.Configuration> {


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


    class Consumption {
       // private TreeMap<LocalDateTime, Double> energyData;

        private LocalDateTime time;
        private Double energyData;
        private boolean isInterpolated;

        public Consumption(LocalDateTime time, double energyData, boolean isInterpolated)
        {
            this.time = time;
            this.energyData = energyData;
            this.isInterpolated = isInterpolated;
        }

        public LocalDateTime getTime() {
            return time;
        }

        public Double getEnergyData() {
            return energyData;
        }

        public boolean isInterpolated() {
            return isInterpolated;
        }


    }
}

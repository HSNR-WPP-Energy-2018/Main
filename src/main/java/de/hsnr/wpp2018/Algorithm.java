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

        public void setEnergyData(Double energyData) {
            this.energyData = energyData;
        }

        public Double getEnergyData() {
            return energyData;
        }

        public boolean isInterpolated() {
            return isInterpolated;
        }


    }


    class Wastings {

        private double processHeating;
        private double processCooling;
        private double ICT;
        private double warmWater;
        private double illumination;
        private double heating;
        private double mechanicalEquip;

        public double getProcessHeating() {
            return processHeating;
        }

        public double getProcessCooling() {
            return processCooling;
        }

        public double getICT() {
            return ICT;
        }

        public double getWarmWater() {
            return warmWater;
        }

        public double getIllumination() {
            return illumination;
        }

        public double getHeating() {
            return heating;
        }

        public double getMechanicalEquip() {
            return mechanicalEquip;
        }

        //Prozentualer Verbrauchsanteil der Haushaltsgeräte
        public Wastings(double waste)
        {
            this.processHeating = waste * 30 / 100; //Prozesswärme
            this.processCooling = waste * 23 / 100; //Prozesskälte
            this.ICT = waste * 17 / 100; //IuK-Systeme
            this.warmWater = waste * 12 / 100; //Warmwasseraufbereitung
            this.illumination = waste * 8 / 100; //Beleuchtung
            this.heating = waste * 7 / 100; //Heizung
            this.mechanicalEquip = waste * 3 / 100; //Mechanische Geräte
        }

    }
}

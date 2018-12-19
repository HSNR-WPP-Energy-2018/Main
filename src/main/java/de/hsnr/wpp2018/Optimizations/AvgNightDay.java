package de.hsnr.wpp2018.Optimizations;

import de.hsnr.wpp2018.Algorithm;
import de.hsnr.wpp2018.Heuristics;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

public class AvgNightDay {


    public static void dailyWaste(ArrayList<Algorithm.Consumption> newdata, Heuristics.Household household) {
        double meanDaily = Heuristics.average_waste_per_day(household);
        double meanHourly = meanDaily / 60;

        Heuristics.Wastings wastings = new Heuristics.Wastings(meanHourly);


    }


    public static void nocturnalWaste(ArrayList<Algorithm.Consumption> newdata, Heuristics.Household household) {
        double meanDaily = Heuristics.average_waste_per_day(household);
        double meanHourly = meanDaily / 60;
        Heuristics.Wastings wastings = new Heuristics.Wastings(meanHourly);
        double minTolerance = wastings.getProcessCooling();
        double maxTolerance = wastings.getHeating() + wastings.getProcessCooling() + (wastings.getICT()/ 2);
        double avgNight = wastings.getHeating() + wastings.getProcessCooling();

        LocalTime weekdayNightBegin = LocalTime.of(23, 00); //evtl aufpassen, falls Heuristik auf 23 Uhr gestellt wird -> betrachtet anderen Tag
        LocalTime weekdayNightEnd = LocalTime.of(07, 00);
        LocalTime weekendNightBegin = LocalTime.of(00, 00);
        LocalTime weekendNightEnd = LocalTime.of(9, 00);

        for (int i = 0; i < newdata.size(); i++) {
            LocalDateTime today = newdata.get(i).getTime();
            //Verbrauch nachts an Werktagen
            if (Heuristics.isBusinessDay(newdata.get(i).getTime())) {
                if (!newdata.get(i).isInterpolated() && today.toLocalTime().isAfter(weekdayNightBegin) || today.toLocalTime().isBefore(weekdayNightEnd)) {
                    if (newdata.get(i).getEnergyData() < minTolerance || newdata.get(i).getEnergyData() > maxTolerance) {
                        newdata.get(i).setEnergyData(avgNight);
                    }
                }
            }
            //Verbrauch nachts an Wochenenden
            else {
                if (!newdata.get(i).isInterpolated() && today.toLocalTime().isAfter(weekendNightBegin) || today.toLocalTime().isBefore(weekendNightEnd)) {
                    if (newdata.get(i).getEnergyData() < minTolerance || newdata.get(i).getEnergyData() > maxTolerance) {
                        newdata.get(i).setEnergyData(avgNight);
                    }
                }
            }
        }
    }


    public static class Household extends Algorithm.Household {

        public Household(int number_of_persons, double living_space) {
            super(number_of_persons, living_space);
        }

    }

    public static class Wastings extends Algorithm.Wastings {

        public Wastings(double wasteVal) {
            super(wasteVal);
        }

    }
}

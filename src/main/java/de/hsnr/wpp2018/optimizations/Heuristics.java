package de.hsnr.wpp2018.optimizations;

import de.hsnr.wpp2018.Algorithm;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;

//Eine Sammlung von Heuristiken, die in den Optimierungsmethoden aufgerufen werden können
public class Heuristics {




    public static double average_waste_per_day(Heuristics.Household household) {
        int persons = household.getNumberOfPersons();
        double avg_waste = 0;

        switch (persons) {
            //Quelle: Stadtwerke Neuss
            case 1: //1500 kwH pro Jahr
                avg_waste = 4.11;
                break;
            case 2: //2200 kwH pro Jahr
                avg_waste = 6.03;
                break;
            case 3: //3000 kwH pro Jahr
                avg_waste = 8.22;
            case 4: //3800 kwH pro Jahr
                avg_waste = 10.41;
            case 5: //5000 kwH pro Jahr
                avg_waste = 13.70;
        }
        return avg_waste;
    }

    public static boolean isBusinessDay(LocalDateTime day) {
        DayOfWeek weekday = day.getDayOfWeek();
        if (!weekday.equals(DayOfWeek.SATURDAY) && !weekday.equals(DayOfWeek.SUNDAY)) {
            return true;
        } else {
            return false;
        }
    }


    public static double minDailyConsumption(double value) {
        Wastings wastings = new Wastings(value);
        value = wastings.getProcessCooling(); //Sei Minimalverbrauch pro Tag lediglich durch Kühlgeräte bestimmt (Person ist nicht zu Hause etc.)
        return value;
    }


    /*Wenn ein Wert unrealistisch hoch ist, dann wird (sofern es sich hier um Wochentage handelt), dieser ignoriert und mit einem
      Differenzwert aufgefüllt, der nötig wäre, um auf den Verbrauchswert vom Vortag zu kommen (sofern positiv)
    */
    public static double yesterdayDiff(LocalDateTime today, ArrayList<Algorithm.Consumption> newdata) {

        LocalDateTime dayStart = today.minusDays(1);
        LocalDateTime yesterdayEnd = dayStart.minusMinutes(15);
        LocalDateTime yesterdayStart = yesterdayEnd.minusDays(1);
        double diff = 0;

        if (isBusinessDay(today) && isBusinessDay(dayStart)) {
            double energyYesterday = 0;
            double energyToday = 0;
            for (int i = 0; i < newdata.size(); i++) {
                //Wenn die Zeit im Intervall von [Anfang_Gestern, Ende_Gestern] liegt
                if (newdata.get(i).getTime().isAfter(yesterdayStart.minusMinutes(15)) && newdata.get(i).getTime().isBefore(dayStart)) {
                    energyYesterday += newdata.get(i).getEnergyData();
                }
                //Wenn die Zeit im Intervall von [Anfang Heute, VOR aktuell betrachtetem zu hohen Wert] liegt
                else if (newdata.get(i).getTime().isAfter(dayStart) && newdata.get(i).getTime().isBefore(today)) {
                    energyToday += newdata.get(i).getEnergyData();
                }
            }
            /*
            Überlegung (hoffe, dass sie Sinn ergibt):
            Hier habe ich nicht abs() genommen, denn wenn energyToday auch ohne den zu hohen Peak nennenswert höher ist als der avg-Verbrauch vom gestrigen Tag,
            ist es mMn sinnlos, anstatt des Peaks eine Differenz draufzurechnen, die auch sehr groß sein kann
            -> Heuristik wende ich später also nur an, wenn diff>0 (Denn das bedeutet, dass energyYesterday höher als Today ohne Betrachtung des Peaks ist
            */
            diff = energyYesterday - energyToday;
        }
        return diff;
    }


    public static ArrayList<Algorithm.Consumption> useHeuristics(ArrayList<Algorithm.Consumption> newdata, Household household) {
        double dailyAvgWaste = average_waste_per_day(household);
        for (int i = 0; i < newdata.size(); i++) {
            //Durchschnittlichen nächtlichen Verbrauchswert für alle UNinterpolierten Werte sammeln

            if (newdata.get(i).getEnergyData() <= 0 && newdata.get(i).isInterpolated()) {
                newdata.get(i).setEnergyData(minDailyConsumption(dailyAvgWaste));
            } else if (newdata.get(i).getEnergyData() > dailyAvgWaste && newdata.get(i).isInterpolated()) {
                double diffFromYesterday = Heuristics.yesterdayDiff(newdata.get(i).getTime(), newdata);
                if (diffFromYesterday >= 0) {
                    newdata.get(i).setEnergyData(diffFromYesterday);
                }
            }
        }
        return newdata;

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

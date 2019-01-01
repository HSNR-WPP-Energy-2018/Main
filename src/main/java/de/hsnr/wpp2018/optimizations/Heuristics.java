package de.hsnr.wpp2018.optimizations;

import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.base.Household;
import de.hsnr.wpp2018.base.WastingData;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.TreeMap;

//Eine Sammlung von Heuristiken, die in den Optimierungsmethoden aufgerufen werden können
public class Heuristics {

    public static double averageWastePerDay(Household household) {
        int persons = household.getNumberOfPersons();
        switch (persons) {
            //Quelle: Stadtwerke Neuss
            case 1: //1500 kwH pro Jahr
                return 4.11;
            case 2: //2200 kwH pro Jahr
                return 6.03;
            case 3: //3000 kwH pro Jahr
                return 8.22;
            case 4: //3800 kwH pro Jahr
                return 10.41;
            case 5: //5000 kwH pro Jahr
                return 13.70;
            default:
                return 0;
        }
    }

    public static boolean isBusinessDay(LocalDateTime day) {
        DayOfWeek weekday = day.getDayOfWeek();
        return !weekday.equals(DayOfWeek.SATURDAY) && !weekday.equals(DayOfWeek.SUNDAY);
    }

    public static double minDailyConsumption(double value) {
        WastingData WastingData = new WastingData(value);
        value = WastingData.getProcessCooling(); //Sei Minimalverbrauch pro Tag lediglich durch Kühlgeräte bestimmt (Person ist nicht zu Hause etc.)
        return value;
    }

    /*Wenn ein Wert unrealistisch hoch ist, dann wird (sofern es sich hier um Wochentage handelt), dieser ignoriert und mit einem
      Differenzwert aufgefüllt, der nötig wäre, um auf den Verbrauchswert vom Vortag zu kommen (sofern positiv)
    */
    public static double yesterdayDiff(LocalDateTime today, TreeMap<LocalDateTime, Consumption> newData) {
        LocalDateTime dayStart = today.minusDays(1);
        LocalDateTime yesterdayEnd = dayStart.minusMinutes(15);
        LocalDateTime yesterdayStart = yesterdayEnd.minusDays(1);
        double diff = 0;

        if (isBusinessDay(today) && isBusinessDay(dayStart)) {
            double energyYesterday = 0;
            double energyToday = 0;
            for (LocalDateTime key : newData.keySet()) {
                //Wenn die Zeit im Intervall von [Anfang_Gestern, Ende_Gestern] liegt
                if (key.isAfter(yesterdayStart.minusMinutes(15)) && key.isBefore(dayStart)) {
                    energyYesterday += newData.get(key).getValue();
                }
                //Wenn die Zeit im Intervall von [Anfang Heute, VOR aktuell betrachtetem zu hohen Wert] liegt
                else if (key.isAfter(dayStart) && key.isBefore(today)) {
                    energyToday += newData.get(key).getValue();
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

    public static TreeMap<LocalDateTime, Consumption> useHeuristics(TreeMap<LocalDateTime, Consumption> newData, Household household) {
        double dailyAvgWaste = averageWastePerDay(household);
        for (LocalDateTime key : newData.keySet()) {
            //Durchschnittlichen nächtlichen Verbrauchswert für alle UNinterpolierten Werte sammeln

            if (newData.get(key).getValue() <= 0 && newData.get(key).isInterpolated()) {
                newData.get(key).setValue(minDailyConsumption(dailyAvgWaste));
            } else if (newData.get(key).getValue() > dailyAvgWaste && newData.get(key).isInterpolated()) {
                double diffFromYesterday = Heuristics.yesterdayDiff(key, newData);
                if (diffFromYesterday >= 0) {
                    newData.get(key).setValue(diffFromYesterday);
                }
            }
        }
        return newData;
    }
}
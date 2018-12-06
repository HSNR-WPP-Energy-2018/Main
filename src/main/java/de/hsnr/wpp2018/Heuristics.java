package de.hsnr.wpp2018;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;


public class Heuristics {

    public static double castNegativesToZero(double value)
    {
        value = 0.0;
        return value;
    }



    public static boolean isBusinessDay(LocalDateTime day)
    {
        DayOfWeek weekday = day.getDayOfWeek();
        if (!weekday.equals(DayOfWeek.SATURDAY) && !weekday.equals(DayOfWeek.SUNDAY)) {
            return true;
        }
        else {
            return false;
        }
    }


    public static double average_waste_per_day(Heuristics.Household household)
    {
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

    public static TreeMap<LocalDateTime,Double> seasonalConsumption(TreeMap<LocalDateTime,Double> newdata)
    {
        return newdata;
    }


    /*Wenn ein Wert unrealistisch hoch ist, dann wird (sofern es sich hier um Wochentage handelt), dieser ignoriert und mit einem
      Differenzwert aufgefüllt, der nötig wäre, um auf den Verbrauchswert vom Vortag zu kommen (sofern positiv)
    */
    public static double yesterdayDiff(LocalDateTime key, LocalDateTime dayStart, LocalDateTime yesterdayEnd, LocalDateTime yesterdayStart, TreeMap<LocalDateTime, Double> newdata)
    {
        double diff = 0;
        if(isBusinessDay(key) && isBusinessDay(dayStart)) {
            double energyYesterday = 0;
            for (LocalDateTime i = yesterdayStart; i.isBefore(dayStart); i = i.plusMinutes(15)) {
                if (newdata.get(i) != null) //Warum sind da überhaupt noch null-Werte drin? Muss ich mal prüfen
                {
                    energyYesterday += newdata.get(i);
                }
            }
            double energy_today = 0;
            for (LocalDateTime i = dayStart; i.isBefore(key); i = i.plusMinutes(15)) {
                if (newdata.get(i) != null) {
                    energy_today += newdata.get(i);
                }
            }
            diff = energyYesterday - energy_today;
        }
        return diff;
    }



    public static TreeMap<LocalDateTime, Double> useHeuristics(TreeMap<LocalDateTime, Double> newdata, Heuristics.Household household)
    {
        AtomicInteger counter = new AtomicInteger();
        double dailyAvgWaste = Heuristics.average_waste_per_day(household);

        newdata.forEach((key, value) ->
        {
            if (value > dailyAvgWaste)
            {
                LocalDateTime dayStart = key.minusDays(1);
                LocalDateTime yesterdayEnd = dayStart.minusMinutes(15);
                LocalDateTime yesterdayStart = yesterdayEnd.minusDays(1);
                double diffFromYesterday = Heuristics.yesterdayDiff(key, dayStart, yesterdayEnd, yesterdayStart, newdata);
                if (diffFromYesterday >= 0)
                    {
                    newdata.put(key, diffFromYesterday);
                    }

            }
            counter.getAndIncrement();
        });
        return newdata;
    }


    public static class Household extends Algorithm.Household {

        public Household(int number_of_persons, double living_space) {
            super(number_of_persons, living_space);
        }

    }
}

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

    //check whether abnormally high consumption is normal on that day
    public static boolean isNationalHoliday(LocalDateTime date)
    {
        boolean isTrue = false;
        Month month = date.getMonth();
        int dayOfMonth  = date.getDayOfMonth();
        if (month.equals(Month.DECEMBER) && dayOfMonth == 24)
        {
            isTrue = !isTrue;
        }
        return isTrue;
    }

    public static boolean isBusinessDay(LocalDateTime day)
    {
        boolean isTrue = false;
        DayOfWeek weekday = day.getDayOfWeek();
        if (!weekday.equals(DayOfWeek.SATURDAY) && !weekday.equals(DayOfWeek.SUNDAY))
        {
            isTrue = !isTrue;
        }
        return isTrue;
    }


    public static double average_waste_per_day(Heuristics.Household household)
    {
        int persons = household.getNumber_of_persons();
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
    public static double yesterdayDiff(LocalDateTime key, LocalDateTime day_start, LocalDateTime yesterday_end, LocalDateTime yesterday_start, TreeMap<LocalDateTime, Double> newdata)
    {
        double diff = 0;
        if(isBusinessDay(key) && isBusinessDay(day_start)) {
            double energy_yesterday = 0;
            for (LocalDateTime i = yesterday_start; i.isBefore(day_start); i = i.plusMinutes(15)) {
                if (newdata.get(i) != null) //Warum sind da überhaupt noch null-Werte drin? Muss ich mal prüfen
                {
                    energy_yesterday += newdata.get(i);
                }
            }
            double energy_today = 0;
            for (LocalDateTime i = day_start; i.isBefore(key); i = i.plusMinutes(15)) {
                if (newdata.get(i) != null) {
                    energy_today += newdata.get(i);
                }
            }
            diff = energy_yesterday - energy_today;
        }
        return diff;
    }



    public static TreeMap<LocalDateTime, Double> useHeuristics(TreeMap<LocalDateTime, Double> newdata, Heuristics.Household household)
    {
        AtomicInteger counter = new AtomicInteger();
        double daily_avg_waste = Heuristics.average_waste_per_day(household);

        newdata.forEach((key, value) ->
        {
            if (value > daily_avg_waste)
            {
                //Wenn es sich um einen Feiertag handelt, ist imo eine separate Heuristik notwendig
                boolean check_holiday = Heuristics.isNationalHoliday(key);
                System.out.println(check_holiday + " + " + value);
                if (!check_holiday)
                {
                LocalDateTime day_start = key.minusDays(1);
                LocalDateTime yesterday_end = day_start.minusMinutes(15);
                LocalDateTime yesterday_start = yesterday_end.minusDays(1);
                double diff_from_yesterday = Heuristics.yesterdayDiff(key, day_start, yesterday_end, yesterday_start, newdata);
                if (diff_from_yesterday >= 0)
                    {
                    newdata.put(key, diff_from_yesterday);
                    }
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

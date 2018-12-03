package de.hsnr.wpp2018;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
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
        boolean isTrue = false;
        DayOfWeek weekday = day.getDayOfWeek();
        if (!weekday.equals(DayOfWeek.SATURDAY) && !weekday.equals(DayOfWeek.SUNDAY))
        {
            isTrue = !isTrue;
        }
        return isTrue;
    }

    /*Wenn ein Wert unrealistisch hoch ist, dann wird (sofern es sich hier um Wochentage handelt), dieser ignoriert und mit einem
      Differenzwert aufgefüllt, der nötig wäre, um auf den Verbrauchswert vom Vortag zu kommen (sofern positiv)
    */
    public static TreeMap<LocalDateTime, Double> interpolateOverDays(TreeMap<LocalDateTime, Double> newdata)
    {

        AtomicInteger counter = new AtomicInteger();
        newdata.forEach((key, value) -> {
            if (value>10)
            {
                LocalDateTime day_start = key.minusDays(1);
                LocalDateTime yesterday_end = day_start.minusMinutes(15);
                LocalDateTime yesterday_start = yesterday_end.minusDays(1);
                if(isBusinessDay(key) && isBusinessDay(day_start))
                {
                    double energy_yesterday = 0;
                    for (LocalDateTime i = yesterday_start; i.isBefore(day_start); i = i.plusMinutes(15))
                    {
                        if (newdata.get(i) != null) //Warum sind da überhaupt noch null-Werte drin? Muss ich mal prüfen
                        {
                            energy_yesterday +=  newdata.get(i);
                        }
                    }
                    System.out.println(energy_yesterday);
                    double energy_today = 0;
                    for (LocalDateTime i = day_start; i.isBefore(key); i = i.plusMinutes(15))
                    {
                        if (newdata.get(i) != null)
                        {
                            energy_today +=  newdata.get(i);
                        }
                    }
                    double diff_from_yesterday = energy_yesterday-energy_today;
                    if (diff_from_yesterday>=0)
                    {
                        newdata.put(key, diff_from_yesterday);
                    }

                }


            }
            counter.getAndIncrement();
        });


        return newdata;
    }
}

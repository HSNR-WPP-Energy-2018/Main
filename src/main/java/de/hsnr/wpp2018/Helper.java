package de.hsnr.wpp2018;

import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.base.Household;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.TreeMap;

public class Helper {



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

    public static long getDistance(LocalDateTime one, LocalDateTime two) {
        Duration duration = Duration.between(one, two);
        return Math.abs(duration.toMinutes() * 60);
    }

    public static double roundDouble(double value, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(value * scale) / scale;
    }
}
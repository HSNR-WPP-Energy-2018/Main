package de.hsnr.wpp2018.base;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * General helper class providing support functions as regards to content tasks
 */
public class Helper {

    /**
     * Determine the average waste per day for a given household configuration
     *
     * @param household household configuration
     * @return average waste per day
     */
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

    /**
     * Determines if an date is a business day
     *
     * @param day input date
     * @return business day determination
     */
    public static boolean isBusinessDay(LocalDateTime day) {
        DayOfWeek weekday = day.getDayOfWeek();
        return !weekday.equals(DayOfWeek.SATURDAY) && !weekday.equals(DayOfWeek.SUNDAY);
    }

    /**
     * Get distance in seconds between to dates (with time)
     *
     * @param one first time
     * @param two second time
     * @return distance in second
     */
    public static long getDistance(LocalDateTime one, LocalDateTime two) {
        Duration duration = Duration.between(one, two);
        return Math.abs(duration.toMinutes() * 60);
    }

    /**
     * Round a double to the given precision
     *
     * @param value    double value
     * @param decimals precision
     * @return rounded value
     */
    public static double roundDouble(double value, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(value * scale) / scale;
    }
}
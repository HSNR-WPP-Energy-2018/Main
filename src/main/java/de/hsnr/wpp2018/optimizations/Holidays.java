package de.hsnr.wpp2018.optimizations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Holidays helper for heuristics
 */
public class Holidays {

    private static ArrayList<LocalDate> holidayArray = new ArrayList<>();

    /**
     * This method is called from other algorithms in order to verify if the actual date is a holiday
     */

    /**
     *
     * @param today current date
     * @return is holiday?
     */
    public static boolean checkHoliday(LocalDate today) {
        for (LocalDate thisDate : holidayArray) {
            if (thisDate.getMonth().equals(today.getMonth()) && thisDate.getDayOfMonth() == today.getDayOfMonth()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Scans input file from the directory (\Main\src\main\resources) and determines the holidays contained in the calendar
     * Required format: iCal
     */

    public static void scanFile() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classloader.getResourceAsStream("feiertage_nordrhein-westfalen_2019.ics");


        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = null;
            while ((line = br.readLine()) != null) {

                if (line.contains("SUMMARY")) {
                    String[] parts1 = line.split("=|\\;|\\:");
                }
                if (line.contains("DTSTART")) {
                    String[] parts2 = line.split("=|\\;|\\:");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                    LocalDate holidayDate = LocalDate.parse(parts2[3], formatter);
                    holidayArray.add(holidayDate);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
package de.hsnr.wpp2018.optimizations;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public class Holidays {

    private static ArrayList<LocalDate> holidayArray = new ArrayList<>();

    public static boolean checkHoliday(LocalDate today)
    {
        for (LocalDate thisDate : holidayArray)
        {
            if (thisDate.getMonth().equals(today.getMonth()) && thisDate.getDayOfMonth() == today.getDayOfMonth())
            {
                return true;
            }
        }
        return false;
    }


    public static void scanFile() {

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classloader.getResourceAsStream("feiertage_2019.ics");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = null;
            while((line = br.readLine()) != null) {
                if (line.contains("DTSTART"))
                {
                    String[] parts = line.split("=|\\;|\\:");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                    LocalDate holidayDate = LocalDate.parse(parts[3], formatter);
                    holidayArray.add(holidayDate);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

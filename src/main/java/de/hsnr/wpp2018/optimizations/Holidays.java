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

        /*Feiertage, die auf einen Samstag oder Sonntag fallen, werden sowieso mit Wochenendheuristik bearbeitet
        * & es werden nur die Feiertage beachtet, die in einigen oder vielen Bundesländern als Feiertag gelten*/
        String[] ignoreDates = {"Neujahr","Tag der Deutschen Einheit", "Silvester", "Allerheiligen",
                "Karfreitag", "Ostermontag", "Tag der Arbeit", "Christi Himmelfahrt", "Pfingstmontag",
                "Heiligabend", "Fronleichnam", "1. Weihnachtsfeiertag", "2. Weihnachtsfeiertag"};

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = null;
            boolean noWorkingDay = false;
            while((line = br.readLine()) != null) {

                if (line.contains("SUMMARY"))
                {
                    noWorkingDay = false;
                    String[] parts1 = line.split("=|\\;|\\:");
                    for (String s : ignoreDates) {
                        if (parts1[1].equals(s)) {
                            noWorkingDay = true;
                        }
                    }

                }

                if (line.contains("DTSTART") && noWorkingDay)
                {
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

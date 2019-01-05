package de.hsnr.wpp2018.optimizations;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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


    public static void scanFile(String federalState) {

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classloader.getResourceAsStream("feiertage_2019.ics");

        /*Feiertage, die auf einen Samstag oder Sonntag fallen, werden sowieso mit Wochenendheuristik bearbeitet
        * & es werden nur die Feiertage beachtet, die in einigen oder vielen Bundesländern als Feiertag gelten*/
        String[] publicHolidays = {"Neujahr","Karfreitag", "Ostermontag", "Tag der Arbeit",
                "Christi Himmelfahrt", "Pfingstmontag", "Tag der Deutschen Einheit", "1. Weihnachtsfeiertag", "2. Weihnachtsfeiertag"};

        ArrayList<String> ignoreDates = new ArrayList<>();
        ignoreDates.addAll(Arrays.asList(publicHolidays));

        List<String> additionalDays;
        switch(federalState)
        {
            case "Baden-Württemberg":
                additionalDays = Arrays.asList("Allerheiligen", "Heilige Drei Könige", "Fronleichnam");
                ignoreDates.addAll(additionalDays);
                break;
            case "Bayern":
                additionalDays = Arrays.asList("Allerheiligen", "Augsburger Friedensfest", "Fronleichnam", "Heilige Drei Könige", "Mariä Himmelfahrt");
                ignoreDates.addAll(additionalDays);
                break;
            case "Berlin":
                additionalDays = Arrays.asList("Internationaler Frauentag"); //gilt ab 2019
                ignoreDates.addAll(additionalDays);
                break;
            case "Brandenburg":
            case "Bremen":
            case "Hamburg":
            case "Mecklenburg-Vorpommern":
            case "Niedersachsen":
            case "Schleswig-Holstein":
                additionalDays = Arrays.asList("Reformationstag");
                ignoreDates.addAll(additionalDays);
                break;
            case "Hessen":
                additionalDays = Arrays.asList("Fronleichnam");
                ignoreDates.addAll(additionalDays);
                break;
            case "Nordrhein-Westfahlen":
            case "NRW": //falls im Aufruf abgekürzt
            case "Rheinland-Pfalz":
            additionalDays = Arrays.asList("Allerheiligen", "Fronleichnam");
            ignoreDates.addAll(additionalDays);
            break;
            case "Saarland":
                additionalDays = Arrays.asList("Allerheiligen", "Fronleichnam", "Mariä Himmelfahrt");
                ignoreDates.addAll(additionalDays);
                break;
            case "Sachsen":
                additionalDays = Arrays.asList("Reformationstag", "Fronleichnam", "Buß- und Bettag");
                ignoreDates.addAll(additionalDays);
                break;
            case "Sachsen-Anhalt":
                additionalDays = Arrays.asList("Heilige Drei Könige", "Reformationstag");
                ignoreDates.addAll(additionalDays);
                break;
            case "Thüringen":
                additionalDays = Arrays.asList("Reformationstag", "Fronleichnam");
                ignoreDates.addAll(additionalDays);
                break;
            default:
                System.out.println("no match");
        }


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
                            System.out.println(parts1[1]);
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

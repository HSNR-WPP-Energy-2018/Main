package de.hsnr.wpp2018;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.ArrayList;
import java.util.TreeMap;

public class Heuristics {

    private double processHeating;
    private double processCooling;
    private double ICT;
    private double warmWater;
    private double illumination;
    private double heating;
    private double mechanicalEquip;

    //Prozentualer Verbrauchsanteil der Haushaltsgeräte
    public Heuristics(double waste) {
        this.processHeating = waste * 30 / 100; //Prozesswärme
        this.processCooling = waste * 23 / 100; //Prozesskälte
        this.ICT = waste * 17 / 100; //IuK-Systeme
        this.warmWater = waste * 12 / 100; //Warmwasseraufbereitung
        this.illumination = waste * 8 / 100; //Beleuchtung
        this.heating = waste * 7 / 100; //Heizung
        this.mechanicalEquip = waste * 3 / 100; //Mechanische Geräte
    }

    public static double average_waste_per_day(Heuristics.Household household) {
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

    public static boolean isBusinessDay(LocalDateTime day) {
        DayOfWeek weekday = day.getDayOfWeek();
        if (!weekday.equals(DayOfWeek.SATURDAY) && !weekday.equals(DayOfWeek.SUNDAY)) {
            return true;
        } else {
            return false;
        }
    }


    //Evtl mergen mit UseHeuristics, sodass wir aus AlgorithmTest nur eine Methode aus der Klasse aufrufen müssen?
    public static void nocturnalWaste(ArrayList<Algorithm.Consumption> newdata, Heuristics.Household household) {
        double meanDaily = average_waste_per_day(household);
        double meanHourly = meanDaily / 60;
        Heuristics devices = new Heuristics(meanHourly);
        double minTolerance = devices.processCooling;
        double maxTolerance = devices.heating + devices.processCooling + (devices.ICT / 2);
        double avgNight = devices.heating + devices.processCooling;

        LocalTime weekdayNightBegin = LocalTime.of(23, 00); //evtl aufpassen, falls Heuristik auf 23 Uhr gestellt wird -> betrachtet anderen Tag
        LocalTime weekdayNightEnd = LocalTime.of(07, 00);
        LocalTime weekendNightBegin = LocalTime.of(00, 00);
        LocalTime weekendNightEnd = LocalTime.of(9, 00);

        for (int i = 0; i < newdata.size(); i++) {
            LocalDateTime today = newdata.get(i).getTime();
            //Verbrauch nachts an Werktagen
            if (isBusinessDay(newdata.get(i).getTime())) {
                if (!newdata.get(i).isInterpolated() && today.toLocalTime().isAfter(weekdayNightBegin) || today.toLocalTime().isBefore(weekdayNightEnd)) {
                    if (newdata.get(i).getEnergyData() < minTolerance || newdata.get(i).getEnergyData() > maxTolerance) {
                        newdata.get(i).setEnergyData(avgNight);
                    }
                }
            }
            //Verbrauch nachts an Wochenenden
            else {
                if (!newdata.get(i).isInterpolated() && today.toLocalTime().isAfter(weekendNightBegin) || today.toLocalTime().isBefore(weekendNightEnd)) {
                    if (newdata.get(i).getEnergyData() < minTolerance || newdata.get(i).getEnergyData() > maxTolerance) {
                        newdata.get(i).setEnergyData(avgNight);
                    }
                }
            }
        }
    }


    public static double minDailyConsumption(double value) {
        Heuristics heuristics = new Heuristics(value);
        value = heuristics.processCooling; //Sei Minimalverbrauch pro Tag lediglich durch Kühlgeräte bestimmt (Person ist nicht zu Hause etc.)
        return value;
    }


    public static TreeMap<LocalDateTime, Double> seasonalConsumption(TreeMap<LocalDateTime, Double> newdata) {
        return newdata;
    }


    /*Wenn ein Wert unrealistisch hoch ist, dann wird (sofern es sich hier um Wochentage handelt), dieser ignoriert und mit einem
      Differenzwert aufgefüllt, der nötig wäre, um auf den Verbrauchswert vom Vortag zu kommen (sofern positiv)
    */
    public static double yesterdayDiff(LocalDateTime today, ArrayList<Algorithm.Consumption> newdata) {

        LocalDateTime dayStart = today.minusDays(1);
        LocalDateTime yesterdayEnd = dayStart.minusMinutes(15);
        LocalDateTime yesterdayStart = yesterdayEnd.minusDays(1);
        double diff = 0;

        if (isBusinessDay(today) && isBusinessDay(dayStart)) {
            double energyYesterday = 0;
            double energyToday = 0;
            for (int i = 0; i < newdata.size(); i++) {
                //Wenn die Zeit im Intervall von [Anfang_Gestern, Ende_Gestern] liegt
                if (newdata.get(i).getTime().isAfter(yesterdayStart.minusMinutes(15)) && newdata.get(i).getTime().isBefore(dayStart)) {
                    energyYesterday += newdata.get(i).getEnergyData();
                }
                //Wenn die Zeit im Intervall von [Anfang Heute, VOR aktuell betrachtetem zu hohen Wert] liegt
                else if (newdata.get(i).getTime().isAfter(dayStart) && newdata.get(i).getTime().isBefore(today)) {
                    energyToday += newdata.get(i).getEnergyData();
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


    public static ArrayList<Algorithm.Consumption> useHeuristics(ArrayList<Algorithm.Consumption> newdata, Household household) {
        double dailyAvgWaste = average_waste_per_day(household);
        for (int i = 0; i < newdata.size(); i++) {
            //Durchschnittlichen nächtlichen Verbrauchswert für alle UNinterpolierten Werte sammeln

            if (newdata.get(i).getEnergyData() <= 0 && newdata.get(i).isInterpolated()) {
                newdata.get(i).setEnergyData(minDailyConsumption(dailyAvgWaste));
            } else if (newdata.get(i).getEnergyData() > dailyAvgWaste && newdata.get(i).isInterpolated()) {
                double diffFromYesterday = Heuristics.yesterdayDiff(newdata.get(i).getTime(), newdata);
                if (diffFromYesterday >= 0) {
                    newdata.get(i).setEnergyData(diffFromYesterday);
                }
            }
        }
        return newdata;

    }


    public static class Household extends Algorithm.Household {

        public Household(int number_of_persons, double living_space) {
            super(number_of_persons, living_space);
        }

    }
}

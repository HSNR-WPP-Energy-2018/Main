package de.hsnr.wpp2018;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
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
    public Heuristics(double waste)
    {
        this.processHeating = waste*30/100; //Prozesswärme
        this.processCooling = waste*23/100; //Prozesskälte
        this.ICT = waste*17/100; //IuK-Systeme
        this.warmWater = waste*12/100; //Warmwasseraufbereitung
        this.illumination = waste*8/100; //Beleuchtung
        this.heating = waste*7/100; //Heizung
        this.mechanicalEquip = waste*3/100; //Mechanische Geräte
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


    public static void nocturnalWaste(Heuristics.Household household)
    {
        double meanDaily = average_waste_per_day(household);
        Heuristics heuristics = new Heuristics(meanDaily);
        //System.out.println(heuristics.heating + " test");

    }

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


    public static TreeMap<LocalDateTime,Double> seasonalConsumption(TreeMap<LocalDateTime,Double> newdata)
    {

        return newdata;
    }


    /*Wenn ein Wert unrealistisch hoch ist, dann wird (sofern es sich hier um Wochentage handelt), dieser ignoriert und mit einem
      Differenzwert aufgefüllt, der nötig wäre, um auf den Verbrauchswert vom Vortag zu kommen (sofern positiv)
    */
    public static double yesterdayDiff(LocalDateTime today, LocalDateTime dayStart, LocalDateTime yesterdayEnd, LocalDateTime yesterdayStart, ArrayList<Algorithm.Consumption> newdata)
    {
        double diff = 0;
        if(isBusinessDay(today) && isBusinessDay(dayStart)) {
            double energyYesterday = 0;
            double energyToday = 0;
            for (int i=0; i<newdata.size();i++)
            {
                //Wenn die Zeit im Intervall von [Anfang_Gestern, Ende_Gestern] liegt
                if (newdata.get(i).getTime().isAfter(yesterdayStart.minusMinutes(15)) && newdata.get(i).getTime().isBefore(dayStart))
                {
                    energyYesterday += newdata.get(i).getEnergyData();
                }
                //Wenn die Zeit im Intervall von [Anfang Heute, VOR aktuell betrachtetem zu hohen Wert] liegt
                else if(newdata.get(i).getTime().isAfter(dayStart) && newdata.get(i).getTime().isBefore(today))
                {
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



    public static ArrayList<Algorithm.Consumption> useHeuristics(ArrayList<Algorithm.Consumption> newdata, Household household)
    {

        double dailyAvgWaste = Heuristics.average_waste_per_day(household);

        for (int i=0; i<newdata.size();i++)
        {
            if (newdata.get(i).getEnergyData() > dailyAvgWaste && newdata.get(i).isInterpolated())
            {
                LocalDateTime today = newdata.get(i).getTime();
                LocalDateTime dayStart = today.minusDays(1);
                LocalDateTime yesterdayEnd = dayStart.minusMinutes(15);
                LocalDateTime yesterdayStart = yesterdayEnd.minusDays(1);
                double diffFromYesterday = Heuristics.yesterdayDiff(today, dayStart, yesterdayEnd, yesterdayStart, newdata);
                if (diffFromYesterday>=0)
                {
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

package de.hsnr.wpp2018.optimizations;

import de.hsnr.wpp2018.Algorithm;
import de.hsnr.wpp2018.Heuristics;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

public class AvgNightDay {


    public static ArrayList<Algorithm.Consumption> nightDayWaste(ArrayList<Algorithm.Consumption> newdata, Heuristics.Household household) {
        double meanDaily = Heuristics.average_waste_per_day(household);
        double meanHourly = meanDaily / 60;
        Heuristics.Wastings wastings = new Heuristics.Wastings(meanHourly); //stündlicher Verbrauch

        double minNightTolerance = wastings.getProcessCooling(); //fängt zusätzlich interpolierte Werte gleich oder unter Null ab
        double maxNightTolerance = wastings.getHeating() + wastings.getProcessCooling() + (wastings.getICT()/ 2);
        double avgNight = wastings.getHeating() + wastings.getProcessCooling();
        double avgMorning = avgNight + wastings.getIllumination() + (wastings.getWarmWater()/6); //Licht + 10 min Duschen
        double avgDay = wastings.getHeating() + wastings.getProcessCooling() + wastings.getICT() + wastings.getIllumination();

        LocalTime weekdayNightBegin = LocalTime.of(23, 00); //evtl aufpassen, falls Heuristik auf 23 Uhr gestellt wird -> betrachtet anderen Tag
        LocalTime weekdayNightEnd = LocalTime.of(07, 00);
        LocalTime weekendNightBegin = LocalTime.of(00, 00);
        LocalTime weekendNightEnd = LocalTime.of(9, 00);
        LocalTime workingTimeEnd = LocalTime.of(18, 00);


        for (int i = 0; i < newdata.size(); i++) {
            LocalDateTime today = newdata.get(i).getTime();

            if (newdata.get(i).isInterpolated())
            {
                if (Heuristics.isBusinessDay(newdata.get(i).getTime())) {
                    //Verbrauch nachts an Werktagen (hier muss ein "oder" hin, weil der Zähler nach 23 wieder auf 00 resettet wird
                    if (today.toLocalTime().isAfter(weekdayNightBegin) || today.toLocalTime().isBefore(weekdayNightEnd)) {
                        if (newdata.get(i).getEnergyData() < minNightTolerance || newdata.get(i).getEnergyData() > maxNightTolerance) {
                            newdata.get(i).setEnergyData(avgNight);
                        }
                    }
                    //Verbrauch morgens nach dem Aufstehen [ca. eine halbe Stunde zwischen Aufstehen und Haus-Verlassen]
                    else if (today.toLocalTime().isAfter(weekdayNightEnd.minusMinutes(15)) && today.toLocalTime().isBefore(weekdayNightEnd.plusMinutes(45)))
                    {
                        //Morgendl.Verbrauch > als maxNightTolerance
                        if (newdata.get(i).getEnergyData() < minNightTolerance || newdata.get(i).getEnergyData() > avgMorning) {
                            newdata.get(i).setEnergyData(avgMorning);
                        }
                    }
                    //Verbrauch an Werktagen, während Person außer Haus ist (sofern sie zur Schule/FH/Arbeit geht)
                    else if (today.toLocalTime().isAfter(weekdayNightEnd.plusMinutes(30)) && today.toLocalTime().isBefore(workingTimeEnd))
                    {
                        //idR sind nur die gleichen Geräte wie nachts an, ggf. ist zusätzlich Heizung aus
                        if (newdata.get(i).getEnergyData() < minNightTolerance || newdata.get(i).getEnergyData() > maxNightTolerance) {
                            newdata.get(i).setEnergyData(avgNight);
                        }
                    }
                    //Verbrauch nach Feierabend, ist grds. schwerer einzuschätzen, da hier meist mehr Peaks entstehen (nachträglich genauere Heuristiken einbauen)
                    else
                    {
                        //Hier wird davon ausgegangen, dass die Person nach Feierabend immer da ist
                        if (newdata.get(i).getEnergyData() < avgDay) {
                            newdata.get(i).setEnergyData(avgDay);
                        }
                        else if (newdata.get(i).getEnergyData() > meanHourly) //Hier könnte ein Peak sein
                        {
                            newdata.get(i).setEnergyData(meanHourly);
                        }
                    }
                }
                else
                {
                    //Verbrauch nachts an Wochenenden
                    if (today.toLocalTime().isAfter(weekendNightBegin) && today.toLocalTime().isBefore(weekendNightEnd)) {
                        if (newdata.get(i).getEnergyData() < minNightTolerance || newdata.get(i).getEnergyData() > maxNightTolerance) {
                            newdata.get(i).setEnergyData(avgNight);
                        }
                    }
                    else
                    {
                        //Hier wird davon ausgegangen, dass die Person am Wochenende auch mal weg ist (ähnl. Verbrauch wie nachts)
                        if (newdata.get(i).getEnergyData() < minNightTolerance) {
                            newdata.get(i).setEnergyData(minNightTolerance);
                        }
                        else if (newdata.get(i).getEnergyData() > meanHourly) //Hier könnte ein Peak sein
                        {
                            newdata.get(i).setEnergyData(meanHourly);
                        }
                    }
                }
            }
        }

        return newdata;

    }

}

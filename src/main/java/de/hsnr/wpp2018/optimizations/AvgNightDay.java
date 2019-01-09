package de.hsnr.wpp2018.optimizations;

import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.base.Household;
import de.hsnr.wpp2018.base.WastingData;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

public class AvgNightDay {

    public static void nightDayWaste(TreeMap<LocalDateTime, Consumption> data, Household household) {
        double meanDaily = Heuristics.averageWastePerDay(household);
        double meanHourly = meanDaily / 60;
        WastingData wastingData = new WastingData(meanHourly); //stündlicher Verbrauch

        /*
         (Quelle: VDE) Es wird geschätzt, dass ein solcher „Standby-Verbrauch“ rund 4 % der Bruttostromnachfrage
         in Deutschland (Betrachtungszeitraum: 2004 bis 2006) betrug
        */
        double minNightTolerance = meanHourly*4/100; //im Bestcase so ziemlich kein Verbrauch -> fängt interpolierte Werte gleich oder unter Null ab
        double maxNightTolerance = minNightTolerance + wastingData.getHeating() + (wastingData.getICT() / 2);
        double avgNight = minNightTolerance + wastingData.getHeating();
        double avgMorning = avgNight + wastingData.getIllumination() + (wastingData.getWarmWater() / 6); //Licht + 10 min Duschen
        double avgDay = wastingData.getHeating() + wastingData.getICT() + wastingData.getIllumination();

        LocalTime weekdayNightBegin = LocalTime.of(23, 0); //evtl aufpassen, falls Heuristik auf 23 Uhr gestellt wird -> betrachtet anderen Tag
        LocalTime weekdayNightEnd = LocalTime.of(7, 0);
        LocalTime weekendNightBegin = LocalTime.of(0, 0);
        LocalTime weekendNightEnd = LocalTime.of(9, 0);
        LocalTime workingTimeEnd = LocalTime.of(18, 0);

        for (LocalDateTime today : data.keySet()) {
            if (data.get(today).isInterpolated()) {

                boolean isHoliday = Holidays.checkHoliday(today.toLocalDate());

                if (Heuristics.isBusinessDay(today) && !isHoliday) {
                    //Verbrauch nachts an Werktagen (hier muss ein "oder" hin, weil der Zähler nach 23 wieder auf 00 resettet wird
                    if (today.toLocalTime().isAfter(weekdayNightBegin) || today.toLocalTime().isBefore(weekdayNightEnd)) {
                        if (data.get(today).getValue() < minNightTolerance || data.get(today).getValue() > maxNightTolerance) {
                            data.get(today).setValue(avgNight);
                        }
                    }
                    //Verbrauch morgens nach dem Aufstehen [ca. eine halbe Stunde zwischen Aufstehen und Haus-Verlassen]
                    else if (today.toLocalTime().isAfter(weekdayNightEnd.minusMinutes(15)) && today.toLocalTime().isBefore(weekdayNightEnd.plusMinutes(45))) {
                        //Morgendl.Verbrauch > als maxNightTolerance
                        if (data.get(today).getValue() < minNightTolerance || data.get(today).getValue() > avgMorning) {
                            data.get(today).setValue(avgMorning);
                        }
                    }
                    //Verbrauch an Werktagen, während Person außer Haus ist (sofern sie zur Schule/FH/Arbeit geht)
                    else if (today.toLocalTime().isAfter(weekdayNightEnd.plusMinutes(30)) && today.toLocalTime().isBefore(workingTimeEnd)) {
                        //idR sind nur die gleichen Geräte wie nachts an, ggf. ist zusätzlich Heizung aus
                        if (data.get(today).getValue() < minNightTolerance || data.get(today).getValue() > maxNightTolerance) {
                            data.get(today).setValue(avgNight);
                        }
                    }
                    //Verbrauch nach Feierabend, ist grds. schwerer einzuschätzen, da hier meist mehr Peaks entstehen (nachträglich genauere Heuristiken einbauen)
                    else {
                        //Hier wird davon ausgegangen, dass die Person nach Feierabend immer da ist
                        if (data.get(today).getValue() < avgDay) {
                            data.get(today).setValue(avgDay);
                        } else if (data.get(today).getValue() > meanHourly) //Hier könnte ein Peak sein
                        {
                            data.get(today).setValue(meanHourly);
                        }
                    }
                } else {
                    //Verbrauch nachts an Wochenenden
                    if (today.toLocalTime().isAfter(weekendNightBegin) && today.toLocalTime().isBefore(weekendNightEnd)) {
                        if (data.get(today).getValue() < minNightTolerance || data.get(today).getValue() > maxNightTolerance) {
                            data.get(today).setValue(avgNight);
                        }
                    } else {
                        //Hier wird davon ausgegangen, dass die Person am Wochenende auch mal weg ist (ähnl. Verbrauch wie nachts)
                        if (data.get(today).getValue() < minNightTolerance) {
                            data.get(today).setValue(minNightTolerance);
                        } else if (data.get(today).getValue() > meanHourly) //Hier könnte ein Peak sein
                        {
                            data.get(today).setValue(meanHourly);
                        }
                    }
                }
            }
        }

        //data.forEach((i) -> System.out.println("Time: " + i.getTime() + ". Value: " + i.getValue() + ". Interpolated? " + i.isInterpolated()));
    }
}
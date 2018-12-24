package de.hsnr.wpp2018.optimizations;

import de.hsnr.wpp2018.Algorithm;
import de.hsnr.wpp2018.Helper;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;


public class PatternRecognition {

    //Stärke der Heuristik: Geht mehr auf individuelles Profil ein, Schwäche: Ignoriert Wochentage <> Wochenenden
    public static ArrayList<Algorithm.Consumption> checkBehaviour(ArrayList<Algorithm.Consumption> newdata, int range, double rangeTolerance) {
        int decimals = 6; //Nachkommastellen zum Runden
        LinkedHashMap<TimeInterval, ArrayList<Double>> intervalWastings = new LinkedHashMap<>();
        LinkedHashMap<TimeInterval, Double> avgWastings = new LinkedHashMap<>();

        for (int i = 0; i < (24 / range); i++) {
            TimeInterval temp = TimeInterval.createRange(LocalTime.of((i * range), 00), LocalTime.of((i * range), 00).plusHours(2).plusMinutes(59));
            ArrayList<Double> tempList = new ArrayList<>();
            tempList.add(0.0);
            intervalWastings.put(temp, tempList);
        }


        for (int i = 0; i < newdata.size(); i++) {
            if (!newdata.get(i).isInterpolated()) {
                int finalI = i;
                intervalWastings.forEach((key, value) -> {
                    if (key.inRange(newdata.get(finalI).getTime().toLocalTime())) {
                        value.add(newdata.get(finalI).getEnergyData());
                        intervalWastings.put(key, value);
                    }
                });

            }
        }

        //Avg Energy Consumption für das jeweilige TimeInterval finden
        intervalWastings.forEach((key, value) -> {
            double sum = 0.0;
            int counter = 0;
            for (int i = 0; i < value.size(); i++) {
                sum += value.get(i);
                counter++;
            }
            if (counter != 0) {
                double avgWaste = sum / counter;
                avgWastings.put(key, avgWaste);
            } else {
                avgWastings.put(key, 0.0);
            }
        });

        //avgWastings.forEach((key, value) -> System.out.println("Intervall: " + key.getStarttime() + "," + key.getEndtime() + " Value: " + value));

        //Ermittelt durchschnittlichen Tagesverbrauch OHNE Haushalts-Heuristik von den Stadtwerken
        double meanDaily = 0.0;
        for (double value : avgWastings.values()) {
            meanDaily += value;
        }
        double meanRange = Double.valueOf(meanDaily / (24 / range));

        Heuristics.Wastings wastings = new Heuristics.Wastings(meanRange); //Verbrauch im Intervall
        //Evtl in eigene Methode outsourcen, weil genau die gleichen Variablen auch in AvgNightDay benutzt werden
        double minNightTolerance = wastings.getProcessCooling(); //fängt zusätzlich interpolierte Werte gleich oder unter Null ab
        double maxNightTolerance = wastings.getHeating() + wastings.getProcessCooling() + (wastings.getICT() / 2);
        double avgNight = wastings.getHeating() + wastings.getProcessCooling();
        double avgMorning = avgNight + wastings.getIllumination() + (wastings.getWarmWater() / 6); //Licht + 10 min Duschen
        double avgDay = wastings.getHeating() + wastings.getProcessCooling() + wastings.getICT() + wastings.getIllumination();

        //Leichte Abweichungen vom Mittelwert betrachten, da kaum ein interpolierter Wert = meanRange sein wird
        double meanRangeUpperBound = meanRange + (rangeTolerance * meanRange);
        double meanRangeLowerBound = meanRange - (rangeTolerance * meanRange);
        for (int i = 0; i < newdata.size(); i++) {
            LocalDateTime today = newdata.get(i).getTime();
            if (newdata.get(i).isInterpolated()) {
                int finalI = i;
                avgWastings.forEach((key, avgRangeValue) -> {
                    if (key.inRange(newdata.get(finalI).getTime().toLocalTime())) {
                        //Anhand der Verbrauchsdaten scheint die Person in dem Zeitintervall zu schlafen oder außer Haus zu sein
                        if (avgRangeValue < meanRangeLowerBound) {
                            if (newdata.get(finalI).getEnergyData() < minNightTolerance || newdata.get(finalI).getEnergyData() > maxNightTolerance) {
                                newdata.get(finalI).setEnergyData(avgNight);
                            }
                        }
                        //Person scheint in dem Zeitintervall viele Geräte zu benutzen
                        else if (avgRangeValue > meanRangeUpperBound) {
                            if (newdata.get(finalI).getEnergyData() < avgDay) {
                                newdata.get(finalI).setEnergyData(avgDay);
                            } else if (newdata.get(finalI).getEnergyData() > meanRange) {
                                newdata.get(finalI).setEnergyData(meanRange);
                            }
                        }
                        //Aktueller Intervallwert entspricht ca. globalem Mean Range -> Interpolierter Wert soll auch auf den Wert angepasst werden
                        else {
                            if (newdata.get(finalI).getEnergyData() > meanRangeUpperBound || newdata.get(finalI).getEnergyData() < meanRangeLowerBound)
                            {
                                newdata.get(finalI).setEnergyData(meanRange);
                            }
                        }
                    }
                });
            }
            newdata.get(i).setEnergyData(Helper.roundDouble(newdata.get(i).getEnergyData(),decimals));
        }

        return newdata;
    }
}


class TimeInterval {
    public LocalTime getStarttime() {
        return starttime;
    }

    private LocalTime starttime;

    public LocalTime getEndtime() {
        return endtime;
    }

    private LocalTime endtime;

    private TimeInterval(LocalTime starttime, LocalTime endtime) {
        this.starttime = starttime;
        this.endtime = endtime;
    }

    static TimeInterval createRange(LocalTime starttime, LocalTime endtime) {
        return new TimeInterval(starttime, endtime);
    }

    boolean inRange(LocalTime time) {
        return !time.isBefore(starttime) && time.isBefore(endtime);
    }
}
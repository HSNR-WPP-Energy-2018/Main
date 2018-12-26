package de.hsnr.wpp2018.optimizations;

import de.hsnr.wpp2018.Algorithm;
import de.hsnr.wpp2018.Helper;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;


public class PatternRecognition {


    public static double calcFromPattern(Algorithm.Consumption currentData, double avgRangeValue, double meanRange, double rangeTolerance) {
        Heuristics.Wastings wastings = new Heuristics.Wastings(meanRange);

        double minNightTolerance = meanRange * 4 / 100;
        double maxNightTolerance = minNightTolerance + wastings.getHeating() + (wastings.getICT() / 2);
        double avgNight = minNightTolerance + wastings.getHeating();
        double avgDay = wastings.getHeating() + wastings.getICT() + wastings.getIllumination();


        //Leichte Abweichungen in % vom Mittelwert betrachten, da kaum ein interpolierter Wert exakt = meanRange sein wird
        double meanRangeUpperBound = meanRange + (rangeTolerance * meanRange);
        double meanRangeLowerBound = meanRange - (rangeTolerance * meanRange);

        double result = 0.0;

        //Person scheint in dem Zeitintervall zu schlafen oder nicht da zu sein, weil avg-Wert dieses Intervalls merkbar unter dem Verbrauchsdurchschnitt liegt
        if (avgRangeValue < meanRangeLowerBound) {
            if (currentData.getEnergyData() < minNightTolerance || currentData.getEnergyData() > maxNightTolerance) {
                result = avgNight;
            }
        }
        //Person scheint in dem Zeitintervall viele Geräte zu benutzen, weil avg-Wert dieses Intervalls merkbar über dem Verbrauchsdurchschnitt liegt
        else if (avgRangeValue > meanRangeUpperBound) {
            if (currentData.getEnergyData() < avgDay) {
                result = avgDay;
            } else if (currentData.getEnergyData() > meanRange) {
                result = meanRange;
            }
        }
        //Aktueller Intervallwert entspricht ca. globalem Mean Range -> Interpolierter Wert soll auch auf den Wert angepasst werden, sofern nötig
        else {
            if (currentData.getEnergyData() > meanRangeUpperBound || currentData.getEnergyData() < meanRangeLowerBound) {
                result = meanRange;
            }
        }

        if (result==0.0)
        {
            result = currentData.getEnergyData();
        }
        return result;
    }

    //Stärke der Heuristik: Geht mehr auf individuelles Profil ein + erkennt zusätzlich nun Wochentage <> Wochenenden
    public static ArrayList<Algorithm.Consumption> checkBehaviour(ArrayList<Algorithm.Consumption> newdata, int range, double rangeTolerance) {
        int decimals = 6; //Nachkommastellen zum Runden

        HashMap<TimeInterval, ArrayList<Double>> intervalWastingsWeekday = new HashMap<>();
        HashMap<TimeInterval, ArrayList<Double>> intervalWastingsWeekend = new HashMap<>();
        HashMap<TimeInterval, Double> avgWastingsWeekday = new HashMap<>();
        HashMap<TimeInterval, Double> avgWastingsWeekend = new HashMap<>();

        for (int i = 0; i < (24 / range); i++) {
            TimeInterval temp = TimeInterval.createRange(LocalTime.of((i * range), 00), LocalTime.of((i * range), 00).plusHours(2).plusMinutes(59));
            TimeInterval temp2 = TimeInterval.createRange(LocalTime.of((i * range), 00), LocalTime.of((i * range), 00).plusHours(2).plusMinutes(59));
            ArrayList<Double> tempList = new ArrayList<>();
            tempList.add(0.0);
            intervalWastingsWeekday.put(temp, tempList);
            intervalWastingsWeekend.put(temp2, tempList);
        }


        for (int i = 0; i < newdata.size(); i++) {
            if (!newdata.get(i).isInterpolated()) {
                int finalI = i;
                if (Heuristics.isBusinessDay(newdata.get(i).getTime())) {

                    intervalWastingsWeekday.forEach((key, value) -> {
                        if (key.inRange(newdata.get(finalI).getTime().toLocalTime())) {
                            value.add(newdata.get(finalI).getEnergyData());
                            intervalWastingsWeekday.put(key, value);
                        }
                    });
                } else {
                    intervalWastingsWeekend.forEach((key, value) -> {
                        if (key.inRange(newdata.get(finalI).getTime().toLocalTime())) {
                            value.add(newdata.get(finalI).getEnergyData());
                            intervalWastingsWeekend.put(key, value);
                        }
                    });
                }

            }
        }

        //Avg Energy Consumption für das jeweilige TimeInterval finden
        intervalWastingsWeekday.forEach((key, value) -> {
            double sum = 0.0;
            int counter = 0;
            for (int i = 0; i < value.size(); i++) {
                sum += value.get(i);
                counter++;
            }
            if (counter != 0) {
                double avgWaste = sum / counter;
                avgWastingsWeekday.put(key, avgWaste);
            } else {
                avgWastingsWeekday.put(key, 0.0);
            }
        });

        intervalWastingsWeekend.forEach((TimeInterval key, ArrayList<Double> value) -> {
            double sum = 0.0;
            int counter = 0;
            for (int i = 0; i < value.size(); i++) {
                sum += value.get(i);
                counter++;
            }
            if (counter != 0) {
                double avgWaste = sum / counter;
                avgWastingsWeekend.put(key, avgWaste);
            } else {
                avgWastingsWeekend.put(key, 0.0);
            }
        });


        //Ermittelt durchschnittlichen Tagesverbrauch OHNE Haushalts-Heuristik von den Stadtwerken
        double meanDailyWeekday = 0.0;
        for (double value : avgWastingsWeekday.values()) {
            meanDailyWeekday += value;
        }
        double meanRangeWeekday = Double.valueOf(meanDailyWeekday / (24 / range));

        double meanDailyWeekend = 0.0;
        for (double value : avgWastingsWeekend.values()) {
            meanDailyWeekend += value;
        }
        double meanRangeWeekend = Double.valueOf(meanDailyWeekend / (24 / range));


        for (int i = 0; i < newdata.size(); i++) {
            if (newdata.get(i).isInterpolated()) {
                int finalI = i;
                AtomicReference<Double> result = new AtomicReference<>(0.0);
                if (Heuristics.isBusinessDay(newdata.get(i).getTime())) {
                    avgWastingsWeekday.forEach((key, avgRangeValue) -> {
                        if (key.inRange(newdata.get(finalI).getTime().toLocalTime())) {
                            result.set(calcFromPattern(newdata.get(finalI), avgRangeValue, meanRangeWeekday, rangeTolerance));
                            newdata.get(finalI).setEnergyData(Double.valueOf(result.toString()));
                        }

                    });
                } else {
                    avgWastingsWeekend.forEach((key, avgRangeValue) -> {
                        if (key.inRange(newdata.get(finalI).getTime().toLocalTime())) {
                            result.set(calcFromPattern(newdata.get(finalI), avgRangeValue, meanRangeWeekend, rangeTolerance));
                            newdata.get(finalI).setEnergyData(Double.valueOf(result.toString()));
                        }

                    });
                }

                newdata.get(i).setEnergyData(Helper.roundDouble(newdata.get(i).getEnergyData(), decimals));
            }

        }
        newdata.forEach((i) -> System.out.println("Time: " + i.getTime() + ". Value: " + i.getEnergyData() + ". Interpolated? " + i.isInterpolated()));
        return newdata;
    }
}


class TimeInterval {
    public LocalTime getStarttime() {
        return starttime;
    }

    public LocalTime getEndtime() {
        return endtime;
    }

    private LocalTime starttime;

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
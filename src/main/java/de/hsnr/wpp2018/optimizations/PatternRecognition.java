package de.hsnr.wpp2018.optimizations;

import de.hsnr.wpp2018.Helper;
import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.base.TimeInterval;
import de.hsnr.wpp2018.base.WastingData;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import static de.hsnr.wpp2018.Helper.isBusinessDay;

public class PatternRecognition {

    public static double calcFromPattern(Consumption currentData, double avgRangeValue, double meanRange, double rangeTolerance) {
        WastingData wastingData = new WastingData(meanRange);

        double minNightTolerance = meanRange * 4 / 100;
        double maxNightTolerance = minNightTolerance + wastingData.getHeating() + (wastingData.getICT() / 2);
        //double avgNight = minNightTolerance + wastingData.getHeating();
        double avgDay = wastingData.getHeating() + wastingData.getICT() + wastingData.getIllumination();
        double peak = avgDay + wastingData.getProcessCooling() + wastingData.getProcessHeating() + wastingData.getWarmWater();


        //Leichte Abweichungen in % vom Mittelwert betrachten, da kaum ein interpolierter Wert exakt = meanRange sein wird
        double meanRangeUpperBound = meanRange + (rangeTolerance * meanRange);
        double meanRangeLowerBound = meanRange - (rangeTolerance * meanRange);

        double result = 0.0;

        //Person scheint in dem Zeitintervall zu schlafen oder nicht da zu sein, weil avg-Wert dieses Intervalls merkbar unter dem Verbrauchsdurchschnitt liegt
        if (avgRangeValue < meanRangeLowerBound) {
            if (currentData.getValue() < minNightTolerance) {
                result = minNightTolerance;
            }
            else if (currentData.getValue() > maxNightTolerance) {
                result = maxNightTolerance;
            }
        }
        //Person scheint in dem Zeitintervall viele Geräte zu benutzen, weil avg-Wert dieses Intervalls merkbar über dem Verbrauchsdurchschnitt liegt
        else if (avgRangeValue > meanRangeUpperBound) {
            if (currentData.getValue() < avgDay) {
                result = avgDay;
            } else if (currentData.getValue() > meanRange) {
                //result = meanRange;
                result = peak;
            }
        }


        if (result == 0.0) {
            result = currentData.getValue();
        }
        return result;
    }

    //Stärke der Heuristik: Geht mehr auf individuelles Profil ein + erkennt zusätzlich nun Wochentage <> Wochenenden
    public static void checkBehaviour(TreeMap<LocalDateTime, Consumption> data, int range, double rangeTolerance) {
        int decimals = 6; //Nachkommastellen zum Runden

        HashMap<TimeInterval, ArrayList<Double>> intervalWastingsWeekday = new HashMap<>();
        HashMap<TimeInterval, ArrayList<Double>> intervalWastingsWeekend = new HashMap<>();
        HashMap<TimeInterval, Double> avgWastingsWeekday = new HashMap<>();
        HashMap<TimeInterval, Double> avgWastingsWeekend = new HashMap<>();

        for (int i = 0; i < (24 / range); i++) {
            TimeInterval temp = TimeInterval.createRange(LocalTime.of((i * range), 0), LocalTime.of((i * range), 0).plusHours(2).plusMinutes(59));
            TimeInterval temp2 = TimeInterval.createRange(LocalTime.of((i * range), 0), LocalTime.of((i * range), 0).plusHours(2).plusMinutes(59));
            ArrayList<Double> tempList = new ArrayList<>();
            tempList.add(0.0);
            intervalWastingsWeekday.put(temp, tempList);
            intervalWastingsWeekend.put(temp2, tempList);
        }

        for (LocalDateTime time : data.keySet()) {
            if (!data.get(time).isInterpolated()) {
                if (isBusinessDay(time)) {
                    intervalWastingsWeekday.forEach((key, value) -> {
                        if (key.inRange(time.toLocalTime())) {
                            value.add(data.get(time).getValue());
                            intervalWastingsWeekday.put(key, value);
                        }
                    });
                } else {
                    intervalWastingsWeekend.forEach((key, value) -> {
                        if (key.inRange(time.toLocalTime())) {
                            value.add(data.get(time).getValue());
                            intervalWastingsWeekend.put(key, value);
                        }
                    });
                }
            }
        }

        //Avg Energy Consumption für das jeweilige TimeInterval finden
        intervalWastingsWeekday.forEach((key, values) -> {
            double sum = 0.0;
            int counter = 0;
            for (double value : values) {
                sum += value;
                counter++;
            }
            if (counter != 0) {
                double avgWaste = sum / counter;
                avgWastingsWeekday.put(key, avgWaste);
            } else {
                avgWastingsWeekday.put(key, 0.0);
            }
        });
        intervalWastingsWeekend.forEach((TimeInterval key, ArrayList<Double> values) -> {
            double sum = 0.0;
            int counter = 0;
            for (double value : values) {
                sum += value;
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
        double meanRangeWeekday = meanDailyWeekday / (24d / range);
        double meanDailyWeekend = 0.0;
        for (double value : avgWastingsWeekend.values()) {
            meanDailyWeekend += value;
        }
        double meanRangeWeekend = meanDailyWeekend / (24d / range);

        for (LocalDateTime time : data.keySet()) {
            if (data.get(time).isInterpolated()) {
                AtomicReference<Double> result = new AtomicReference<>(0.0);
                if (isBusinessDay(time)) {
                    avgWastingsWeekday.forEach((key, avgRangeValue) -> {
                        if (key.inRange(time.toLocalTime())) {
                            result.set(calcFromPattern(data.get(time), avgRangeValue, meanRangeWeekday, rangeTolerance));
                            data.get(time).setValue(Double.valueOf(result.toString()));
                        }

                    });
                } else {
                    avgWastingsWeekend.forEach((key, avgRangeValue) -> {
                        if (key.inRange(time.toLocalTime())) {
                            result.set(calcFromPattern(data.get(time), avgRangeValue, meanRangeWeekend, rangeTolerance));
                            data.get(time).setValue(Double.valueOf(result.toString()));
                        }

                    });
                }
                data.get(time).setValue(Helper.roundDouble(data.get(time).getValue(), decimals));
            }
        }
        //data.forEach((time, value) -> System.out.println("Time: " + time + ". Value: " + value.getValue() + ". Interpolated? " + value.isInterpolated()));
    }
}
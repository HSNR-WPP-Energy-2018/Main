package de.hsnr.wpp2018.optimizations;

import de.hsnr.wpp2018.base.Helper;
import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.base.TimeInterval;
import de.hsnr.wpp2018.base.WastingData;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import static de.hsnr.wpp2018.base.Helper.isBusinessDay;

/**
 * Pattern recognition heuristics
 */
public class PatternRecognition {

    /**
     *
     * @param currentData current time
     * @param avgRangeValue average consumption in this time interval
     * @param meanRange global average consumption for every "weekday" or "weekend/holiday" interval
     * @param rangeTolerance manually configurable deviation in % from the mean value of the current interval
     *                       - values within this range are accepted; values outside this range are replaced by using the heuristic
     * @return
     */
    public static double calcFromPattern(Consumption currentData, double avgRangeValue, double meanRange, double rangeTolerance) {


        WastingData wastingData = new WastingData(meanRange);

        double minNightTolerance = meanRange * 4 / 100;
        /* minimum amount of energy consumed at night -> is applied if the interpolated data is much too small */
        double maxNightTolerance = minNightTolerance + wastingData.getHeating() + (wastingData.getICT() / 2);
        /* maximum amount of energy consumed at night -> is applied if when the interpolated data is much too big */
        //double avgNight = minNightTolerance + wastingData.getHeating();
        /* average consumption at night -> not used in this configuration, but it can be applied as an alternative parameter
           of minNightTolerance and maxNightTolerance in order to look for more realistic results */
        double avgDay = wastingData.getHeating() + wastingData.getICT() + wastingData.getIllumination();
        /* average consumption at day */
        double peak = avgDay + wastingData.getProcessCooling() + wastingData.getProcessHeating() + wastingData.getWarmWater();
        /* identifies a high consumption of energy */

        /**
         * Consider slight deviations in + or - % from the mean value, since hardly any interpolated value will be exact = meanRange.
         */

        double meanRangeUpperBound = meanRange + (rangeTolerance * meanRange);
        double meanRangeLowerBound = meanRange - (rangeTolerance * meanRange);
        /* tolerance intervals -> max/min tolerance limits which are defined with parameters for realistic energy consumption
            -> if interpolated data > UpperBound or < LowerBound, then it has to be corrected */

        double result = 0.0;

        /**
         *This time interval seems to indicate the person's sleeping time (or that the person normally is not at home during this time interval),
         * because avg value of this interval is noticeably below the consumption average.
         */
        if (avgRangeValue < meanRangeLowerBound) {
            if (currentData.getValue() < minNightTolerance) {
                result = minNightTolerance;
            }
            else if (currentData.getValue() > maxNightTolerance) {
                result = maxNightTolerance;
            }
        }

        /**
         * The person seems to use many devices in the time interval, because avg-value of this interval is noticeably above the consumption average
         */
        else if (avgRangeValue > meanRangeUpperBound) {
            if (currentData.getValue() < avgDay) {
                /**
                 * Interpolated data is smaller than the avg consumption during this interval. In this config, it is considered an interpolation error
                 * and it is set to the average daily waste.
                 */
                result = avgDay;
            } else if (currentData.getValue() > meanRange) {
                /**
                 * If the interpolated data is very high, there might be a peak, but the consumption has to be set to a more realistic value
                 */
                result = peak;
            }
        }

        if (result == 0.0) {
            result = currentData.getValue();
        }
        return result;
    }

    /**
     *
     * @param data source data
     * @param range Amount of hours in an interval, e.g. 4: [00:00-03:59], [04:00-07:59]...
     * @param rangeTolerance Determines the deviation from the interval in % at which the interpolated value is regarded as an error and has to be reassigned
     */

    public static void checkBehaviour(TreeMap<LocalDateTime, Consumption> data, int range, double rangeTolerance) {

        int decimals = 6; /* number of decimal places */

        HashMap<TimeInterval, ArrayList<Double>> intervalWastingsWeekday = new HashMap<>();
        HashMap<TimeInterval, ArrayList<Double>> intervalWastingsWeekend = new HashMap<>();
        HashMap<TimeInterval, Double> avgWastingsWeekday = new HashMap<>();
        HashMap<TimeInterval, Double> avgWastingsWeekend = new HashMap<>();

        /**
         * create empty intervals
         */
        for (int i = 0; i < (24 / range); i++) {
            TimeInterval temp = TimeInterval.createRange(LocalTime.of((i * range), 0), LocalTime.of((i * range), 0).plusHours(2).plusMinutes(59));
            TimeInterval temp2 = TimeInterval.createRange(LocalTime.of((i * range), 0), LocalTime.of((i * range), 0).plusHours(2).plusMinutes(59));
            ArrayList<Double> tempList = new ArrayList<>();
            tempList.add(0.0);
            intervalWastingsWeekday.put(temp, tempList);
            intervalWastingsWeekend.put(temp2, tempList);
        }

        /**
         * time intervals are filled with data -> distinguish between consumption during weekdays and weekends/holidays -> output: two sets of time intervals
         */

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

        /**
         * find avg energy consumption for the respective time interval
         */
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


        /**
         * Determines the global average daily consumption (not indidivually for the intervals)
         * By comparing the average daily consumption to the average consumption of each interval, some behavior patterns concerning the daily routine can be identicated
         */
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

        /**
         * Iterate through the interpolated values and call the calcFromPattern method if necessary
         */

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
    }
}
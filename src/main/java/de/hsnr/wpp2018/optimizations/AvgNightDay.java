package de.hsnr.wpp2018.optimizations;

import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.base.Household;
import de.hsnr.wpp2018.base.WastingData;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

import static de.hsnr.wpp2018.base.Helper.averageWastePerDay;
import static de.hsnr.wpp2018.base.Helper.isBusinessDay;

/**
 * AvgNightDay heuristics
 */
public class AvgNightDay {

    /**
     *
     * @param data input data
     * @param household configuration for amount of people living in the household
     */
    public static void nightDayWaste(TreeMap<LocalDateTime, Consumption> data, Household household) {

        double meanDaily = averageWastePerDay(household);
        /* determines the daily energy consumption on the basis of the amount of people living in the household */
        double meanHourly = meanDaily / 60;
        /* determines the hourly energy consumption */
        WastingData wastingData = new WastingData(meanHourly);


        /**
         * (Source: VDE) It is estimated that there is a "standby consumption" for around 4 % of gross electricity demand
         * in Germany (observation period: 2004 to 2006)
         */

        double minNightTolerance = meanHourly*4/100;
        /* nearly no consumption at night -> the variable catches interpolated (unrealistic) values equal or below zero */
        double maxNightTolerance = minNightTolerance + wastingData.getHeating() + (wastingData.getICT() / 2);
        /* some devices are switched on */
        double avgNight = minNightTolerance + wastingData.getHeating();
        /* estimated average consumption at night */
        double avgMorning = avgNight + wastingData.getIllumination() + (wastingData.getWarmWater() / 6);
        /* estimated average consumption in the morning */
        double avgDay = wastingData.getHeating() + wastingData.getICT() + wastingData.getIllumination();
        /* estimated average consumption during the day (normally in the evening when the person is at home) */

        /**
         * In this configuration, LocalTime weekdayNightBegin the variable considers a different day than weekdayNightEnd
         */
        LocalTime weekdayNightBegin = LocalTime.of(23, 0);
        LocalTime weekdayNightEnd = LocalTime.of(7, 0);
        LocalTime weekendNightBegin = LocalTime.of(0, 0);
        LocalTime weekendNightEnd = LocalTime.of(9, 0);
        LocalTime workingTimeEnd = LocalTime.of(18, 0);

        for (LocalDateTime today : data.keySet()) {
            if (data.get(today).isInterpolated()) {

                boolean isHoliday = Holidays.checkHoliday(today.toLocalDate());

                if (isBusinessDay(today) && !isHoliday) {
                    /**
                     * Consumption at night on working days
                     */
                    if (today.toLocalTime().isAfter(weekdayNightBegin) || today.toLocalTime().isBefore(weekdayNightEnd)) {
                        if (data.get(today).getValue() < minNightTolerance || data.get(today).getValue() > maxNightTolerance) {
                            data.get(today).setValue(avgNight);
                        }
                    }
                    /**
                     * Consumption in the morning on working days (30 minutes)
                     */
                    else if (today.toLocalTime().isAfter(weekdayNightEnd.minusMinutes(15)) && today.toLocalTime().isBefore(weekdayNightEnd.plusMinutes(45))) {
                        /**
                         * identifies unrealistically high or low values
                         */
                        if (data.get(today).getValue() < minNightTolerance || data.get(today).getValue() > avgMorning) {
                            data.get(today).setValue(avgMorning);
                        }
                    }
                    /**
                     * Consumption on working days while person is away from home (going to school/university/work)
                     */
                    else if (today.toLocalTime().isAfter(weekdayNightEnd.plusMinutes(30)) && today.toLocalTime().isBefore(workingTimeEnd)) {
                        /**
                         * normally, only the same devices as at night are switched on (heating might be turned off)
                         */
                        if (data.get(today).getValue() < minNightTolerance || data.get(today).getValue() > maxNightTolerance) {
                            data.get(today).setValue(avgNight);
                        }
                    }
                    /**
                     * Consumption after work (it is generally more difficult to estimate the consumption after work
                     * because more peaks may occur here -> more accurate heuristics might be useful)
                     */
                    else {
                        /**
                         * It is assumed that the person is always at home after work
                         */
                        if (data.get(today).getValue() < avgDay) {
                            data.get(today).setValue(avgDay);
                        }
                        /**
                         * identifies unrealistically high values (but there also might be a peak)
                         */
                        else if (data.get(today).getValue() > meanHourly)
                        {
                            data.get(today).setValue(meanHourly);
                        }
                    }
                } else {
                    /**
                     * consumption at weekends
                     */
                    if (today.toLocalTime().isAfter(weekendNightBegin) && today.toLocalTime().isBefore(weekendNightEnd)) {
                        if (data.get(today).getValue() < minNightTolerance || data.get(today).getValue() > maxNightTolerance) {
                            data.get(today).setValue(avgNight);
                        }
                    } else {
                        /**
                         * Here it is assumed that the person is sometimes not at home at the weekend (consumption is similar to consumption at night).
                         */
                        if (data.get(today).getValue() < minNightTolerance) {
                            data.get(today).setValue(minNightTolerance);
                        }
                        /**
                         * identifies unrealistically high values (but there also might be a peak)
                         */
                        else if (data.get(today).getValue() > meanHourly)
                        {
                            data.get(today).setValue(meanHourly);
                        }
                    }
                }
            }
        }

    }
}
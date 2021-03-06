package de.hsnr.wpp2018.optimizations;

import de.hsnr.wpp2018.base.Helper;
import de.hsnr.wpp2018.base.Consumption;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.TreeMap;

/**
 * Seasonal differences heuristics
 *
 *This method is only suitable for very incomplete data or for rough consumption estimates.
 * Preferably, it is switched behind the AvgNightDay-method in order to create a more realistic "standard" consumption
 */
public class SeasonalDifferences {
    /**
     * source: Geographie Innsbruck Tirol Atlas (Projekt vom Europäischen Fonds für regionale Entwicklung (EFRE))
     * http://tirolatlas.uibk.ac.at/maps/thema/query.py/text?lang=de;id=1099
     * Winter season: Duration from 1 November to 30 April of the following year [consumes more heating and light].
     * Summer season with the duration from 1 May to 31 October
     *
     * source: Strom-Magazin https://www.strom-magazin.de/strommarkt/stromverbrauch-im-jahresverlauf-der-winter-ist-stromsaison_51786.html
     * During summer, the electricity consumption curve is about ten percentage points below the consumption curve in winter in summer (however, this statistic is from 2002)
     */

    public static boolean isWinterSeason(Month month) {
        Month[] months = {
                Month.NOVEMBER,
                Month.DECEMBER,
                Month.JANUARY,
                Month.FEBRUARY,
                Month.MARCH,
                Month.APRIL,
        };
        for (Month m : months) {
            if (month.equals(m)) {
                return true;
            }
        }
        return false;
    }


    /**
     * calculates mean consumption in winter and summer
     *
     * @param data input data
     * @return energy consumption difference between summer and winter in percent
     */
    public static double percentageFromConsumption(TreeMap<LocalDateTime, Consumption> data) {
        double winter = 0;
        double summer = 0;
        for (LocalDateTime time : data.keySet()) {
                if (isWinterSeason(time.getMonth())) {
                    winter += data.get(time).getValue();
                } else {
                    summer += data.get(time).getValue();
                }
        }

        double percentageRate = summer / winter * 100;

        if (percentageRate >= 100) {
            System.out.println("Calculated higher consumption values in summer. Please check for correctness. Taking 10%-Heuristic instead.");
            percentageRate = 10;
        }
        return percentageRate;
    }


    /**
     *
     * @param data input data
     * @param heuristic tries to generate an individual percentage for the difference between winter and summer on the basis of the interpolated data
     */
    public static void adjustSeasons(TreeMap<LocalDateTime, Consumption> data, boolean heuristic) {
        double percents;
        int decimals = 6;

        if (!heuristic) {
            percents = percentageFromConsumption(data);
        } else {
            /**
             * choose heuristic from EFRE
             */
            percents = 10;
        }

        for (LocalDateTime time : data.keySet()) {
            if (data.get(time).isInterpolated()) {
                /**
                 * winter season -> (+10%) basic consumption or (+measured percentage%) basic consumption
                 */
                if (isWinterSeason(time.getMonth())) {
                    data.get(time).setValue(data.get(time).getValue() + (percents / 100) * data.get(time).getValue());
                    data.get(time).setValue(Helper.roundDouble(data.get(time).getValue(), decimals));
                }
                /**
                 * summer season -> (-10%) basic consumption or (-measured percentage%) basic consumption
                 */
                else {
                    data.get(time).setValue(data.get(time).getValue() - (percents / 100) * data.get(time).getValue());
                    data.get(time).setValue(Helper.roundDouble(data.get(time).getValue(), decimals));
                }
            }

        }
    }
}
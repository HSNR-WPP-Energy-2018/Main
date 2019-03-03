package de.hsnr.wpp2018.evaluation;

import de.hsnr.wpp2018.base.Consumption;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeMap;

/**
 * Rating calculator class
 */
public class Rating {

    /**
     * Calculate the rating for two (different) datasets by calculating the absolute distance between all given source entries and the interpolated values
     *
     * @param source        source data (the truth)
     * @param interpolation interpolation data that is scored
     * @return distance value
     */
    public static double calculateDifference(TreeMap<LocalDateTime, Consumption> source, TreeMap<LocalDateTime, Consumption> interpolation) {
        Set<LocalDateTime> keys = source.keySet();
        double sum = 0;
        for (LocalDateTime key : keys) {
            sum += Math.pow(source.get(key).getValue() - interpolation.getOrDefault(key, new Consumption(0)).getValue(), 2);
        }
        return sum / keys.size();
    }
}
package de.hsnr.wpp2018.evaluation;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeMap;

public class Rating {

    public static double calculateDifference(TreeMap<LocalDateTime, Double> source, TreeMap<LocalDateTime, Double> interpolation) {
        TreeMap<LocalDateTime, Double> merge = new TreeMap<>();
        merge.putAll(source);
        merge.putAll(interpolation);
        Set<LocalDateTime> keys = merge.keySet();
        double sum = 0, defaultValue = 0;
        for (LocalDateTime key : keys) {
            // TODO better default values - different handling for source and interpolation
            sum += Math.pow(source.getOrDefault(key, defaultValue) - interpolation.getOrDefault(key, defaultValue), 2);
        }
        return sum / keys.size();
    }
}

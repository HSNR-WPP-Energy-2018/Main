package de.hsnr.wpp2018.evaluation;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeMap;

public class Rating {

    public static double calculateDifference(TreeMap<LocalDateTime, Double> source, TreeMap<LocalDateTime, Double> interpolation) {
        Set<LocalDateTime> keys = source.keySet();
        double sum = 0;
        for (LocalDateTime key : keys) {
            //TODO: rethink default value
            sum += Math.pow(source.get(key) - interpolation.getOrDefault(key, 0D), 2);
        }
        return sum / keys.size();
    }
}

package de.hsnr.wpp2018;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.TreeMap;

public class Helper {

    public static TreeMap<LocalDateTime, Double> mergeTreeMaps(TreeMap<LocalDateTime, Double> map1, TreeMap<LocalDateTime, Double> map2) {
        map2.putAll(map1);
        return map2;
    }

    public static long getDistance(LocalDateTime one, LocalDateTime two) {
        Duration duration = Duration.between(one, two);
        return Math.abs(duration.toMinutes() * 60);
    }

    public static double roundDouble(double value, int decimal_places) {
        double scale = Math.pow(10, decimal_places);
        return Math.round(value * scale) / scale;
    }
}
package de.hsnr.wpp2018;

import de.hsnr.wpp2018.base.Consumption;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.TreeMap;

public class Helper {

    public static TreeMap<LocalDateTime, Consumption> mergeTreeMaps(TreeMap<LocalDateTime, Consumption> map1, TreeMap<LocalDateTime, Consumption> map2) {
        map2.putAll(map1);
        return map2;
    }

    public static long getDistance(LocalDateTime one, LocalDateTime two) {
        Duration duration = Duration.between(one, two);
        return Math.abs(duration.toMinutes() * 60);
    }

    public static double roundDouble(double value, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(value * scale) / scale;
    }
}
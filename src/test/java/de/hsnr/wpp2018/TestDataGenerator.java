package de.hsnr.wpp2018;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

public class TestDataGenerator {

    public static void cutRange(TreeMap<LocalDateTime, Double> data, LocalDateTime from, LocalDateTime to) {
        data.subMap(from, to).keySet().removeAll(data.subMap(from, to).keySet());
    }

    private TreeMap<LocalDateTime, Double> data;

    public TestDataGenerator(TreeMap<LocalDateTime, Double> data) {
        this.data = data;
    }

    private void cutRange(TreeMap<LocalDateTime, Double> data, RangeAdjuster adjuster, float threshold) {
        Random random = new Random();
        LocalDateTime start = data.firstKey();
        while (start.isBefore(data.lastKey())) {
            LocalDateTime end = adjuster.nextRange(start);
            if (random.nextFloat() <= threshold) {
                cutRange(data, start, end);
            }
            start = end;
        }
    }

    /**
     * Creates artificial gaps in the data. At first weeks are cut, then days, hours and elements.
     *
     * @param weekCut Percentage of weeks to cut. Zero for no cuts
     * @param dayCut Percentage of weeks to cut. Zero for no cuts
     * @param hourCut Percentage of weeks to cut. Zero for no cuts
     * @param elementCut Percentage of weeks to cut. Zero for no cuts
     * @return subset of the data where the specified share of times is cut
     */
    public TreeMap<LocalDateTime, Double> cutRanges(float weekCut, float dayCut, float hourCut, float elementCut) {
        TreeMap<LocalDateTime, Double> data = new TreeMap<>(this.data);
        if (weekCut > 0) {
            cutRange(data, (LocalDateTime current) -> current.plusWeeks(1), weekCut);
        }
        if (dayCut > 0) {
            cutRange(data, (LocalDateTime current) -> current.plusDays(1), dayCut);
        }
        if (hourCut > 0) {
            cutRange(data, (LocalDateTime current) -> current.plusHours(1), weekCut);
        }
        if (elementCut > 0) {
            Random random = new Random();
            Set<LocalDateTime> keys = data.keySet(), removeSet = new HashSet<>();
            for (LocalDateTime key : keys) {
                if (random.nextFloat() <= elementCut) {
                    removeSet.add(key);
                }
            }
            data.keySet().removeAll(removeSet);
        }
        return data;
    }

    interface RangeAdjuster {
        LocalDateTime nextRange(LocalDateTime current);
    }
}

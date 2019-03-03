package de.hsnr.wpp2018.evaluation;

import de.hsnr.wpp2018.base.Consumption;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * Generator for building test data based on a fully provided dataset
 */
public class TestDataGenerator {

    /**
     * remove a subset range of a {@link TreeMap}
     *
     * @param data data map
     * @param from cut range start
     * @param to   cut range end
     */
    public static void cutRange(TreeMap<LocalDateTime, Consumption> data, LocalDateTime from, LocalDateTime to) {
        data.subMap(from, to).keySet().removeAll(data.subMap(from, to).keySet());
    }

    /**
     * Cut single elements from the data
     *
     * @param data      input data
     * @param interval  data point interval
     * @param threshold threshold for removing an element (percentage between [0;1])
     */
    private static void cutRange(TreeMap<LocalDateTime, Consumption> data, int interval, float threshold) {
        Random random = new Random();
        LocalDateTime start = data.firstKey();
        while (start.isBefore(data.lastKey())) {
            LocalDateTime end = start.plusMinutes(interval);
            if (random.nextFloat() <= threshold) {
                cutRange(data, start, end);
            }
            start = end;
        }
    }

    private TreeMap<LocalDateTime, Consumption> data;

    public TestDataGenerator(TreeMap<LocalDateTime, Consumption> data) {
        this.data = data;
    }

    /**
     * Creates artificial gaps in the data. At first weeks are cut, then days, hours and elements.
     *
     * @param weekCut    Percentage of weeks to cut. Zero for no cuts
     * @param dayCut     Percentage of weeks to cut. Zero for no cuts
     * @param hourCut    Percentage of weeks to cut. Zero for no cuts
     * @param elementCut Percentage of weeks to cut. Zero for no cuts
     * @return subset of the data where the specified share of times is cut
     */
    public TreeMap<LocalDateTime, Consumption> cutRanges(float weekCut, float dayCut, float hourCut, float elementCut) {
        TreeMap<LocalDateTime, Consumption> data = new TreeMap<>(this.data);
        if (weekCut > 0) {
            cutRange(data, Math.toIntExact(TimeUnit.DAYS.toMinutes(7)), weekCut);
        }
        if (dayCut > 0) {
            cutRange(data, Math.toIntExact(TimeUnit.DAYS.toMinutes(1)), dayCut);
        }
        if (hourCut > 0) {
            cutRange(data, Math.toIntExact(TimeUnit.HOURS.toMinutes(1)), weekCut);
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
}
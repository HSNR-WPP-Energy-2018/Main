package de.hsnr.wpp2018.evaluation;

import de.hsnr.wpp2018.algorithms.*;
import de.hsnr.wpp2018.base.Consumption;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Analyser {

    public static List<MissingDataInterval> getMissingRanges(TreeMap<LocalDateTime, Consumption> data, LocalDateTime start, LocalDateTime end, int interval) {
        List<MissingDataInterval> result = new ArrayList<>();
        boolean inMissing = false;
        LocalDateTime lastStart = null;

        LocalDateTime time = start;
        while (!time.isAfter(end)) {
            if (!inMissing && !data.containsKey(time)) {
                inMissing = true;
                lastStart = time;
            }
            if (inMissing && data.containsKey(time)) {
                inMissing = false;
                result.add(new MissingDataInterval(lastStart, time.minusSeconds(interval)));
                lastStart = null;
            }
            time = time.plusSeconds(interval);
        }
        if (inMissing) {
            result.add(new MissingDataInterval(lastStart, time.minusSeconds(interval)));
        }
        return result;
    }

    public static List<MissingDataInterval> getMissingRanges(TreeMap<LocalDateTime, Consumption> data, int interval) {
        return getMissingRanges(data, data.firstKey(), data.lastKey(), interval);
    }

    public static MissingRangesStatistics getMissingRangeStatistics(TreeMap<LocalDateTime, Consumption> data, LocalDateTime start, LocalDateTime end, int interval) {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        for (MissingDataInterval entry : getMissingRanges(data, start, end, interval)) {
            int duration = entry.getEntryCount(interval);
            if (!result.containsKey(duration)) {
                result.put(duration, 0);
            }
            result.put(duration, result.get(duration) + 1);
        }
        return new MissingRangesStatistics(result);
    }

    public static MissingRangesStatistics getMissingRangeStatistics(TreeMap<LocalDateTime, Consumption> data, int interval) {
        return getMissingRangeStatistics(data, data.firstKey(), data.lastKey(), interval);
    }

    // TODO: improve recommendations (maybe based on rating with randomly removed data)
    public static List<String> recommendAlgorithm(TreeMap<LocalDateTime, Consumption> data, LocalDateTime start, LocalDateTime end, int interval) {
        MissingRangesStatistics statistics = getMissingRangeStatistics(data, start, end, interval);
        // more than one month missing => only recommend database
        if (statistics.hasHigherRange(Math.toIntExact(TimeUnit.DAYS.toSeconds(30) / interval))) {
            return Collections.singletonList(DatabaseInterface.NAME);
        }
        // no more than three days missing => recommend
        if (!statistics.hasHigherRange(Math.toIntExact(TimeUnit.DAYS.toSeconds(3) / interval))) {
            return Arrays.asList(Averaging.NAME, CubicSplines.NAME, Linear.NAME, Newton.NAME, Yesterday.NAME);
        }
        return Arrays.asList(Averaging.NAME, CubicSplines.NAME, DatabaseInterface.NAME, Linear.NAME, Newton.NAME, Yesterday.NAME);
    }

    public static List<String> recommendAlgorithm(TreeMap<LocalDateTime, Consumption> data, int interval) {
        return recommendAlgorithm(data, data.firstKey(), data.lastKey(), interval);
    }
}
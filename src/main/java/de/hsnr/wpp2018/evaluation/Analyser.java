package de.hsnr.wpp2018.evaluation;

import de.hsnr.wpp2018.algorithms.*;
import de.hsnr.wpp2018.base.Consumption;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Analyser for recommending the best suitable algorithm for a provided dataset
 * Some algorithms have limitations on their usability depending on the desired start / end time and the max size of missing ranges
 */
public class Analyser {

    /**
     * determine the missing ranges of the provided dataset
     *
     * @param data     source dataset
     * @param start    start time
     * @param end      end time
     * @param interval data interval in seconds
     * @return list of missing interval ranges data
     */
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

    /**
     * determine the missing ranges of the provided dataset
     *
     * @param data     source dataset
     * @param interval data interval in seconds
     * @return list of missing interval ranges data
     */
    public static List<MissingDataInterval> getMissingRanges(TreeMap<LocalDateTime, Consumption> data, int interval) {
        return getMissingRanges(data, data.firstKey(), data.lastKey(), interval);
    }

    /**
     * provide statistics of the missing ranges for the provided dataset
     *
     * @param data     source dataset
     * @param start    start time
     * @param end      end time
     * @param interval data interval in seconds
     * @return information about the missing ranges length and the counts for every distinct length
     */
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

    /**
     * provide statistics of the missing ranges for the provided dataset
     *
     * @param data     source dataset
     * @param interval data interval in seconds
     * @return information about the missing ranges length and the counts for every distinct length
     */
    public static MissingRangesStatistics getMissingRangeStatistics(TreeMap<LocalDateTime, Consumption> data, int interval) {
        return getMissingRangeStatistics(data, data.firstKey(), data.lastKey(), interval);
    }

    /**
     * Provide a list of recommendations for the given dataset
     *
     * @param data     source dataset
     * @param start    start time
     * @param end      end time
     * @param interval data interval in seconds
     * @return list of recommended algorithms
     */
    public static List<String> recommendAlgorithm(TreeMap<LocalDateTime, Consumption> data, LocalDateTime start, LocalDateTime end, int interval) {
        // TODO: improve recommendations (maybe based on rating with randomly removed data)
        // only the database approach can handle interpolation of data before the start or after the end
        if (start.until(data.firstKey(), ChronoUnit.MINUTES) > 0) {
            return Collections.singletonList(DatabaseInterface.NAME);
        }
        if (data.lastKey().until(end, ChronoUnit.MINUTES) > 0) {
            return Collections.singletonList(DatabaseInterface.NAME);
        }
        MissingRangesStatistics statistics = getMissingRangeStatistics(data, start, end, interval);
        // more than one month missing => only recommend database
        if (statistics.hasHigherRange(Math.toIntExact(TimeUnit.DAYS.toSeconds(30) / interval))) {
            return Collections.singletonList(DatabaseInterface.NAME);
        }
        // no more than three days missing => recommend
        if (!statistics.hasHigherRange(Math.toIntExact(TimeUnit.DAYS.toSeconds(3) / interval))) {
            return Arrays.asList(Averaging.NAME, Splines.NAME, Linear.NAME, Newton.NAME, Yesterday.NAME);
        }
        return Arrays.asList(Averaging.NAME, Splines.NAME, DatabaseInterface.NAME, Linear.NAME, Newton.NAME, Yesterday.NAME);
    }

    /**
     * Provide a list of recommendations for the given dataset
     *
     * @param data     source dataset
     * @param interval data interval in seconds
     * @return list of recommended algorithms
     */
    public static List<String> recommendAlgorithm(TreeMap<LocalDateTime, Consumption> data, int interval) {
        return recommendAlgorithm(data, data.firstKey(), data.lastKey(), interval);
    }
}
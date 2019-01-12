package de.hsnr.wpp2018.evaluation;

import de.hsnr.wpp2018.Importer;
import de.hsnr.wpp2018.algorithms.AlgorithmTest;
import de.hsnr.wpp2018.base.Consumption;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TreeMap;

public class AnalyserTest {

    private static TreeMap<LocalDateTime, Consumption> data;
    private static LocalDateTime start = LocalDateTime.of(2015, 1, 1, 0, 0, 0);
    private static LocalDateTime end   = LocalDateTime.of(2018, 1, 1, 0, 0, 0);

    @BeforeClass
    public static void initialize() throws IOException {
        Importer importer = new Importer();
        importer.readFile("2016.csv");
        data = new TestDataGenerator(importer.getData()).cutRanges(0.01f, 0.05f, 0f, 0.1f);
    }

    @Test
    public void testIntervals() {
        List<MissingDataInterval> intervals = Analyser.getMissingRanges(data, start, end, AlgorithmTest.INTERVAL);
        intervals.forEach((interval) -> System.out.println("interval missing from " + interval.getStart() + " to " + interval.getEnd() + " - number of missing elements: " + interval.getEntryCount(AlgorithmTest.INTERVAL)));
    }

    @Test
    public void testStatistics() {
        MissingRangesStatistics statistics = Analyser.getMissingRangeStatistics(data, start, end, AlgorithmTest.INTERVAL);
        System.out.println(" lowest number of missing elements: " + statistics.lowestElementCount());
        System.out.println("highest number of missing elements: " + statistics.highestElementCount());
        statistics.getData().forEach((key, value) -> System.out.println("number of missing ranges with " + key + " missing elements: " + value));
    }
}
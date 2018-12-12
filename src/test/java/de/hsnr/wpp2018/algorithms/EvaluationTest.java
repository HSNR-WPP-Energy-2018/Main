package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.Importer;
import de.hsnr.wpp2018.RangeAdjuster;
import de.hsnr.wpp2018.evaluation.Rating;
import de.hsnr.wpp2018.evaluation.TestDataGenerator;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.TreeMap;

public class EvaluationTest {
    private static TreeMap<LocalDateTime, Double> original;
    private static TreeMap<LocalDateTime, Double> testData;
    private TreeMap<LocalDateTime, Double> result;

    @BeforeClass
    public static void loadTestData() throws IOException {
        Importer importer = new Importer();
        importer.readFile("2016.csv");
        original = importer.getData();
        System.out.println("Starting: " + original.size() + " elements");
        testData = new TestDataGenerator(original).cutRanges(0.01f, 0.05f, 0f, 0.1f);
        System.out.println("TestData: " + testData.size() + " elements");
    }

    @Test
    public void evaluateAveraging() {
        ArrayList<RangeAdjuster> adjusters = new ArrayList<>();
        adjusters.add((LocalDateTime current) -> current.plusWeeks(1));
        adjusters.add((LocalDateTime current) -> current.plusDays(1));
        Averaging.Configuration configuration = new Averaging.Optimizer(AlgorithmTest.INTERVAL, adjusters, 1, 10, 1, 10, 0.1).optimize(original, testData);
        System.out.println("Configuration for best score: " + configuration);
        result = new Averaging().interpolate(testData, configuration);
    }

   // @After
    public void output() {
        System.out.println("  Result: " + result.size() + " elements");
        System.out.println("Euclid difference (original -> interpolated): " + Rating.calculateDifference(original, result));
    }
}

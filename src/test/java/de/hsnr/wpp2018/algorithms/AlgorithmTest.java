package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.Algorithm;
import de.hsnr.wpp2018.Heuristics;
import de.hsnr.wpp2018.Importer;
// import de.hsnr.wpp2018.optimizations.AvgNightDay;
import de.hsnr.wpp2018.optimizations.AvgNightDay;
import de.hsnr.wpp2018.evaluation.TestDataGenerator;
import de.hsnr.wpp2018.optimizations.SeasonalDifferences;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.TreeMap;

public class AlgorithmTest {
    static final int INTERVAL = 15 * 60;
    private static final int PERSONS = 2;
    private static final double SIZE = 80.0;

    private static TreeMap<LocalDateTime, Double> original;
    private static TreeMap<LocalDateTime, Double> testData;
    private TreeMap<LocalDateTime, Double> result;
    private  ArrayList<Algorithm.Consumption> resultList;

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
    public void linear() {
        result = new Linear().interpolate(testData, new Algorithm.Configuration(INTERVAL));
    }

    @Test
    public void newton() {
        resultList = new Newton().interpolate(testData, new Newton.Configuration(INTERVAL, 10));
        //Heuristics.useHeuristics(resultList, new Heuristics.Household(PERSONS, SIZE));
         resultList = AvgNightDay.nightDayWaste(resultList, new Heuristics.Household(PERSONS, SIZE));
         resultList = SeasonalDifferences.adjustSeasons(resultList);
    }

    @Test
    public void averaging() {
        ArrayList<Averaging.ConfigurationInterval> intervals = new ArrayList<>();
        intervals.add(new Averaging.ConfigurationInterval(5, (LocalDateTime current) -> current.plusWeeks(1), true, 5));
        intervals.add(new Averaging.ConfigurationInterval(7, (LocalDateTime current) -> current.plusDays(1), true, 3));
        intervals.add(new Averaging.ConfigurationInterval(15, (LocalDateTime current) -> current.plusSeconds(INTERVAL)));
        result = new Averaging().interpolate(testData, new Averaging.Configuration(INTERVAL, intervals));
    }

    /* Ich musste das erstmal auskommentieren, weil die Methode beim neuen Datentyp Algorithm.Consumption noch nicht funktioniert und das Programm sonst abschmiert,
    wenn man die Newton-Methode ausführt

    @After
    public void output() {
        System.out.println("  Result: " + result.size() + " elements");
        System.out.println("Euclid difference (original -> interpolated): " + Rating.calculateDifference(original, result));
    }
    */
}
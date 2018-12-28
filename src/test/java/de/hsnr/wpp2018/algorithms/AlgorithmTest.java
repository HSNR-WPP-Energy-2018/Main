package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.Algorithm;
import de.hsnr.wpp2018.optimizations.*;
import de.hsnr.wpp2018.Importer;
// import de.hsnr.wpp2018.optimizations.AvgNightDay;
import de.hsnr.wpp2018.evaluation.TestDataGenerator;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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


    public void applyHeuristics(ArrayList<Algorithm.Consumption> resultlist, int algorithmNumber)
    {
        Holidays.scanFile();

        switch (algorithmNumber) {
            case 1:
                Heuristics.useHeuristics(resultList, new Heuristics.Household(PERSONS, SIZE)); //noch umbauen!
                break;
            case 2:
                resultList = AvgNightDay.nightDayWaste(resultList, new Heuristics.Household(PERSONS, SIZE));
                resultList = SeasonalDifferences.adjustSeasons(resultList);
                break;
            case 3: //Die Ergebnisse von linear + case 3 scheinen meist recht realistisch zu sein
                resultList = PatternRecognition.checkBehaviour(resultList, 3, 0.1);
        }


    }

    @Test
    public void linear() {
        resultList = new Linear().interpolate(testData, new Algorithm.Configuration(INTERVAL));
        applyHeuristics(resultList, 2);
    }

    @Test
    public void newton() {
        resultList = new Newton().interpolate(testData, new Newton.Configuration(INTERVAL, 10));
        applyHeuristics(resultList, 2);
    }

    @Test
    public void averaging() {
        ArrayList<Averaging.ConfigurationInterval> intervals = new ArrayList<>();
        intervals.add(new Averaging.ConfigurationInterval(5, (LocalDateTime current) -> current.plusWeeks(1), true, 5));
        intervals.add(new Averaging.ConfigurationInterval(7, (LocalDateTime current) -> current.plusDays(1), true, 3));
        intervals.add(new Averaging.ConfigurationInterval(15, (LocalDateTime current) -> current.plusSeconds(INTERVAL)));
        result = new Averaging().interpolate(testData, new Averaging.Configuration(INTERVAL, intervals));
    }

    //funktioniert noch nicht!!!!!!!!

    @Test
    public void splines() {
        resultList = new CubicSplines().interpolate(testData, new CubicSplines.Configuration(INTERVAL, 10));
        applyHeuristics(resultList, 2);
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
package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.base.Household;
import de.hsnr.wpp2018.database.*;
import de.hsnr.wpp2018.evaluation.Rating;
import de.hsnr.wpp2018.evaluation.TestDataGenerator;
import de.hsnr.wpp2018.io.Importer;
import de.hsnr.wpp2018.optimizations.AvgNightDay;
import de.hsnr.wpp2018.optimizations.Heuristics;
import de.hsnr.wpp2018.optimizations.PatternRecognition;
import de.hsnr.wpp2018.optimizations.SeasonalDifferences;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class ExtendedTest {
    private static TreeMap<LocalDateTime, Consumption> original;

    @BeforeClass
    public static void readTestData() throws IOException {
        Importer importer = new Importer();
        importer.readFile("2016.csv");
        original = importer.getData();
    }

    @Test
    public void testRanges() throws IOException {
        Averaging averaging = new Averaging();
        ArrayList<Averaging.ConfigurationInterval> intervals = new ArrayList<>();
        intervals.add(new Averaging.ConfigurationInterval(5, Math.toIntExact(TimeUnit.DAYS.toSeconds(7)), true, 5));
        intervals.add(new Averaging.ConfigurationInterval(7, Math.toIntExact(TimeUnit.DAYS.toSeconds(1)), true, 3));
        intervals.add(new Averaging.ConfigurationInterval(15, AlgorithmTest.INTERVAL));
        Averaging.Configuration averagingConfiguration = new Averaging.Configuration(AlgorithmTest.INTERVAL, intervals);

        Splines splines = new Splines();
        Splines.Configuration cubicSplinesConfiguration = new Splines.Configuration(AlgorithmTest.INTERVAL, 10);

        DatabaseInterface databaseInterface = new DatabaseInterface();
        Database database = new Database();
        ArrayList<Descriptor> descriptors = new ArrayList<>();
        descriptors.add(new StringDescriptor("test"));
        database.addElement(new Element(AlgorithmTest.INTERVAL, descriptors, ElementKey.Type.WEEK_OF_YEAR, original));
        DatabaseInterface.Configuration databaseConfiguration = new DatabaseInterface.Configuration(AlgorithmTest.INTERVAL, database, descriptors);

        Linear linear = new Linear();
        Linear.Configuration linearConfiguration = new Linear.Configuration(AlgorithmTest.INTERVAL);

        Newton newton = new Newton();
        Newton.Configuration newtonConfiguration = new Newton.Configuration(AlgorithmTest.INTERVAL, 10);

        Yesterday yesterday = new Yesterday();
        Yesterday.Configuration yesterdayConfiguration = new Yesterday.Configuration(AlgorithmTest.INTERVAL);

        ArrayList<Result> results = new ArrayList<>();
        String[] algorithms = new String[]{Averaging.NAME, Splines.NAME, DatabaseInterface.NAME, Linear.NAME, Newton.NAME, Yesterday.NAME};

        for (float weekCut = 0.0f; weekCut <= 0.5f; weekCut += 0.1f) {
            for (float dayCut = 0.0f; dayCut <= 0.5f; dayCut += 0.1f) {
                for (float hourCut = 0.0f; hourCut <= 0.5f; hourCut += 0.1f) {
                    for (float elementCut = 0.0f; elementCut <= 0.5f; elementCut += 0.1f) {
                        System.out.print("Testing with weekCut=" + weekCut + ", dayCut=" + dayCut + ", hourCut=" + hourCut + ", elementCut=" + elementCut + ": ");
                        TreeMap<LocalDateTime, Consumption> testData = new TestDataGenerator(original).cutRanges(weekCut, dayCut, hourCut, elementCut);
                        HashMap<String, Double> differences = new HashMap<>();

                        differences.put(Averaging.NAME, Rating.calculateDifference(original, averaging.interpolate(new TreeMap<>(testData), averagingConfiguration)));
                        differences.put(Splines.NAME, Rating.calculateDifference(original, splines.interpolate(new TreeMap<>(testData), cubicSplinesConfiguration)));
                        differences.put(DatabaseInterface.NAME, Rating.calculateDifference(original, databaseInterface.interpolate(new TreeMap<>(testData), databaseConfiguration)));
                        differences.put(Linear.NAME, Rating.calculateDifference(original, linear.interpolate(new TreeMap<>(testData), linearConfiguration)));
                        differences.put(Newton.NAME, Rating.calculateDifference(original, newton.interpolate(new TreeMap<>(testData), newtonConfiguration)));
                        differences.put(Yesterday.NAME, Rating.calculateDifference(original, yesterday.interpolate(new TreeMap<>(testData), yesterdayConfiguration)));

                        Result result = new Result(weekCut, dayCut, hourCut, elementCut, differences);
                        System.out.println(result.toCSV(algorithms));
                        results.add(result);
                    }
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        builder.append(Result.buildHeader(algorithms)).append("\n");
        for (Result result : results) {
            builder.append(result.toCSV(algorithms)).append("\n");
        }
        PrintWriter printWriter = new PrintWriter(new File("out/evaluation-algorithms.csv"));
        printWriter.write(builder.toString());
        printWriter.close();
    }

    @Test
    public void testSmallRanges() throws IOException {
        Averaging averaging = new Averaging();
        ArrayList<Averaging.ConfigurationInterval> intervals = new ArrayList<>();
        intervals.add(new Averaging.ConfigurationInterval(5, Math.toIntExact(TimeUnit.DAYS.toSeconds(7)), true, 5));
        intervals.add(new Averaging.ConfigurationInterval(7, Math.toIntExact(TimeUnit.DAYS.toSeconds(1)), true, 3));
        intervals.add(new Averaging.ConfigurationInterval(15, AlgorithmTest.INTERVAL));
        Averaging.Configuration averagingConfiguration = new Averaging.Configuration(AlgorithmTest.INTERVAL, intervals);

        Splines splines = new Splines();
        Splines.Configuration cubicSplinesConfiguration = new Splines.Configuration(AlgorithmTest.INTERVAL, 10);

        DatabaseInterface databaseInterface = new DatabaseInterface();
        Database database = new Database();
        ArrayList<Descriptor> descriptors = new ArrayList<>();
        descriptors.add(new StringDescriptor("test"));
        database.addElement(new Element(AlgorithmTest.INTERVAL, descriptors, ElementKey.Type.WEEK_OF_YEAR, original));
        DatabaseInterface.Configuration databaseConfiguration = new DatabaseInterface.Configuration(AlgorithmTest.INTERVAL, database, descriptors);

        Linear linear = new Linear();
        Linear.Configuration linearConfiguration = new Linear.Configuration(AlgorithmTest.INTERVAL);

        Newton newton = new Newton();
        Newton.Configuration newtonConfiguration = new Newton.Configuration(AlgorithmTest.INTERVAL, 10);

        Yesterday yesterday = new Yesterday();
        Yesterday.Configuration yesterdayConfiguration = new Yesterday.Configuration(AlgorithmTest.INTERVAL);

        ArrayList<Result> results = new ArrayList<>();
        String[] algorithms = new String[]{Averaging.NAME, Splines.NAME, DatabaseInterface.NAME, Linear.NAME, Newton.NAME, Yesterday.NAME};

        float weekCut = 0;
        float dayCut = 0;
        float hourCut = 0;
        for (float elementCut = 0.0f; elementCut <= 0.25f; elementCut += 0.01f) {
            System.out.println("Testing with weekCut=" + weekCut + ", dayCut=" + dayCut + ", hourCut=" + hourCut + ", elementCut=" + elementCut);
            TreeMap<LocalDateTime, Consumption> testData = new TestDataGenerator(original).cutRanges(weekCut, dayCut, hourCut, elementCut);
            HashMap<String, Double> differences = new HashMap<>();

            differences.put(Averaging.NAME, Rating.calculateDifference(original, averaging.interpolate(new TreeMap<>(testData), averagingConfiguration)));
            differences.put(Splines.NAME, Rating.calculateDifference(original, splines.interpolate(new TreeMap<>(testData), cubicSplinesConfiguration)));
            differences.put(DatabaseInterface.NAME, Rating.calculateDifference(original, databaseInterface.interpolate(new TreeMap<>(testData), databaseConfiguration)));
            differences.put(Linear.NAME, Rating.calculateDifference(original, linear.interpolate(new TreeMap<>(testData), linearConfiguration)));
            differences.put(Newton.NAME, Rating.calculateDifference(original, newton.interpolate(new TreeMap<>(testData), newtonConfiguration)));
            differences.put(Yesterday.NAME, Rating.calculateDifference(original, yesterday.interpolate(new TreeMap<>(testData), yesterdayConfiguration)));

            results.add(new Result(weekCut, dayCut, hourCut, elementCut, differences));
        }

        StringBuilder builder = new StringBuilder();
        builder.append(Result.buildHeader(algorithms)).append("\n");
        for (Result result : results) {
            builder.append(result.toCSV(algorithms)).append("\n");
        }
        PrintWriter printWriter = new PrintWriter(new File("out/evaluation-algorithms-small.csv"));
        printWriter.write(builder.toString());
        printWriter.close();
    }

    @Test
    public void testHeuristics() throws FileNotFoundException {
        Averaging averaging = new Averaging();
        ArrayList<Averaging.ConfigurationInterval> intervals = new ArrayList<>();
        intervals.add(new Averaging.ConfigurationInterval(5, Math.toIntExact(TimeUnit.DAYS.toSeconds(7)), true, 5));
        intervals.add(new Averaging.ConfigurationInterval(7, Math.toIntExact(TimeUnit.DAYS.toSeconds(1)), true, 3));
        intervals.add(new Averaging.ConfigurationInterval(15, AlgorithmTest.INTERVAL));
        Averaging.Configuration averagingConfiguration = new Averaging.Configuration(AlgorithmTest.INTERVAL, intervals);

        ArrayList<Result> results = new ArrayList<>();
        String[] heuristics = new String[]{
                "heuristics_base",
                Heuristics.NIGHT_DAY.toString(),
                Heuristics.PATTERN.toString(),
                Heuristics.SEASON.toString(),
        };

        for (float weekCut = 0.0f; weekCut <= 0.5f; weekCut += 0.1f) {
            for (float dayCut = 0.0f; dayCut <= 0.5f; dayCut += 0.1f) {
                for (float hourCut = 0.0f; hourCut <= 0.5f; hourCut += 0.1f) {
                    for (float elementCut = 0.0f; elementCut <= 0.5f; elementCut += 0.1f) {
                        System.out.println("Testing with weekCut=" + weekCut + ", dayCut=" + dayCut + ", hourCut=" + hourCut + ", elementCut=" + elementCut);
                        TreeMap<LocalDateTime, Consumption> testData, temp;
                        testData = averaging.interpolate(new TestDataGenerator(original).cutRanges(weekCut, dayCut, hourCut, elementCut), averagingConfiguration);
                        HashMap<String, Double> differences = new HashMap<>();

                        differences.put("heuristics_base", Rating.calculateDifference(original, testData));

                        temp = new TreeMap<>(testData);
                        AvgNightDay.nightDayWaste(temp, new Household(1, 150));
                        differences.put(Heuristics.NIGHT_DAY.toString(), Rating.calculateDifference(original, temp));

                        temp = new TreeMap<>(testData);
                        PatternRecognition.checkBehaviour(temp, 4, 0.1);
                        differences.put(Heuristics.PATTERN.toString(), Rating.calculateDifference(original, temp));

                        temp = new TreeMap<>(testData);
                        SeasonalDifferences.adjustSeasons(temp, true);
                        differences.put(Heuristics.SEASON.toString(), Rating.calculateDifference(original, temp));

                        results.add(new Result(weekCut, dayCut, hourCut, elementCut, differences));
                    }
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        builder.append(Result.buildHeader(heuristics)).append("\n");
        for (Result result : results) {
            builder.append(result.toCSV(heuristics)).append("\n");
        }
        PrintWriter printWriter = new PrintWriter(new File("out/evaluation-heuristics.csv"));
        printWriter.write(builder.toString());
        printWriter.close();
    }

    private static class Result {
        private static final String HEADER = "weekCut;dayCut;hourCut;elementCut";

        public static String buildHeader(String[] keys) {
            StringBuilder builder = new StringBuilder(HEADER);
            for (String key : keys) {
                builder.append(";").append(key);
            }
            return builder.toString();
        }

        private float weekCut;
        private float dayCut;
        private float hourCut;
        private float elementCut;
        private HashMap<String, Double> differences;

        public Result(float weekCut, float dayCut, float hourCut, float elementCut, HashMap<String, Double> differences) {
            this.weekCut = weekCut;
            this.dayCut = dayCut;
            this.hourCut = hourCut;
            this.elementCut = elementCut;
            this.differences = differences;
        }

        public float getWeekCut() {
            return weekCut;
        }

        public float getDayCut() {
            return dayCut;
        }

        public float getHourCut() {
            return hourCut;
        }

        public float getElementCut() {
            return elementCut;
        }

        public HashMap<String, Double> getDifferences() {
            return differences;
        }

        public String toCSV(String[] keys) {
            StringBuilder builder = new StringBuilder()
                    .append(Float.toString(weekCut).replace('.', ','))
                    .append(";")
                    .append(Float.toString(dayCut).replace('.', ','))
                    .append(";")
                    .append(Float.toString(hourCut).replace('.', ','))
                    .append(";")
                    .append(Float.toString(elementCut).replace('.', ','));
            for (String key : keys) {
                builder.append(";").append(Double.toString(differences.getOrDefault(key, 0D)).replace('.', ','));
            }
            return builder.toString();
        }
    }
}

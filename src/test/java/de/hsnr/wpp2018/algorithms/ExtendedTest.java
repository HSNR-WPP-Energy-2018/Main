package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.database.*;
import de.hsnr.wpp2018.evaluation.Rating;
import de.hsnr.wpp2018.evaluation.TestDataGenerator;
import de.hsnr.wpp2018.io.Importer;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class ExtendedTest {

    @Test
    public void testRanges() throws IOException {
        Importer importer = new Importer();
        importer.readFile("2016.csv");
        TreeMap<LocalDateTime, Consumption> original = importer.getData();

        Averaging averaging = new Averaging();
        ArrayList<Averaging.ConfigurationInterval> intervals = new ArrayList<>();
        intervals.add(new Averaging.ConfigurationInterval(5, Math.toIntExact(TimeUnit.DAYS.toSeconds(7)), true, 5));
        intervals.add(new Averaging.ConfigurationInterval(7, Math.toIntExact(TimeUnit.DAYS.toSeconds(1)), true, 3));
        intervals.add(new Averaging.ConfigurationInterval(15, AlgorithmTest.INTERVAL));
        Averaging.Configuration averagingConfiguration = new Averaging.Configuration(AlgorithmTest.INTERVAL, intervals);

        CubicSplines cubicSplines = new CubicSplines();
        CubicSplines.Configuration cubicSplinesConfiguration = new CubicSplines.Configuration(AlgorithmTest.INTERVAL, 10);

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

        HashMap<String, ArrayList<Result>> results = new HashMap<>();
        String[] algorithms = new String[]{Averaging.NAME, CubicSplines.NAME, DatabaseInterface.NAME, Linear.NAME, Newton.NAME, Yesterday.NAME};
        for (String algorithm : algorithms) {
            results.put(algorithm, new ArrayList<>());
        }

        for (float weekCut = 0.1f; weekCut <= 0.5f; weekCut += 0.1f) {
            for (float dayCut = 0.1f; dayCut <= 0.5f; dayCut += 0.1f) {
                for (float hourCut = 0.1f; hourCut <= 0.5f; hourCut += 0.1f) {
                    for (float elementCut = 0.1f; elementCut <= 0.5f; elementCut += 0.1f) {
                        System.out.println("Testing with weekCut=" + weekCut + ", dayCut=" + dayCut + ", hourCut=" + hourCut + ", elementCut=" + elementCut);
                        TreeMap<LocalDateTime, Consumption> testData = new TestDataGenerator(original).cutRanges(weekCut, dayCut, hourCut, elementCut);

                        results.get(Averaging.NAME).add(new Result(weekCut, dayCut, hourCut, elementCut, Rating.calculateDifference(original, averaging.interpolate(testData, averagingConfiguration))));
                        results.get(CubicSplines.NAME).add(new Result(weekCut, dayCut, hourCut, elementCut, Rating.calculateDifference(original, cubicSplines.interpolate(testData, cubicSplinesConfiguration))));
                        results.get(DatabaseInterface.NAME).add(new Result(weekCut, dayCut, hourCut, elementCut, Rating.calculateDifference(original, databaseInterface.interpolate(testData, databaseConfiguration))));
                        results.get(Linear.NAME).add(new Result(weekCut, dayCut, hourCut, elementCut, Rating.calculateDifference(original, linear.interpolate(testData, linearConfiguration))));
                        results.get(Newton.NAME).add(new Result(weekCut, dayCut, hourCut, elementCut, Rating.calculateDifference(original, newton.interpolate(testData, newtonConfiguration))));
                        results.get(Yesterday.NAME).add(new Result(weekCut, dayCut, hourCut, elementCut, Rating.calculateDifference(original, yesterday.interpolate(testData, yesterdayConfiguration))));
                    }
                }
            }
        }

        for (String algorithm : algorithms) {
            StringBuilder builder = new StringBuilder();
            builder.append(Result.HEADER).append("\n");
            for (Result result : results.get(algorithm)) {
                builder.append(result.toCSV()).append("\n");
            }
            PrintWriter printWriter = new PrintWriter(new File("out/evaluation-" + algorithm + ".csv"));
            printWriter.write(builder.toString());
            printWriter.close();
        }
    }

    private static class Result {
        public static final String HEADER = "weekCut;dayCut;hourCut;elementCut;difference";

        private float weekCut;
        private float dayCut;
        private float hourCut;
        private float elementCut;
        private double difference;

        public Result(float weekCut, float dayCut, float hourCut, float elementCut, double difference) {
            this.weekCut = weekCut;
            this.dayCut = dayCut;
            this.hourCut = hourCut;
            this.elementCut = elementCut;
            this.difference = difference;
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

        public double getDifference() {
            return difference;
        }

        public String toCSV() {
            return weekCut + ";" + dayCut + ";" + hourCut + ";" + elementCut + ";" + difference;
        }
    }
}

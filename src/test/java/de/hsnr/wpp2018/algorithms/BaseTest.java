package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.io.Exporter;
import de.hsnr.wpp2018.io.Importer;
import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.evaluation.Rating;
import de.hsnr.wpp2018.evaluation.TestDataGenerator;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.TreeMap;

/**
 * Basic test class for testing algorithms and heuristics
 * This class reads the original file, generates a test dataset once and evaluates the result after every test
 */
public class BaseTest {
    static TreeMap<LocalDateTime, Consumption> original;
    static TreeMap<LocalDateTime, Consumption> testData;
    TreeMap<LocalDateTime, Consumption> result;

    /**
     * Reference to the current test name (defined by the test method name)
     */
    @Rule
    public TestName name = new TestName();

    /**
     * Load test data once and prepare a test dataset with missing elements
     *
     * @throws IOException io error
     */
    @BeforeClass
    public static void loadTestData() throws IOException {
        Importer importer = new Importer();
        importer.readFile("2016.csv");
        original = importer.getData();
        System.out.println(" Starting: " + original.size() + " elements");
        testData = new TestDataGenerator(original).cutRanges(0.01f, 0.05f, 0f, 0.1f);
        System.out.println(" TestData: " + testData.size() + " elements");
    }

    /**
     * Evaluate the score after the interpolation and write the result dataset into a file
     *
     * @throws FileNotFoundException
     */
    @After
    public void output() throws FileNotFoundException {
        System.out.println("   Result: " + result.size() + " elements - difference (original -> interpolated): " + Rating.calculateDifference(original, result) + " for \"" + name.getMethodName() + "\"");
        Exporter.writeConsumption(result, "out/interpolated-" + name.getMethodName() + ".csv");
    }
}
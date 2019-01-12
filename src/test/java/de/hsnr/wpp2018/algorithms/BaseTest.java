package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.Exporter;
import de.hsnr.wpp2018.Importer;
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

public class BaseTest {
    static TreeMap<LocalDateTime, Consumption> original;
    static TreeMap<LocalDateTime, Consumption> testData;
    TreeMap<LocalDateTime, Consumption> result;

    @Rule
    public TestName name = new TestName();
    public Exporter exporter = new Exporter();

    @BeforeClass
    public static void loadTestData() throws IOException {
        Importer importer = new Importer();
        importer.readFile("2016.csv");
        original = importer.getData();
        System.out.println(" Starting: " + original.size() + " elements");
        testData = new TestDataGenerator(original).cutRanges(0.01f, 0.05f, 0f, 0.1f);
        System.out.println(" TestData: " + testData.size() + " elements");
    }

    @After
    public void output() throws FileNotFoundException {
        System.out.println("   Result: " + result.size() + " elements - difference (original -> interpolated): " + Rating.calculateDifference(original, result) + " for \"" + name.getMethodName() + "\"");
        exporter.writeFile(result, "out/interpolated-" + name.getMethodName() + ".csv");
    }
}
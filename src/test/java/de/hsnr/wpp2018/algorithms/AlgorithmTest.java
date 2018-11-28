package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.Algorithm;
import de.hsnr.wpp2018.Importer;
import de.hsnr.wpp2018.TestDataGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.TreeMap;

public class AlgorithmTest {
    private static final int INTERVAL = 15 * 60;

    private TreeMap<LocalDateTime, Double> data;
    private TreeMap<LocalDateTime, Double> res;

    @Before
    public void loadTestData() throws IOException {
        Importer importer = new Importer();
        importer.readFile("2016.csv");
        System.out.println("Starting with " + importer.getData().size() + " data points");
        data = new TestDataGenerator(importer.getData()).cutRanges(0.01f, 0.05f, 0f, 0.1f);
        System.out.println("After random cuts: " + importer.getData().size() + " data points");
    }

    @Test
    public void linear() {
        res = new Linear().interpolate(data, new Algorithm.Configuration(INTERVAL));
    }

    @Test
    public void newton() {
        res = new Newton().interpolate(data, new Newton.Configuration(INTERVAL, 10));
    }

    @Test
    public void averaging() {
        res = new Averaging().interpolate(data, new Averaging.Configuration(INTERVAL, 4));
    }

    @After
    public void output() {
        System.out.println("After interpolation: " + res.size() + " data points");
    }
}
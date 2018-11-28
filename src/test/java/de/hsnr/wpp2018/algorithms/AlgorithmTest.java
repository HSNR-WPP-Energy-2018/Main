package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.Algorithm;
import de.hsnr.wpp2018.Importer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.TreeMap;

public class AlgorithmTest {
    private static final int INTERVAL = 15 * 60;

    private Importer importer = new Importer();

    @Before
    public void loadTestData() throws IOException {
        importer.readFile("2016.csv");
    }

    @Test
    public void linear() {
        TreeMap<LocalDateTime, Double> data = importer.getData();
        System.out.println(data.size());
        TreeMap<LocalDateTime, Double> res = new Linear().interpolate(data, new Algorithm.Configuration(INTERVAL));
        System.out.println(res.size());
    }

    @Test
    public void newton() throws Algorithm.ConfigurationException {
        TreeMap<LocalDateTime, Double> data = importer.getData();
        System.out.println(data.size());
        TreeMap<LocalDateTime, Double> res = new Newton().interpolate(data, new Newton.Configuration(INTERVAL, 10));
        System.out.println(res.size());
    }

    @Test
    public void averaging() throws Algorithm.ConfigurationException {
        TreeMap<LocalDateTime, Double> data = importer.getData();
        System.out.println(data.size());
        TreeMap<LocalDateTime, Double> res = new Averaging().interpolate(data, new Averaging.Configuration(15 * 60, 4));
        System.out.println(res.size());
    }
}
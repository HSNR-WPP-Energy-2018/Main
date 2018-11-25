package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.Importer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.TreeMap;

public class InterpolationTest {

    private Importer importer = new Importer();
    private Interpolation algorithm = new Interpolation();

    @Before
    public void loadTestData() throws IOException {
        importer.readFile("2016.csv");
    }

    @Test
    public void linear() {
        TreeMap<LocalDateTime, Double> data = importer.getData();
        System.out.println(data.size());
        TreeMap<LocalDateTime, Double> res = algorithm.linear(data);
        System.out.println(res.size());
    }

    @Test
    public void newton() {
        TreeMap<LocalDateTime, Double> data = importer.getData();
        System.out.println(data.size());
        TreeMap<LocalDateTime, Double> res = algorithm.newton(data);
        System.out.println(res.size());
    }
}
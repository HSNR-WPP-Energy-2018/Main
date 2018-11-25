package de.hsnr.wpp2018.algorithms;

import de.hsnr.wpp2018.Importer;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class InterpolationTest {

    private Importer importer = new Importer();
    private Interpolation algorithm = new Interpolation();

    @BeforeClass
    public void loadTestData() throws IOException {
        importer.readFile("2016.csv");
    }

    @Test
    public void linear() {
        algorithm.linear(importer.data);
    }

    @Test
    public void newton() {
        algorithm.newton(importer.data);
    }
}
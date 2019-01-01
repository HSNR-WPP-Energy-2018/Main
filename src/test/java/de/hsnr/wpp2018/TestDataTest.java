package de.hsnr.wpp2018;

import de.hsnr.wpp2018.base.Consumption;
import de.hsnr.wpp2018.evaluation.TestDataGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.TreeMap;

public class TestDataTest {

    @Test
    public void simpleTest() throws IOException {
        Importer importer = new Importer();
        importer.readFile("2016.csv");
        TreeMap<LocalDateTime, Consumption> data = importer.getData();
        System.out.println(data.size());
        TreeMap<LocalDateTime, Consumption> subSet = new TestDataGenerator(data).cutRanges(0.1f, 0.2f, 0f, 0.2f);
        System.out.println(subSet.size());
        Assert.assertNotEquals(data.size(), subSet.size());
    }
}
package de.hsnr.wpp2018;

import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.TreeMap;

public class ImporterTest {

    @Test
    public void excelFormat() throws IOException {
        Importer importer = new Importer();
        importer.readFile("2016.csv");
        for (Date key : importer.getData().keySet()) {
            System.out.println(key + " - " + importer.getValue(key));
        }
    }

    @Test
    public void rangeTest() throws Exception {
        Importer importer = new Importer();
        importer.readFile("2016.csv");
        Date from = new Date(2016 - 1900, 5, 1, 0, 0, 0);
        Date to = new Date(2016 - 1900, 10, 1, 0, 0, 0);
        TreeMap<Date, Double> subData = importer.getData(from, to);
        if (subData.size() == 0) {
            throw new Exception("no elements");
        }
        System.out.println("sublist size: " + subData.size());
        if (subData.firstKey().getTime() < from.getTime()) {
            throw new Exception("first key to small");
        }
        if (subData.lastKey().getTime() > to.getTime()) {
            throw new Exception("last key to small");
        }
    }
}

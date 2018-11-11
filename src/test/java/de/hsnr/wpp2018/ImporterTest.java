package de.hsnr.wpp2018;

import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.TreeMap;

public class ImporterTest {
    @Test
    public void excelFormat() throws IOException {
        Importer importer = new Importer();
        importer.readFile("2016.csv");
        for (LocalDateTime key : importer.getData().keySet()) {
            System.out.println(key + " - " + importer.getValue(key));
        }
    }

    @Test
    public void rangeTest() throws Exception {
        Importer importer = new Importer();
        importer.readFile("2016.csv");
        LocalDateTime from = LocalDateTime.of(2016, 5, 1, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(2016, 10, 1, 0, 0, 0);
        TreeMap<LocalDateTime, Double> subData = importer.getData(from, to);
        if (subData.size() == 0) {
            throw new Exception("no elements");
        }
        System.out.println("sublist size: " + subData.size());
        if (subData.firstKey().compareTo(from) < 0) {
            throw new Exception("first key to small");
        }
        if (subData.lastKey().compareTo(to) > 0) {
            throw new Exception("last key to small");
        }
    }



}
package de.hsnr.wpp2018;

import org.junit.Test;

import java.io.IOException;
import java.util.Date;

public class ImporterTest {

    @Test
    public void excelFormat() throws IOException {
        Importer importer = new Importer();
        importer.readFile("2016.csv");
        for (Date key : importer.getData().keySet()) {
            System.out.println(key + " - " + importer.getValue(key));
        }
    }
}

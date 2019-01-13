package de.hsnr.wpp2018.io;

import de.hsnr.wpp2018.base.Consumption;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.TreeMap;


public class Exporter {

    public void writeConsumption(TreeMap<LocalDateTime, Consumption> result, String path) throws FileNotFoundException {
        String fileHeader = "Datum;Uhrzeit;Wert [kWh]";
        String colSeparator = ";";
        String rowSeparator = "\n";

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(fileHeader);
        stringBuilder.append(rowSeparator);

        result.forEach((time, value) -> {
            stringBuilder.append(time.toLocalDate());
            stringBuilder.append(colSeparator);
            stringBuilder.append(time.toLocalTime());
            stringBuilder.append(colSeparator);
            stringBuilder.append(value.getValue());
            stringBuilder.append(rowSeparator);
        });

        PrintWriter printWriter = new PrintWriter(new File(path));
        printWriter.write(stringBuilder.toString());
        printWriter.close();
    }
}
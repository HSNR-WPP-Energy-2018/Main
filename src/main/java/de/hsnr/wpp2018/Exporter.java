package de.hsnr.wpp2018;

import de.hsnr.wpp2018.base.Consumption;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.TreeMap;


public class Exporter {

    public void writeFile(TreeMap<LocalDateTime, Consumption> result, String path) throws FileNotFoundException {
        String fileHeader = "Datum;Uhrzeit;Wert [kWh]";
        String colSeparator = ";";
        String rowSeparator = "\n";

        PrintWriter printWriter = new PrintWriter(new File(path));
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

        printWriter.write(stringBuilder.toString());
        printWriter.close();
    }
}
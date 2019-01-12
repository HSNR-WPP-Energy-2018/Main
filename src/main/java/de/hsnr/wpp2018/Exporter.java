package de.hsnr.wpp2018;

import de.hsnr.wpp2018.base.Consumption;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.TreeMap;


public class Exporter {

    public void writeFile(TreeMap<LocalDateTime, Consumption> result) throws FileNotFoundException
    {
        String fileHeader = "Datum;Uhrzeit;Wert [kWh]";
        String colSeperator = ";";
        String rowSeperator = "\n";

        PrintWriter printWriter = new PrintWriter(new File("interpolated.csv"));
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(fileHeader.toString());
        stringBuilder.append(rowSeperator);

        result.forEach((time, value) ->
        {
            stringBuilder.append(time.toLocalDate());
            stringBuilder.append(colSeperator);
            stringBuilder.append(time.toLocalTime());
            stringBuilder.append(colSeperator);
            stringBuilder.append(value.getValue());
            stringBuilder.append(rowSeperator);
        });

        printWriter.write(stringBuilder.toString());
        printWriter.close();
    }
}
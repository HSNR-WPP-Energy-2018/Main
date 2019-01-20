package de.hsnr.wpp2018.io.database;

import de.hsnr.wpp2018.base.ParserException;
import de.hsnr.wpp2018.database.Database;
import de.hsnr.wpp2018.database.DayEntry;
import de.hsnr.wpp2018.database.Descriptor;
import de.hsnr.wpp2018.database.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

public class Writer {
    private static final String SEPARATOR = "\n";

    public static void write(Database database, String folderPath) throws ParserException, FileNotFoundException {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            throw new ParserException("folder not found or is not a directory");
        }
        List<Element> elements = database.getElements();
        for (int i = 0; i < elements.size(); i++) {
            write(elements.get(i), folder.getPath() + "/" + i + "." + Element.EXTENSION);
        }
    }

    public static void write(Element element, String filePath) throws FileNotFoundException {
        StringBuilder builder = new StringBuilder();
        builder.append(element.getInterval());
        builder.append(SEPARATOR);
        for (Descriptor descriptor : element.getDescriptors()) {
            builder.append(descriptor.output());
            builder.append(SEPARATOR);
        }
        element.getValues().forEach((key, entry) -> {
            builder.append(key.getKeyType());
            builder.append(":");
            builder.append(key.getInterval());
            builder.append(":");
            builder.append(key.getDayOfWeek());
            builder.append("-");

            boolean first = true;

            HashMap<DayEntry.Key, Double> values = entry.getValues();
            for (DayEntry.Key time : values.keySet()) {
                if (!first) {
                    builder.append(";");
                }
                first = false;
                builder.append(time.getHour());
                builder.append(":");
                builder.append(time.getMinute());
                builder.append(":");
                builder.append(time.getSecond());
                builder.append("=");
                builder.append(values.get(time));
            }
            builder.append(SEPARATOR);
        });
        PrintWriter printWriter = new PrintWriter(new File(filePath));
        printWriter.write(builder.toString());
        printWriter.close();
    }
}
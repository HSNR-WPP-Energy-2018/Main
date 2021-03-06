package de.hsnr.wpp2018.io.database;

import de.hsnr.wpp2018.base.ParserException;
import de.hsnr.wpp2018.base.ParserHelper;
import de.hsnr.wpp2018.database.*;

import java.io.*;
import java.nio.charset.Charset;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Parser for database entries read from files
 */
public class Parser {
    private static final String SEPARATOR = "\n";

    /**
     * Parse a whole folder where every single file represents a database entry
     *
     * @param folderPath path of folder
     * @return instantiated database
     * @throws FileNotFoundException on IO error
     * @throws ParserException on parse error
     */
    public static Database parseFolder(String folderPath) throws FileNotFoundException, ParserException {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            throw new ParserException("folder not found or is not a directory");
        }
        Database database = new Database();
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isFile()) {
                database.addElement(parse(file));
            }
        }
        return database;
    }

    /**
     * Parse a single database element saved in a single file
     *
     * @param file file reference
     * @return instantiated element
     * @throws ParserException on parse error
     * @throws FileNotFoundException on IO error
     */
    public static Element parse(File file) throws ParserException, FileNotFoundException {
        InputStream inputStream = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()));
        String content = br.lines().collect(Collectors.joining(System.lineSeparator()));
        return parse(content);
    }

    /**
     * Parse a string into an element object
     *
     * @param data string representation of the element
     * @return instantiated element
     * @throws ParserException on parse error
     */
    public static Element parse(String data) throws ParserException {
        String[] lines = data.split(SEPARATOR);
        if (lines.length < 3) {
            throw new ParserException("to few lines");
        }
        int interval;
        try {
            interval = Integer.parseInt(lines[0]);
        } catch (NumberFormatException e) {
            throw new ParserException("first line is not a number");
        }
        List<Descriptor> descriptors = new ArrayList<>();
        HashMap<ElementKey, DayEntry> entries = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.contains("|")) { // line represents descriptor
                descriptors.add(Descriptor.parse(line));
            } else if (line.contains("-")) { // line represents value
                String[] parts = line.split("[-]");
                if (parts.length != 2) {
                    throw new ParserException("value line needs to contain two parts separated by \"-\"");
                }
                String[] keyParts = parts[0].split("[:]");
                if (keyParts.length != 3) {
                    throw new ParserException("entry-key needs to consists of three parts split by \":\"");
                }
                ElementKey elementKey = new ElementKey(ElementKey.Type.valueOf(keyParts[0]), ParserHelper.getInteger(keyParts[1]), DayOfWeek.valueOf(keyParts[2]));
                HashMap<DayEntry.Key, Double> values = new HashMap<>();
                for (String value : parts[1].split("[;]")) {
                    String[] valueParts = value.split("[=]");
                    if (valueParts.length != 2) {
                        throw new ParserException("value needs to contain two parts split by \"=\"");
                    }
                    String[] valueKey = valueParts[0].split("[:]");
                    if (valueKey.length != 3) {
                        throw new ParserException("value-key needs to contain two parts split by \"=\"");
                    }
                    values.put(new DayEntry.Key(ParserHelper.getInteger(valueKey[0]), ParserHelper.getInteger(valueKey[1]), ParserHelper.getInteger(valueKey[2])), ParserHelper.getDouble(valueParts[1]));
                }
                entries.put(elementKey, new DayEntry(values));
            }
        }
        return new Element(interval, descriptors, entries);
    }
}
package de.hsnr.wpp2018.database;

import de.hsnr.wpp2018.base.ParserException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * @deprecated Needs to be adjusted to the new structure
 */
public class Parser {

    /**
     * Read a element from a file
     * The first line just contains the interval in minutes
     * The second line contains the comma separated descriptors - numeric descriptors follow the format: key=value:tolerance
     * All other lines are interpreted as values. The may be comma-separated
     *
     * @param data File contents
     * @return instantiated {@link Element}
     */
    public static Element parse(String data) throws ParserException {
        String[] parts = data.split("\n");
        if (parts.length < 3) {
            throw new ParserException("to few parts");
        }
        int interval;
        ArrayList<Double> values = new ArrayList<>();
        ArrayList<Descriptor> descriptors = new ArrayList<>();
        try {
            interval = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            throw new ParserException("first line is not a number");
        }
        String[] descriptorStrings = parts[1].split("[,]");
        for (String descriptorString : descriptorStrings) {
            String[] descriptorParts = descriptorString.split("[=]");
            if (descriptorParts.length > 1) {
                String key = descriptorParts[0];
                String[] descriptorData = descriptorParts[1].split("[:]");
                double value, tolerance = 0;
                try {
                    value = Double.parseDouble(descriptorData[0]);
                } catch (NumberFormatException e) {
                    throw new ParserException("number-descriptor value not a number");
                }
                if (descriptorData.length > 1) {
                    try {
                        tolerance = Double.parseDouble(descriptorData[1]);
                    } catch (NumberFormatException e) {
                        throw new ParserException("number-descriptor tolerance not a number");
                    }
                }
                descriptors.add(new NumberDescriptor(key, value, tolerance));
            } else {
                descriptors.add(new StringDescriptor(descriptorString));
            }
        }
        for (int i = 2; i < parts.length; i++) {
            String[] lineParts = parts[i].split("[,]");
            for (String element : lineParts) {
                try {
                    double value = Double.parseDouble(element);
                    values.add(value);
                } catch (NumberFormatException e) {
                    throw new ParserException("invalid value at line " + i);
                }
            }
        }
        return null; // new Element(interval, values, descriptors);
    }

    public static Element parse(InputStream inputStream) throws ParserException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()));
        String content = br.lines().collect(Collectors.joining(System.lineSeparator()));
        return parse(content);
    }
}
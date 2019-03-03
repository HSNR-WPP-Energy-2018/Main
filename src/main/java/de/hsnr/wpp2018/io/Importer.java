package de.hsnr.wpp2018.io;

import de.hsnr.wpp2018.base.Consumption;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;
import java.util.TreeMap;

/**
 * CSV importer for reading a dataset from an input file into the memory
 */
public class Importer {
    public static final String DEFAULT_SEPARATOR = ";";

    private TreeMap<LocalDateTime, Consumption> data = new TreeMap<>();

    /**
     * Read the input file
     *
     * @param file      input file reference
     * @param separator CSV separator
     * @throws IOException io error
     */
    public void readFile(File file, String separator) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            double value;
            LocalDateTime time;
            String[] parts = line.split(separator);

            try {
                value = Double.parseDouble(parts[parts.length - 1].replace(',', '.'));
            } catch (NumberFormatException e) {
                continue;
            }
            switch (parts.length) {
                case 3:
                    String[] localDateTimeParts = parts[0].split("[.]");
                    String[] timeParts = parts[1].split(":");

                    if (localDateTimeParts.length != 3) {
                        continue;
                    }
                    if (timeParts.length != 2) {
                        continue;
                    }

                    String year = localDateTimeParts[2];
                    if (year.length() < 4) {
                        year = "20" + year;
                    }
                    time = LocalDateTime.of(Integer.parseInt(year), Integer.parseInt(localDateTimeParts[1]), Integer.parseInt(localDateTimeParts[0]), Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]));
                    break;
                case 2:
                    String timestamp = parts[0];
                    time = LocalDateTime.ofInstant(Instant.ofEpochSecond(Integer.parseInt(timestamp)), TimeZone.getDefault().toZoneId());
                    break;
                default:
                    continue;
            }
            this.data.put(time, new Consumption(value));
        }
    }

    /**
     * Read the input file
     *
     * @param name      input file location
     * @param separator CSV separator
     * @throws IOException io error
     */
    public void readFile(String name, String separator) throws IOException {
        readFile(new File(this.getClass().getClassLoader().getResource(name).getFile()), separator);
    }

    /**
     * Read the input file
     *
     * @param file input file reference
     * @throws IOException io error
     */
    public void readFile(File file) throws IOException {
        readFile(file, DEFAULT_SEPARATOR);
    }

    /**
     * Read the input file
     *
     * @param name input file location
     * @throws IOException io error
     */
    public void readFile(String name) throws IOException {
        readFile(name, DEFAULT_SEPARATOR);
    }

    /**
     * Getter for the data read before
     *
     * @return read data
     */
    public TreeMap<LocalDateTime, Consumption> getData() {
        return data;
    }

    /**
     * Get a subset of the data read before
     *
     * @param from start time
     * @param to   end time
     * @return subset data
     */
    public TreeMap<LocalDateTime, Consumption> getData(LocalDateTime from, LocalDateTime to) {
        return new TreeMap<>(data.subMap(from, to));
    }

    /**
     * Checks if read data contains a value for the given key
     *
     * @param LocalDateTime data key
     * @return result if data contains value for key
     */
    public boolean hasValue(LocalDateTime LocalDateTime) {
        return this.data.containsKey(LocalDateTime);
    }

    /**
     * Get the value for a given key date
     *
     * @param LocalDateTime data key
     * @return data for the given key
     */
    public Consumption getValue(LocalDateTime LocalDateTime) {
        return this.data.get(LocalDateTime);
    }
}
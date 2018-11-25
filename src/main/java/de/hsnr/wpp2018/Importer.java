package de.hsnr.wpp2018;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;
import java.util.TreeMap;

public class Importer {

    private TreeMap<LocalDateTime, Double> data = new TreeMap<>();

    public void readFile(String name, String separator) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(this.getClass().getClassLoader().getResource(name).getFile())));
        String line;
        while ((line = reader.readLine()) != null) {
            Double value;
            LocalDateTime date;
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
                    date = LocalDateTime.of(Integer.parseInt(year), Integer.parseInt(localDateTimeParts[1]), Integer.parseInt(localDateTimeParts[0]), Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]));
                    break;
                case 2:
                    String timestamp = parts[0];
                    date = LocalDateTime.ofInstant(Instant.ofEpochSecond(Integer.parseInt(timestamp)), TimeZone.getDefault().toZoneId());
                    break;
                default:
                    continue;
            }
            this.data.put(date, value);
        }
    }

    public void readFile(String name) throws IOException {
        readFile(name, ";");
    }

    public TreeMap<LocalDateTime, Double> getData() {
        return data;
    }

    public TreeMap<LocalDateTime, Double> getData(LocalDateTime from, LocalDateTime to) {
        return new TreeMap<>(data.subMap(from, to));
    }

    public boolean hasValue(LocalDateTime LocalDateTime) {
        return this.data.containsKey(LocalDateTime);
    }

    public double getValue(LocalDateTime LocalDateTime) {
        return this.data.get(LocalDateTime);
    }

}
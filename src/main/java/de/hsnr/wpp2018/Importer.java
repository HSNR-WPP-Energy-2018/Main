package de.hsnr.wpp2018;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;
import java.util.TreeMap;

class Importer {

    private TreeMap<LocalDateTime, Double> data = new TreeMap<>();

    public void readFile(String name, String separator) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(this.getClass().getClassLoader().getResource(name).getFile())));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(separator);
            if (parts.length < 2) {
                continue;
            }
            LocalDateTime date;
            Double value;
            try {
                value = Double.parseDouble(parts[parts.length - 1].replace(',', '.'));
            } catch (NumberFormatException e) {
                continue;
            }
            switch (parts.length) {
                case 3:
                    String[] LocalDateTimeParts = parts[0].split("[.]");
                    String[] timeParts = parts[1].split(":");
                    if (LocalDateTimeParts.length != 3) {
                        continue;
                    }
                    if (timeParts.length != 2) {
                        continue;
                    }
                    String year = LocalDateTimeParts[2];
                    if (year.length() < 4) {
                        year = "20" + year;
                    }
                    date = LocalDateTime.of(Integer.parseInt(year), Integer.parseInt(LocalDateTimeParts[1]), Integer.parseInt(LocalDateTimeParts[0]), Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]));
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

    public void readFile(String name) throws IOException {
        readFile(name, ";");
    }
}
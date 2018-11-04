package de.hsnr.wpp2018;

import java.io.*;
import java.util.Date;
import java.util.HashMap;

class Importer {

    private HashMap<Date, Double> data = new HashMap<>();

    public void readFile(String name, String separator) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(this.getClass().getClassLoader().getResource(name).getFile())));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(separator);
            if (parts.length < 2) {
                continue;
            }
            Date date;
            Double value;
            try {
                value = Double.parseDouble(parts[parts.length - 1].replace(',', '.'));
            } catch (NumberFormatException e) {
                continue;
            }
            switch (parts.length) {
                case 3:
                    String[] dateParts = parts[0].split("[.]");
                    String[] timeParts = parts[1].split(":");
                    if (dateParts.length != 3) {
                        continue;
                    }
                    if (timeParts.length != 2) {
                        continue;
                    }
                    String year = dateParts[2];
                    if (year.length() < 4) {
                        year = "20" + year;
                    }
                    date = new Date(Integer.parseInt(year) - 1900, Integer.parseInt(dateParts[1]), Integer.parseInt(dateParts[0]), Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]));
                    break;
                case 2:
                    String timestamp = parts[0];
                    date = new Date(Integer.parseInt(timestamp) * 1000L);
                    break;
                default:
                    continue;
            }
            this.data.put(date, value);
        }
    }

    public HashMap<Date, Double> getData() {
        return data;
    }

    public boolean hasValue(Date date) {
        return this.data.containsKey(date);
    }

    public double getValue(Date date) {
        return this.data.get(date);
    }

    public void readFile(String name) throws IOException {
        readFile(name, ";");
    }
}
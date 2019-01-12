package de.hsnr.wpp2018.base;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TimeZone;

public class ParserHelper {

    public static String getString(Map<String, String> data, String key) throws ParserException {
        if (!data.containsKey(key)) {
            throw new ParserException("parameter " + key + " missing");
        }
        return data.get(key);
    }

    public static String getString(Map<String, String> data, String key, int minLength) throws ParserException {
        String value = getString(data, key);
        if (value.length() < minLength) {
            throw new ParserException("parameter " + key + " needs to be at least " + minLength + " characters long");
        }
        return value;
    }

    public static boolean getBoolean(String value) throws ParserException {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1");
    }

    public static boolean getBoolean(Map<String, String> data, String key) throws ParserException {
        return getBoolean(getString(data, key));
    }

    public static int getInteger(String value) throws ParserException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ParserException("invalid integer string");
        }
    }

    public static int getInteger(Map<String, String> data, String key) throws ParserException {
        try {
            return Integer.parseInt(getString(data, key));
        } catch (NumberFormatException e) {
            throw new ParserException("invalid integer for parameter " + key);
        }
    }

    public static int getInteger(String value, int min) throws ParserException {
        int val = getInteger(value);
        if (val < min) {
            throw new ParserException("invalid interval");
        }
        return val;
    }

    public static int getInteger(Map<String, String> data, String key, int min) throws ParserException {
        int val = getInteger(data, key);
        if (val < min) {
            throw new ParserException("invalid interval");
        }
        return val;
    }

    public static double getDouble(String value) throws ParserException {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ParserException("invalid double");
        }
    }

    public static double getDouble(Map<String, String> data, String key) throws ParserException {
        try {
            return Double.parseDouble(getString(data, key));
        } catch (NumberFormatException e) {
            throw new ParserException("invalid double for parameter " + key);
        }
    }

    public static double getDouble(String value, double min) throws ParserException {
        double val = getDouble(value);
        if (val < min) {
            throw new ParserException("invalid interval");
        }
        return val;
    }

    public static double getDouble(Map<String, String> data, String key, double min) throws ParserException {
        double val = getDouble(data, key);
        if (val < min) {
            throw new ParserException("invalid interval");
        }
        return val;
    }

    public static LocalDateTime getDate(String value) throws ParserException {
        try {
            // first try timestamp
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(Integer.parseInt(value)), TimeZone.getDefault().toZoneId());
        } catch (NumberFormatException ignored) {
        }
        String[] parts = value.split("[-]");
        if (parts.length != 2) {
            throw new ParserException("date needs to contains two parts separated by \"-\"");
        }
        String[] localDateTimeParts = parts[0].split("[.]");
        String[] timeParts = parts[1].split(":");

        if (localDateTimeParts.length != 3) {
            throw new ParserException("date part needs to contain three parts separated by \".\"");
        }
        if (timeParts.length != 2) {
            throw new ParserException("time part needs to contain two parts separated by \":\"");
        }

        String year = localDateTimeParts[2];
        if (year.length() < 4) {
            year = "20" + year;
        }
        return LocalDateTime.of(Integer.parseInt(year), Integer.parseInt(localDateTimeParts[1]), Integer.parseInt(localDateTimeParts[0]), Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]));
    }

    public static LocalDateTime getDate(Map<String, String> data, String key) throws ParserException {
        return getDate(getString(data, key));
    }
}

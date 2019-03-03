package de.hsnr.wpp2018.base;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TimeZone;

/**
 * Helper functionality for parsing input data (directly retrieved as string or already separated)
 */
public class ParserHelper {

    /**
     * Get a string from the provided key-value data
     *
     * @param data key-value data
     * @param key  key of the string
     * @return data for the given key
     * @throws ParserException on parse error
     */
    public static String getString(Map<String, String> data, String key) throws ParserException {
        if (!data.containsKey(key)) {
            throw new ParserException("parameter " + key + " missing");
        }
        return data.get(key);
    }

    /**
     *  Get a string from the provided key-value data. Check for a minimum text length
     *
     * @param data      key-value data
     * @param key       key of the string
     * @param minLength minimum text length
     * @return data for the given key
     * @throws ParserException on parse error
     */
    public static String getString(Map<String, String> data, String key, int minLength) throws ParserException {
        String value = getString(data, key);
        if (value.length() < minLength) {
            throw new ParserException("parameter " + key + " needs to be at least " + minLength + " characters long");
        }
        return value;
    }

    /**
     * Get the boolean value of a string
     *
     * @param value input string
     * @return boolean value
     */
    public static boolean getBoolean(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1");
    }

    /**
     * Get a boolean form the provided key-value data
     *
     * @param data key-value data
     * @param key  key of the boolean
     * @return data for the given key
     * @throws ParserException on parse error
     */
    public static boolean getBoolean(Map<String, String> data, String key) throws ParserException {
        return getBoolean(getString(data, key));
    }

    /**
     * Get the integer value of a string
     *
     * @param value input string
     * @return integer value
     * @throws ParserException on parse error
     */
    public static int getInteger(String value) throws ParserException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ParserException("invalid integer string");
        }
    }

    /**
     * Get a integer from the provided key-value data
     *
     * @param data key-value data
     * @param key  key of the integer
     * @return data for the given key
     * @throws ParserException on parse error
     */
    public static int getInteger(Map<String, String> data, String key) throws ParserException {
        try {
            return Integer.parseInt(getString(data, key));
        } catch (NumberFormatException e) {
            throw new ParserException("invalid integer for parameter " + key);
        }
    }

    /**
     * Get the integer value of a string and check for min value
     *
     * @param value input string
     * @param min   min integer value
     * @return integer value
     * @throws ParserException on parse error
     */
    public static int getInteger(String value, int min) throws ParserException {
        int val = getInteger(value);
        if (val < min) {
            throw new ParserException("invalid interval");
        }
        return val;
    }

    /**
     * Get a integer from the provided key-value data and check for min value
     *
     * @param data key-value data
     * @param key  key of the integer
     * @param min  min integer value
     * @return integer value
     * @throws ParserException on parse error
     */
    public static int getInteger(Map<String, String> data, String key, int min) throws ParserException {
        int val = getInteger(data, key);
        if (val < min) {
            throw new ParserException("invalid interval");
        }
        return val;
    }

    /**
     * Get the double value of a string
     *
     * @param value input string
     * @return double value
     * @throws ParserException on parse error
     */
    public static double getDouble(String value) throws ParserException {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ParserException("invalid double");
        }
    }

    /**
     * Get a double from the provided key-value data
     *
     * @param data key-value data
     * @param key  key of the double
     * @return double value
     * @throws ParserException on parse error
     */
    public static double getDouble(Map<String, String> data, String key) throws ParserException {
        try {
            return Double.parseDouble(getString(data, key));
        } catch (NumberFormatException e) {
            throw new ParserException("invalid double for parameter " + key);
        }
    }

    /**
     * Get the double value of a string and check for min value
     *
     * @param value input string
     * @param min   min double value
     * @return double value
     * @throws ParserException on parse error
     */
    public static double getDouble(String value, double min) throws ParserException {
        double val = getDouble(value);
        if (val < min) {
            throw new ParserException("invalid interval");
        }
        return val;
    }

    /**
     * Get a double from the provided key-value data and check for min value
     *
     * @param data key-value data
     * @param key  key of the integer
     * @param min  min double value
     * @return double value
     * @throws ParserException on parse error
     */
    public static double getDouble(Map<String, String> data, String key, double min) throws ParserException {
        double val = getDouble(data, key);
        if (val < min) {
            throw new ParserException("invalid interval");
        }
        return val;
    }

    /**
     * Get the {@link LocalDateTime} represented by a string of the format YYYY.MM.DD-HH:MM
     *
     * @param value input string
     * @return {@link LocalDateTime} value
     * @throws ParserException on parse error
     */
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

    /**
     * Get a {@link LocalDateTime} represented by a string of the format YYYY.MM.DD-HH:MM form the provided key-value data
     *
     * @param data key-value data
     * @param key  key of the date
     * @return {@link LocalDateTime} value
     * @throws ParserException on parse error
     */
    public static LocalDateTime getDate(Map<String, String> data, String key) throws ParserException {
        return getDate(getString(data, key));
    }
}
package de.hsnr.wpp2018.base;

import java.util.Map;

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
}

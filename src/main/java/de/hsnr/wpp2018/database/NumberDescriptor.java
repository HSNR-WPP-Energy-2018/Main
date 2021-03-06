package de.hsnr.wpp2018.database;

import de.hsnr.wpp2018.base.ParserException;
import de.hsnr.wpp2018.base.ParserHelper;

/**
 * Number descriptor. Contains the string value as well as a number representation and optionally a tolerance.
 * Equality is defined by the same key and a value within the positive and negative tolerance from the value
 */
public class NumberDescriptor implements Descriptor {
    public static final String TYPE = "number";

    /**
     * Parse the descriptor from the string representation without the type prefix
     *
     * @param string string representation
     * @return instantiated descriptor
     *
     * @throws ParserException on invalid data
     */
    public static NumberDescriptor fromString(String string) throws ParserException {
        String[] parts = string.split("[-]");
        if (parts.length != 2) {
            throw new ParserException("number-descriptor needs to contain one \"=\"");
        }
        String key = parts[0];
        String[] valueParts = parts[1].split("[:]");
        double value = ParserHelper.getDouble(valueParts[0]);
        double tolerance = (valueParts.length > 1) ? ParserHelper.getDouble(valueParts[1]) : 0;
        return new NumberDescriptor(key, value, tolerance);
    }

    private String key;
    private double value;
    private double tolerance = 0;

    public NumberDescriptor(String key, double value, double tolerance) {
        this.key = key;
        this.value = value;
        this.tolerance = tolerance;
    }

    public NumberDescriptor(String key, double value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public double getValue() {
        return value;
    }

    public double getTolerance() {
        return tolerance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(Descriptor descriptor) {
        if (descriptor instanceof NumberDescriptor) {
            if (((NumberDescriptor) descriptor).getKey().equals(getKey())) {
                return Math.abs(this.getValue() - ((NumberDescriptor) descriptor).getValue()) <= getTolerance();
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String output() {
        return TYPE + "|" + getKey() + "-" + getValue() + ":" + getTolerance();
    }

    @Override
    public String toString() {
        return getKey() + "-" + getValue() + ":" + getTolerance();
    }
}
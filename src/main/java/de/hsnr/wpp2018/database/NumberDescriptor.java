package de.hsnr.wpp2018.database;

public class NumberDescriptor implements Descriptor {

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

    @Override
    public boolean matches(Descriptor descriptor) {
        if (descriptor instanceof NumberDescriptor) {
            if (((NumberDescriptor) descriptor).getKey().equals(getKey())) {
                return Math.abs(this.getValue() - ((NumberDescriptor) descriptor).getValue()) <= getTolerance();
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return getKey() + "=" + getValue() + ":" + getTolerance();
    }
}
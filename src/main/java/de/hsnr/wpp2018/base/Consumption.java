package de.hsnr.wpp2018.base;

public class Consumption {
    private double value;
    private boolean isInterpolated;

    public Consumption(double value, boolean isInterpolated) {
        this.value = value;
        this.isInterpolated = isInterpolated;
    }

    public Consumption(double value) {
        this(value, false);
    }

    public double getValue() {
        return value;
    }

    public boolean isInterpolated() {
        return isInterpolated;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Consumption copyAsOriginal() {
        return new Consumption(getValue());
    }
}
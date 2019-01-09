package de.hsnr.wpp2018.base;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Consumption that = (Consumption) o;
        return Double.compare(that.getValue(), getValue()) == 0 &&
                isInterpolated() == that.isInterpolated();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue(), isInterpolated());
    }
}
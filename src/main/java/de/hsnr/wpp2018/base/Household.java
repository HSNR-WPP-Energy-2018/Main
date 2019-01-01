package de.hsnr.wpp2018.base;

public class Household {
    private int numberOfPersons;
    private double livingSpace;

    public Household(int numberOfPersons, double livingSpace) {
        this.numberOfPersons = numberOfPersons;
        this.livingSpace = livingSpace;
    }

    public int getNumberOfPersons() {
        return numberOfPersons;
    }

    public double getLivingSpace() {
        return livingSpace;
    }
}
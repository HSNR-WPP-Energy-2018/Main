package de.hsnr.wpp2018.database;

public class NumberDescriptor implements Descriptor {

    private String key;
    private double vale;
    private double tolerance = 0;

    public NumberDescriptor(String key, double vale, double tolerance) {
        this.key = key;
        this.vale = vale;
        this.tolerance = tolerance;
    }

    public NumberDescriptor(String key, double vale) {
        this.key = key;
        this.vale = vale;
    }

    public String getKey() {
        return key;
    }

    public double getVale() {
        return vale;
    }

    public double getTolerance() {
        return tolerance;
    }

    @Override
    public boolean matches(Descriptor descriptor) {
        if (descriptor instanceof NumberDescriptor) {
            if (((NumberDescriptor) descriptor).getKey().equals(getKey())) {
                return Math.abs(this.getVale() - ((NumberDescriptor) descriptor).getVale()) <= getTolerance();
            }
        }
        return false;
    }
}